package model.thewar.warmap.grid;

import cfg.TheWarConstConfig;
import common.GameConst;
import common.GlobalTick;
import java.util.List;
import java.util.Map.Entry;
import model.thewar.warplayer.entity.WarPlayer;
import protocol.Battle.BattleRemainPet;
import protocol.TheWarDB.GridCacheData;
import protocol.TheWarDefine.TheWarCellPropertyEnum;

public class BossGrid extends FootHoldGrid {
    long lastRecoverHpTime;

    public long getLastRecoverHpTime() {
        return lastRecoverHpTime;
    }

    public void setLastRecoverHpTime(long lastRecoverHpTime) {
        this.lastRecoverHpTime = lastRecoverHpTime;
    }

    public void recoverPetHp() {
        // 玩家占据的格子血量回复通过玩家逻辑回复
        if (getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) > 0) {
            return;
        }
        int curHp = TheWarConstConfig.getById(GameConst.ConfigId).getPetrecoverrate();
        for (Entry<String, Integer> entry : monsterPetHpMap.entrySet()) {
            curHp = Math.min(1000, entry.getValue() + curHp);
            monsterPetHpMap.put(entry.getKey(), entry.getValue());
        }
        setLastRecoverHpTime(GlobalTick.getInstance().getCurrentTime());
    }

    @Override
    public void settleBattle(WarPlayer warPlayer, boolean attackResult, List<BattleRemainPet> remainPets, int fightStar) {
        if (attackResult) {
            bossUnlockTargetPos();
        }
        super.settleBattle(warPlayer, attackResult, remainPets, fightStar);
    }

    @Override
    public GridCacheData.Builder buildGridCacheBuilder() {
        gridCacheData = super.buildGridCacheBuilder();
        gridCacheData.setLastRecoverHpTime(lastRecoverHpTime);
        return gridCacheData;
    }

    @Override
    public void parseFromCacheData(GridCacheData cacheData) {
        super.parseFromCacheData(cacheData);
        lastRecoverHpTime = cacheData.getLastRecoverHpTime();
    }
}
