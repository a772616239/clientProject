package common.entity;

import common.load.ServerConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/27
 */
@Getter
@Setter
public class ChatAuthority {
    private String clientId = ServerConfig.getInstance().getClientId();
    private String sign;
    private ChatAuthorityData data;

    @Override
    public String toString() {
        return clientId + "&" + data + "&" + ServerConfig.getInstance().getClientSecret();
    }
}
