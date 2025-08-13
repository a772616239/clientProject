package code;


import Excel.ExcelUtil;
import bean.ConfigData;
import bean.RowData;
import helper.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.poi.ss.usermodel.Row;


public class ConfigDataL {

    public static void setConfigDataNotValue() {
        int size1 = CodeTool.M_fieldName.size();
        int size2 = CodeTool.M_fieldType.size();
        int size3 = CodeTool.M_fieldKey.size();

        if (size1 != size2 || size1 != size3) {
            return;
        }

        for (Entry<Integer, String> entry : CodeTool.M_fieldName.entrySet()) {
            int index = entry.getKey();
            String name = CodeTool.M_fieldName.get(index);
            String type = CodeTool.M_fieldType.get(index);
            String key = CodeTool.M_fieldKey.get(index);
            String skip = CodeTool.M_fieldSkip.get(index);

            ConfigData data = new ConfigData();
            data.setFieldName(name);
            data.setFieldType(type);
            data.setFieldKey(key);
            data.setFieldSkip(skip);
            setKeyField(data);
            setFieldConfigDataNotValue(index, data);
        }
    }

    private static void setKeyField(ConfigData data) {
        try {
            if (Integer.parseInt(data.getFieldKey()) == TypeConfig.KEY_FIELD) {
                CodeTool.keyField.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void setFieldConfigDataNotValue(int index, ConfigData data) {
        try {
            CodeTool.M_fieldPro.put(index, data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<RowData> getData() {
        List<RowData> ret = new ArrayList<>();
        HashSet<String> firstKeySet = new HashSet<>();
        for (int i = TypeConfig.FIELD_VALUE_START_ROW_NUM; i <= CodeTool.totalRowNum; i++) {
            Row row = CodeTool.sheet.getRow(i);
            if (row == null) {
                break;
            }
            RowData rowData = getConfigData(row);
            if (rowData != null) {
                String firstKey = rowData.getFirstKey();
                if (firstKey != null) {
                    if (firstKeySet.contains(firstKey)) {
                        System.out.println("code.ConfigDataL.getData, Key Duplicate fileName:" + CodeTool.className + ", Row Num:" + row.getRowNum());
                        //单元格前缀配置为空,直接退出
                        System.exit(0);
                    } else {
                        firstKeySet.add(firstKey);
                    }
                }
                ret.add(rowData);
            }
        }
        return ret;
    }

    private static RowData getConfigData(Row row) {
        RowData result = new RowData();
        for (int index = 0; index < CodeTool.maxColumn; index++) {
            ConfigData dataWithOutValue = CodeTool.M_fieldPro.get(index);
            if (dataWithOutValue == null) {
                System.out.println("code.ConfigDataL.getConfigData, cur index:" + index + ", row num:" + row.getRowNum() + ", filedPro is null");
                //单元格前缀配置为空,直接退出
                System.exit(0);
                return null;
            }

            if (dataWithOutValue.isSkip()) {
                continue;
            }

            ConfigData data = dataWithOutValue.clone();
            String cellData = ExcelUtil.getCellFormatValue(row.getCell(index));
            //为空使用默认代替
            if (StringUtils.isEmpty(cellData)) {
                cellData = getEmptyCellDataDefaultValue(dataWithOutValue.getFieldType());
            }
            data.setFieldValue(cellData);

            if (needSkipRow(data)) {
                return null;
            }

            result.addRowData(data);
        }
        return result;
    }

    //是主键字段且值""跳过
    private static boolean needSkipRow(ConfigData data) {
        if (data == null) {
            return true;
        }

        boolean invalidValue = Objects.equals(data.getFieldValue(), "");

        if (Objects.equals(data.getFieldKey(), TypeConfig.FIELD_PRIMARY_KEY) && invalidValue) {
            return true;
        }
        return false;
    }


    private final static Map<String, String> FILED_TYPE_DEFAULT_VALUE = new HashMap<>();

    static {
        FILED_TYPE_DEFAULT_VALUE.put("int".toLowerCase(), "0");
        FILED_TYPE_DEFAULT_VALUE.put("long".toLowerCase(), "0L");
        FILED_TYPE_DEFAULT_VALUE.put("string".toLowerCase(), "");

        FILED_TYPE_DEFAULT_VALUE.put("intList".toLowerCase(), "{}");
        FILED_TYPE_DEFAULT_VALUE.put("longList".toLowerCase(), "{}");
        FILED_TYPE_DEFAULT_VALUE.put("strList".toLowerCase(), "{}");

        FILED_TYPE_DEFAULT_VALUE.put("intList2".toLowerCase(), "{{}}");
        FILED_TYPE_DEFAULT_VALUE.put("longList2".toLowerCase(), "{{}}");
        FILED_TYPE_DEFAULT_VALUE.put("strList2".toLowerCase(), "{{}}");
        FILED_TYPE_DEFAULT_VALUE.put("boolean".toLowerCase(), Boolean.FALSE.toString());
    }

    /**
     * 处理字段未填值的情况
     *
     * @param fieldType
     */
    private static String getEmptyCellDataDefaultValue(String fieldType) {
        if (StringUtils.isEmpty(fieldType)) {
            return "";
        }
        fieldType = fieldType.toLowerCase();
        if (FILED_TYPE_DEFAULT_VALUE.containsKey(fieldType)) {
            return FILED_TYPE_DEFAULT_VALUE.get(fieldType);
        } else {
            System.out.println("code.ConfigDataL.getEmptyCellDataDefaultValue, fieldType have not mapping default value，type" + fieldType);
            return "";
        }
    }
}
