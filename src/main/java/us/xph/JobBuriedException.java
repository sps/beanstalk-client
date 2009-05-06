package us.xph;

/**
 *
 * @author Mike F.
 */
public class JobBuriedException extends Exception {

    JobBuriedException(String string) {
        super(string);
    }

    public JobBuriedException(Throwable t) {
        super(t);
    }

}
