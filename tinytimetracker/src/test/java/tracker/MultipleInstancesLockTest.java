// SPDX-License-Identifier: GPL-2.0-only
package tracker;
/**
 * Tests for MultipleInstancesLock class.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

public class MultipleInstancesLockTest {
    MultipleInstancesLock lock;
    
    @BeforeEach
    public void setUp() throws IOException {
        File fileToLock = File.createTempFile("TinyTimeTracker", "lock");
        lock = new MultipleInstancesLock(fileToLock);
    }
    
    @Test
    public void testPreventMultipleInstances_notOverlapping() throws IOException, MultipleInstancesException {
        lock.preventMultipleInstances();
        lock.allowOtherInstances();
        lock.preventMultipleInstances();
        lock.allowOtherInstances();
    }
    
    @Test
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
