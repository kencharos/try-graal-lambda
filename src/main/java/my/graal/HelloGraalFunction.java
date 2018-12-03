package my.graal;

import io.micronaut.function.FunctionBean;

import javax.inject.Inject;
import java.util.function.Function;

@FunctionBean("hello-graal")
public class HelloGraalFunction implements Function<SampleRequest, SampleResponse> {

    @Inject private CalculationService service;

    @Override
    public SampleResponse apply(SampleRequest input) {
        int answer = service.calc(input.getV1(), input.getV2());
        SampleResponse res = new SampleResponse();
        res.setAnswer(answer);

        return res;

    }
}
