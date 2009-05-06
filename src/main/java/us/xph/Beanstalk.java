package us.xph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Hashtable;

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
    private Hashtable watchList = new Hashtable();

    /**
     * Allow override of the socket client (mainly for unit testing,
     * but this could be useful...maybe)
     * @param socketClient
     */
    public Beanstalk(BeanstalkSocketClient socketClient) {
        watchList.put("default", "default");
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
        watchList.put("default", "default");
        socketClient = new BeanstalkSocketClientImpl(host, port);
    }

    /**
     * @param tubeName The name of the tube you with to use.
     */
    public void useTube(String tubeName) throws BeanstalkException {
        // check for null here too?
        if (tubeName.getBytes().length > MAX_TUBE_NAME_BYTES) {
            throw new BeanstalkException("tube name is too long");
        }

        String[] response = socketClient.byteWriteWithTokenizedResponse("use " + tubeName + "\r\n");

        // make sure the server says we're watchig the same tube we requested...
        if (response == null || response.length != 2 || !tubeName.equals(response[1])) {
            throw new BeanstalkException("use tube failed");
        }
    }


    /**
     * @param tubeName The name of the tube you wish to watch
     * @throws us.xph.beanstalk.BeanstalkException
     */
    public void watchTube(String tubeName) throws BeanstalkException {

        if (tubeName == null || tubeName.getBytes().length > MAX_TUBE_NAME_BYTES) {
            throw new BeanstalkException("invalid tube name:" + tubeName);
        }

        //Check if we are already watching that tube.
        if(!watchList.containsKey(tubeName)) {
            String[] response = socketClient.byteWriteWithTokenizedResponse("watch " + tubeName + "\r\n");

            //Reponse is in the form: WATCHING <count>\r\n
            if(checkResponseLength(response, 2)){
                if(response[0].startsWith("WATCHING")){
                    watchList.put(tubeName, tubeName);
                    
                    //Check their count matches my count
                    if( watchList.size() != Integer.parseInt(response[1]) ){
                        watchList.remove(tubeName);
                        throw new BeanstalkException("watch tube failed");
                    }
                } else {
                    throw new BeanstalkException("Unexpected server response: "+response[0]);
                }
            }
        }
    }

    /**
     * @param tubeName The name of the tube you wish to ignore
     * @throws us.xph.beanstalk.BeanstalkException
     * @throws us.xph.beanstalk.NotIgnoredException
     */
    public void ignoreTube(String tubeName) throws BeanstalkException, NotIgnoredException {

        if (tubeName == null || tubeName.getBytes().length > MAX_TUBE_NAME_BYTES) {
            throw new BeanstalkException("invalid tube name:" + tubeName);
        }
        
        if(watchList.containsKey(tubeName)){
            String[] response = socketClient.byteWriteWithTokenizedResponse("ignore " + tubeName + "\r\n");

            //Have to do error checking myself due to varible responses
            if(response == null){
                throw new BeanstalkException("Server returned null response");
            } else if( response.length == 2 && response[0].startsWith("WATCHING") ){
                if(watchList.size()-1 == Integer.parseInt(response[1])){
                    watchList.remove(tubeName);
                } else {
                    throw new BeanstalkException("Ignore tube failed");
                }
            } else if( response.length == 1 && response[0].startsWith("NOT_IGNORED") ){
                throw new NotIgnoredException("Can't ignore your last tube");
            } else {
                throw new BeanstalkException("Unexpected server response: "+response[0]);
            }


        }
    }

    public Integer putJob(String body) throws BeanstalkException, JobBuriedException {
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
     * @throws us.xph.beanstalk.JobBuriedException
     */
    public Integer putJob(String body, Integer priority, Integer delay, Integer ttr) throws BeanstalkException, JobBuriedException {

        Integer jobId = null;

        byte[] byteray = body.getBytes();
        Integer len = byteray.length;

        if (len > MAX_JOB_BYTES) {
            throw new BeanstalkException("job exceeds MAX_JOB_BYTES");
        }

        StringBuilder msg = new StringBuilder(String.format("put %d %d %d ", priority, delay, ttr)).append(len).append("\r\n").append(body).append("\r\n");

        String[] response = socketClient.byteWriteWithTokenizedResponse(msg.toString());

        if (response != null && response.length > 0) {
            if(response.length == 2){
                if(response[0].startsWith("INSERTED")){
                    jobId = Integer.valueOf(response[1]);
                } else if(response[0].startsWith("BURIED")){
                    throw new JobBuriedException("Job buried, id is "+response[1]);
                } else {
                    throw new BeanstalkException("Unexpected Server Response: "+response[0]);
                }
            } else if(response.length == 1){
                // While we should NEVER get these, better safe than sorry
                if(response[0].startsWith("EXPECTED_CRLF")){
                    throw new BeanstalkException("Transmission error. Lost CRLF");
                } else if(response[0].startsWith("JOB_TOO_BIG")){
                    throw new BeanstalkException("Job was too big.");
                } else {
                    throw new BeanstalkException("Unexpected Server Response: "+response[0]);
                }
            } else {
                throw new BeanstalkException("invalid PUT response");
            }
        } else {
            throw new BeanstalkException("invalid PUT response");
        }

        return jobId;
    }

    /**
     * @return the job 
     * @throws us.xph.beanstalk.BeanstalkException
     * @throws us.xph.beanstalk.TimedOutException 
     * @throws us.xph.beanstalk.DeadlineSoonException
     */
    public Job getJob() throws BeanstalkException, TimedOutException, DeadlineSoonException {
        return getJob(-1);
    }


    /**
     * @param timeout timeout in seconds
     * @return the job 
     * @throws us.xph.beanstalk.BeanstalkException
     * @throws us.xph.beanstalk.TimedOutException 
     * @throws us.xph.beanstalk.DeadlineSoonException
     */
    public Job getJob(Integer timeout) throws BeanstalkException, TimedOutException, DeadlineSoonException {
        String[] response;
        if(timeout < 0){
            response = socketClient.byteWriteWithTokenizedResponse("reserve\r\n");
        } else {
            response = socketClient.byteWriteWithTokenizedResponse("reserve-with-timeout "+timeout+"\r\n");
        }

        //Check the response
        if (response == null) {
            throw new BeanstalkException("Server sent null response");
        } else if( response.length == 1 ){
            if(response[0].startsWith("TIMED_OUT")){
                throw new TimedOutException("Reserve timed out");
            } else if(response[0].startsWith("DEADLINE_SOON")){
                throw new DeadlineSoonException("The job's deadline is soon");
            } else {
                throw new BeanstalkException("response from server was: "+response[0]);
            }
        } else if(response.length == 3){
            Integer id = Integer.parseInt(response[1]);
            Integer bytes = Integer.parseInt(response[2]);
            String msg = socketClient.byteRead(bytes + 2);
            // get rid of the trailing \r\n
            msg = msg.substring(0, msg.length() - 2);

            Job job = new Job(response, id, bytes, msg);

            return job;
        } else {
            throw new BeanstalkException("invalid RESERVE response");
        }
    }

    /**
     * @param id the id of the job to delete
     * @return true, if deleted. false, if not found.
     */
    public boolean deleteJob(Integer id) {
        //delete job
        boolean deleted = false;
        try {
            String[] response = socketClient.byteWriteWithTokenizedResponse("delete " + id + "\r\n");
            if(response == null){
                throw new BeanstalkException("Server sent null response");
            } else if( response.length == 1 ){
                if(response[0].startsWith("DELETED")){
                    deleted = true;
                } else if( response[0].startsWith("NOT_FOUND") ){
                    deleted =  false;
                } else {
                    throw new BeanstalkException("Unexpected server response: "+response[0]);
                }
            } else {
                throw new BeanstalkException("invalid DELETE response");
            }
        } catch (BeanstalkException e) {
            LOG.error(e);
        }

        return deleted;
    }

    /**
     * @param id The id of the job to be released
     * @param priority A new priority to assign to the job
     * @param delay An integer number of seconds to wait before putting the job in the ready queue
     * @return 0, if released. 1, if not found.
     * @throws us.xph.beanstalk.BeanstalkException
     * @throws us.xph.beanstalk.JobBuriedException
     */
    public int releaseJob(Integer id, Integer priority, Integer delay) throws BeanstalkException, JobBuriedException {
        int returnVal = 0;
        String[] response = socketClient.byteWriteWithTokenizedResponse("release " + id + " " + priority + " " + delay +"\r\n");   

        if(checkResponseLength(response, 1)){
            if(response[0].startsWith("RELEASED")){
                returnVal = 0;
            } else if( response[0].startsWith("BURIED") ){
                throw new JobBuriedException("Job buried");
            } else if( response[0].startsWith("NOT_FOUND") ){
                returnVal = 1;
            } else {
                throw new BeanstalkException("Unexpected server response: "+response[0]);
            }
        }

        return returnVal;
    }

    /**
     * @param id The id of the job to be buried
     * @param priority A new priority to assign to the job
     * @return true, if buried. false, if not found
     * @throws us.xph.beanstalk.BeanstalkException
     */
    public boolean buryJob(Integer id, Integer priority) throws BeanstalkException {
        boolean returnVal = false;

        String[] response = socketClient.byteWriteWithTokenizedResponse("bury " + id + " " + priority +"\r\n");

        if(checkResponseLength(response, 1)){
            if(response[0].startsWith("BURIED")){
                returnVal = true;
            } else if(response[0].startsWith("NOT_FOUND")){
                returnVal = false;
            } else {
                throw new BeanstalkException("Unexpected server response: "+response[0]);
            }
        }

        return returnVal;
    }

    public void close() {
        socketClient.close();
    }

    private boolean checkResponseLength(String[] response, Integer length) throws BeanstalkException{
        if( response == null){
            throw new BeanstalkException("server returned null");
        } else if( response.length != length) {
            throw new BeanstalkException("server returned a response of invalid length.");
        }
        return true;
    }
}
