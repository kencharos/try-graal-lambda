FROM oracle/graalvm-ce:1.0.0-rc9
COPY build/libs/*-all.jar my-graal.jar
# for picclli cli application.
COPY src/main/resources/refcli.json refcli.json 
ADD . build
RUN java -cp my-graal.jar io.micronaut.graal.reflect.GraalClassLoadingAnalyzer
RUN cat build/reflect.json
# -H:-UseServiceLoaderFeature is for RC8
RUN native-image --no-server \
             --class-path my-graal.jar \
			 -H:ReflectionConfigurationFiles=build/reflect.json,refcli.json \
			 -H:EnableURLProtocols=http \
			 -H:IncludeResources="logback.xml|application.yml|META-INF/services/*.*" \
			 -H:Name=my-graal \
			 -H:Class=my.graal.SampleCommand \
			 -H:+ReportUnsupportedElementsAtRuntime \
			 -H:-AllowVMInspection \
			 -H:-UseServiceLoaderFeature \
			 -R:-InstallSegfaultHandler \
			 --rerun-class-initialization-at-runtime='sun.security.jca.JCAUtil$CachedSecureRandomHolder,javax.net.ssl.SSLContext' \
			 --delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder,io.netty.handler.ssl.util.ThreadLocalInsecureRandom
ENTRYPOINT ["./my-graal"]
