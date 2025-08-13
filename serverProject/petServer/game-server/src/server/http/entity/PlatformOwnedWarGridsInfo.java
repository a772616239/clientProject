package server.http.entity;

import common.load.ServerConfig;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import protocol.ServerTransfer.WarGridLogDbData;
import protocol.TheWarDefine.WarCellTagFlag;

@Getter
@Setter
public class PlatformOwnedWarGridsInfo {
    private String roleName;
    private String roleId;
    private String userId;
    private int shortId;
    private String zone;
    private int serverIndex;
    private int level;

    private String mapName;
    private List<OwnedWarGridInfo> ownedWarGrids = new ArrayList<>();

    public PlatformOwnedWarGridsInfo(playerEntity player) {
        if (player == null) {
            return;
        }
        setRoleId(player.getIdx());
        setUserId(player.getUserid());
        setRoleName(player.getName());
        setShortId(player.getShortid());
        setLevel(player.getLevel());
        setZone(ServerConfig.getInstance().getZone());
        setServerIndex(ServerConfig.getInstance().getServer());
        setMapName(TheWarManager.getInstance().getMapName());

        if (TheWarManager.getInstance().open()) {
            for (WarGridLogDbData warGridLogData : player.getDb_data().getTheWarData().getOwedGridDataList()) {
                ownedWarGrids.add(new OwnedWarGridInfo(warGridLogData));
            }
        }
    }

    @Getter
    @Setter
    public static class OwnedWarGridInfo {
        int posX;
        int posY;
        String gridType;
        int gridLevel;
        boolean hasTrooped;

        public OwnedWarGridInfo(WarGridLogDbData warGridLogDbData) {
            setPosX(warGridLogDbData.getPos().getX());
            setPosY(warGridLogDbData.getPos().getY());
            setGridLevel(warGridLogDbData.getGridLevel());
            setHasTrooped(warGridLogDbData.getHasTrooped());

            String gridTypeStr = "";
            int gridType = warGridLogDbData.getGridType();
            if ((gridType & WarCellTagFlag.WCTF_Normal_Manor_VALUE) > 0) {
                gridTypeStr = "普通领地";
            } else if ((gridType & WarCellTagFlag.WCTF_WarGold_Mine_VALUE) > 0) {
                gridTypeStr = "远征币领地";
            } else if ((gridType & WarCellTagFlag.WCTF_OpenDoor_Mine_VALUE) > 0) {
                gridTypeStr = "圣水领地";
            } else if ((gridType & WarCellTagFlag.WCTF_HolyWater_Mine_VALUE) > 0) {
                gridTypeStr = "开门资源领地";
            } else if ((gridType & WarCellTagFlag.WCTF_Fortress_VALUE) > 0) {
                gridTypeStr = "要塞领地";
            }
            setGridType(gridTypeStr);
        }
    }
}
