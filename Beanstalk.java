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
    } catch (Exception e) {
        System.out.println("fucked");
        System.exit(1);
    }
    
  }

  public Integer putJob() {
    return 0;
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

      String stuff = bytesRead();
      int hend = stuff.indexOf("\r\n");
      job.header = stuff.substring(0, hend);
      temp = job.header.split(" ");
      job.id = Integer.parseInt(temp[1]);
      job.bytes = Integer.parseInt(temp[2]);

      job.msg = stuff.substring(hend+2, stuff.length()-2);

      //System.out.println("header: " + job.header);
      //System.out.println("id: " + job.id);
      //System.out.println("bytes: " + job.bytes);
      //System.out.println("msg: " + job.msg);

    } catch(Exception e) { System.out.println(e); }
 
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
    String retval = "";

    //delete job
    try {
      wr.write("delete " + id + "\r\n");
      wr.flush();

      String stuff = bytesRead();

      System.out.println(stuff);

   } catch (Exception e) {}
  }

  public String bytesRead() {
      int bytesRead=0;
      String read = "";

      try {
        InputStream istream = bsock.getInputStream();
        int avail = istream.available();
        byte[] input = new byte[avail];
        while(bytesRead < avail) {
          int result = istream.read(input, bytesRead, avail - bytesRead);
          if(result == -1) break;
          bytesRead += result;
        }

        read = new String(input);
      } catch(Exception e) {}

    return read;
  }

  //closes out our connection
  public void close() {
    try {
      wr.close();
      bsock.close();
    } catch (Exception e) {}
 
  }

}
