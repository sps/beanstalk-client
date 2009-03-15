package us.xph;

/**
 *
 * @author Sean Scanlon
 */
public class BeanstalkException extends Exception {

    BeanstalkException(String string) {
        super(string);
    }

    public BeanstalkException(Throwable t) {
        super(t.fillInStackTrace());
    }


}
