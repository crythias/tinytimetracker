package tracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;;

public class MultipleInstancesLockTest extends TestCase {
    MultipleInstancesLock lock;
    
    public void setUp() throws IOException {
        File fileToLock = File.createTempFile("TinyTimeTracker", "lock");
        lock = new MultipleInstancesLock(fileToLock);
    }
    
    public void testPreventMultipleInstances_notOverlapping() throws IOException, MultipleInstancesException {
        lock.preventMultipleInstances();
        lock.allowOtherInstances();
        lock.preventMultipleInstances();
        lock.allowOtherInstances();
    }
    
    public void testPreventMultipleInstances_Overlapping() throws IOException, MultipleInstancesException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stream));
        
        lock.preventMultipleInstances();
        try {
            assertEquals(0, stream.size());
            lock.preventMultipleInstances();
            fail();
        } catch(MultipleInstancesException expected) {
            if (stream.size() == 0) {
                fail();
            }
        }
    }
    
}
