package platform.logs.entity.thewar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class TheWarLevelUpTechLog extends AbstractPlayerLog {
    private String mapName;
    private int roleLevel;
    private String techType;
    private int level;

    public TheWarLevelUpTechLog(playerEntity player, String mapName, int techType, int level) {
        super(player);
        setRoleLevel(player.getLevel());
        setMapName(mapName);
        setLevel(level);
        switch (techType) {
            case 1 : setTechType("战士");break;
            case 2 : setTechType("坦克");break;
            case 3 : setTechType("辅助");break;
            case 4 : setTechType("远程");break;
            default : break;
        }
    }
}
