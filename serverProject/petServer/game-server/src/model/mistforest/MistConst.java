package model.mistforest;

import cfg.MistCommonConfig;
import cfg.MistCommonConfigObject;
import cfg.MistMonsterFightConfig;
import cfg.MistMonsterFightConfigObject;
import java.util.Map.Entry;
import model.player.util.PlayerUtil;

public class MistConst {
    public static MistCommonConfigObject getMistLevelCfgByPlayerLv(int playerLevel) {
        MistCommonConfigObject cfg;
        for (Entry<Integer, MistCommonConfigObject> entry : MistCommonConfig._ix_mistlevel.entrySet()) {
            cfg = entry.getValue();
            if (cfg.getEntrancelevelsection() == null) {
                continue;
            }
            if (cfg.getEntrancelevelsection().length < 2) {
                continue;
            }
            if (playerLevel >= cfg.getEntrancelevelsection()[0] && playerLevel < cfg.getEntrancelevelsection()[1]) {
                return cfg;
            }

        }
        return null;
    }

    public static int getPlayerBelongMistLv(String playerIdx) {
        MistCommonConfigObject mistLvCfg = getMistLevelCfgByPlayerLv(PlayerUtil.queryPlayerLv(playerIdx));
        return mistLvCfg == null ? 0 : mistLvCfg.getMistlevel();
    }

    public static int getPlayerLvBelongMistLv(int playerLv) {
        MistCommonConfigObject mistLvCfg = getMistLevelCfgByPlayerLv(playerLv);
        return mistLvCfg == null ? 0 : mistLvCfg.getMistlevel();
    }

    public static int getMonsterType(int monsterCfgId) {
        MistMonsterFightConfigObject cfg = MistMonsterFightConfig.getById(monsterCfgId);
        return cfg == null ? 0 : cfg.getMonstertype();
    }
}
