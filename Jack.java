import java.io.*;
import java.net.*;
import java.lang.*;

public class Jack {

  public static void main(String[] args) throws Exception {
    Beanstalk bean = new Beanstalk();
    Job job = new Job();

    job = bean.getJob();
    
    //debug output?
    System.out.println("processing job #: " + job.id + "\r");
    System.out.println("job txt: " + job.msg + "\r");

    //do some work here..

    //delete job
    bean.deleteJob(job.id);

    bean.close();
  }

}
