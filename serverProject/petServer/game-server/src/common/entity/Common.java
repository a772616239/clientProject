package common.entity;

import com.alibaba.fastjson.JSONObject;
import common.load.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.LoginProto.ClientData;
import protocol.LoginProto.ClientData.Builder;

/**
 * 平台请求头实体
 *
 * @author xiao_FL
 * @date 2019/7/3
 */
@Getter
@Setter
public class Common {
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

    public Common(playerEntity player) {
        this();
        if (player != null) {
            Builder clientData = player.getClientData();
            if (clientData != null) {
                this.channel = clientData.getChannel();
                this.platform = clientData.getPlatform();
            }
        }
    }

    /**
     * 创建一个和玩家有关的默认配置的请求参数
     *
     * @param playerId 玩家id
     * @return 平台http请求参数
     */
    public static Common getPlayerCommon(String playerId) {
        Common common = new Common();
        playerEntity player = playerCache.getByIdx(playerId);
        if (player != null) {
            common.channel = player.getClientData().getChannel();
            common.platform = player.getClientData().getPlatform();
        }
        return common;
    }

    /**
     * 登录时创建配置数据
     *
     * @param clientData 客户端请求信息
     * @return 平台http请求参数
     */
    public static Common getLoginCommon(ClientData clientData) {
        Common common = getInstance();
        if (clientData != null) {
            common.channel = clientData.getChannel();
            common.platform = clientData.getPlatform();
        }
        return common;
    }

    /**
     * 创建一个默认配置的请求参数
     *
     * @return 平台http请求参数
     */
    public static Common getInstance() {
        return new Common();
    }

    public String toJsonString() {
        return JSONObject.toJSONString(this);
    }
}
