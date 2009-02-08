import org.junit.*;
import static org.junit.Assert.*;
 
public class TestBeanstalk {

  private Beanstalk bean;

  // setups and teardowns are not quite working here yet..
  protected void setUp() {
    bean = new Beanstalk();
  }

  protected void tearDown() {
    bean.close();
  }

  @Test
  public void testputJob() {
    bean = new Beanstalk();
    bean.putJob("test");
    bean.close();
  }

  @Test
  public void testgetJob() {
    bean = new Beanstalk();
    Job job = new Job();
    job = bean.getJob();
    assertEquals("test", job.msg);
    bean.close();
  }

  //prob should be refactored to return a 1/0 depending
  //on if the job was deleted or not
  @Test
  public void testdeleteJob() {
    bean = new Beanstalk();
    Job job = new Job();
    job = bean.getJob();
    bean.deleteJob(job.id);
  }

  //make sure our getJob can handle utf8 characters
  @Test
  public void testUTF8() {
    bean = new Beanstalk();
    Job job = new Job();
    bean.putJob("€");
    job = bean.getJob();
    assertEquals("€", job.msg);
  }

  @Test
  public void testJobsReady() {
    //assertEquals((Integer)0, bean.jobsReady());
  }
 
}
