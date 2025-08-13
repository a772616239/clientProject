package Excel;

import helper.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;

import code.CodeTool;
import code.ConfigDataL;
import code.CreateCodeJava;
import code.TypeConfig;

/**
 * @author Administrator
 * @date
 */
public class ExcelSheetL {

    public static void setExcelSheetData(String filePath, String filename) {

        clearExcelSheetData();

        CodeTool.wb = ExcelUtil.readExcel(filePath);
        if (CodeTool.wb == null) {
            System.out.println("Excel.ExcelSheetL.setExcelSheetData,createConfigAndObject is error>>>>>wb == null");
            System.exit(0);
            return;
        }
        //获取第一张表
        CodeTool.sheet = CodeTool.wb.getSheetAt(0);
        if (CodeTool.sheet == null) {
            System.out.println("Excel.ExcelSheetL.setExcelSheetData,createConfigAndObject is error>>>>>the first sheet is null");
            System.exit(0);
            return;
        }

        //获取表的物理行数, 行数从0开始
        CodeTool.totalRowNum = CodeTool.sheet.getLastRowNum();
        CodeTool.row = CodeTool.sheet.getRow(0);
        CodeTool.maxColumn = CodeTool.row.getPhysicalNumberOfCells();

//        System.out.println("Excel.ExcelSheetL.setExcelSheetData, total row:" + CodeTool.totalRowNum
//                + ", total column:" + CodeTool.maxColumn);

        CodeTool.M_fieldName = getAllCellDataByRowNum(TypeConfig.FIELD_NAME_ROW_NUM);

//        System.out.println("codeTool.M_fieldName>>>>>>>>>." + CodeTool.M_fieldName);

        CodeTool.M_fieldType = getAllCellDataByRowNum(TypeConfig.FIELD_TYPE_ROW_NUM);

//        System.out.println("codeTool.M_fieldType>>>>>>>>>." + CodeTool.M_fieldType);

        //设置csk字段
        setCSK();

        CodeTool.objectName = filename + "Object";

        CodeTool.className = filename;

        ConfigDataL.setConfigDataNotValue();

        CodeTool.datas = ConfigDataL.getData();

        CodeTool.M_keyStr = CreateCodeJava.getKeyStr();
    }

    /****************** setCSK *************************/

    private static void setCSK() {
        Map<Integer, String> m = getAllCellDataByRowNum(TypeConfig.FIELD_CSK_ROW_NUM);

        Map<Integer, String> key = new HashMap<>();

        Map<Integer, String> skip = new HashMap<>();

        for (Entry<Integer, String> entry : m.entrySet()) {
            int index = entry.getKey();
            String value = entry.getValue().toLowerCase();

            String isKey = StringUtils.contains(value, TypeConfig.FILED_PRIMARY_KEY_MARK)
                    ? TypeConfig.FIELD_PRIMARY_KEY : TypeConfig.FIELD_NOT_PRIMARY_KEY;

            key.put(index, isKey);

            String isSkip = StringUtils.contains(value, TypeConfig.FIELD_SERVER_MARK)
                    ? TypeConfig.FIELD_NOT_SKIP : TypeConfig.FIELD_SKIP;

            skip.put(index, isSkip);
        }

        CodeTool.M_fieldKey = key;

        CodeTool.M_fieldSkip = skip;

//        System.out.println("codeTool.M_fieldKey>>>>>>>>>." + CodeTool.M_fieldKey);
//
//        System.out.println("codeTool.M_fieldSkip>>>>>>>>>." + CodeTool.M_fieldSkip);
    }


    private static void clearExcelSheetData() {
        CodeTool.wb = null;
        CodeTool.sheet = null;
        CodeTool.row = null;
        CodeTool.maxColumn = 0;
        CodeTool.totalRowNum = 0;
        CodeTool.M_fieldName.clear();
        CodeTool.M_fieldType.clear();
        CodeTool.M_fieldKey.clear();
        CodeTool.M_fieldPro.clear();
        CodeTool.keyField.clear();
        CodeTool.datas.clear();
        CodeTool.M_keyStr.clear();
        CodeTool.objectName = "";
        CodeTool.className = "";
    }

    private static Map<Integer, String> getAllCellDataByRowNum(int rowNum) {
        Row row = CodeTool.sheet.getRow(rowNum);
        return getRowCellByColumn(row);
    }

    private static Map<Integer, String> getRowCellByColumn(Row row) {
        Map<Integer, String> result = new HashMap<>();
        if (row == null) {
            for (int j = 0; j < CodeTool.maxColumn; j++) {
                result.put(j, "0");
            }
            return result;
        }

        for (int j = 0; j < CodeTool.maxColumn; j++) {
            String cellData = ExcelUtil.getCellFormatValue(row.getCell(j));
            if (StringUtils.isBlank(cellData)) {
                cellData = "0";
            }
            result.put(j, cellData);
        }
        return result;
    }
}
