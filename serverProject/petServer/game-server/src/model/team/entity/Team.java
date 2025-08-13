package model.team.entity;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import protocol.PrepareWar;

@Data
public class Team implements Serializable {
    private static final long serialVersionUID = -4533542353038651129L;

    public Team() {
        teamNum = PrepareWar.TeamNumEnum.TNE_Team_Null;
        teamName = "";
        linkPetMap = new ConcurrentHashMap<>();
        linkSkillMap = new ConcurrentHashMap<>();
        isLock = false;
    }

    /**
     * 小队编号
     */
    private PrepareWar.TeamNumEnum teamNum;
    /**
     * 小队名
     */
    private String teamName;
    /**
     * 位置对应宠物
     */
    private Map<Integer, String> linkPetMap;
    /**
     * 位置对应的技能
     */
    private Map<Integer, Integer> linkSkillMap;
    /**
     * 矿区小队使用,上锁后不能编辑
     */
    private boolean isLock;

    public void putLinkSkill(int position, int skillId) {
        this.linkSkillMap.put(position, skillId);
    }

    public void clearLinkPet() {
        this.linkPetMap.clear();
    }

    public void clearLinkSkill() {
        this.linkSkillMap.clear();
    }

    public Team putAllLinkPet(Map<Integer, String> selectPetsMap) {
        if (CollectionUtils.isEmpty(selectPetsMap)) {
            return this;
        }
        selectPetsMap.forEach((k, v) -> this.linkPetMap.put(k, v));
        return this;
    }

    public void removeLinkPet(int position) {
        this.linkPetMap.remove(position);
    }

    public int getLinkPetCount() {
        return this.linkPetMap.size();
    }

    public int getTeamNumValue() {
        return this.getTeamNum().getNumber();
    }

    public int getLinkSkillCount() {
        return this.getLinkSkillMap().size();
    }

    public void putLinkPet(int position, String petIdx) {
        this.linkPetMap.put(position, petIdx);
    }

    public void removeLinkSkill(int key) {
        this.linkSkillMap.remove(key);
    }
}
