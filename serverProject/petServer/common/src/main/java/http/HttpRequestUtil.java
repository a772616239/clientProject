package http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.GameConst.RankingName;
import datatool.StringHelper;
import http.entity.RankingUpdateRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import protocol.Battle.BattleCheckParam;
import protocol.Battle.CS_BattleResult;
import protocol.Chat.PlayerChatBaseInfo;
import util.LogUtil;
import util.ServerConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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

    /**
     * 用户登陆
     *
     * @param userId
     * @param userToken
     * @param clientData
     * @param channel
     * @param platform
     * @return
     */
    public static boolean login(String userId, String userToken, String clientData, String channel, String platform) {
        String url = ServerConfig.getInstance().getPlatformLogin() + "verifyLoginUser";
        http.entity.HttpLoginAccount httpLoginAccountEntity = new http.entity.HttpLoginAccount(userId, userToken);
        Common common = Common.getLoginCommon(channel, platform);
        common.setData(httpLoginAccountEntity.toString());
        LogUtil.info("method login request:" + JSON.toJSONString(common));
        common.setSign(string2md5(common.getClientId() + httpLoginAccountEntity.toString() + common.getVersion() + common.getSalt() + ServerConfig.getInstance().getClientSecret()));
        String result = doPost(url, JSON.toJSONString(common));
        LogUtil.info("method login result:" + result);
        if (result == null) {
            return false;
        }
        http.entity.HttpLoginResponse response = JSON.parseObject(result, http.entity.HttpLoginResponse.class);
        return response.getRetCode() == PlatFormRetCode.SUCCESS;
    }

    /**
     * 替换更新排行榜
     *
     * @param updateData 排行榜数据
     * @return 操作结果/异常返null
     */
    public static boolean updateRanking(RankingUpdateRequest updateData) {
        if (updateData == null || updateData.getItems() == null || updateData.getItems().size() == 0) {
            LogUtil.info("params is null");
            return false;
        }
        String url = ServerConfig.getInstance().getPlatformRank() + "ranking/replace/update";
        Common common = Common.getInstance();
        common.setData(updateData);
        common.setSign(getSignSalt(JSON.toJSONString(common.getData()), common.getVersion(), common.getClientId(), common.getSalt()));
        String result = doPost(url, JSON.toJSONString(common));
        if (result == null) {
            LogUtil.error("update ranking " + updateData.getRank() + " failed");
            return false;
        }
        http.entity.HttpLoginResponse response = JSON.parseObject(result, http.entity.HttpLoginResponse.class);
        return response.getRetCode() == PlatFormRetCode.SUCCESS;
    }

    /**
     * 查询排行榜
     *
     * @param queryRequest 查询参数
     * @return 查询结果
     */
    public static http.entity.HttpRankingResponse queryRanking(http.entity.RankingQueryRequest queryRequest) {
        if (queryRequest == null) {
            LogUtil.error("http.HttpRequestUtil.queryRanking, params is null");
            return null;
        }
        String url = ServerConfig.getInstance().getPlatformRank() + "ranking/replace/page";
        Common common = Common.getInstance();
        common.setData(queryRequest);
        common.setSign(getSignSalt(JSON.toJSONString(common.getData()), common.getVersion(), common.getClientId(), common.getSalt()));
        String result = doPost(url, JSON.toJSONString(common));
        if (result == null) {
            LogUtil.error("query ranking " + queryRequest.getRank() + " failed");
            return null;
        }
        if (queryRequest.getRank().equals(RankingName.RN_MistTransSvrRank)) {
            LogUtil.debug("MistForest JSON return" + result);
        }
        return JSON.parseObject(result, http.entity.HttpRankingResponse.class);
    }

    /**
     * 清理排行榜
     *
     * @param rank        更新排行榜的名称,无该字段请求将无效
     * @param serverIndex 该字段用于区分数据不互通区服,无该字段请求将无效
     * @return 操作结果/异常返null
     */
    public static boolean clearRanking(String rank, int serverIndex) {
        String url = ServerConfig.getInstance().getPlatformRank() + "ranking/replace/clear";
        Common common = Common.getInstance();
        http.entity.RankingServerMsg queryRequest = new http.entity.RankingServerMsg();
        queryRequest.setRank(rank);
        queryRequest.setServerIndex(serverIndex);
        common.setData(queryRequest);
        common.setSign(getSignSalt(JSON.toJSONString(common.getData()), common.getVersion(), common.getClientId(), common.getSalt()));
        String result = doPost(url, JSON.toJSONString(common));
        if (result == null) {
            LogUtil.error("clear Ranking failed, ranking name :" + rank);
            return false;
        }
        http.entity.HttpLoginResponse response = JSON.parseObject(result, http.entity.HttpLoginResponse.class);
        return response.getRetCode() == PlatFormRetCode.SUCCESS;
    }

    /**
     * 通知聊天服务器，玩家聊天权限
     */
//    public static void playerChatAuthority(int chatRight, String userId, String playerId, String playerName, int vip, int avatar, int playerLvl) {
//        String url = ServerConfig.getInstance().getPlatformChat() + "user/updateUserStatus";
//        ChatAuthority data = new ChatAuthority();
//        ChatAuthorityData authority = new ChatAuthorityData();
//        authority.setRightType(chatRight);
//        authority.setUserId(userId);
//        authority.setExtInfo(JSON.toJSONString(new ChatExtInfoContent(getChatMsg(playerId, playerName, vip, avatar, playerLvl))));
//        authority.setRoleId(playerId);
//        authority.setMsg(user.getChatStateMsg());
//        data.setData(authority);
//        // 这个用String2md5不对
//        data.setSign(md5(data.toString()));
//        String request = JSON.toJSONString(data);
//        LogUtil.debug("update playerChatAuthority param:" + request);
//        LogUtil.debug("update playerChatAuthority return:" + doPost(url, request));
//    }

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

    private static String doPost(String httpUrl, String param) {
        if (httpUrl == null) {
            LogUtil.error("http.HttpRequestUtil.doPost, httpUrl is null");
            return null;
        }
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
            // 设置连接主机服务器超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取主机服务器返回数据超时时间：60000毫秒
            connection.setReadTimeout(60000);
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
            inputStreamReader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            String result = bufferedReader.readLine();
            LogUtil.info("HttpRequestUtil,Http Response:" + result + "\n");
            return result;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        } finally {
            // 关闭资源
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
    }

    private static String getSigningStr(Map<String, Object> map) {
        TreeMap<String, Object> treeMap = new TreeMap<>(map);
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
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

    /**
     * 验证激活码可用性
     *
     * @return
     */
    public static JSONObject checkActiveCode(String playerId, String activeCode, String userId, String channel, String platform) {
        ActiveCodeData activeCodeData = new ActiveCodeData();
        activeCodeData.setRoleId(playerId);
        activeCodeData.setACode(activeCode);
        activeCodeData.setUserId(userId);
        activeCodeData.setServerIndex(ServerConfig.getInstance().getServer());

        Common common = Common.getPlayerCommon(channel, platform);
        common.setData(activeCodeData.toString());
        common.setSign(getJsonDataSign(activeCodeData.toString(), common.getVersion(), common.getClientId(), common.getSalt()));
        String result = doPost(ServerConfig.getInstance().getPlatformActiveCode(), JSON.toJSONString(common));
        if (result == null) {
            return null;
        }

        return JSONObject.parseObject(result);
    }

    public static byte[] getChatMsg(String playerId, String playerName, int vip, int avatar, int playerLvl) {
        PlayerChatBaseInfo.Builder builder = PlayerChatBaseInfo.newBuilder().setPlayerIdx(playerId)
                .setName(playerName)
                .setVIPLv(vip)
                .setAvatarId(avatar)
                .setLv(playerLvl);
        return builder.build().toByteArray();
    }

    public static CS_BattleResult checkBattle(BattleCheckParam battleCheckParam) {
        try {
            String url = ServerConfig.getInstance().getBattleCheckUrl();
            if (StringHelper.isNull(url)) {
                return null;
            }

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("BattleCheckParam", battleCheckParam.toByteArray());
            String jsonStr = jsonObj.toJSONString();
            LogUtil.debug("check battle jsonStr=" + jsonStr);
            String result = doPost(url, jsonStr);
            if (result == null) {
                return null;
            }
            JSONObject retJson = JSONObject.parseObject(result);
            int errCode = retJson.getInteger("errCode");
            if (errCode != 200) {
                String errMsg = retJson.getString("errMsg");
                if (errMsg == null) {
                    errMsg = "";
                }
                LogUtil.error("check battle error,errCode=" + errCode + ",errMsg=" + errMsg);
                return null;
            }
            byte[] data = retJson.getBytes("data");
            if (data == null) {
                LogUtil.error("check battle failed,data is null");
                return null;
            }
            CS_BattleResult battleResult = CS_BattleResult.parseFrom(data);
            return battleResult;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    /**
     * ==========================防沉迷=======================================
     */

//    public static void antiLogIn(playerEntity player) {
//        //测试跳过
//        if (ServerConfig.getInstance().GMAble() && player.getUserid().startsWith("robot")) {
//            return;
//        }
//        String result = doPost(ServerConfig.getInstance().getPlatformAntiLogIn(), buildAntiInfo(player));
//        LogUtil.debug("playerIdx = " + player.getIdx() + ", antiLogIn result :" + result);
//    }
//
//    public static void antiLogOut(playerEntity player) {
//        //测试跳过
//        if (ServerConfig.getInstance().GMAble() && player.getUserid().startsWith("robot")) {
//            return;
//        }
//        String result = doPost(ServerConfig.getInstance().getPlatformAntiLogOut(), buildAntiInfo(player));
//        LogUtil.debug("playerIdx = " + player.getIdx() + ", antiLogOut result :" + result);
//    }
//
//    public static String buildAntiInfo(playerEntity player) {
//        JSONObject jsonObj = new JSONObject();
//        jsonObj.put("userId", player.getUserid());
//        Common playerCommon = new Common(player);
//        String jsonStr = jsonObj.toJSONString();
//        playerCommon.setData(jsonStr);
//        playerCommon.setSign(getJsonDataSign(jsonStr, playerCommon.getVersion(), playerCommon.getClientId(), playerCommon.getSalt()));
//        return JSONObject.toJSONString(playerCommon);
//    }

    /**
     * ==========================防沉迷=======================================
     */
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
    private Common() {
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

//    public Common(playerEntity player) {
//        this();
//        if (player != null) {
//            Builder clientData = player.getClientData();
//            if (clientData != null) {
//                this.channel = clientData.getChannel();
//                this.platform = clientData.getPlatform();
//            }
//        }
//    }

    /**
     * 创建一个和玩家有关的默认配置的请求参数
     *
     * @param channel
     * @param platform
     * @return
     */
    public static Common getPlayerCommon(String channel, String platform) {
        Common common = new Common();
        common.channel = channel;
        common.platform = platform;
        return common;
    }

    /**
     * 登录时创建配置数据
     *
     * @param channel  客户端请求信息
     * @param platform 平台http请求参数
     * @return 平台http请求参数
     */
    static Common getLoginCommon(String channel, String platform) {
        Common common = getInstance();
        common.channel = channel;
        common.platform = platform;
        return common;
    }

    /**
     * 创建一个默认配置的请求参数
     *
     * @return 平台http请求参数
     */
    static Common getInstance() {
        return new Common();
    }
}


@Getter
@Setter
class ActiveCodeData {
    private String roleId;
    private String aCode;
    private String userId;
    private int serverIndex;

    @Override
    public String toString() {
        return "{" +
                "\"roleId\":" + "\"" + roleId + "\"" +
                ",\"aCode\":" + "\"" + aCode + "\"" +
                ",\"userId\":" + "\"" + userId + "\"" +
                ",\"serverIndex\":" + serverIndex +
                '}';
    }
}

@Getter
@Setter
class ChatAuthority {
    private String clientId = ServerConfig.getInstance().getClientId();
    private String sign;
    private ChatAuthorityData data;

    @Override
    public String toString() {
        return clientId + "&" + data + "&" + ServerConfig.getInstance().getClientSecret();
    }
}

@Getter
@Setter
class ChatAuthorityData {
    private String roleId;
    private String userId;
    private int rightType;
    private String extInfo;
    private String msg;

    @Override
    public String toString() {
        return "extInfo=" + extInfo + "&msg=" + msg + "&rightType=" + rightType + "&roleId=" + roleId + "&userId=" + userId;
    }
}