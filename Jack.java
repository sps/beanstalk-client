import java.io.*;
import java.net.*;
import java.lang.*;

public class Jack {

  public static void main(String[] args) throws Exception {
    
    //connect to queue and grab a job
    Beanstalk bean = new Beanstalk();

    //put a job in..
    bean.putJob("€");
    bean.putJob("test");

    Job job = new Job();

    job = bean.getJob();
    //debug output?
    System.out.println("processing job #: " + job.id + "\r");
    System.out.println("job txt: " + job.msg + "\r");
    //delete job
    bean.deleteJob(job.id);

    job = bean.getJob();
    //debug output?
    System.out.println("processing job #: " + job.id + "\r");
    System.out.println("job txt: " + job.msg + "\r");
    //delete job
    bean.deleteJob(job.id);


    //close our connection
    bean.close();
  }

}
