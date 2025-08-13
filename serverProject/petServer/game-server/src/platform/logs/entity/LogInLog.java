package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.mainLine.dbCache.mainlineCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;
import protocol.LoginProto.ClientData;

@Getter
@Setter
@NoArgsConstructor
public class LogInLog extends AbstractPlayerLog {
    private int userLevel;
    private String networkType;
    private String userType = "";
    private String ip;
    private String device;
    private String macAddr;
    private int curCoupon;
    private long curGold;
    private int vipLv;
    private long curDiamond;
    private long curMainLinePoint;

    public LogInLog(String playerIdx, ClientData clientData, String ip) {
        this(playerCache.getByIdx(playerIdx), clientData, ip);
    }

    public LogInLog(playerEntity player, ClientData clientData, String ip) {
        super(player);
        if (player == null) {
            return;
        }
        this.setUserLevel(player.getLevel());
        this.setIp(ip);
        this.vipLv = player.getVip();
        this.curCoupon = player.getCoupon();
        this.curDiamond = player.getDiamond();
        this.curMainLinePoint = mainlineCache.getInstance().getPlayerCurNode(player.getIdx());

        if (clientData != null) {
            this.setPlatform(clientData.getPlatform());
            this.setChannel(clientData.getChannel());
            this.setSourceId(String.valueOf(clientData.getSourceId()));
            this.setDevice(clientData.getDevice());
            this.setMacAddr(clientData.getMacAddr());
            this.setNetworkType(clientData.getIsWify() ? "WIFI" : "移动");
        }
    }
}
