package common.entity;

import com.alibaba.fastjson.JSONObject;
import common.load.ServerConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/27
 */
@Getter
@Setter
public class NewChatAuthority {
    private String clientId;
    private String serverIndex;
    private String sign;
    /**
     * 业务参数
     */
    private Object data;

    @Override
    public String toString() {
        return clientId + "&" + data + "&" + ServerConfig.getInstance().getClientSecret();
    }

    public String toJson() {
        return JSONObject.toJSONString(this);
    }

    public static NewChatAuthority create() {
        NewChatAuthority common = new NewChatAuthority();
        common.setClientId(ServerConfig.getInstance().getClientId());
        common.setServerIndex(String.valueOf(ServerConfig.getInstance().getServer()));
        return common;
    }
}
