package common;

import cfg.NewbeeTag;
import cfg.NewbeeTagObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bowlong.third.FastJSON;
import common.GameConst.ChatRetCode;
import common.GameConst.RankingName;
import common.entity.ActiveCodeData;
import common.entity.ChatAuthority;
import common.entity.ChatAuthorityData;
import common.entity.Common;
import common.entity.HttpLoginAccount;
import common.entity.HttpLoginResponse;
import common.entity.HttpRankingResponse;
import common.entity.NewChatAuthority;
import common.entity.NewChatAuthorityData;
import common.entity.PushNotificationData;
import common.entity.RankingQueryRequest;
import common.entity.RankingServerMsg;
import common.entity.RankingUpdateRequest;
import common.load.ServerConfig;
import datatool.StringHelper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import model.arena.ArenaManager;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.player.util.PlayerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Battle.BattleCheckParam;
import protocol.Battle.CS_BattleResult;
import protocol.Chat.PlayerChatBaseInfo;
import protocol.LoginProto.CS_Login;
import protocol.RetCodeId.RetCodeEnum;
import server.http.entity.ChatExtInfoContent;
import util.GameUtil;
import util.LogUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Http请求工具
 *
 * @author xiao_FL
 * @date 2019/7/3
 */
public class HttpRequestUtil {
    public static class PlatFormRetCode {
        public static final int SUCCESS = 0;
        public static final int ERROR_PARAMS = 98;                  //参数错误
        public static final int ServerNotOpen = 100;                 //服务器维护
        public static final int SIGN_ERROR = 102;                   //签名错误
        public static final int LOGIN_ERROR_VERSION = 103;          //客户端版本错误
        public static final int ActiveCode_LoseEfficacy = 108;      //激活码已失效
        public static final int ActiveCode_Used = 109;              //激活码已被使用
        public static final int ActiveCode_Error = 110;             //激活码错误
        public static final int Account_Lock = 111;                 //账号锁定
        public static final int ActiveCode_UpperLimit = 114;        //激活码使用次数上限
        public static final int LOGIN_ERROR_ServerBusy = 115;       //服务器火爆状态
        public static final int ActiveCode_UseSameTypeCode = 802;   //使用过同类型的激活码

        public static Map<Integer, RetCodeEnum> platClientErrorCodeMap = new HashMap<>();

        public static final RetCodeEnum defaultErrorCode = RetCodeEnum.RCE_Login_ErrorPwd;

        static {
            platClientErrorCodeMap.put(SUCCESS, RetCodeEnum.RCE_Success);
            platClientErrorCodeMap.put(LOGIN_ERROR_VERSION, RetCodeEnum.RCE_Login_ClientVersionError);
            platClientErrorCodeMap.put(LOGIN_ERROR_ServerBusy, RetCodeEnum.RCE_Login_ServerBusy);
            platClientErrorCodeMap.put(ServerNotOpen, RetCodeEnum.RCE_Login_ServerNotOpen);
            platClientErrorCodeMap.put(Account_Lock, RetCodeEnum.RSE_Login_AccountLock);
            platClientErrorCodeMap = Collections.unmodifiableMap(platClientErrorCodeMap);
        }

        public static RetCodeEnum convertPlatLoginErrorCode(int retCode) {
            RetCodeEnum codeEnum = platClientErrorCodeMap.get(retCode);
            return codeEnum == null ? defaultErrorCode : codeEnum;
        }
    }

    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 用户登陆RankingUpdateRequest
     *
     * @param loginData 登录信息
     * @return 登陆结果
     */
    public static RetCodeEnum login(CS_Login loginData) {
        if (loginData == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        String url = ServerConfig.getInstance().getPlatformLogin() + "verifyLoginUser";
        HttpLoginAccount httpLoginAccountEntity = new HttpLoginAccount(loginData);
        Common common = Common.getLoginCommon(loginData.getClientData());
        common.setData(httpLoginAccountEntity.toString());
        LogUtil.info("userId=" + loginData.getUserId() + ",method login request:" + FastJSON.toJSONString(common));
        common.setSign(platformMD5(common.getClientId() + httpLoginAccountEntity.toString() + common.getVersion()
                + common.getSalt() + ServerConfig.getInstance().getClientSecret()));
        String result = doPost(url, FastJSON.format(common));
        LogUtil.info("method login result:" + result);
        if (result == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        HttpLoginResponse response = FastJSON.parseObject(result, HttpLoginResponse.class);
        return PlatFormRetCode.convertPlatLoginErrorCode(response.getRetCode());
    }

    public static void reportNewPlayerData(playerEntity player) {
        if (player == null) {
            LogUtil.error("reportNewPlayerData player is null");
            return;
        }
        String url = ServerConfig.getInstance().getPlatformExtraInfo();
        Common common = new Common(player);
        JSONObject reportData = new JSONObject();
        reportData.put("userId", player.getUserid());
        reportData.put("roleId", player.getIdx());
        reportData.put("roleName", player.getName());
        reportData.put("shortId", player.getShortid());
        reportData.put("serverIndex", ServerConfig.getInstance().getServer());
        common.setData(reportData.toJSONString());
        common.setSign(platformMD5(common.getClientId() + reportData.toJSONString() + common.getVersion()
                + common.getSalt() + ServerConfig.getInstance().getClientSecret()));
        doPost(url, FastJSON.format(common));
        LogUtil.info("reportNewPlayerData finished");
    }


    /**
     * 推荐使用方法进行加密
     *
     * @param data     JsonString
     * @param version
     * @param clientId
     * @param salt
     * @return
     */
    public static String platformMd5(String data, String version, String clientId, String salt) {
        String clientSecret = ServerConfig.getInstance().getClientSecret();
        String primitiveStr = "";
        if (salt == null || "".equals(salt)) {
            primitiveStr = clientId + data + version + clientSecret;
        } else {
            primitiveStr = clientId + data + version + salt + clientSecret;
        }
        return platformMD5(primitiveStr);
    }

    private static String platformMD5(String key) {
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

    public static void asyncUpdateRanking(RankingUpdateRequest updateData) {
        if (updateData == null) {
            return;
        }
        executor.execute(() -> {
            boolean success = updateRanking(updateData);
            printRankLog(updateData, success);
        });
    }

    private static void printRankLog(RankingUpdateRequest updateData, boolean success) {
        if (updateData.getRank().contains("FestivalBoss")){
            LogUtil.info("FestivalBoss rank update result:{},updateData :{}", success, updateData);
        }
        if (success) {
            if (!CollectionUtils.isEmpty(updateData.getItems())) {
                LogUtil.debug("playerIdx :" + updateData.getItems().get(0).getPrimaryKey() + " update " + updateData.getRank() + " ranking");
            }
        } else {
            LogUtil.error(" update " + updateData + " ranking info failed");
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
        return response.getRetCode() == PlatFormRetCode.SUCCESS;
    }

    /**
     * 添加用户标签
     *
     * @param userId 用户id
     * @param tag    标签
     */
    public static void addPushTag(String userId, String tag) {
        if (userId == null) {
            return;
        }
        String url = ServerConfig.getInstance().getPushTagAdd();
        Common common = Common.getInstance();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(tag, userId);
        common.setData(jsonObj.toJSONString());
        common.setSign(getSignSalt(FastJSON.toJSONString(common.getData()), common.getVersion(), common.getClientId(), common.getSalt()));
        asyncDoPost(url, FastJSON.toJSONString(common));
    }

    /**
     * 移除用户标签
     *
     * @param userId 用户id
     * @param tag    标签
     */
    public static void deletePushTag(String userId, String tag) {
        if (userId == null) {
            return;
        }
        String url = ServerConfig.getInstance().getPushTagDelete();
        Common common = Common.getInstance();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(tag, userId);
        common.setData(jsonObj.toJSONString());
        common.setSign(getSignSalt(FastJSON.toJSONString(common.getData()), common.getVersion(), common.getClientId(), common.getSalt()));
        asyncDoPost(url, FastJSON.toJSONString(common));
    }

    /**
     * 推送消息
     *
     * @param data
     */
    public static void pushNotify(PushNotificationData data) {
        if (data == null) {
            return;
        }

        String url = ServerConfig.getInstance().getPushNotification();
        Common common = Common.getInstance();
        common.setData(data.toString());
        common.setSign(platformMD5(common.getClientId() + data.toString() + common.getVersion()
                + common.getSalt() + ServerConfig.getInstance().getClientSecret()));
        LogUtil.info("pushNotify,url:{}");
        asyncDoPost(url, FastJSON.format(common));

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
        if (queryRequest.getRank().equals(GameUtil.buildTransServerRankName(RankingName.RN_MistTransSvrRank))) {
            LogUtil.debug("MistForest JSON return" + result);
        }
        return FastJSON.parseObject(result, HttpRankingResponse.class);
    }

    /**
     * 清理排行榜
     *
     * @param rank        更新排行榜的名称,无该字段请求将无效
     * @param serverIndex 该字段用于区分数据不互通区服,无该字段请求将无效
     * @param sortRules   清除指定排行类型的排行榜
     * @return 操作结果/异常返null
     */
    public static boolean clearRanking(String rank, int serverIndex, List<Integer> sortRules) {
        return clearRanking(rank, serverIndex, sortRules, null);
    }

    /**
     * 清理排行榜
     *
     * @param rank        更新排行榜的名称,无该字段请求将无效
     * @param serverIndex 该字段用于区分数据不互通区服,无该字段请求将无效
     * @param sortRules   清除指定排行榜的排序规则排行榜
     * @param keys        清除指定的keys,为空，删除整个排行榜； 非空，删除该排行榜里面的该条记录
     * @return 操作结果/异常返null
     */
    public static boolean clearRanking(String rank, int serverIndex, List<Integer> sortRules, List<String> keys) {
        String url = ServerConfig.getInstance().getPlatformRankClear();
        Common common = Common.getInstance();

        RankingServerMsg queryRequest = new RankingServerMsg();
        queryRequest.setRank(rank);
        queryRequest.setServerIndex(serverIndex);
        queryRequest.setSortRules(sortRules);
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
        LogUtil.info("HttpRequestUtil clearRanking rank:{},serverIndex:{},sortRules:{},keys:{}"
                    ,rank,serverIndex,sortRules,keys);

        HttpLoginResponse response = FastJSON.parseObject(result, HttpLoginResponse.class);
        return response.getRetCode() == PlatFormRetCode.SUCCESS;
    }


    public static boolean updatePlayerChatAuthority(playerEntity player) {
//        if (!playerChatAuthority(player)) {
//            LogUtil.error("common.HttpRequestUtil.updatePlayerChatAuthority， update old chat authority failed");
//        }
        if (!updatePlayerNewChatAuthority(player)) {
            LogUtil.error("common.HttpRequestUtil.updatePlayerChatAuthority， update new chat authority failed");
            return false;
        }

        return true;
    }

    public static boolean updatePlayerNewChatAuthority(playerEntity player) {
        if (player == null) {
            return false;
        }
        NewChatAuthorityData authority = new NewChatAuthorityData();
        authority.setRoleId(player.getIdx());
        authority.setAppPermission(player.getCurChatState() == ChatRetCode.OPEN);
        authority.setAppPermissionMsg(player.getChatStateMsg());

        NewChatAuthority common = NewChatAuthority.create();
        common.setData(authority);
        common.setSign(md5(common.toString()));
        String updateResult = doPost(ServerConfig.getInstance().getPlatformNewChat(), common.toJson());
        LogUtil.debug("HttpRequestUtil.updatePlayerNewChatAuthority, update player chat authority: status:"
                + authority.isAppPermission() + " result:" + updateResult);

        JSONObject resultJson = JSONObject.parseObject(updateResult);
        return resultJson != null && resultJson.containsKey("code") && resultJson.getInteger("code") == 0;
    }

    /**
     * 通知聊天服务器，玩家聊天权限
     *
     * @param user 玩家信息
     */
    public static boolean playerChatAuthority(playerEntity user) {
        if (user == null) {
            return false;
        }

        String url = ServerConfig.getInstance().getPlatformChat() + "user/updateUserStatus";
        ChatAuthorityData authority = new ChatAuthorityData();
        authority.setRightType(user.getCurChatState());
        authority.setUserId(user.getUserid());
        authority.setExtInfo(FastJSON.toJSONString(new ChatExtInfoContent(getChatMsg(user))));
        authority.setRoleId(user.getIdx());
        authority.setMsg(user.getChatStateMsg());
        authority.setServerIndex(String.valueOf(ServerConfig.getInstance().getServer()));

        ChatAuthority data = new ChatAuthority();
        data.setData(authority);
        // 这个用String2md5不对
        data.setSign(md5(data.toString()));
        String request = FastJSON.format(data);
        String updateResult = doPost(url, request);
        LogUtil.debug("update playerChatAuthority, playerIdx+" + user.getIdx() + " return:" + updateResult);

        JSONObject resultJson = JSONObject.parseObject(updateResult);
        return resultJson != null && resultJson.containsKey("code") && resultJson.getInteger("code") == 0;
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

    public static void asyncDoPost(String httpUrl, String param) {
        executor.execute(() -> doPost(httpUrl, param));
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
               /* if (!ServerConfig.getInstance().isDebug()) {
                    throw new RuntimeException("http请求错误,请检查服务状态或请求地址:" + httpUrl);
                } else {*/
                LogUtil.error("http请求错误,请检查服务状态或请求地址:" + httpUrl);
                // }
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
//            String result = bufferedReader.readLine();
            String result = bufferedReader.lines().collect(Collectors.joining());
            LogUtil.debug("HttpRequestUtil,HttpUrl=" + httpUrl + " Response:" + result + "\n");
            return result;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        } finally {
            // 关闭资源
            releaseResource(connection, outputStream, inputStreamReader, bufferedReader);
        }
    }

    public static String getSigningStr(Map<String, Object> map) {
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
    public static JSONObject checkActiveCode(String playerId, String activeCode) {
        ActiveCodeData activeCodeData = new ActiveCodeData();
        activeCodeData.setRoleId(playerId);
        activeCodeData.setACode(activeCode);
        activeCodeData.setUserId(PlayerUtil.queryPlayerUserId(playerId));
        activeCodeData.setServerIndex(ServerConfig.getInstance().getServer());

        Common common = Common.getPlayerCommon(playerId);
        common.setData(activeCodeData.toString());
        common.setSign(getJsonDataSign(activeCodeData.toString(), common.getVersion(), common.getClientId(), common.getSalt()));
        String result = doPost(ServerConfig.getInstance().getActiveCodeUrl(), FastJSON.toJSONString(common));
        if (result == null) {
            return null;
        }

        return JSONObject.parseObject(result);
    }

    public static JSONObject queryIpInfo(String ip) {
        if (ip == null) {
            LogUtil.warn("common.HttpRequestUtil.queryIpInfo, ip is null");
            return new JSONObject();
        }

        JSONObject ipObj = new JSONObject();
        ipObj.put("ip", GameUtil.getIpPort(ip)[0]);
        JSONObject dataObj = new JSONObject();
        dataObj.put("data", ipObj);


        String result = doPost(ServerConfig.getInstance().getPlatformIpInfo(), dataObj.toJSONString());
        if (result == null) {
            LogUtil.error("query ipInfo error, ip = " + ip);
            return new JSONObject();
        }
        LogUtil.debug("common.HttpRequestUtil.queryIpInfo, result = " + result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (jsonObject.containsKey("ipInfo")) {
            return jsonObject.getJSONObject("ipInfo");
        }
        return new JSONObject();
    }

    public static byte[] getChatMsg(playerEntity user) {
        if (user != null) {
            PlayerChatBaseInfo.Builder builder = PlayerChatBaseInfo.newBuilder().setPlayerIdx(user.getIdx())
                    .setName(user.getName())
                    .setVIPLv(user.getVip())
                    .setAvatarId(user.getAvatar())
                    .setLv(user.getLevel())
                    .setAvatarBorder(user.getDb_data().getCurAvatarBorder());
            if (builder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
                ArenaManager.getInstance().getPlayerRank(user.getIdx());
            }
            return builder.build().toByteArray();
        } else {
            return null;
        }
    }

    public static CS_BattleResult checkBattle(String playerIdx, long battleId, BattleCheckParam battleCheckParam) {
        try {
            String url = ServerConfig.getInstance().getBattleCheckUrl();
            if (StringHelper.isNull(url)) {
                LogUtil.error("CheckBattle HttpUrl is null");
                return null;
            }

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("BattleCheckParam", battleCheckParam.toByteArray());
            String jsonStr = jsonObj.toJSONString();
            LogUtil.warn("checkBattleHttpResult playerIdx={},battleId={},fightMakeId={},subBattleType={},jsonStr={}",
                    playerIdx, battleId, battleCheckParam.getEnterFightData().getFightMakeId(), battleCheckParam.getEnterFightData().getSubType(),jsonStr);
            long startTime = System.currentTimeMillis();
            String result = doPost(url, jsonStr);
            long costTime = System.currentTimeMillis() - startTime;
            LogUtil.debug("CheckBattle costTime:" + costTime);
            if (result == null) {
                LogUtil.warn("checkBattle[" + battleId + "] error,timeout");
                return null;
            }
            JSONObject retJson = JSONObject.parseObject(result);
            int errCode = retJson.getInteger("errCode");
            if (errCode != 200) {
                String errMsg = retJson.getString("errMsg");
                if (errMsg == null) {
                    errMsg = "";
                }
                LogUtil.warn("checkBattle[" + battleId + "] error,errCode=" + errCode + ",errMsg=" + errMsg);
                return null;
            }
            byte[] data = retJson.getBytes("data");
            if (data == null) {
                LogUtil.warn("checkBattle[" + battleId + "] failed,data is null");
                return null;
            }
            LogUtil.info("checkBattle getHttpResult finished,playerIdx={},battleId={},fightmakeid={},subBattleType={}",
                    playerIdx, battleId, battleCheckParam.getEnterFightData().getFightMakeId(), battleCheckParam.getEnterFightData().getSubType());
            return CS_BattleResult.parseFrom(data);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    /**
     * ==========================防沉迷=======================================
     */

    public static void antiLogIn(playerEntity player) {
        //测试跳过
        if (player.getUserid().startsWith("robot")) {
            return;
        }
        executor.execute(() -> {
            String result = doPost(ServerConfig.getInstance().getPlatformAntiLogIn(), buildAntiInfo(player));
            LogUtil.debug("playerIdx = " + player.getIdx() + ", antiLogIn result :" + result);
        });
    }

    public static void antiLogOut(playerEntity player) {
        //测试跳过
        if (player.getUserid().startsWith("robot")) {
            return;
        }
        executor.execute(() -> {
            String result = doPost(ServerConfig.getInstance().getPlatformAntiLogOut(), buildAntiInfo(player));
            LogUtil.debug("playerIdx = " + player.getIdx() + ", antiLogOut result :" + result);
        });
    }

    public static String buildAntiInfo(playerEntity player) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("userId", player.getUserid());
        jsonObj.put("si", player.getShortid() + "" + ServerConfig.getInstance().getServer());
        Common playerCommon = new Common(player);
        String jsonStr = jsonObj.toJSONString();
        playerCommon.setData(jsonStr);
        playerCommon.setSign(getJsonDataSign(jsonStr, playerCommon.getVersion(), playerCommon.getClientId(), playerCommon.getSalt()));
        return JSONObject.toJSONString(playerCommon);
    }

    /**
     * ==========================防沉迷=======================================
     */

    /**
     * ==========================appsFlyer start =======================================
     */

    public static void platformAppsflyerLogin(playerEntity entity) {
        if (entity == null) {
            return;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("userId", entity.getUserid());

        doPlatformPost(entity, jsonObj, ServerConfig.getInstance().getPlatformAppsflyerLogin());
    }

    public static void platformAppsflyerLevel(playerEntity entity) {
        if (entity == null) {
            return;
        }


        JSONObject jsonObj = new JSONObject();
        jsonObj.put("userId", entity.getUserid());
        jsonObj.put("level", entity.getLevel());
        //score还是传和等级一样的值把。
        jsonObj.put("score", entity.getLevel());

        doPlatformPost(entity, jsonObj, ServerConfig.getInstance().getPlatformAppsflyerLevel());
    }

    public static void platformAppsflyerTutorial(playerEntity entity, int newBeeStepId) {
        NewbeeTagObject newBeeTagCfg = NewbeeTag.getById(newBeeStepId);
        if (entity == null || newBeeTagCfg == null) {
            LogUtil.debug("common.HttpRequestUtil.platformAppsflyerTutorial, stepId:" + newBeeStepId
                    + " is not exist in tag cfg");
            return;
        }

        if (!newBeeTagCfg.getJoinappsflyer()) {
            return;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("userId", entity.getUserid());
        jsonObj.put("success", true);
        jsonObj.put("tutorialId", String.valueOf(newBeeStepId));
        jsonObj.put("content", newBeeTagCfg.getComment());

        doPlatformPost(entity, jsonObj, ServerConfig.getInstance().getPlatformAppsflyerTutorial());
    }

    public static void platformAppsflyerWatchAdFinish(playerEntity entity) {
        if (entity == null) {
            return;
        }
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("userId", entity.getUserid());
        doPlatformPost(entity, jsonObj, ServerConfig.getInstance().getPlatformAppsflyerWatchAdFinish());
    }

    /**
     * ==========================appsFlyer end =======================================
     */

    public static void doPlatformPost(playerEntity entity, Object data, String url) {
        if (entity == null || data == null || StringUtils.isBlank(url)) {
            LogUtil.error("common.HttpRequestUtil.doPlatformPost, error params, entity:" + entity + ", data:" + data + ", url:" + url);
            return;
        }

        Common playerCommon = new Common(entity);
        String jsonStr = JSONObject.toJSONString(data);
        playerCommon.setData(jsonStr);
        playerCommon.setSign(platformMd5(jsonStr, playerCommon.getVersion(), playerCommon.getClientId(), playerCommon.getSalt()));

        executor.execute(() -> {
            String result = doPost(url, playerCommon.toJsonString());
            LogUtil.debug("playerIdx = " + entity.getIdx() + ", url:" + url + " result :" + result);
        });
    }
}

