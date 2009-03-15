package us.xph;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sean Scanlon
 */
public class BeanstalkSocketClientImpl implements BeanstalkSocketClient {
    private final static Log LOG = LogFactory.getLog(BeanstalkSocketClientImpl.class);
    private Socket socket;

    public BeanstalkSocketClientImpl() {
    }

    public BeanstalkSocketClientImpl(String host, int port) throws BeanstalkException {
        try {
            socket = new Socket(host, port);
        } catch (UnknownHostException ex) {
            throw new BeanstalkException(ex);
        } catch (IOException ex) {
            throw new BeanstalkException(ex);
        }
    }

    // TODO: this needs to throw an exception
    @Override
    public String byteRead(int availibleData) {
        int bytesRead = 0;
        String read = "";
        try {
            InputStream istream = socket.getInputStream();
            byte[] input = new byte[availibleData];
            while (bytesRead < availibleData) {
                int result = istream.read(input, bytesRead, availibleData - bytesRead);
                if (result == -1) {
                    break;
                }
                bytesRead += result;
            }
            read = new String(input);
        } catch (Exception e) {
            LOG.error(e);
        }

        return read;
    }

    // TODO: this needs to throw an exception
    @Override
    public void byteWrite(String msg) {
        // int bytesRead = 0;
        byte[] byteray = msg.getBytes();

        try {
            OutputStream ostream = socket.getOutputStream();
            ostream.write(byteray);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    @Override
    public String[] byteWriteWithTokenizedResponse(String msg) throws BeanstalkException {
        String[] response = null;
        try {
            DataInputStream dis = getInputStream();
            byteWrite(msg);
            response = dis.readLine().split(" ");
        } catch (IOException ex) {
            throw new BeanstalkException(ex);
        }
        return response;
    }

    // TODO: this needs to throw an exception?
    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ex) {
            LOG.error(ex);
        }
    }

    @Override
    public DataInputStream getInputStream() throws IOException {
        return new DataInputStream(socket.getInputStream());
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}