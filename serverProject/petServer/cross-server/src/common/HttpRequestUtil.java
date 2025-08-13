package common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.bowlong.third.FastJSON;
import common.entity.HttpLoginResponse;
import common.entity.HttpRankingResponse;
import common.entity.RankingQueryRequest;
import common.entity.RankingServerMsg;
import common.entity.RankingUpdateRequest;
import common.load.ServerConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import util.LogUtil;

/**
 * Http请求工具
 *
 * @author xiao_FL
 * @date 2019/7/3
 */
public class HttpRequestUtil {
    public static class PlatFormRetCode {
        public static final int SUCCESS = 0;
        public static final int ActiveCode_LoseEfficacy = 108;      //激活码已失效
        public static final int ActiveCode_Used = 109;              //激活码已被使用
        public static final int ActiveCode_Error = 110;             //激活码错误
        public static final int ActiveCode_UpperLimit = 114;        //激活码使用次数上限
        public static final int ActiveCode_UseSameTypeCode = 802;   //使用过同类型的激活码
    }


    public static String platformMD5(String key) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        try {
            byte[] btInput = key.getBytes("UTF-8");
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 替换更新排行榜
     *
     * @param updateData 排行榜数据
     * @return 操作结果/异常返null
     */
    public static boolean updateRanking(RankingUpdateRequest updateData) {
        if (updateData == null || updateData.getItems() == null || updateData.getItems().size() == 0) {
            LogUtil.info("common.HttpRequestUtil.updateRanking,params is null");
            return false;
        }
        String url = ServerConfig.getInstance().getPlatformRankUpdate();
        Common common = Common.getInstance();
        common.setData(updateData);
        common.setSign(getSignSalt(FastJSON.toJSONString(common.getData()), common.getVersion(), common.getClientId(), common.getSalt()));
        String result = doPost(url, FastJSON.toJSONString(common));
        if (result == null) {
            LogUtil.error("update ranking " + updateData.getRank() + " failed");
            return false;
        }
        HttpLoginResponse response = FastJSON.parseObject(result, HttpLoginResponse.class);
        boolean ret = response.getRetCode() == PlatFormRetCode.SUCCESS;
        if (!ret) {
            LogUtil.error("update ranking failed retCode={},retMsg={}", response.getRetCode(), response.getRetDes());
        }
        return ret;
    }

    /**
     * 查询排行榜
     *
     * @param queryRequest 查询参数
     * @return 查询结果
     */
    public static HttpRankingResponse queryRanking(RankingQueryRequest queryRequest) {
        if (queryRequest == null) {
            LogUtil.error("common.HttpRequestUtil.queryRanking, params is null");
            return null;
        }
        String url = ServerConfig.getInstance().getPlatformRankPage();
        Common common = Common.getInstance();
        common.setData(queryRequest);
        common.setSign(getSignSalt(FastJSON.toJSONString(common.getData()), common.getVersion(), common.getClientId(), common.getSalt()));
        String result = doPost(url, FastJSON.toJSONString(common));
        if (result == null) {
            LogUtil.error("query ranking " + queryRequest.getRank() + " failed");
            return null;
        }
        return FastJSON.parseObject(result, HttpRankingResponse.class);
    }


    /**
     * 清理排行榜
     *
     * @param rank        更新排行榜的名称,无该字段请求将无效
     * @param serverIndex 该字段用于区分数据不互通区服,无该字段请求将无效
     * @param keys        清除指定的keys,为空，删除整个排行榜； 非空，删除该排行榜里面的该条记录
     * @return 操作结果/异常返null
     */
    public static boolean clearRanking(String rank, int serverIndex, List<String> keys) {
        String url = ServerConfig.getInstance().getPlatformRankClear();
        Common common = Common.getInstance();
        RankingServerMsg queryRequest = new RankingServerMsg();
        queryRequest.setRank(rank);
        queryRequest.setServerIndex(serverIndex);
        if (CollectionUtils.isNotEmpty(keys)) {
            queryRequest.addAllKeyList(keys);
        }
        common.setData(queryRequest);
        common.setSign(getSignSalt(FastJSON.toJSONString(common.getData()), common.getVersion(), common.getClientId(), common.getSalt()));
        String result = doPost(url, FastJSON.toJSONString(common));
        if (result == null) {
            LogUtil.error("clear Ranking failed, ranking name :" + rank);
            return false;
        }
        HttpLoginResponse response = FastJSON.parseObject(result, HttpLoginResponse.class);
        boolean ret = response.getRetCode() == PlatFormRetCode.SUCCESS;
        if (!ret) {
            LogUtil.error("clear Ranking failed retCode={},retMsg={}", response.getRetCode(), response.getRetDes());
        }
        return ret;
    }

    public static boolean clearRanking(String rank, List<String> keys) {
        return clearRanking(rank, ServerConfig.getInstance().getServer(), keys);
    }

    public static boolean clearCrossRanking(String rank, List<String> keys) {
        return clearRanking(rank, GameConst.CROSS_RANKING_SERVER_INDEX, keys);
    }

    public static boolean clearRanking(String rank, int serverIndex) {
        return clearRanking(rank, serverIndex, null);
    }

    public static boolean clearRanking(String rank) {
        return clearRanking(rank, ServerConfig.getInstance().getServer());
    }

    public static String md5(String key) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static String doPost(String httpUrl, String param) {
        if (httpUrl == null) {
            LogUtil.error("common.HttpRequestUtil.doPost, httpUrl is null");
            return null;
        }

        LogUtil.debug("common.HttpRequestUtil.doPost, url:" + httpUrl + ", param :" + param);

        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：5000毫秒
            connection.setConnectTimeout(5000);
            // 设置读取主机服务器返回数据超时时间：5000毫秒
            connection.setReadTimeout(5000);
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/json");
            // 通过连接对象获取一个输出流
            outputStream = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
            outputStream.write(param.getBytes());
            inputStreamReader = new InputStreamReader(connection.getInputStream(), UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            String result = bufferedReader.readLine();
            //TODO 暂时不打返回日志
//            LogUtil.info("HttpRequestUtil,HttpUrl=" + httpUrl + " Response:" + result + "\n");
            return result;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        } finally {
            // 关闭资源
            releaseResource(connection, outputStream, inputStreamReader, bufferedReader);
        }
    }

    public static String doPostConnectTest(String httpUrl, String param) {
        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：5000毫秒
            connection.setConnectTimeout(5000);
            // 设置读取主机服务器返回数据超时时间：5000毫秒
            connection.setReadTimeout(5000);
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/json");
            // 通过连接对象获取一个输出流
            outputStream = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
            outputStream.write(param.getBytes());
            inputStreamReader = new InputStreamReader(connection.getInputStream(), UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.readLine();
        } catch (Exception ex) {
            //400状态码参数不对,但是url是通的
            if (ex.getMessage().contains("response code: 400")) {
                return null;
            } else {
                LogUtil.printStackTrace(ex);
                if (!ServerConfig.getInstance().isDebug()) {
                    throw new RuntimeException("http请求错误,请检查服务状态或请求地址:" + httpUrl);
                } else {
                    LogUtil.error("http请求错误,请检查服务状态或请求地址:" + httpUrl);
                }
            }
        } finally {
            // 关闭资源
            releaseResource(connection, outputStream, inputStreamReader, bufferedReader);
        }
        return null;
    }

    private static void releaseResource(HttpURLConnection connection, OutputStream outputStream, InputStreamReader inputStreamReader, BufferedReader bufferedReader) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Exception e) {
            LogUtil.warn("exception in HttpRequestUtil,method doPost(),return resource exception");
        }
    }


    public static String getSigningStr(Map<String, Object> map) {
        TreeMap<String, Object> treeMap = new TreeMap<>(map);
        List<String> list = new ArrayList<>();
        for (Entry<String, Object> entry : treeMap.entrySet()) {
            Object value = entry.getValue();
            // 数组不参与签名
            if (value instanceof JSONArray || value == null
                    || value instanceof String && StringUtils.isBlank((String) value)) {
            } else if (value instanceof Map) {
                String data = getSigningStr((Map) value);
                list.add(entry.getKey() + "=" + data);
            } else {
                list.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        return StringUtils.join(list, "&");
    }

    public static String getSignSalt(String data, String version, String clientId, String salt) {
        Map map = JSON.parseObject(data, Map.class);
        String businessData = getSigningStr(map);
        String primitiveStr;
        if (salt == null || "".equals(salt)) {
            primitiveStr = clientId + businessData + version + ServerConfig.getInstance().getClientSecret();
        } else {
            primitiveStr = clientId + businessData + version + salt + ServerConfig.getInstance().getClientSecret();
        }
        return string2md5(primitiveStr);
    }

    /**
     * 获取激活码的MD5
     *
     * @param data
     * @param version
     * @param clientId
     * @param salt
     * @return
     */
    private static String getJsonDataSign(String data, String version, String clientId, String salt) {
        String primitiveStr;
        if (salt == null || "".equals(salt)) {
            primitiveStr = clientId + data + version + ServerConfig.getInstance().getClientSecret();
        } else {
            primitiveStr = clientId + data + version + salt + ServerConfig.getInstance().getClientSecret();
        }
        return string2md5(primitiveStr);
    }

    private static String string2md5(String inStr) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            LogUtil.error(e.toString());
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

}

/**
 * 平台请求头实体
 *
 * @author xiao_FL
 * @date 2019/7/3
 */
@Getter
@Setter
class Common {
    protected Common() {
        setClientId(ServerConfig.getInstance().getClientId());
        setVersion(ServerConfig.getInstance().getPlatformProtocolVersion());
        setSalt(ServerConfig.getInstance().getPlatformProtocolSalt());
    }

    private String clientId;
    private String version;
    private String salt;
    private String channel;
    private String platform;
    private String sign;
    /**
     * 平台请求业务参数
     */
    private Object data;

    /**
     * 创建一个默认配置的请求参数
     *
     * @return 平台http请求参数
     */
    static Common getInstance() {
        return new Common();
    }


}
