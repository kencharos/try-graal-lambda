package my.graal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import picocli.CommandLine;

import javax.inject.Inject;
import java.net.URL;

@CommandLine.Command(name = "hello-graal")
public class SampleCommand implements Runnable {

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(SampleCommand.class, args);
    }

    /** AWS_LAMBDA_RUNTIME_API , env*/
    @Value("${aws.lambda.runtime.api}") String lambdaEndpoint;

    @Inject CalculationService service;

    @Override
    public void run() {
        // event loop
        while(true) {
            // get function event
            Pair pair = getContext();
            if (pair == null) {
                continue;
            }
            // call function
            SampleResponse answer = service.calc(pair.context, pair.request);

            // output function result
            System.out.println("Answer is " + answer.getAnswer());

            // send OK
            if (!sendResult("response", pair.context.getAwsRequestId(), answer)) {
                sendResult("error", pair.context.getAwsRequestId(), "error raised");
            }
        }
    }

    private Pair getContext() {

        String url = "http://" + lambdaEndpoint;
        String path = "/2018-06-01/runtime/invocation/next";
        try(RxHttpClient client = RxHttpClient.create(new URL(url))) {
            HttpResponse<SampleRequest> res =  client.exchange(HttpRequest.GET(path), SampleRequest.class).blockingLast();

            LambdaContext ctx = new LambdaContext();
            ctx.setAwsRequestId(res.header("Lambda-Runtime-Aws-Request-Id"));
            ctx.setTraceId(res.header("Lambda-Runtime-Trace-Id"));

            return  new Pair(ctx, res.body());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param responseOrError  success,error
     * @param requestId
     * @param body
     * @param <T>
     * @throws Exception
     */
    private <T>  boolean sendResult(String responseOrError, String requestId, T body) {
        String url = "http://" + lambdaEndpoint;
        String path = "/2018-06-01/runtime/invocation/" + requestId + "/" + responseOrError;
        try(RxHttpClient client = RxHttpClient.create(new URL(url))) {
            client.exchange(HttpRequest.POST("path", body), String.class).blockingLast();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class Pair {

        public final LambdaContext context;
        public final SampleRequest request;

        public Pair(LambdaContext context, SampleRequest request) {
            this.context = context;
            this.request = request;
        }

    }
}