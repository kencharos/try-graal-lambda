FROM oracle/graalvm-ce:1.0.0-rc9
EXPOSE 8080
COPY build/libs/*-all.jar my-graal.jar
ADD . build
RUN java -cp my-graal.jar io.micronaut.graal.reflect.GraalClassLoadingAnalyzer
RUN native-image --no-server \
             --class-path my-graal.jar \
			 -H:ReflectionConfigurationFiles=build/reflect.json \
			 -H:EnableURLProtocols=http \
			 -H:IncludeResources="logback.xml|application.yml|META-INF/services/*.*" \
			 -H:Name=my-graal \
			 -H:Class=my.graal.Application \
			 -H:+ReportUnsupportedElementsAtRuntime \
			 -H:+AllowVMInspection \
			 --rerun-class-initialization-at-runtime='sun.security.jca.JCAUtil$CachedSecureRandomHolder,javax.net.ssl.SSLContext' \
			 --delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder,io.netty.handler.ssl.util.ThreadLocalInsecureRandom
ENTRYPOINT ["./try-graal"]