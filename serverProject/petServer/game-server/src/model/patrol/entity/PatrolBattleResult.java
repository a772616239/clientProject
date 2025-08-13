package model.patrol.entity;

import entity.CommonResult;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import protocol.Battle;
import protocol.Battle.ExtendProperty;

/**
 * @author xiao_FL
 * @date 2019/8/7
 */

@Getter
@Setter
public class PatrolBattleResult extends CommonResult {
    /**
     * 战斗id
     */
    private int makeId;

    /**
     * 增益/减益效果
     */
    private List<Battle.PetBuffData> buffList;

    /**
     * 敌方增强
     */
    private List<Battle.PetBuffData> debuffList;

    private ExtendProperty monsterExProperty;

    public int getMakeId() {
        return makeId;
    }

    public void setMakeId(int makeId) {
        this.makeId = makeId;
    }

    public List<Battle.PetBuffData> getBuffList() {
        return buffList;
    }

    public void setBuffList(List<Battle.PetBuffData> buffList) {
        this.buffList = buffList;
    }

    public List<Battle.PetBuffData> getDebuffList() {
        return debuffList;
    }

    public void setDebuffList(List<Battle.PetBuffData> debuffList) {
        this.debuffList = debuffList;
    }

    public void addBuff(Battle.PetBuffData buff) {
        if (buffList == null) {
            buffList = new ArrayList<>();
        }
        buffList.add(buff);
    }

    public void adddebuff(Battle.PetBuffData debuff) {
        if (debuffList == null) {
            debuffList = new ArrayList<>();
        }
        debuffList.add(debuff);
    }
}
