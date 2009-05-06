package us.xph;

/**
 *
 * @author Mike F.
 */
public class TimedOutException extends Exception {

    TimedOutException(String string) {
        super(string);
    }

    public TimedOutException(Throwable t) {
        super(t);
    }

}
