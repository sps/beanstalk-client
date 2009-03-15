package us.xph.beanstalk;

import us.xph.BeanstalkSocketClientImpl;
import java.net.Socket;
import org.junit.Test;

import org.jmock.Mockery;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 *
 * @author sscanlon
 */
public class BeanStockSocketClientImplTest {

    private Mockery context;

    @Before
    public void setUp() throws Exception {
        context = new Mockery();
    }

    /**
     * Test of byteRead method, of class BeanstalkSocketClientImpl.
     */
    @Test
    public void testByteRead() throws Exception {
        Socket s = new MockSocket();
        int avail = 1;
        BeanstalkSocketClientImpl instance = new BeanstalkSocketClientImpl();
        instance.setSocket(s);
        assertNotNull(instance.byteRead(avail));
        
    }

    /**
     * Test of byteWrite method, of class BeanstalkSocketClientImpl.
     */
    @Test
    public void testByteWrite() {
        Socket s = new MockSocket();
        String msg = "";
        BeanstalkSocketClientImpl instance = new BeanstalkSocketClientImpl();
        instance.setSocket(s);
        instance.byteWrite(msg);
    }

  
}