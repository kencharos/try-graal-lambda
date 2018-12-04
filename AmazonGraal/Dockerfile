FROM amazonlinux:2017.03.1.20170812
# install graal to amazon linux.
ENV LANG=en_US.UTF-8


RUN yum install -y gcc gcc-c++ libc6-dev  zlib1g-dev curl bash zlib zlib-devel  \
#    && yum install -y libcxx libcxx-devel llvm-toolset-7 \
    && rm -rf /var/cache/yum


ENV GRAAL_VERSION 1.0.0-rc9
ENV GRAAL_FILENAME graalvm-ce-${GRAAL_VERSION}-linux-amd64.tar.gz

RUN curl -4 -L https://github.com/oracle/graal/releases/download/vm-${GRAAL_VERSION}/${GRAAL_FILENAME} -o /tmp/${GRAAL_FILENAME}

RUN tar -zxvf /tmp/${GRAAL_FILENAME} -C /tmp \
    && mv /tmp/graalvm-ce-${GRAAL_VERSION} /usr/lib/graalvm

RUN rm -rf /tmp/*


CMD ["/usr/lib/graalvm/bin/native-image"]