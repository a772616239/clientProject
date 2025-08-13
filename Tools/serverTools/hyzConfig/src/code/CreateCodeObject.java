package code;

import bean.ConfigData;
import java.util.HashMap;
import java.util.Map;

import com.bowlong.lang.StrEx;
import com.bowlong.util.StrBuilder;

public class CreateCodeObject {

    public static String getObjectStr() {

        Map<String, String> proAndFun = getProStrAndFunctionStr();

        StrBuilder sb = StrEx.builder();
        sb.pn(getHeadStr());
        sb.pn(proAndFun.get("pro"));
        sb.pn(proAndFun.get("fun"));
        sb.pn("}");

        return sb.toString();

    }

    private static String getHeadStr() {
        StrBuilder sb = StrEx.builder();
        sb.pn("package cfg;");
        sb.pn("import model.base.baseConfigObject;");
        sb.pn("public class  ${1} implements baseConfigObject{", CodeTool.objectName);
        sb.pn("");
        return sb.toString();

    }


    private static Map<String, String> getProStrAndFunctionStr() {

        Map<String, String> ret = new HashMap<String, String>();

        StrBuilder sb_pro = StrEx.builder();
        StrBuilder sb_fun = StrEx.builder();

        for (ConfigData data : CodeTool.M_fieldPro.values()) {
            if (data.isSkip()) {
                continue;
            }
            String propertiesName = data.getFieldName();
            String propertiesType = data.getFieldType();
            sb_pro.pn("");
            sb_pro.pn(setPropertiesStr(propertiesName, propertiesType));
            sb_fun.pn("");
            sb_fun.pn(setFunctionStr(propertiesName, propertiesType));

        }
        sb_pro.pn("");
        sb_pro.pn("");
        sb_fun.pn("");
        sb_fun.pn("");
        ret.put("pro", sb_pro.toString());
        ret.put("fun", sb_fun.toString());

        return ret;

    }


    private static String setPropertiesStr(String propertiesName,
                                           String propertiesType) {

        String name = propertiesName.toLowerCase();
        String type = getJavaTypeStr(propertiesType);

        String str = "private " + type + " " + name + ";";
        return str;

    }

    private static String setFunctionStr(String propertiesName,
                                         String propertiesType) {

        String name = (propertiesName.toLowerCase());
        String name1 = Util.toUpperCaseFirstOne(name);
        String type = getJavaTypeStr(propertiesType);

        StrBuilder sb = StrEx.builder();
        sb.pn("public void set${1}(${2} ${3}) {", name1, type, name);
        sb.pn("");
        sb.pn("this.${1} = ${1};", name);
        sb.pn("");
        sb.pn("}");

        sb.pn("");

        sb.pn("public ${2} get${1}() {", name1, type);
        sb.pn("");
        sb.pn("return this.${1};", name);
        sb.pn("");
        sb.pn("}");
        return sb.toString();

    }

    private static String getJavaTypeStr(String propertiesType) {

        String javaTypeStr = TypeConfig.getPreFix(propertiesType);
        if (javaTypeStr == null || "".equals(javaTypeStr)) {
            System.out
                    .println("createObject getJavaTypeStr is error>>>>>>>>propertiesType >>>>"
                            + propertiesType);
            System.exit(0);
            return "";
        }

        return javaTypeStr;

    }

}
