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
    bean.getJob();
    assertEquals("test", job.msg);
    bean.close();
  }

  @Test
  public void testJobsReady() {
    //assertEquals((Integer)0, bean.jobsReady());
  }
 
}
