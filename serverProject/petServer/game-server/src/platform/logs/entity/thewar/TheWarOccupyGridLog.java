package platform.logs.entity.thewar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class TheWarOccupyGridLog extends AbstractPlayerLog {
    private String mapName;
    private int roleLevel;
    private String gridType;
    private int gridLevel;
    private int posX;
    private int posY;
    private String ownerName;
    private boolean hasTrooped;

    public TheWarOccupyGridLog(playerEntity player, String mapName, String gridType, int gridLevel, int posX, int posY, String ownerName, boolean hasTrooped) {
        super(player);
        setMapName(mapName);
        setRoleLevel(player.getLevel());
        setGridType(gridType);
        setGridLevel(gridLevel);
        setPosX(posX);
        setPosY(posY);
        setOwnerName(ownerName);
        setHasTrooped(hasTrooped);
    }
}