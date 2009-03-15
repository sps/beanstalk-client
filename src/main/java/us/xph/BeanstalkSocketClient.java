package us.xph;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Sean Scanlon
 */
public interface BeanstalkSocketClient {

    public String byteRead(int availibleData);

    public void byteWrite(String msg);

    public DataInputStream getInputStream() throws IOException;

    public void close();

    public String[] byteWriteWithTokenizedResponse(String msg) throws BeanstalkException;
}
