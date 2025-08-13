package server.http.entity;

import common.load.ServerConfig;
import common.tick.GlobalTick;
import lombok.Getter;
import lombok.Setter;
import model.player.entity.playerEntity;
import protocol.LoginProto.ClientData;
import protocol.PlayerDB.DB_BanInfo;
import protocol.PlayerDB.DB_PlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author huhan
 * @date 2020.02.27
 */
@Getter
@Setter
public class PlatformPlayerBaseInfo {
    private String userId;
    private String channel;
    private String roleName;
    private String roleId;
    private String zone;
    private int serverIndex;
    private int level;
    private int vipLv;
    private long lastOnLineTime;
    private boolean onlineState;
    private long createRoleTime;
    private String device;
    private List<BanInfo> banState = new ArrayList<>();

    @Getter
    @Setter
    class BanInfo {
        private int type;
        private long endTime;

        public BanInfo(int type, long endTime) {
            this.type = type;
            this.endTime = endTime;
        }
    }

    public PlatformPlayerBaseInfo(playerEntity player) {
        if (player == null) {
            return;
        }

        this.zone = ServerConfig.getInstance().getZone();
        this.serverIndex = ServerConfig.getInstance().getServer();
        this.userId = player.getUserid();
        this.roleId = player.getIdx();
        this.roleName = player.getName();
        this.level = player.getLevel();
        this.vipLv = player.getVip();
        this.onlineState = player.isOnline();
        this.lastOnLineTime = player.getLogouttime() == null ? 0 : player.getLogouttime().getTime();
        this.createRoleTime = player.getCreatetime() == null ? 0 : player.getCreatetime().getTime();

        DB_PlayerData.Builder db_data = player.getDb_data();
        if (db_data != null) {
            Map<Integer, DB_BanInfo> bannedInfosMap = db_data.getBannedInfosMap();
            long currentTime = GlobalTick.getInstance().getCurrentTime();
            for (DB_BanInfo value : bannedInfosMap.values()) {
                if (value.getEndTime() > currentTime) {
                    banState.add(new BanInfo(value.getType(), value.getEndTime()));
                }
            }
        }

        ClientData.Builder clientData = player.getClientData();
        if (clientData != null) {
            this.channel = clientData.getChannel();
            this.device = clientData.getDevice();
        }
    }
}
