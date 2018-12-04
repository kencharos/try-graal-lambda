package my.graal;

import javax.inject.Singleton;

/**
 * DI Sample
 */
@Singleton
public class CalculationService {

    public int calc(LambdaContext ctx, SampleRequest req) {
        System.out.println(ctx.getAwsRequestId() + ":" + ctx.getFunctionName());
        return  req.getV1() + req.getV2();
    }

}
