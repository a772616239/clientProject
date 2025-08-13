package platform.logs;

import lombok.Getter;
import lombok.Setter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.LoginProto.ClientData.Builder;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.3.18
 * 此类型用于处理lang型roleId
 */
@Getter
@Setter
public abstract class AbstractPlayerLogLongRoleId extends AbstractServerLog {
    private String uid;
    private long roleId;
    private String roleName;
    private String platform;
    private String channel;
    private String sourceId;

    public AbstractPlayerLogLongRoleId() {}

    public AbstractPlayerLogLongRoleId(String playerIdx) {
        this(playerCache.getByIdx(playerIdx));
    }

    public AbstractPlayerLogLongRoleId(playerEntity player) {
        if (player == null) {
            return;
        }
        this.uid =  player.getUserid();
        this.roleId = GameUtil.stringToLong(player.getIdx(), 0);
        this.roleName =  player.getName();
        Builder clientData = player.getClientData();
        if (clientData != null) {
            this.platform =  clientData.getPlatform();
            this.channel = clientData.getChannel();
            this.sourceId = String.valueOf(clientData.getSourceId());
        }
    }
}
