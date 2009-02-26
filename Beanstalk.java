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

  // takes a string, inserts
  // FUTURE: should return either the string or a true/false
  public Integer putJob(String body) {
   byte[] byteray = body.getBytes();
   Integer len = byteray.length;

    try {
      DataInputStream dis = new DataInputStream(bsock.getInputStream());
      byteWrite("put 65536 0 120 " + len + "\r\n" +
              body + "\r\n");
      String stuff = dis.readLine();

    } catch(Exception e) { System.out.println(e);}

    return 0;
  }

  // grab a new job from the queue
  // returns: the job
  public Job getJob() {
    String read = "";

    int ch, total=0;

    try {
      byteWrite("reserve\r\n");
    } catch(Exception e) {}

    Job job = new Job();
 
    try {
      DataInputStream dis = new DataInputStream(bsock.getInputStream());
      job.header = dis.readLine();
      temp = job.header.split(" ");
      job.id = Integer.parseInt(temp[1]);
      job.bytes = Integer.parseInt(temp[2]);

      job.msg = byteRead(bsock, job.bytes+2);

    } catch(Exception e) { System.out.println(e); }
 
    return job;
 }

  //deletes job number id from the queue
  public void deleteJob(Integer id) {
    String retval = "";

    //delete job
    try {
      DataInputStream dis = new DataInputStream(bsock.getInputStream());
      byteWrite("delete " + id + "\r\n");
      String stuff = dis.readLine();

    } catch (Exception e) {}
  }

  public static String byteRead(Socket blah, int avail) {
      int bytesRead=0;
      String read = "";

      try {
        InputStream istream = blah.getInputStream();

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

  // converts msg to byte array first for utf8
  public void byteWrite(String msg) {
      int bytesRead=0;
      String read = "";
      byte[] byteray = msg.getBytes();
 
      try {
        OutputStream ostream = bsock.getOutputStream();
        ostream.write(byteray);
      } catch(Exception e) {}

  }

  //closes out our connection
  public void close() {
    try {
      wr.close();
      bsock.close();
    } catch (Exception e) {}
 
  }

}
