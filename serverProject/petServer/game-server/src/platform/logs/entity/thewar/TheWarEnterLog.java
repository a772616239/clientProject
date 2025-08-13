package platform.logs.entity.thewar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class TheWarEnterLog extends AbstractPlayerLog {
    private int roleLevel;
    private String mapName;
    private boolean isEnter;
    private int jobTileLevel;

    public TheWarEnterLog(playerEntity player, String mapName, boolean isEnter, int jobTileLevel) {
        super(player);
        setRoleLevel(player.getLevel());
        setMapName(mapName);
        setEnter(isEnter);
        setJobTileLevel(jobTileLevel);
    }
}
