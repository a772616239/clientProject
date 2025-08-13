package platform.logs.entity.thewar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class TheWarAttackGridLog extends AbstractPlayerLog {
    private String mapName;
    private int roleLevel;
    private String gridType;
    private int gridLevel;
    private String posX;
    private String posY;
    private String ownerName;
    private String hasTrooped;
    private String attackResult;

    public TheWarAttackGridLog(playerEntity player, String mapName, String gridType, int gridLevel, String posX, String posY, String ownerName, String hasTrooped, String attackResult) {
        super(player);
        setMapName(mapName);
        setRoleLevel(player.getLevel());
        setGridType(gridType);
        setGridLevel(gridLevel);
        setPosX(posX);
        setPosY(posY);
        setOwnerName(ownerName);
        setHasTrooped(hasTrooped);
        setAttackResult(attackResult);
    }
}
