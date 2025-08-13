package cfg;

import model.base.baseConfigObject;
import model.pet.dbCache.petCache;
import protocol.Battle;
import util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class FightMakeObject implements baseConfigObject {


    private int id;

    private int type;

    private int rewardid;

    private long needfightpower;

    private int[][] enemydata;

    private int[][] monsterpropertyext;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setType(int type) {

        this.type = type;

    }

    public int getType() {

        return this.type;

    }


    public void setRewardid(int rewardid) {

        this.rewardid = rewardid;

    }

    public int getRewardid() {

        return this.rewardid;

    }


    public void setNeedfightpower(long needfightpower) {

        this.needfightpower = needfightpower;

    }

    public long getNeedfightpower() {

        return this.needfightpower;

    }


    public void setEnemydata(int[][] enemydata) {

        this.enemydata = enemydata;

    }

    public int[][] getEnemydata() {

        return this.enemydata;

    }


    public void setMonsterpropertyext(int[][] monsterpropertyext) {

        this.monsterpropertyext = monsterpropertyext;

    }

    public int[][] getMonsterpropertyext() {

        return this.monsterpropertyext;

    }

    /**
     * 计算fightMakeObject 敌方阵营战斗力
     * 1. 当needFightPower = -1 战斗校验时动态计算
     * 2. needFightPower = 0  敌方阵营固定,提前计算,然后赋值到needFightPower
     * 3. needFightPower >0  配置表有值得情况不再计算
     */
    public void calcFightPower() {
        if (getNeedfightpower() != 0) {
            return;
        }
        long sumFightPower = 0;
        List<Integer> petBookIdList = null;
        if (enemydata != null && enemydata.length > 0) {
            for (int i = 0; i < enemydata.length; i++) {
                if (enemydata[i] == null || enemydata[i].length < 3) {
                    continue;
                }
                if (petBookIdList == null) {
                    petBookIdList = new ArrayList<>();
                }
                petBookIdList.add(enemydata[i][0]);
                sumFightPower += petCache.getInstance().getBasicPetFightPower(enemydata[i][0], enemydata[i][1], enemydata[i][2]);
            }
        }
        sumFightPower += sumFightPower * petCache.getInstance().getBasicPetBonusRate(Battle.BattleSubTypeEnum.forNumber(getType()),petBookIdList, false) / 1000;
        setNeedfightpower(sumFightPower);
    }

    public long getDynamicFightPower(int level, int star) {
        try {
            if (level <= 0 || star <= 0) {
                return 0;
            }
            long sumFightPower = 0;
            List<Integer> petBookIdList = null;
            if (enemydata != null && enemydata.length > 0) {
                for (int i = 0; i < enemydata.length; i++) {
                    if (enemydata[i] == null || enemydata[i].length < 3) {
                        continue;
                    }
                    if (petBookIdList == null) {
                        petBookIdList = new ArrayList<>();
                    }
                    petBookIdList.add(enemydata[i][0]);
                    sumFightPower += petCache.getInstance().getBasicPetFightPower(enemydata[i][0], level, star);
                }
            }

            long bonusPower = petCache.getInstance().getBasicPetBonusRate(Battle.BattleSubTypeEnum.forNumber(getType()),petBookIdList, false);
            return sumFightPower + sumFightPower * bonusPower / 1000;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return 0;
        }
    }

}
