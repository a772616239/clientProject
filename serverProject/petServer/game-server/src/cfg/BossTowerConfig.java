/*CREATED BY TOOL*/

package cfg;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import protocol.BossTower.EnumBossTowerDifficult;

@annationInit(value ="BossTowerConfig", methodname = "initConfig")
public class BossTowerConfig extends baseConfig<BossTowerConfigObject>{


private static BossTowerConfig instance = null;

public static BossTowerConfig getInstance() {

if (instance == null)
instance = new BossTowerConfig();
return instance;

}


public static Map<Integer, BossTowerConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (BossTowerConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"BossTowerConfig");

for(Map e:ret)
{
put(e);
}

}

public static BossTowerConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, BossTowerConfigObject config){

config.setId(MapHelper.getInt(e, "ID"));

config.setUnlevel(MapHelper.getInt(e, "unLevel"));

config.setFightmakeid(MapHelper.getInt(e, "fightMakeId"));

config.setDifficultfightmakeid(MapHelper.getInt(e, "difficultFightMakeId"));

config.setUnbeatablefirstreward(MapHelper.getInt(e, "unbeatableFirstReward"));

config.setUnbeatablefightmakeid(MapHelper.getInt(e, "unbeatableFightMakeId"));

config.setPasslimit(MapHelper.getInt(e, "passLimit"));

config.setCommonrandom(MapHelper.getIntArray(e, "commonRandom"));

config.setDifficultrandom(MapHelper.getIntArray(e, "difficultRandom"));

config.setUnbeatablerandom(MapHelper.getIntArray(e, "unbeatableRandom"));


_ix_id.put(config.getId(),config);

putToFightMake(config);


}



private static final Set<Integer> TOTAL_FIGHT_MAKE = new HashSet<>();

private void putToFightMake(BossTowerConfigObject config) {
    if (config == null) {
        return;
    }

    TOTAL_FIGHT_MAKE.add(config.getFightmakeid());
    TOTAL_FIGHT_MAKE.add(config.getDifficultfightmakeid());
    TOTAL_FIGHT_MAKE.add(config.getUnbeatablefightmakeid());
}

public static boolean fightMakeExist(int fightMake) {
    return TOTAL_FIGHT_MAKE.contains(fightMake);
}

public static EnumBossTowerDifficult getDifficult(int towerLv, int fightMakeId) {
    return getFightMakeDiff(getById(towerLv), fightMakeId);
}

public static EnumBossTowerDifficult getFightMakeDiff(BossTowerConfigObject cfg, int fightMakeId) {
    if (cfg != null) {
        if (cfg.getFightmakeid() == fightMakeId) {
            return EnumBossTowerDifficult.EBS_Common;

        } else if (cfg.getDifficultfightmakeid() == fightMakeId) {
            return EnumBossTowerDifficult.EBS_Difficult;

        } else if (cfg.getUnbeatablefightmakeid() == fightMakeId) {
            return EnumBossTowerDifficult.EBS_Unbeatable;
        }
    }
    return EnumBossTowerDifficult.EBS_Null;
}

/**
 * 根据fightMakeId获取对用的随机奖励数组
 *
 * @param towerLv
 * @param fightMakeId
 * @return
 */
public static int[][] getRandomRewardArray(int towerLv, int fightMakeId) {
    BossTowerConfigObject object = getById(towerLv);
    if (object != null) {
        if (object.getFightmakeid() == fightMakeId) {
            return object.getCommonrandom();

        } else if (object.getDifficultfightmakeid() == fightMakeId) {
            return object.getDifficultrandom();

        } else if (object.getUnbeatablefightmakeid() == fightMakeId) {
            return object.getUnbeatablerandom();
        }
    }
    return null;
}

public static BossTowerConfigObject getMaxUnlockLvCfg(int playerLv) {
    return _ix_id.values().stream()
            .filter(e -> playerLv >= e.getUnlevel())
            .max(Comparator.comparingInt(BossTowerConfigObject::getId))
            .orElse(null);
}

public static int getMaxUnlockLv(int playerLv) {
    BossTowerConfigObject maxUnlockLvCfg = getMaxUnlockLvCfg(playerLv);
    return maxUnlockLvCfg == null ? 0 : maxUnlockLvCfg.getId();
}

public static int getFirstReward(int cfgId){
    BossTowerConfigObject byId = getById(cfgId);
    if(byId != null){
        return byId.getUnbeatablefirstreward();
    }
    return 0;
}
}
