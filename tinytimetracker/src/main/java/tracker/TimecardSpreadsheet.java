// SPDX-License-Identifier: GPL-2.0-only
package tracker;

/**
 * Manages the timecard spreadsheet.
 */

import java.io.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;

public class TimecardSpreadsheet {

    private final HSSFSheet timecardSheet;
    private HSSFWorkbook wb;
    private final File f;
    private final File tmpFile;
    private CellStyle elapsedTimeStyle, hmsStyle;

    private int taskColumn;
    private int totalTimeColumn;
    private HSSFRow headingRow;
    private HSSFSheet daySheet;
    private final long scheduledExecutionTime;
    private final Week week;
//  private String lastTask;
//  private Date lastEnd, lastStart, secondToLastEnd, secondToLastStart;
    private TaskRow lastTaskRow, secondToLastTaskRow;
    //private HSSFCell lastEndCell, lastStartCell, secondToLastEndCell, secondToLastStartCell;
    private String currentTask;

    public TimecardSpreadsheet(File f, long scheduledExecutionTime, Week week) throws IOException {
        this.f = f;
        this.scheduledExecutionTime = scheduledExecutionTime;
        this.week = week;

        tmpFile = new File(f.toString() + ".tmp"); //$NON-NLS-1$

        String elapsedTimeFormatString = "[h]:mm"; //$NON-NLS-1$
        String hmsFormatString = "h:mm:ss"; //$NON-NLS-1$

        if (!f.isFile()) {
            timecardSheet = createNewWorkbook(elapsedTimeFormatString);
        } else {
            timecardSheet = openWorkbook(f);
        }

        if (elapsedTimeStyle == null) {
            elapsedTimeStyle = findStyle(elapsedTimeFormatString);
        }

        if (hmsStyle == null) {
            hmsStyle = findStyle(hmsFormatString);
        }

        if (hmsStyle == null) {
            hmsStyle = wb.createCellStyle();
            hmsStyle.setDataFormat((short)0x15);
        }

        if (elapsedTimeStyle == null) {
            short elapsedTimeFormatIndex = wb.createDataFormat().getFormat(elapsedTimeFormatString);
            elapsedTimeStyle = wb.createCellStyle();
            elapsedTimeStyle.setDataFormat(elapsedTimeFormatIndex);
        }

        taskColumn = -1;
        totalTimeColumn = -1;

        Iterator<?> rowIterator = timecardSheet.rowIterator();
        while (rowIterator.hasNext() && !(taskColumn >= 0 || totalTimeColumn >= 0)) {
            HSSFRow row = (HSSFRow)rowIterator.next();
            Iterator<?> cellIterator = row.cellIterator();
            while (cellIterator.hasNext() && (totalTimeColumn == -1 || taskColumn == -1)) {
                HSSFCell cell = (HSSFCell) cellIterator.next();
                if (cell.getRichStringCellValue().getString().equals(Messages.getString("Tracker.Task"))) { //$NON-NLS-1$
                    taskColumn = cell.getColumnIndex();
                    headingRow = row;
                } else if (cell.getRichStringCellValue().getString().equals(Messages.getString("Tracker.Total"))) { //$NON-NLS-1$
                    totalTimeColumn = cell.getColumnIndex();
                    headingRow = row;
                }
            }
        }

        if (taskColumn == -1) taskColumn = 0;
        if (totalTimeColumn == -1) totalTimeColumn = 1;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(scheduledExecutionTime);
        daySheet = wb.getSheet( Week.getLocalizedWeekDay( calendar ) );
        if (daySheet == null)
        {
            // this can happen if the days of the week have characters are 
            // incompatible with the spreadsheet encoding
            daySheet = wb.getSheetAt(week.getDaysSinceFirstDOW(calendar)+1);
        }

        lastTaskRow = getTaskRowOnOrBefore(daySheet.getLastRowNum());
        if (lastTaskRow != null) {
            currentTask = lastTaskRow.getTask();
            secondToLastTaskRow = getTaskRowOnOrBefore(lastTaskRow.getRowNum() - 1);
        }
        /*
      // find wrap style
      short numCellStyles = wb.getNumCellStyles();
      for(short i=0; i<numCellStyles; i++) {
          CellStyle thestyle = wb.getCellStyleAt(i);
          if (thestyle.getWrapText()) {
              wrapStyle = thestyle;
          }
      }
      if (wrapStyle == null) {
          wrapStyle = wb.createCellStyle();
          wrapStyle.setWrapText(true);
      }
         */
    }

    private HSSFSheet openWorkbook(File f) throws IOException {
        if (tmpFile.exists()) {
            FileInputStream fis = new FileInputStream(tmpFile);
            try {
                wb = new HSSFWorkbook(fis);
            }
            catch (Throwable t) {
                // we'll try again below
            }
            finally {
                fis.close();
            }
        }
        if (wb == null) {
            FileInputStream fis = new FileInputStream(f);
            try {
                wb = new HSSFWorkbook(fis);
            }
            finally {
                fis.close();
            }
        }
        return wb.getSheetAt(0);
    }

    private HSSFSheet createNewWorkbook(String elapsedTimeFormatString) {
        wb = new HSSFWorkbook();
        HSSFSheet timecardSheet = wb.createSheet(Messages.getString("Tracker.Timecard")); //$NON-NLS-1$
        HSSFRow row = timecardSheet.createRow(0);
        HSSFFont boldFont = wb.createFont();
        boldFont.setBold(true);

        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFont(boldFont);

        timecardSheet.setDefaultColumnWidth(12);

        createCell(row, Messages.getString("Tracker.Task"), headerStyle); //$NON-NLS-1$
        createCell(row, Messages.getString("Tracker.Total"), headerStyle); //$NON-NLS-1$

        Iterator<String> iterator = week.getDayOfWeekIterator();
        while(iterator.hasNext()) {
            createCell(row, iterator.next(), headerStyle);
        }

        short elapsedTimeFormatIndex = wb.createDataFormat().getFormat(elapsedTimeFormatString);
        elapsedTimeStyle = wb.createCellStyle();
        elapsedTimeStyle.setDataFormat(elapsedTimeFormatIndex);

        CellStyle underlineTimeStyle = wb.createCellStyle();
        underlineTimeStyle.setDataFormat(elapsedTimeFormatIndex);
        underlineTimeStyle.setBorderBottom(BorderStyle.THIN);

        HSSFRow row2 = timecardSheet.createRow(1);
        createCell(row2, Messages.getString("Tracker.DailyTotals"), underlineTimeStyle); //$NON-NLS-1$
        createFormulaCell(row2, "SUM(C2:I2)", underlineTimeStyle); //$NON-NLS-1$
        createFormulaCell(row2, "SUM(C3:C10000)", underlineTimeStyle); //$NON-NLS-1$
        createFormulaCell(row2, "SUM(D3:D10000)", underlineTimeStyle); //$NON-NLS-1$
        createFormulaCell(row2, "SUM(E3:E10000)", underlineTimeStyle); //$NON-NLS-1$
        createFormulaCell(row2, "SUM(F3:F10000)", underlineTimeStyle); //$NON-NLS-1$
        createFormulaCell(row2, "SUM(G3:G10000)", underlineTimeStyle); //$NON-NLS-1$
        createFormulaCell(row2, "SUM(H3:H10000)", underlineTimeStyle); //$NON-NLS-1$
        createFormulaCell(row2, "SUM(I3:I10000)", underlineTimeStyle); //$NON-NLS-1$

        iterator = week.getDayOfWeekIterator();
        while(iterator.hasNext()) {
            createDaySheet(iterator.next(), headerStyle);
        }
        return timecardSheet;
    }

    private TaskRow getTaskRowOnOrBefore(int rowNum) {
        while (rowNum >= 1) {
            HSSFRow row = daySheet.getRow(rowNum);
            if (row != null) {
                try {
                    return new TaskRow(row);
                }
                catch (Throwable t) {
                    System.err.println("There is an exception in getTaskRowOnOrBefore(): " + t.getLocalizedMessage());
                    t.printStackTrace();
                }
            }
            rowNum--;
        }
        return null;
    }

    private CellStyle findStyle(String formatString) {
        int numCellStyles = wb.getNumCellStyles();
        for(int i=0; i<numCellStyles; i++) {
            CellStyle thestyle = wb.getCellStyleAt(i);
            short dataFormat = thestyle.getDataFormat();
            String format = wb.createDataFormat().getFormat(dataFormat);
            if (format.equals(formatString) && thestyle.getBorderBottom() == BorderStyle.NONE) {
                return thestyle;
            }
        }
        return null;
    }

    private void createDaySheet(String day, CellStyle headerStyle) {
        HSSFSheet daySheet = wb.createSheet(day);
        daySheet.setDefaultColumnWidth(12);
        //daySheet.setColumnWidth((short)4, (short)(80 * 256));
        HSSFRow headingRow = daySheet.createRow(0);
        createCell(headingRow, Messages.getString("Tracker.Task"), headerStyle); //$NON-NLS-1$
        createCell(headingRow, Messages.getString("Tracker.Start"), headerStyle); //$NON-NLS-1$
        createCell(headingRow, Messages.getString("Tracker.End"), headerStyle); //$NON-NLS-1$
        createCell(headingRow, Messages.getString("Tracker.Duration"), headerStyle); //$NON-NLS-1$
        //createCell(headingRow, "Details", headerStyle);
    }

    /**
     *
     * to get the current task as object for time 
     * @author Gerald Young
     */

    public TaskRow getCurrentTaskRow() {
	    return lastTaskRow;
    }

    /**
     *
     * @param seconds
     * @param beep This is an "out" parameter.  Pass in a an array of length 1, and this
     * method will set beep[0] = true if the operation was unsuccessful.
     * @return The previous task, or null if time cannot be adjusted
     */
    public TaskRow moveLastSwitch(int seconds, boolean[] beep) {

        beep[0] = true;

        if (lastTaskRow == null) return null;

        Date limit;
        if (secondToLastTaskRow != null) {
            limit = secondToLastTaskRow.getStart();
        }
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(scheduledExecutionTime);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            limit = calendar.getTime();
        }

        if (!lastTaskRow.getStart().after(limit) && seconds < 0) return null;

        if (lastTaskRow.getDurationMillis() <= 0 && seconds > 0) return null;

        Date newLastStart = new Date(lastTaskRow.getStart().getTime() + seconds * 1000);
        if (newLastStart.before(limit)) newLastStart = limit;


        if (newLastStart.after(lastTaskRow.getEnd())) newLastStart = lastTaskRow.getEnd();

        boolean linked = secondToLastTaskRow != null && lastTaskRow.getStart().equals(secondToLastTaskRow.getEnd());

        lastTaskRow.setStart(newLastStart);
        if (linked || secondToLastTaskRow != null && newLastStart.before(secondToLastTaskRow.getEnd())) {
            secondToLastTaskRow.setEnd(newLastStart);
        }
        beep[0] = false;
        return secondToLastTaskRow;
    }

    public void tick(String task) {

        Date now = new Date(scheduledExecutionTime);

        // see if we should make a new row

        if (lastTaskRow == null || !lastTaskRow.getTask().equals(task) || lastTaskRow.getEnd() == null ||
                TimeTracker.lastTimeWrittenToFile > lastTaskRow.getEnd().getTime() ) {

            int newRowNum = daySheet.getLastRowNum()+1;

            HSSFRow newRow = daySheet.createRow(newRowNum);

            createCell(newRow, task, null);
            createCell(newRow, new Date(TimeTracker.lastTimeWrittenToFile), hmsStyle);
            createCell(newRow, now, hmsStyle);
            createFormulaCell(newRow, "C" + (newRowNum+1) + "-" + "B" + (newRowNum+1), elapsedTimeStyle); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            //createCell(newRow, currentWindowTitle == null? "" : currentWindowTitle, wrapStyle); //details
        }
        else {
            // just change the end time of the last row
            lastTaskRow.setEnd(now);

            // update the details
            /*
          if (currentWindowTitle != null) {

              // see if we need to change it
              String details = lastTaskRow.getDetails();
              if (!details.equals(currentWindowTitle) && !details.startsWith(currentWindowTitle + "\n")) {
                  StringTokenizer st = new StringTokenizer(details, "\n");
                  StringBuffer newDetails = new StringBuffer(currentWindowTitle);
                  while (st.hasMoreTokens()) {
                      String title = st.nextToken();
                      if (!title.equals(currentWindowTitle)) {
                          newDetails.append("\n").append(title);
                      }
                  }
                  lastTaskRow.setDetails(newDetails.toString());
              }
          }
             */
        }

        TimeTracker.lastTimeWrittenToFile = scheduledExecutionTime;

        // make sure the task appears on the timecard sheet

        Iterator<?> rowIterator = timecardSheet.rowIterator();

        boolean found = false;
        while (rowIterator.hasNext()) {
            HSSFRow row = (HSSFRow)rowIterator.next();
            if (row.getRowNum() <= headingRow.getRowNum()) continue;

            HSSFCell taskCell = row.getCell(taskColumn);
            if (taskCell != null) {
                String taskName = taskCell.getRichStringCellValue().toString();
                if (taskName.equalsIgnoreCase(task)) {
                    found = true;
                }
            }
        }

        if (!found) {
            HSSFRow nextAvailableRow = timecardSheet.createRow(timecardSheet.getLastRowNum() + 1);

            HSSFCell taskCell = nextAvailableRow.createCell(taskColumn);
            taskCell.setCellValue(task);

            CellReference first = new CellReference(nextAvailableRow.getRowNum(), totalTimeColumn + 1);
            CellReference last = new CellReference(nextAvailableRow.getRowNum(), totalTimeColumn + 7);

            createFormulaCell(nextAvailableRow, "SUM(" + first.formatAsString() + ":" + last.formatAsString() + ")", elapsedTimeStyle); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            //CellReference taskCellReference = new CellReference(nextAvailableRow.getRowNum(), taskColumn);

            for(int i=1; i<=7; i++) {
                String day = wb.getSheetName(i);
                String formula = "SUMIF('"+day+"'!A1:A65000,\""+task+"\",'"+day+"'!D1:D65000)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                createFormulaCell(nextAvailableRow, formula, elapsedTimeStyle);
            }
        }

        currentTask = task;
    }

    /**
     * Saves the file and returns the number of seconds attributed to the current task.
     * @return
     * @throws IOException
     */
    public long save() throws IOException {
        long millis = 0;
        Iterator<?> rowIterator = daySheet.rowIterator();
        while (rowIterator.hasNext()) {
            HSSFRow row = (HSSFRow)rowIterator.next();
            if (row.getRowNum() <= 0) continue; // skip the heading row

            try {
                TaskRow taskRow = new TaskRow(row);
                if (taskRow.getTask().equals(currentTask)) {
                    millis += taskRow.getDurationMillis();
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
            }

        }
        FileOutputStream fos = new FileOutputStream(tmpFile);
        try {
            wb.write(fos);
        }
        finally {
            fos.close();
        }
        f.delete();
        tmpFile.renameTo(f);
        return Math.round((float)millis/1000);
    }

    /**
     * @param row
     * @param style
     * @return
     */
    private HSSFCell createCell(HSSFRow row, String text, CellStyle style) {
        int lastCellNum = row.getLastCellNum();
        if (lastCellNum == -1) {lastCellNum=0;}
        HSSFCell cell = row.createCell(lastCellNum);
        if (style != null) cell.setCellStyle(style);
        cell.setCellValue(text);
        return cell;
    }

    /**
     * @param row
     * @param style
     * @return
     */
    private HSSFCell createCell(HSSFRow row, Date date, CellStyle style) {
        int lastCellNum = row.getLastCellNum();
        if (lastCellNum == -1) {lastCellNum=0;}
        HSSFCell cell = row.createCell(lastCellNum);
        if (style != null) cell.setCellStyle(style);
        cell.setCellValue(date);
        return cell;
    }

    /**
     * @param row
     * @param style
     * @return
     */
    private HSSFCell createFormulaCell(HSSFRow row, String formula, CellStyle style) {
        int lastCellNum = row.getLastCellNum();
        if (lastCellNum == -1) {lastCellNum=0;}
        HSSFCell cell = row.createCell(lastCellNum);
        if (style != null) cell.setCellStyle(style);
        cell.setCellFormula(formula);
        return cell;
    }

}
