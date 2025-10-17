/*
 * Created on May 31, 2006
 *
 * Copyright (c) 2006 iArchives
 */
package tracker;

import java.net.*;
import java.util.*;

public class Messages {
    private static ResourceBundle RESOURCE_BUNDLE;

    public static void initialize(String bundleName) {
        try {
            // look in timecards directory (overrides for localization)
            getBundle(bundleName);
        } 
        catch (MissingResourceException e)
        {
            // now look in the normal place
            getBundle("tracker."+bundleName);
        }
    }

    private static void getBundle(String bundleName) throws MissingResourceException {
        try {
            ClassLoader extra = new URLClassLoader(new URL[]{new URL("file:" + System.getProperty("user.home") + "/timecards/")}, Messages.class.getClassLoader());
            RESOURCE_BUNDLE = ResourceBundle.getBundle(bundleName, Locale.getDefault(), extra);
            System.out.println("Using the messages file: " + bundleName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public synchronized static String getString(String key) {
        if (RESOURCE_BUNDLE == null)
        {
            initialize("messages");
        }
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
