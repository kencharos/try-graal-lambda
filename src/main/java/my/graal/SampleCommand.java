package my.graal;

import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine;

import javax.inject.Inject;

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

    @Override
    public void run() {
        System.out.println(service.calc(Integer.parseInt(context), Integer.parseInt(event)));
    }
}