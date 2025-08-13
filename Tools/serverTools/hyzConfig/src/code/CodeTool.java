package code;

import Excel.ExcelSheetL;
import JsonTool.AliJson.AliJsonTool;
import bean.ConfigData;
import bean.RowData;
import com.alibaba.fastjson.JSON;
import com.bowlong.lang.StrEx;
import com.bowlong.util.StrBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import main.CreateAllAndMoveJson;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * @author ---
 * @date
 */
public class CodeTool {

    public static Workbook wb = null;
    public static Sheet sheet = null;
    public static Row row = null;
    /**
     * 获取最大列数
     **/
    public static int maxColumn = 0;
    /**
     * 获取行数
     **/
    public static int totalRowNum = 0;
    /**
     * 所有的属性
     **/
    public static Map<Integer, String> M_fieldName = new HashMap<Integer, String>();
    /**
     * 所有的类型
     **/
    public static Map<Integer, String> M_fieldType = new HashMap<>();
    /**
     * 是否是主键
     **/
    public static Map<Integer, String> M_fieldKey = new HashMap<>();
    /**
     * 是否需要略过服务器
     **/
    public static Map<Integer, String> M_fieldSkip = new HashMap<>();
    /**
     * 关键字段（作为查询的key值）
     **/
    public static List<ConfigData> keyField = new ArrayList<>();

    /**
     * 获取属性名，类型等字段的集合, 数据表前缀的集合。保存表头的类型csk,和字段名等
     **/
    public static Map<Integer, ConfigData> M_fieldPro = new HashMap<>();

    /**
     * 每行的数据,依次顺序添加，所有的数据
     **/
    public static List<RowData> datas = new ArrayList<>();

    /**
     * 生成 cache ,get ,putToCache 字符串
     **/
    public static Map<Integer, String> M_keyStr = new HashMap<>();

    /**
     * 对象名
     */
    public static String objectName = "";
    /**
     * 类名
     */
    public static String className = "";

    public static void createAllCode(String filePath, String filename, boolean moveJson, boolean moveJava) {
        System.out.println("============start create :" + filename + "============");
        ExcelSheetL.setExcelSheetData(filePath, filename);
        if (!CodeTool.needCreate()) {
            CreateAllAndMoveJson.addSkipFile(filename);
            return;
        }

        String outJsonStr = CreateCodeJson.getOutJsonStr();
        String outJavaStr = CreateCodeJava.getOutJavaStr();
        String outObjectStr = CreateCodeObject.getObjectStr();
        ExportCodeFile.export(outJsonStr, outObjectStr, outJavaStr, moveJson, moveJava);
        System.out.println("============create finished:" + filename + "============");
    }

    /**
     * 当前数据表是否需要生成
     * 所有字段均不含服务器字段则跳过
     *
     * @return
     */
    private static boolean needCreate() {
        boolean containPrimaryKey = false;
        int skipFieldSize = 0;
        for (ConfigData value : M_fieldPro.values()) {
            if (Objects.equals(value.getFieldSkip(), TypeConfig.FIELD_SKIP)) {
                skipFieldSize++;
            } else if (Objects.equals(value.getFieldKey(), TypeConfig.FIELD_PRIMARY_KEY)) {
                containPrimaryKey = true;
            }
        }

        if (!containPrimaryKey || skipFieldSize == M_fieldPro.size()) {
            System.out.println("filename:" + className + " is not contain `k` filed or not have `s` filed, skip generate this file");
            return false;
        }

        //检查主键是否重复
        Map<String, Set<String>> keyNumSet = new HashMap<>();
        for (RowData data : datas) {
            List<ConfigData> dataList = data.getKeyDataList();
            if (dataList == null) {
                continue;
            }
            for (ConfigData configData : dataList) {
                if (Objects.equals(configData.getFieldValue(), "0")) {
                    continue;
                }
                Set<String> value = keyNumSet.computeIfAbsent(configData.getFieldName(), e -> new HashSet<>());
                if (value.contains(configData.getFieldValue())) {
                    System.out.println("filename:" + className + " is have repeated key num, filed name:"
                            + configData.getFieldName() + ", repeated value:" + configData.getFieldValue());
                    System.exit(0);
                    return false;
                }
                value.add(configData.getFieldValue());
            }
        }

        return true;
    }

    public static String getValue(ConfigData data, int setType) {
        String type = data.getFieldType();
        String preFix = TypeConfig.getPreFix(type);
        String value = data.getFieldValue();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        if (preFix == null || preFix.isEmpty()) {
            System.out.println("codeTool is error>>>>>>>>preFix >>>>" + preFix);
            return value;
        }

        if (setType == TypeConfig.SET_JAVA_STR) {
            if (StringUtils.equals(preFix.toLowerCase(), type)) {
                return value;
            }
        }
        return "new" + " " + preFix + value;
    }


    public static void main(String[] args) {
        StrBuilder sb = StrEx.builder();

        String string = "[{'name':'toke','shuxue':'78','yuwen':'80'},"
                + "{'name':'seri','shuxue':'20','yuwen':'90'},"
                + "{'name':'heer','shuxue':'99','yuwen':'56'}]";

        String name = "\"ID\"";
        String name1 = "\"NAME\"";
        sb.pn("{");
        sb.pn("Map e = new HashMap();");
        sb.pn("e.put(ID,1)");
        sb.pn("e.put(NAME,12)");
        sb.pn("}");

        System.out.println(">>>>>>>>>" + JSON.parse(sb.toString()));
        try {
            System.out.println(AliJsonTool.StrToJson("hgjbkj"));
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /******************** setConfigMap **************************/

}
