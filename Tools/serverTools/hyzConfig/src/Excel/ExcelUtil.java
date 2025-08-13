package Excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import code.Util;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

    /**
     * ��ȡExcel
     *
     * @param filePath
     * @return
     */
    public static Workbook readExcel(String filePath) {
        Workbook wb = null;
        if (filePath == null) {
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                return wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                return wb = new XSSFWorkbook(is);
            } else if (".xlsm".equals(extString)) {
                return wb = new XSSFWorkbook(is);
            } else {
                return wb = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wb;
    }

    public static void main(String[] args) {
        double a = 1.0;

    }

    public static String getCellFormatValue(Cell cell) {

        try {
            String cellValue = "";
            if (cell == null) {
                return cellValue;
            }

            //判断ceil类型
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    cellValue = String.valueOf(Util.getShWr(cell.getNumericCellValue()));

                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cellValue = String.valueOf(cell.getDateCellValue());
                    } else {
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                case Cell.CELL_TYPE_BOOLEAN: {
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                }
                case Cell.CELL_TYPE_BLANK: {
                    cellValue = "0";
                    break;
                }
                default:
                    break;

            }

//		System.out.println("cell.getCellType()>>>>>"+cell.getCellType());
//		System.out.println("cellValue>>>>>"+cellValue);
//		System.out.println("++++++++++++++++++++++++++++");
            return cellValue;
        } catch (Exception e) {
            e.printStackTrace();
            return "";

        }
    }
}
