package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;
import protocol.LoginProto.ClientData.Builder;

@Getter
@Setter
@NoArgsConstructor
public class LogOutLog extends AbstractPlayerLog {
    private String networkType;
    private String ip;
    private String device;

    public LogOutLog(playerEntity player) {
        super(player);
        if (player == null) {
            return;
        }

        this.networkType = player.getNetType();
        this.ip = player.getIp();

        Builder clientData = player.getClientData();
        if (clientData != null) {
            this.device = clientData.getDevice();
        }
    }

    public LogOutLog(String playerIdx) {
        this(playerCache.getByIdx(playerIdx));
    }
}
