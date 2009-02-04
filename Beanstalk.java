import java.io.*;
import java.net.*;
import java.lang.*;

public class Beanstalk {
  Socket bsock = null;
  OutputStreamWriter wr = null;
  BufferedReader in = null;
  String [] temp = null;

  Beanstalk() {
     try {
        bsock = new Socket("127.0.0.1", 11300);
        wr = new OutputStreamWriter(bsock.getOutputStream());
        in = new BufferedReader(new InputStreamReader(bsock.getInputStream()));
    } catch (Exception e) {
        System.out.println("fucked");
        System.exit(1);
    }
    
  }

  // grab a new job from the queue
  // returns: the job
  public Job getJob() {
    String read = "";
    int ch, total=0;

    try {
      wr.write("reserve\r\n");
      wr.flush();
    } catch(Exception e) {}

    Job job = new Job();
 
    try {
      job.header = in.readLine();
 
      //grab the bytes to be read and the id
      job.header = job.header.substring(9);
      temp = job.header.split(" ");
      job.id = Integer.parseInt(temp[0]);
      job.bytes = Integer.parseInt(temp[1]);

     
      int bytesRead=0;
      char[] input = new char[job.bytes];
      while (bytesRead < job.bytes) {
        int result = in.read(input, bytesRead, job.bytes - bytesRead);
        if (result == -1) break;
        bytesRead += result;
      }

      job.msg = new String(input);
    } catch(Exception e) {}
 
    return job;
 }

  // returns server stats yaml file
  public String stats() {
    String header = "";
    String read = "";
    String[] ray = null;
    Integer bytes;
    int ch, total = 0;

    try {
      wr.write("stats\r\n");
      wr.flush();

      header = in.readLine();
      ray = header.split(" ");
      bytes = Integer.parseInt(ray[1]);

      int bytesRead=0;
      char[] input = new char[bytes];
      while (bytesRead < bytes) {
        int result = in.read(input, bytesRead, bytes - bytesRead);
        if (result == -1) break;
        bytesRead += result;
      }

      read = new String(input);
    } catch (Exception e) {}

    return read;
  }

  // returns number of jobs pending in queue
  public Integer jobsReady() {
    String stats = stats();
    String[] ray = null;
    
    ray = stats.split("\n");
   
    stats = ray[8];
    ray = stats.split(": ");
    
    return Integer.parseInt(ray[1]);
  }

  //deletes job number id from the queue
  public void deleteJob(Integer id) {
    //delete job
    try {
      wr.write("delete " + id + "\r\n");
      wr.flush();

    //debug?
    System.out.println("deleting job " + id + " from queue.");
   } catch (Exception e) {}
  }

  //closes out our connection
  public void close() {
    try {
      wr.close();
      in.close();
      bsock.close();
    } catch (Exception e) {}
 
  }

}
