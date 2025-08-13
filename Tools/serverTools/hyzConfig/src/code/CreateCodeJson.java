package code;


import bean.ConfigData;
import bean.RowData;
import com.bowlong.lang.StrEx;
import com.bowlong.util.StrBuilder;


/**
 * @author Administrator
 * @date
 */
public class CreateCodeJson {
    public static String getOutJsonStr() {
        return getBodyJsonStr();
    }

    private static String getBodyJsonStr() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append("\r\n");
        int i = 1;

        for (RowData v : CodeTool.datas) {
            sb.append("{");
            int ii = 1;
            for (ConfigData data : v.getRowData()) {
                sb.append(setJson(data));

                if (ii < v.getRowData().size()) {
                    sb.append(",");
                }
                ii++;
            }
            sb.append("}");

            if (i < CodeTool.datas.size()) {
                sb.append(",");
                sb.append("\r\n");
            }
            i++;
        }

        sb.append("\r\n");
        sb.append("]");
        return sb.toString();
    }

    private static String setJson(ConfigData data) {
        StrBuilder sb = StrEx.builder();
        String name = "\"${1}\"";
        String value = CodeTool.getValue(data, TypeConfig.SET_JSON_STR);
        sb.pn(name + ":" + "\"${2}\"", data.getFieldName(), value);
        return sb.toString();
    }
}
