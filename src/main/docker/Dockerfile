FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD crawler-1.0.jar
RUN sh -c 'touch /crawler-1.0.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /crawler-1.0.jar" ]