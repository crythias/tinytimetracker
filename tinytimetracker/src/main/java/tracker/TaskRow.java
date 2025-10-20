package tracker;

/**
 * Represents a row in the task spreadsheet.
 */

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

public class TaskRow {

    private HSSFCell taskCell, startCell, endCell /*, detailsCell */;
    private String task;
    private Date start, end;
    //private String details;
    private final HSSFRow row;

    public TaskRow(HSSFRow row) {
        this.row = row;

        taskCell = row.getCell(0);
        task = taskCell.getRichStringCellValue().getString();

        startCell = row.getCell(1);
        start = startCell.getDateCellValue();

        endCell = row.getCell(2);
        end = endCell.getDateCellValue();
    }

    public int getRowNum() {
        return row.getRowNum();
    }

    public long getDurationMillis() {
        return end.getTime() - start.getTime();
    }

    public void setEnd(Date end) {
        this.end = end;
        endCell.setCellValue(end);
    }

    public void setStart(Date start) {
        this.start = start;
        startCell.setCellValue(start);
    }

    /**
     * @return Returns the task.
     */
    public String getTask() {
        return task;
    }
    /**
     * @return Returns the start.
     */
    public Date getStart() {
        return start;
    }

    /**
     * @return Returns the end.
     */
    public Date getEnd() {
        return end;
    }

    public String toString() {
        return task + " " + start + " - " + end; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
