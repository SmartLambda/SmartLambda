FROM java:openjdk-8
RUN apt-get update -y
RUN apt-get install git -y

RUN git clone https://github.com/SmartLambda/SmartLambda.git
WORKDIR SmartLambda 

COPY hibernate.xml /etc/smartlambda/
COPY smartlambda.xml /etc/smartlambda/
RUN ./gradlew build -x test

CMD java -jar build/libs/smartlambda-1.0-SNAPSHOT.jar
