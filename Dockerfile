FROM openjdk:17

WORKDIR /app

COPY . .

RUN javac Server.java

CMD ["java", "Server"]
