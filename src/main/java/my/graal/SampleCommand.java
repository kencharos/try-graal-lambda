package my.graal;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import picocli.CommandLine;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URL;

@CommandLine.Command(name = "hello-graal")
public class SampleCommand implements Runnable {

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(SampleCommand.class, args);
    }

    @Value("${aws.lambda.runtime.api}") String lambdaRuntimeEndpoint;

    @Inject CalculationService service;

    RxHttpClient client;

    @PostConstruct
    public void buildHttpClient() throws Exception{
        System.out.println("Build Http Client");
        this.client = RxHttpClient.create(new URL("http://" + lambdaRuntimeEndpoint));
    }

    private Pair<String, SampleRequest> fetchContext() {
        HttpResponse<SampleRequest> response =
                client.exchange(HttpRequest.GET("/2018-06-01/runtime/invocation/next"), SampleRequest.class)
                    .blockingFirst();

        String requestId = response.header("Lambda-Runtime-Aws-Request-Id");
        return new Pair<>(requestId, response.body());
    }

    private void sendResponse(String requestId, SampleResponse answer) {
        String path = "/2018-06-01/runtime/invocation/" + requestId + "/response";
        HttpResponse<String> response =
                client.exchange(HttpRequest.POST(path, answer), String.class)
                        .blockingFirst();
        System.out.println(response.status() + " " + response.body());
    }

    @Override
    public void run() {

        while (true) {
            Pair<String, SampleRequest> input = fetchContext();
            // call function
            SampleResponse answer = service.calc(input.t2);
            // output function result
            System.out.println(input.t1 + " Answer is " + answer.getAnswer());
            sendResponse(input.t1, answer);
        }
    }


}