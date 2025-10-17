package tracker;

public class MultipleInstancesException extends Exception {
    public MultipleInstancesException(String message, Throwable e) {
        super(message,e);
    }
    public MultipleInstancesException(String message) {
        super(message);
    }    
}
