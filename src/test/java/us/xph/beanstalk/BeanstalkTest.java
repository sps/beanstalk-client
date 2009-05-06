package us.xph.beanstalk;

import us.xph.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import java.io.UnsupportedEncodingException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BeanstalkTest {

    private Beanstalk bean;
    private Mockery context;
    private BeanstalkSocketClient socketClient;

    @Before
    public void setUp() throws BeanstalkException {
        context = new Mockery();
        socketClient = context.mock(BeanstalkSocketClient.class);
        bean = new Beanstalk(socketClient);
    }

    @Test
    public void testPutJob() throws Exception {
        final String[] response = {"INSERTED", "1234"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });

        Integer jobId = bean.putJob("test");
        assertTrue(jobId == 1234);

    }

    @Test(expected = JobBuriedException.class)
    public void testPutJobBuried() throws Exception {
        final String[] response = {"BURIED", "1234"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });

        Integer jobId = bean.putJob("test");
        assertTrue(jobId == 1234);
    }

    @Test(expected = BeanstalkException.class)
    public void testPutJobCrlfError() throws Exception {
        final String[] response = {"EXPECTED_CRLF"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });

        Integer jobId = bean.putJob("test");
    }

    @Test(expected = BeanstalkException.class)
    public void testPutJobTooBigError() throws Exception {
        final String[] response = {"JOB_TOO_BIG"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });

        Integer jobId = bean.putJob("test");
    }

    @Test(expected = BeanstalkException.class)
    public void testMaxJobSize() throws Exception {
        StringBuilder oversizedJob = new StringBuilder("1");
        for (int i = 0; i <= Beanstalk.MAX_JOB_BYTES; i++) {
            oversizedJob.append("1");
        }
        bean.putJob(oversizedJob.toString());
    }

    @Test
    public void testUseTube() throws Exception {
        String tubeName = "foo";

        final String[] response = {"USING", "foo"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.useTube(tubeName);
    }

    @Test(expected = BeanstalkException.class)
    public void testUseTubeException() throws Exception {
        String tubeName = "foo";
        final String[] response = {"USING", "XXX"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.useTube(tubeName);
    }

    @Test
    public void testGetJob() throws Exception {
        final String[] response = {"RESERVED", "1234", "100"};
        final String jobBody = "foo\r\n";
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
                one(socketClient).byteRead(102);
                will(returnValue(jobBody));
            }
        });

        Job job = bean.getJob();
        assertEquals(job.getMsg(), "foo");
        assertTrue(job.getId() == 1234);
        assertTrue(job.getBytes() == 100);
    }

    @Test(expected = DeadlineSoonException.class)
    public void testGetJobDeadlineSoon() throws Exception {
        final String[] response = {"DEADLINE_SOON"};
        final String jobBody = "foo\r\n";
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
                one(socketClient).byteRead(102);
                will(returnValue(jobBody));
            }
        });

        Job job = bean.getJob();
    }

    @Test
    public void testGetJobWithTimeout() throws Exception {
        final String[] response = {"RESERVED", "1234", "100"};
        final String jobBody = "foo\r\n";
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
                one(socketClient).byteRead(102);
                will(returnValue(jobBody));
            }
        });

        Job job = bean.getJob(2);
        assertEquals(job.getMsg(), "foo");
        assertTrue(job.getId() == 1234);
        assertTrue(job.getBytes() == 100);
    }

    @Test(expected = TimedOutException.class)
    public void testGetJobWithTimeoutError() throws Exception {
        final String[] response = {"TIMED_OUT"};
        final String jobBody = "foo\r\n";
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
                one(socketClient).byteRead(102);
                will(returnValue(jobBody));
            }
        });

        Job job = bean.getJob(2);
    }

    @Test
    public void testDeleteJob() throws Exception {
        final String[] response = {"DELETED"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse("delete 1234\r\n");
                will(returnValue(response));
            }
        });
        bean.deleteJob(1234);
    }

    @Test
    public void testDeleteJobNotFound() throws Exception {
        final String[] response = {"NOT_FOUND"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse("delete 1234\r\n");
                will(returnValue(response));
            }
        });
        bean.deleteJob(1234);
    }

    @Test
    public void testReleaseJob() throws Exception {
        final String[] response = {"RELEASED"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.releaseJob(123, 100, 0);
    }

    @Test
    public void testReleaseNonexistantJob() throws Exception {
        final String[] response = {"NOT_FOUND"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.releaseJob(1234, 100, 0);
    }

    @Test(expected = JobBuriedException.class)
    public void testReleaseJobBuried() throws Exception {
        final String[] response = {"BURIED"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.releaseJob(1234, 100, 0);
    }

    @Test
    public void testBuryJob() throws Exception {
        final String[] response = {"BURIED"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.buryJob(1234, 100);
    }

    @Test
    public void testBuryNonexistantJob() throws Exception {
        final String[] response = {"NOT_FOUND"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.buryJob(1234, 100);
    }

    @Test
    public void testWatchTube() throws Exception {
        final String[] response = {"WATCHING", "2"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.watchTube("aSecondTube");
    }

    @Test
    public void testIgnoreTube() throws Exception {
        final String[] response = {"WATCHING", "1"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.ignoreTube("aSecondTube");
    }

    @Test(expected = NotIgnoredException.class)
    public void testIgnoreTubeError() throws Exception {
        final String[] response = {"NOT_IGNORED"};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse(with(any(String.class)));
                will(returnValue(response));
            }
        });
        bean.ignoreTube("default");
    }

    @Test
    public void testClose() {
        context.checking(new Expectations() {

            {
                one(socketClient).close();
            }
        });
        bean.close();
    }

    private DataInputStream makeStreamWithString(String msg) {
        byte[] byteArray = null;
        try {
            byteArray = new String(msg).getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            System.err.println(ex.toString());
        }
        ByteArrayInputStream baos = new ByteArrayInputStream(byteArray);
        return new DataInputStream(baos);
    }
}
