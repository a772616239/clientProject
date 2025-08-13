package platform.logs;

import lombok.Getter;
import lombok.Setter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.LoginProto.ClientData.Builder;

@Getter
@Setter
public abstract class AbstractPlayerLog extends AbstractServerLog {
    private String uid;
    private String roleId;
    private String roleName;
    private String platform;
    private String channel;
    private String sourceId;
    private int vipLv;
    private int shortId;
    private int level;

    public AbstractPlayerLog() {
    }

    public AbstractPlayerLog(String playerIdx) {
        this(playerCache.getByIdx(playerIdx));
    }

    public AbstractPlayerLog(playerEntity player) {
        if (player == null) {
            return;
        }
        this.uid = player.getUserid();
        this.roleId = player.getIdx();
        this.roleName = player.getName();
        this.vipLv = player.getVip();
        this.shortId = player.getShortid();
        this.level = player.getLevel();
        Builder clientData = player.getClientData();
        if (clientData != null) {
            this.platform = clientData.getPlatform();
            this.channel = clientData.getChannel();
            this.sourceId = String.valueOf(clientData.getSourceId());
        }
    }
}
