package my.graal;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import picocli.CommandLine;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.*;

import javax.inject.Inject;
import java.net.URL;

@CommandLine.Command(name = "hello-graal")
public class SampleCommand implements Runnable {

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(SampleCommand.class, args);
    }

    @CommandLine.Option(names = {"-c"}, description = "context")
    public String context;


    @CommandLine.Option(names = {"-e"}, description = "event")
    public String event;

    @Inject CalculationService service;

    @Inject ObjectMapper mapper;

    @Value("${lambda.url}") String lambdaUrl;

    @Override
    public void run() {
        SampleRequest req = null;
        LambdaContext ctx = null;

        try {
            req = mapper.readValue(event, SampleRequest.class);
            ctx = mapper.readValue(context, LambdaContext.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int answer = service.calc(ctx, req);

        System.out.println("Answer is " + answer);

        SampleResponse res = new SampleResponse();
        res.setAnswer(answer);

        // send OK
        try(RxHttpClient client = RxHttpClient.create(new URL(lambdaUrl))) {

            HttpResponse<String> result = client.exchange(HttpRequest.POST("/api/next", res), String.class).blockingLast();
            System.out.println(result.body());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}