Java Beanstalk Client (not done yet)
--------------------------------------------

What is Beanstalkd?
http://xph.us/software/beanstalkd/

Currently supported operations:
- use tube
- watch tube
- ignore tube
- put job
- reserve jobs
- reserve jobs with timeout
- delete jobs
- release jobs
- bury jobs
- kick jobs

Required:
- [Java 1.6](http://developers.sun.com/downloads/)
- [Maven 2.x](http://maven.apache.org/download.html) 

build:
  mvn clean package
  this will generate 2 jars: one with the required dependencies and one standalone without the deps
  build the apidocs: mvn javadoc:javadoc && open target/site/apidocs/index.html
  mvn install (if you plan on using this as a dependency in another maven project) 

run the builtin example worker:
    start beanstalkd ( beanstalkd -l 0.0.0.0 -p 11300 )
    throw some jobs into the queue
    java -jar target/beanstalk-client-1.0-SNAPSHOT-jar-with-dependencies.jar
