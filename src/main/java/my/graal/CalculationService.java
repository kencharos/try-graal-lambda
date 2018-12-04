package my.graal;

import javax.inject.Singleton;

/**
 * Function sample
 */
@Singleton
public class CalculationService {

    public SampleResponse calc(LambdaContext ctx, SampleRequest req) {
        System.out.println(ctx.getAwsRequestId() + ":" + ctx.getTraceId());
        SampleResponse res = new SampleResponse();
        res.setAnswer(req.getV1() + req.getV2());
        return res;
    }

}
