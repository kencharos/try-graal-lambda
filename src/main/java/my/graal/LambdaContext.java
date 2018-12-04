package my.graal;

public class LambdaContext {

    private String awsRequestId;

    private String traceId;

    public String getAwsRequestId() {
        return awsRequestId;
    }

    public void setAwsRequestId(String awsRequestId) {
        this.awsRequestId = awsRequestId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
