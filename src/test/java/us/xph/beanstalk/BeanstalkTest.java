package us.xph.beanstalk;

import us.xph.Beanstalk;
import us.xph.BeanstalkException;
import us.xph.BeanstalkSocketClient;
import us.xph.Job;
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
    public void testClose() {
        context.checking(new Expectations() {

            {
                one(socketClient).close();
            }
        });
        bean.close();
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

    @Test
    public void testdeleteJob() throws Exception {
        final String[] response = {""};
        context.checking(new Expectations() {

            {
                one(socketClient).byteWriteWithTokenizedResponse("delete 1234\r\n");
                will(returnValue(response));
            }
        });
        bean.deleteJob(1234);
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