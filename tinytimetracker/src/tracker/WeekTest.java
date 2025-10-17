package tracker;

import java.util.*;

import junit.framework.TestCase;

public class WeekTest extends TestCase {

    // TODO Fix this: 
    // If you run this in a locale in which some day is not suitable as an excel sheet name,
    // these tests will fail. In general, it would be nice to run these tests in different 
    // locales. Perhaps we should just pick 2 or 3 locales and have these tests be explicit
    // about which local it is testing. So, write tests like these for a local with days that  
    // are not suitable for excel sheet names.

    String[] frenchWeekDays = { "dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

    public void testGetLocalizedDayOfWeekFor() throws Exception {
        Locale originalLocale = Locale.getDefault();
        Locale.setDefault( Locale.FRANCE ); // Why France? Why not?

        // No matter what 1st day of week is given to Week, getLocalizedDayOfWeekFor should always return
        // the same value.
        for(int firstDayOfWeek = 1; firstDayOfWeek<8; firstDayOfWeek++) {
            Week w = new Week(firstDayOfWeek);

            assertEquals("loop#"+firstDayOfWeek, frenchWeekDays[0], w.getLocalizedDayOfWeekFor(1) ); //$NON-NLS-1$
            assertEquals("loop#"+firstDayOfWeek, frenchWeekDays[1], w.getLocalizedDayOfWeekFor(2) ); //$NON-NLS-1$
            assertEquals("loop#"+firstDayOfWeek, frenchWeekDays[2], w.getLocalizedDayOfWeekFor(3) ); //$NON-NLS-1$
            assertEquals("loop#"+firstDayOfWeek, frenchWeekDays[3], w.getLocalizedDayOfWeekFor(4) ); //$NON-NLS-1$
            assertEquals("loop#"+firstDayOfWeek, frenchWeekDays[4], w.getLocalizedDayOfWeekFor(5) ); //$NON-NLS-1$
            assertEquals("loop#"+firstDayOfWeek, frenchWeekDays[5], w.getLocalizedDayOfWeekFor(6) ); //$NON-NLS-1$
            assertEquals("loop#"+firstDayOfWeek, frenchWeekDays[6], w.getLocalizedDayOfWeekFor(7) ); //$NON-NLS-1$

            try {  
                w.getLocalizedDayOfWeekFor(8);
                fail();
            } catch( Exception expected ) {}

            try {  
                w.getLocalizedDayOfWeekFor(0);
                fail();
            } catch( Exception expected ) {}
        }

        Locale.setDefault( originalLocale );
    }

    public void getDaysSinceFirstDOW()
    {
        Week w = new Week(Calendar.WEDNESDAY);
        assertEquals(0, w.getDaysSinceFirstDOW(Calendar.WEDNESDAY));
        assertEquals(1, w.getDaysSinceFirstDOW(Calendar.THURSDAY));
        assertEquals(6, w.getDaysSinceFirstDOW(Calendar.TUESDAY));

        w = new Week(Calendar.SATURDAY);
        assertEquals(0, w.getDaysSinceFirstDOW(Calendar.SATURDAY));
        assertEquals(5, w.getDaysSinceFirstDOW(Calendar.THURSDAY));
        assertEquals(3, w.getDaysSinceFirstDOW(Calendar.TUESDAY));

        w = new Week(Calendar.SUNDAY);
        assertEquals(6, w.getDaysSinceFirstDOW(Calendar.SATURDAY));
        assertEquals(4, w.getDaysSinceFirstDOW(Calendar.THURSDAY));
        assertEquals(2, w.getDaysSinceFirstDOW(Calendar.TUESDAY));
}
    
    public void testIterator() throws Exception {
        Locale originalLocale = Locale.getDefault();
        Locale.setDefault( Locale.FRANCE ); // Why France? Why not?
        for(int firstDayOfWeek = 1; firstDayOfWeek<8; firstDayOfWeek++) {
//          System.out.println();
            Week w = new Week(firstDayOfWeek);

            Iterator<String> iterator = w.getDayOfWeekIterator();
            int ordinal = 0;
            while(iterator.hasNext()) {
                String day = iterator.next();
                assertEquals("loop#"+firstDayOfWeek, getFrenchWeekDay(ordinal + firstDayOfWeek), day ); //$NON-NLS-1$
                ordinal++;
            }
        }      
        Locale.setDefault( originalLocale );
    }

    private String getFrenchWeekDay(int dayOfWeek) {
        if( dayOfWeek > 7 ) dayOfWeek -= 7 ;
//      System.out.print(" "+dayOfWeek);
        return frenchWeekDays[dayOfWeek-1];
    }
}
