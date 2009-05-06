package us.xph;

/**
 *
 * @author Mike F.
 */
public class DeadlineSoonException extends Exception {

    DeadlineSoonException(String string) {
        super(string);
    }

    public DeadlineSoonException(Throwable t) {
        super(t);
    }

}
