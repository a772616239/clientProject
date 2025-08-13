package common.entity;

import com.alibaba.fastjson.JSONObject;
import common.load.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import protocol.LoginProto.CS_Login;

/**
 * 请求业务参数：登陆账号
 *
 * @author xiao_FL
 * @date 2019/7/3
 */

@Getter
@Setter
public class HttpLoginAccount {
    private String userId;
    private String userToken;
    private String zoneName = ServerConfig.getInstance().getZone();
    private Integer serverIndex = ServerConfig.getInstance().getServer();
    private String endPoint;
    private String clientVersion;

    public HttpLoginAccount(String userId, String userToken) {
        this.userId = userId;
        this.userToken = userToken;
    }

    public HttpLoginAccount(CS_Login login) {
        if (login != null) {
            this.userId = login.getUserId();
            this.userToken = login.getToken();
            this.endPoint = login.getClientData().getClientVersionType();
            this.clientVersion = login.getClientData().getClientVersion();
        }
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("userId", userId);
        object.put("userToken", userToken);
        object.put("zoneName", zoneName);
        object.put("serverIndex", serverIndex);
        object.put("endPoint", endPoint);
        object.put("clientVersion", clientVersion);
        return object.toJSONString();
    }
}