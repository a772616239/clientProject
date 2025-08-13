package model.activityboss.entity;

import entity.CommonResult;

public class ActivityBossEnterResult extends CommonResult {
    /**
     * 战斗id
     */
    private int fightMakeId;

    /**
     * 敌方buff id
     */
    private int enemyBuffId;

    public int getFightMakeId() {
        return fightMakeId;
    }

    public void setFightMakeId(int fightMakeId) {
        this.fightMakeId = fightMakeId;
    }

    public int getEnemyBuffId() {
        return enemyBuffId;
    }

    public void setEnemyBuffId(int enemyBuffId) {
        this.enemyBuffId = enemyBuffId;
    }
}
