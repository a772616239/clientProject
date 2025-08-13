package platform.logs.entity.thewar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class TheWarJobTileLog extends AbstractPlayerLog {
    private String mapName;
    private int roleLevel;
    private int jobTileLevel;

    public TheWarJobTileLog(playerEntity player, String mapName, int jobTileLevel) {
        super(player);
        setMapName(mapName);
        setRoleLevel(player.getLevel());
        setJobTileLevel(jobTileLevel);
    }
}
