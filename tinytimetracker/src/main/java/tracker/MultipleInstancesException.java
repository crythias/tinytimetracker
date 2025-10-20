package tracker;

/**
 * Thrown when multiple instances of the application are detected.
 */

public class MultipleInstancesException extends Exception {
    public MultipleInstancesException(String message, Throwable e) {
        super(message,e);
    }
    public MultipleInstancesException(String message) {
        super(message);
    }    
}
