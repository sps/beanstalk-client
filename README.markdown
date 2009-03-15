Java Beanstalk Client (not done yet)
--------------------------------------------

What is Beanstalkd?
http://xph.us/software/beanstalkd/

Currently supported operations:
- put job
- reserve jobs
- delete jobs
- use tube
- watch tube

Required:
- [Java 1.6](http://developers.sun.com/downloads/)
- [Maven 2.x](http://maven.apache.org/download.html) 

build:
  mvn clean package
  this will generate 2 jars: one with the required dependencies and one standalone without the deps

run the builtin example worker:
    start beanstalkd ( beanstalkd -l 0.0.0.0 -p 11300 )
    throw some jobs into the queue
    java -jar target/beanstalk-client-1.0-SNAPSHOT-jar-with-dependencies.jar
