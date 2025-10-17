package tracker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class MnemonicActionNameTest  {
    void setUp() {
        Messages.initialize("testMessages");
    }
    
    void testNoMnemonic() {
        MnemonicActionName name = new MnemonicActionName("foo");
        assertEquals('F', (char)name.actionMnemonic.intValue());
        assertEquals("foo", name.actionMessage);
    }
    
    void testMnemonic_FirstPosition() {
        MnemonicActionName name = new MnemonicActionName("&foo");
        assertEquals('F', (char)name.actionMnemonic.intValue());
        assertEquals("foo", name.actionMessage);
    }
    
    void testMnemonic_SecondPosition() {
        MnemonicActionName name = new MnemonicActionName("f&oo");
        assertEquals('O', (char)name.actionMnemonic.intValue());
        assertEquals("foo", name.actionMessage);
    }  

    void testMnemonic_Period() {
        MnemonicActionName name = new MnemonicActionName("&.foo");
        assertEquals('.', (char)name.actionMnemonic.intValue());
        assertEquals(".foo", name.actionMessage);
    } 
    
    void testMnemonic_PeriodFirstPosition() {
        MnemonicActionName name = new MnemonicActionName(".foo");
        assertEquals('.', (char)name.actionMnemonic.intValue());
        assertEquals(".foo", name.actionMessage);
    }  
    
    void testMnemonic_LastValidPosition() {
        MnemonicActionName name = new MnemonicActionName("ba&r");
        assertEquals('R', (char)name.actionMnemonic.intValue());
        assertEquals("bar", name.actionMessage);
    } 
    
    void testMnemonic_InvalidPosition() {
        try {
            new MnemonicActionName("bar&");
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }    
    
}
