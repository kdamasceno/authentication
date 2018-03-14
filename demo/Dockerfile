FROM anapsix/alpine-java:8_jdk

ENV MAVEN_VERSION 3.5.2
ENV MAVEN_HOME /usr/lib/mvn
ENV PATH $MAVEN_HOME/bin:$PATH

RUN wget http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  rm apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  mv apache-maven-$MAVEN_VERSION /usr/lib/mvn

RUN mkdir -p /opt/app
WORKDIR /opt/app 

COPY pom.xml /opt/app/

COPY src /opt/app/src
RUN mvn install
RUN mvn dependency:resolve-plugins

EXPOSE 8080

CMD ["mvn", "tomcat7:run"]