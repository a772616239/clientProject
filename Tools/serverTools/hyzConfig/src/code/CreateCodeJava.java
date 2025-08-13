package code;

import bean.ConfigData;
import helper.StringUtils;

import java.util.HashMap;
import java.util.Map;

import com.bowlong.lang.StrEx;
import com.bowlong.util.StrBuilder;

public class CreateCodeJava {

    public static String getOutJavaStr() {

        String headStr = getHeadStr();
        String endStr = getEndString();

        StringBuffer sb = new StringBuffer();
        sb.append(headStr);
        sb.append(endStr);
        return sb.toString();

    }

    /************************ getHeadStr ***************************************************/
    private static String getHeadStr() {

        StrBuilder sb = StrEx.builder();
        sb.pn("/*CREATED BY TOOL*/");
        sb.pn("");
        sb.pn("package cfg;");
        sb.pn("import java.util.HashMap;");
        sb.pn("import java.util.Map;");
        sb.pn("import java.util.List;");
        sb.pn("import java.util.concurrent.ConcurrentHashMap;");
        sb.pn("import model.base.baseConfig;");
        sb.pn("import datatool.MapHelper;");
        sb.pn("import annotation.annationInit;");
        sb.pn("import common.load.ServerConfig;");
        sb.pn("import JsonTool.readJsonFile;");
        sb.pn("");
        String annationstr = getAnnotationStr("initConfig");
        sb.pn(annationstr, CodeTool.className);
        sb.pn("public class ${1} extends baseConfig<${2}>{", CodeTool.className, CodeTool.objectName);
        sb.pn("");

        sb.pn("");
        sb.pn(getInstanceMethodStr(CodeTool.className));
        sb.pn("");

        String cacheStr = CodeTool.M_keyStr.get(TypeConfig.KEY_CACHE_STR);
        sb.pn(cacheStr);

        sb.pn("public void initConfig(baseConfig o){");
        sb.pn("if (instance == null)");
        sb.pn("instance = (${1}) o;", CodeTool.className);
        sb.pn("initConfig();");
        sb.pn("}");
        sb.pn("");
        sb.pn("");
        sb.pn("private void initConfig() {");

        String fileName = "\"${1}\"";
        String jsonPath = "ServerConfig.getInstance().getJsonPath()";
        sb.pn("List<Map> ret=readJsonFile.getMaps(" + jsonPath + "," + fileName + ");", CodeTool.className);
        sb.pn("");
        sb.pn("for(Map e:ret)");
        sb.pn("{");
        sb.pn("put(e);");
        sb.pn("}");

        sb.pn("");
        sb.pn("}");
        return sb.toString();

    }

    private static String getAnnotationStr(String AnnotationMethodName) {
        String value = "\"${1}\"";
        String methodName = "\"" + AnnotationMethodName + "\"";
        String annotationStr = "@annationInit(value =" + value + ", methodname = " + methodName + ")";
        return annotationStr;

    }

    private static String getInstanceMethodStr(String className) {
        StrBuilder sb = StrEx.builder();
        sb.pn("private static ${1} instance = null;", className);

        sb.pn("");
        sb.pn("public static ${1} getInstance() {",
                CodeTool.className);
        sb.pn("");

        sb.pn("if (instance == null)");
        sb.pn("instance = new ${1}();", className);

        sb.pn("return instance;");
        sb.pn("");
        sb.pn("}");

        return sb.toString();

    }

    /**************************** getHeadStrEND **********************************/

    private static String getEndString() {

        StrBuilder sb = StrEx.builder();

        sb.pn("");

        String getStr = CodeTool.M_keyStr.get(TypeConfig.KEY_GET_STR);
        sb.pn(getStr);

        sb.pn("public  void putToMem(Map e, ${1} config){", CodeTool.objectName);
        String setObjectStr = getConfigObject();
        sb.pn("");
        sb.pn(setObjectStr);

        String putToCacheStr = CodeTool.M_keyStr
                .get(TypeConfig.KEY_PUT_TO_CACHE_STR);
        sb.pn(putToCacheStr);

        sb.pn("");
        sb.pn("}");
        sb.pn("}");
        return sb.toString();

    }

    private static String getConfigObject() {
        StrBuilder sb = StrEx.builder();

        for (ConfigData data : CodeTool.M_fieldPro.values()) {
            if (data.isSkip()) {
                continue;
            }
            String name = data.getFieldName();
            String type = data.getFieldType();
            String proName = getProName(name);
            String configValue = getConfigValue(name, type);
            sb.pn("config.set${1}${2}", proName, configValue);
        }

        return sb.toString();

    }

    private static String getProName(String name) {

        return Util.toUpperCaseFirstOne(name.toLowerCase());

    }

    private static String getConfigValue(String name, String type) {
        int proType = TypeConfig.getJavaType(type);
        StrBuilder sb = StrEx.builder();

        String proName = "\"${1}\"";
        switch (proType) {
            case 1:
                sb.pn("(MapHelper.getInt(e, " + proName + "));", name);
                break;
            case 2:
                sb.pn("(MapHelper.getInts(e, " + proName + "));", name);
                break;
            case 3:
                sb.pn("(MapHelper.getIntArray(e, " + proName + "));", name);
                break;

            case 4:
                sb.pn("(MapHelper.getLong(e, " + proName + "));", name);
                break;
            case 5:
                sb.pn("(MapHelper.getLongs(e, " + proName + "));", name);
                break;
            case 6:
                sb.pn("(MapHelper.getLongArray(e, " + proName + "));", name);
                break;

            case 7:
                sb.pn("(MapHelper.getStr(e, " + proName + "));", name);
                break;
            case 8:
                sb.pn("(MapHelper.getStrs(e, " + proName + "));", name);
                break;
            case 9:
                sb.pn("(MapHelper.getStrss(e, " + proName + "));", name);
                break;
            case 10:
                sb.pn("(MapHelper.getBoolean(e, " + proName + "));", name);
                break;
            case 11:
                sb.pn("(MapHelper.getFloat(e, " + proName + "));", name);
                break;
            default:
                break;

        }
        return sb.toString();

    }

    public static Map<Integer, String> getKeyStr() {
        Map<Integer, String> m = new HashMap<>();

        StringBuffer sb_Map = new StringBuffer();
        StringBuffer sb_Get = new StringBuffer();
        StringBuffer sb_PutToCache = new StringBuffer();

        for (ConfigData keyData : CodeTool.keyField) {
            if (isEmptyNameOrTypeOrKey(keyData)) {
                System.out.println("getKeyStr is error>>>>>isEmptyNameOrTypeOrKey");
                System.exit(0);
                return m;
            }

            String keyMapStr = getKeyMapStr(keyData);
            sb_Map.append(keyMapStr);

            String keyGetStr = getKeyGetStr(keyData);
            sb_Get.append(keyGetStr);

            String keyPutToCacheStr = getKeyPutToCacheStr(keyData);
            sb_PutToCache.append(keyPutToCacheStr);
        }
        m.put(TypeConfig.KEY_CACHE_STR, sb_Map.toString());
        m.put(TypeConfig.KEY_GET_STR, sb_Get.toString());
        m.put(TypeConfig.KEY_PUT_TO_CACHE_STR, sb_PutToCache.toString());
        return m;

    }

    private static String getKeyMapStr(ConfigData keyData) {
        StrBuilder sb = StrEx.builder();
        String s = "_ix_" + keyData.getFieldName().toLowerCase();
        String type = "Integer";
        if ("string".equals(keyData.getFieldType())) {
            type = "String";
        }
        sb.pn("public static Map<${1}, ${2}> ${3} = new HashMap<>();", type, CodeTool.objectName, s);
        sb.pn("");
        return sb.toString();

    }

    private static String getKeyGetStr(ConfigData keyData) {
        StrBuilder sb = StrEx.builder();

        String name = keyData.getFieldName().toLowerCase();
        String functionName = "getBy" + Util.toUpperCaseFirstOne(name);
        String type = TypeConfig.getPreFix(keyData.getFieldType());

        String cache = "_ix_" + name;

        sb.pn("public static ${1} ${2}(${3} ${4}){", CodeTool.objectName, functionName, type, name);
        sb.pn("");
        sb.pn("return ${1}.get(${2});", cache, name);
        sb.pn("");
        sb.pn("}");
        sb.pn("");
        sb.pn("");
        return sb.toString();

    }

    private static String getKeyPutToCacheStr(ConfigData keyData) {

        StrBuilder sb = StrEx.builder();

        String name = keyData.getFieldName().toLowerCase();
        String functionName = Util.toUpperCaseFirstOne(name);
        String cache = "_ix_" + name;
        sb.pn("${1}.put(config.get${2}(),config);", cache,
                functionName);
        sb.pn("");
        return sb.toString();

    }

    private static boolean isEmptyNameOrTypeOrKey(ConfigData keyData) {

        if (StringUtils.isEmpty(keyData.getFieldName())) {
            return true;
        }
        if (StringUtils.isEmpty(keyData.getFieldType())) {
            return true;
        }
        if (StringUtils.isEmpty(keyData.getFieldKey())) {
            return true;
        }
        return false;
    }
}
