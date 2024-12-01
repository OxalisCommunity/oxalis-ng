FROM maven:3.9.9-amazoncorretto-11-alpine AS mvn

ADD . $MAVEN_HOME

RUN cd $MAVEN_HOME \
 && mvn -B clean package -Pdist -DskipTests=true -Dgit.shallow=true \
 && mv $MAVEN_HOME/target/oxalis-ng-server /oxalis-ng-server \
 && mv $MAVEN_HOME/target/oxalis-ng-standalone /oxalis-ng-standalone \
 && mkdir -p /oxalis-ng/lib \
 && for f in $(ls /oxalis-ng-server/lib); do \
    if [ -e /oxalis-ng-standalone/lib/$f ]; then \
        mv /oxalis-ng-server/lib/$f /oxalis/lib/; \
        rm /oxalis-ng-standalone/lib/$f; \
    fi; \
 done \
 && mv /oxalis-ng-server/bin /oxalis-ng/bin-server \
 && mv /oxalis-ng-server/lib /oxalis-ng/lib-server \
 && mv /oxalis-ng-standalone/bin /oxalis-ng/bin-standalone \
 && mv /oxalis-ng-standalone/lib /oxalis-ng/lib-standalone \
 && cat /oxalis-ng/bin-server/run.sh | sed "s|lib/\*|lib-server/*:lib/*|" > /oxalis-ng/bin-server/run-docker.sh \
 && chmod 755 /oxalis-ng/bin-server/run-docker.sh \
 && cat /oxalis-ng/bin-standalone/run.sh | sed "s|lib/\*|lib-standalone/*:lib/*|" > /oxalis-ng/bin-standalone/run-docker.sh \
 && chmod 755 /oxalis-ng/bin-standalone/run-docker.sh \
 && mkdir /oxalis-ng/bin /oxalis-ng/conf /oxalis-ng/ext /oxalis-ng/inbound /oxalis-ng/outbound /oxalis-ng/plugin \
 && printf "#!/bin/sh\n\nexec /oxalis-ng/bin-\$MODE/run-docker.sh \$@" > /oxalis-ng/bin/run-docker.sh \
 && find /oxalis-ng -name .gitkeep -exec rm -rf '{}' \;


FROM amazoncorretto:11.0.25-alpine AS  oxalis-ng-base

COPY --from=mvn /oxalis-ng /oxalis-ng

ENV MODE=server

FROM oxalis-ng-base AS  oxalis-ng

VOLUME /oxalis-ng/conf /oxalis-ng/ext /oxalis-ng/inbound /oxalis-ng/outbound /oxalis-ng/plugin

EXPOSE 8080

WORKDIR /oxalis-ng

ENTRYPOINT ["sh", "bin/run-docker.sh"]
