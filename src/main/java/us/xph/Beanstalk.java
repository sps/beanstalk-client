package us.xph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * TODO: replace datainputstream.readlines()'s with BufferedReaders
 * http://java.sun.com/j2se/1.4.2/docs/api/java/io/DataInputStream.html#readLine()
 */
public class Beanstalk {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 11300;

    // maximum size of a tube
    public static final int MAX_TUBE_NAME_BYTES = 200;
    
    // 2**16 is the default maximum size of a job body
    // TODO: make this instance configurable
    public static final int MAX_JOB_BYTES = 65536;
    public static final int DEFAULT_DELAY = 0;

    // lowest priority is: 4294967295
    public static final int DEFAULT_PRIORITY = 100;

    // default time to run should be something reasonable 
    public static final int DEFAULT_TTR = 120;
    private BeanstalkSocketClient socketClient;
    private static final Log LOG = LogFactory.getLog(Beanstalk.class);

    /**
     * Allow override of the socket client (mainly for unit testing,
     * but this could be useful...maybe)
     * @param socketClient
     */
    public Beanstalk(BeanstalkSocketClient socketClient) {
        this.socketClient = socketClient;
    }

    /**
     * Default contructor that will connect to beanstalk running on
     * DEFAULT_HOST on DEFAULT_PORT
     */
    public Beanstalk() throws BeanstalkException {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * @param host - the hostname or ip address where beanstalk is running
     * @param port - the port where beanstalk is listening
     */
    public Beanstalk(String host, int port) throws BeanstalkException {
        socketClient = new BeanstalkSocketClientImpl(host, port);
    }

    public void useTube(String tubeName) throws BeanstalkException {
        // check for null here too?
        if (tubeName.getBytes().length > MAX_TUBE_NAME_BYTES) {
            throw new BeanstalkException("tube name is too long");
        }

        String[] response = socketClient.byteWriteWithTokenizedResponse("use " + tubeName + "\r\n");

        // make sure the server says we're watchig the same tube we requested...
        if (response == null || response.length != 2 || !tubeName.equals(response[1])) {
            LOG.error("!! NOT USING REQUESTED TUBE:" + new StringBuilder().append(response).toString());
            throw new BeanstalkException("use tube failed");
        }
    }

    // TODO: track watched tubes
    public void watchTube(String tubeName) throws BeanstalkException {

        if (tubeName == null || tubeName.getBytes().length > MAX_TUBE_NAME_BYTES) {
            throw new BeanstalkException("invalid tube name:" + tubeName);
        }

        String[] response = socketClient.byteWriteWithTokenizedResponse("watch " + tubeName + "\r\n");

        // make sure the server says we're watchig the same tube we requested...
        if (response == null || response.length != 2 || !tubeName.equals(response[1])) {
            LOG.error("!! NOT WATCHING REQUESTED TUBE:" + new StringBuilder().append(response).toString());
            throw new BeanstalkException("watch tube failed");
        }
    }

    public Integer putJob(String body) throws BeanstalkException {
        return putJob(body, DEFAULT_PRIORITY, DEFAULT_DELAY, DEFAULT_TTR);
    }

    /**
     * TODO: finish javadocs
     * @param body - the job data
     * @param priority job priority
     * @param delay delay before job becomes available
     * @param ttr time to run
     * @return the job id
     * @throws us.xph.beanstalk.BeanstalkException
     */
    public Integer putJob(String body, Integer priority, Integer delay, Integer ttr) throws BeanstalkException {

        Integer jobId = null;

        byte[] byteray = body.getBytes();
        Integer len = byteray.length;

        if (len > MAX_JOB_BYTES) {
            throw new BeanstalkException("job exceeds MAX_JOB_BYTES");
        }

        StringBuilder msg = new StringBuilder(String.format("put %d %d %d ", priority, delay, ttr)).append(len).append("\r\n").append(body).append("\r\n");

        String[] response = socketClient.byteWriteWithTokenizedResponse(msg.toString());

        // TODO: need to deal with non-success situations (anything other than "INSERTED") response[0]
        if (response != null && response.length > 0) {
            jobId = Integer.valueOf(response[1]);
        }

        return jobId;
    }

    public Job getJob() throws BeanstalkException {
        String[] response = socketClient.byteWriteWithTokenizedResponse("reserve\r\n");
        /*
         * TODO: check for DEADLINE_SOON and TIMED_OUT response
         * and throw approriate exception
         * stop assuming we got a job reserved...
         */
        if (response == null || response.length != 3) {
            throw new BeanstalkException("invalid RESERVE response");
        }

        Integer id = Integer.parseInt(response[1]);
        Integer bytes = Integer.parseInt(response[2]);
        String msg = socketClient.byteRead(bytes + 2);
        // get rid of the trailing \r\n
        msg = msg.substring(0, msg.length() - 2);

        Job job = new Job(response, id, bytes, msg);

        return job;
    }

    public void deleteJob(Integer id) {
        //delete job
        try {
            String[] response = socketClient.byteWriteWithTokenizedResponse("delete " + id + "\r\n");
        } catch (BeanstalkException e) {
            LOG.error(e);
        }
    }

    public void close() {
        socketClient.close();
    }
}
