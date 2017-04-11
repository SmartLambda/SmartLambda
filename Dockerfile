FROM java:openjdk-8

WORKDIR smartlambda
ADD . /smartlambda/

COPY docker/hibernate.xml /etc/smartlambda/
COPY docker/smartlambda.xml /etc/smartlambda/
CMD ./gradlew run