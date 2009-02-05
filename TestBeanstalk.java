import org.junit.*;
import static org.junit.Assert.*;
 
public class TestBeanstalk {

  private Beanstalk bean;

  protected void setUp() {
    bean = new Beanstalk();
  }

  protected void tearDown() {
    bean.close();
  }

  @Test
  public void testJobsReady() {
    //assertEquals((Integer)0, bean.jobsReady());
  }
 
}
