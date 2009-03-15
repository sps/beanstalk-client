package us.xph.beanstalk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Sean Scanlon
 */
public class MockSocket extends Socket {

    @Override
    public InputStream getInputStream() {
        return new InputStream() {

            @Override
            public int read(byte[] b, int read, int foo) throws IOException {
                return 1;
            }

            @Override
            public int read() throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Override
    public OutputStream getOutputStream() {
        return new OutputStream() {

            @Override
            public void write(int arg0) throws IOException {
            }
        };
    }
}
