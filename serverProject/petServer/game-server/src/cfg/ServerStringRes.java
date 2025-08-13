/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import com.alibaba.fastjson.JSONObject;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.text.MessageFormat;
import java.util.*;

import java.util.Map.Entry;
import model.base.baseConfig;
import protocol.Common.LanguageEnum;
import util.LogUtil;

@annationInit(value = "ServerStringRes", methodname = "initConfig")
public class ServerStringRes extends baseConfig<ServerStringResObject> {


    private static ServerStringRes instance = null;

    public static ServerStringRes getInstance() {

        if (instance == null)
            instance = new ServerStringRes();
        return instance;

    }


    public static Map<Integer, ServerStringResObject> _ix_id = new HashMap<Integer, ServerStringResObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ServerStringRes) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ServerStringRes");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ServerStringResObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ServerStringResObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setContent_cn(MapHelper.getStr(e, "Content_CN"));

        config.setContent_tw(MapHelper.getStr(e, "Content_TW"));

        config.setContent_en(MapHelper.getStr(e, "Content_EN"));


        _ix_id.put(config.getId(), config);


    }

    /**
     * ==============================================================
     */

    /**
     * 得到指定字符串并填充参数
     * MessageFormat对单引号包裹的内容不做任何处理,要在带参数的字符串中使用单引号必须必须使用双单引号包裹  即使用‘’ 表示‘
     * @param id
     * @param language
     * @param params     填充
     * @return
     */
    public static String getContentByLanguage(int id, LanguageEnum language, Object... params) {
        ServerStringResObject config = getById(id);
        String content = "";
        if (config == null) {
            LogUtil.warn("language content is null,id=" + id + ",invoke functionName=" + Thread.currentThread().getStackTrace()[2].getMethodName());
            return "";
        }

        //默认使用中文处理
        switch (language) {
            case LE_TraditionalChinese:
                content = config.getContent_tw();
                break;
            case LE_English:
                content = config.getContent_en();
                break;
            default:
                content = config.getContent_cn();
                break;
        }
        return MessageFormat.format(content, params);
    }


    /**
     * 根据语言枚举,封装为json
     * @param id
     * @return
     */
    public static String getLanguageNumContent(int id) {
        ServerStringResObject byId = getById(id);
        if (byId == null) {
            return "";
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(String.valueOf(LanguageEnum.LE_SimpleChinese_VALUE), byId.getContent_cn());
        jsonObject.put(String.valueOf(LanguageEnum.LE_TraditionalChinese_VALUE), byId.getContent_tw());
        jsonObject.put(String.valueOf(LanguageEnum.LE_English_VALUE), byId.getContent_en());
        return jsonObject.toJSONString();
    }

    /**
     * 根据语言枚举,封装为json
     * @param id
     * @return
     */
    public static Map<Integer,String> getLanguageNumContentMap(int id, Object... params) {
        Map<Integer,String> result = new HashMap<>();
        ServerStringResObject byId = getById(id);
        if (byId == null) {
            return result;
        }

        result.put(LanguageEnum.LE_SimpleChinese_VALUE, MessageFormat.format(byId.getContent_cn(), params));
        result.put(LanguageEnum.LE_TraditionalChinese_VALUE, MessageFormat.format(byId.getContent_tw(), params));
        result.put(LanguageEnum.LE_English_VALUE, MessageFormat.format(byId.getContent_en(), params));
        return result;
    }

    public static Map<Integer, String> buildLanguageContentMap(int strId) {
        Map<Integer, String> result = new HashMap<>();
        ServerStringResObject byId = getById(strId);
        if (byId == null) {
            return result;
        }

        result.put(LanguageEnum.LE_SimpleChinese_VALUE, byId.getContent_cn());
        result.put(LanguageEnum.LE_TraditionalChinese_VALUE, byId.getContent_tw());
        result.put(LanguageEnum.LE_English_VALUE, byId.getContent_en());
        return result;
    }

    public static String buildLanguageNumContentJson(int strId) {
        JSONObject jsonObject = new JSONObject();
        for (Entry<Integer, String> entry : getLanguageNumContentMap(strId).entrySet()) {
            jsonObject.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return jsonObject.toJSONString();
    }


    public static List<String> getContentListByLanguage(int[] strIds, LanguageEnum languageEnum) {
        if (strIds.length < 1) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (int strId : strIds) {
            result.add(ServerStringRes.getContentByLanguage(strId, languageEnum));
        }
        return result;
    }
}
