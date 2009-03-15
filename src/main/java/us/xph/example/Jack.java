package us.xph.example;

import us.xph.Beanstalk;
import us.xph.Job;

public class Jack {

    public static void main(String[] args) throws Exception {
        int running = 1;

        //connect to queue and grab a job
        Beanstalk bean = new Beanstalk();
        //Job job = new Job();

        while (running == 1) {
            Job job = bean.getJob();
            //debug output?
            System.out.println("processing job #: " + job.getId() + "\r");
            System.out.println("job txt: " + job.getMsg() + "\r");
            //delete job
            bean.deleteJob(job.getId());
        }

        //close our connection
        bean.close();
    }
}
