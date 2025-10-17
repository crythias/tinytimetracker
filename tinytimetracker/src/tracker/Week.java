package tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParser;

public class Week {
    private static final DateFormat dowFormat = new SimpleDateFormat("EEEE"); //$NON-NLS-1$
    private static final DateFormat englishDowFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH); //$NON-NLS-1$
    
    private final String[] dow;
    private final int firstDayOfWeek;

    /**
     * The 1-based weekday, that is, Calendar.SUNDAY - Calendar.SATURDAY
     * @param firstDayOfWeek
     */
    public Week(int firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
        dow = new String[8]; 
        // the first index won't get used, since days of the week are 1-based,
        // but for convenience we'll live with that.

        Calendar c = Calendar.getInstance();
        for(int i=1; i<8; i++)
        {
            c.set(Calendar.DAY_OF_WEEK, i);
            dow[i] = getLocalizedWeekDay(c);
        }
    }

    /**
     * 
     * @param day One of the day of week fields on Calendar (Calendar.SUNDAY)
     * @return
     */
    public String getLocalizedDayOfWeekFor(int day)
    {
        if (day < 1 || day > 7)
        {
            throw new IllegalArgumentException("Expected a number between 1 and 7"); //$NON-NLS-1$
        }
        return dow[day];
    }

    public Iterator<String> getDayOfWeekIterator() {
        return new Iterator<String>() {
            private int ordinal = 0;
            public boolean hasNext() { return ordinal < 7; }
            public String next() { 
                return getLocalizedDayOfWeekFor((((ordinal++ + firstDayOfWeek)-1)%7)+1);
            }
            public void remove() {
                throw new UnsupportedOperationException("Can't remove from this iterator"); //$NON-NLS-1$
            }
        };
    }

    public static String getLocalizedWeekDay(Calendar calendar) {
        String day = dowFormat.format(calendar.getTime());
        try {
            makeSureDayIsValidExcelLabel(day);
        }
        catch (Exception e)   {
            String englishDay = englishDowFormat.format(calendar.getTime());
            System.err.println("The localized day of week " + day + //$NON-NLS-1$ 
                    " is not suitable as an excel sheet name.  Reverting to the English" + //$NON-NLS-1$
                    " representation. " + englishDay); //$NON-NLS-1$
            day = englishDay;
        }
        return day;
    }

    private static void makeSureDayIsValidExcelLabel(String day) {
        String formula = "SUM('"+day+"'!A1:A65000)"; //$NON-NLS-1$ //$NON-NLS-2$
        HSSFEvaluationWorkbook evaluationWorkbook = HSSFEvaluationWorkbook.create(new HSSFWorkbook());
        FormulaParser.parse(formula, evaluationWorkbook);
    }

    /**
     * @return the number of days between the the first day of the week and the passed in calendar. 
     * @param calendar the calendar containing the weekday in question
     */
    public int getDaysSinceFirstDOW(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return getDaysSinceFirstDOW(day);
    }

    /**
     * @param day
     * @return
     */
    public int getDaysSinceFirstDOW(int day) {
        if (day < firstDayOfWeek)
        {
            return day + 7 - firstDayOfWeek;
        }
        else
        {
            return day - firstDayOfWeek;
        }
    }
}
