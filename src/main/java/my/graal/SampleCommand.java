package my.graal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@CommandLine.Command(name = "hello-graal")
public class SampleCommand implements Runnable {

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(SampleCommand.class, args);
    }
    /**  */
    @CommandLine.Option(names = {"-r"}, description = "RequestId")
    public String requestId;

    /** event data, json string */
    @CommandLine.Option(names = {"-d"}, description = "event data")
    public String event;

    @CommandLine.Option(names = {"-o"}, description = "output")
    public String result;

    @CommandLine.Option(names = {"-e"}, description = "error output")
    public String errorResult;

    @Inject CalculationService service;

    @Inject ObjectMapper mapper;

    @Override
    public void run() {

        SampleRequest req;
        try {
            req = mapper.readValue(event, SampleRequest.class);
        } catch (IOException e) {
            outputError("invalid event data format");
            return;
        }

        // call function
        SampleResponse answer = service.calc(req);

        // output function result
        System.out.println(requestId + " Answer is " + answer.getAnswer());
        outputResult(answer);
    }

    private void outputResult(SampleResponse res) {
        try {
            Files.write(Paths.get(result), mapper.writeValueAsBytes(res));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputError(String error) {
        try {
            Files.write(Paths.get(errorResult), error.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}