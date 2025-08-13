package petrobot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelUtil {
    /**
     * EXCEL文件导入导出
     */
    public static Map<Integer, Integer> showExcelFileDialog(File file) {
        FileInputStream is = null;
        Map<Integer, Integer> map = new HashMap<>();
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } //文件流
        try {
            Workbook book = WorkbookFactory.create(is);
            Sheet sheet = book.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows(); //获取总行数
            Row row = sheet.getRow(0);
            if (null == row) {
                return map;
            }
            int cellCount = row.getPhysicalNumberOfCells(); //获取总列数
            String keyStr = "index";
            String combatRatioStr = "method";
            int key = 0;
            int combatRatio = 3;
            for (int j = 0; j < cellCount; j++) {
                Cell cell = row.getCell(j);
                if (keyStr.equals(cell.getStringCellValue())) {
                    key = j;
                }
                if (combatRatioStr.equals(cell.getStringCellValue())) {
                    combatRatio = j;
                }
            }
            for (int i = 1; i < rowCount; i++) {
                Row row1 = sheet.getRow(i);
                Cell cellKey = row1.getCell(key);
                Cell cellRate = row1.getCell(combatRatio);
                if (null != cellKey && null != cellRate) {
                    int key1 = (int) cellKey.getNumericCellValue();
                    int rate1 = (int) cellRate.getNumericCellValue();
                    map.put(key1, rate1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
