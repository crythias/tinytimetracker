package tracker;

public class MnemonicActionName {
    final String actionMessage;
    final Integer actionMnemonic;
    
    public MnemonicActionName(String key) {
        String value = Messages.getString(key);
        
        int index = value.indexOf('&');
        // If there is no '&', index will be -1 and we'll just 
        // use the first character.
        actionMnemonic = Integer.valueOf( Character.toUpperCase( value.charAt(index+1) ) );
        if (!((actionMnemonic >= 'A' && actionMnemonic <= 'Z' )
            || actionMnemonic > '0' && actionMnemonic <= '9' ))
        {
            // just give a warning that the desired mnemonic will likely not be used
            System.err.println("Unsupported mnemonic character: " + actionMnemonic);
        } 

        actionMessage = value.replaceFirst("&", "");
    }
}
