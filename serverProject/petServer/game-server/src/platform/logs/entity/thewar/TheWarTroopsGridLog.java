package platform.logs.entity.thewar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class TheWarTroopsGridLog extends AbstractPlayerLog {
    private String mapName;
    private int roleLevel;
    private String gridType;
    private int gridLevel;

    public TheWarTroopsGridLog(playerEntity player, String mapName, String gridType, int gridLevel) {
        super(player);
        setRoleLevel(player.getLevel());
        setMapName(mapName);
        setGridType(gridType);
        setGridLevel(gridLevel);
    }
}
