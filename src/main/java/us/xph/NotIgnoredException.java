package us.xph;

/**
 *
 * @author Mike F.
 */
public class NotIgnoredException extends Exception {

    NotIgnoredException(String string) {
        super(string);
    }

    public NotIgnoredException(Throwable t) {
        super(t);
    }

}
