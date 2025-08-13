package model.cp.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class CpTeamPublish implements Serializable {
    private static final long serialVersionUID = 5298166208775781216L;
    private int teamId;
    private String teamName;
    private List<String> members = new ArrayList<>();
    private long needAbility;
    private String leaderIdx;
    private long leaderAbility;
    private int teamLv;
    private boolean activeCopy;
    private Map<String, Long> playerAbility = new HashMap<>();
    private boolean autoJoin;

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public Map<String, Long> getPlayerAbility() {
        return playerAbility;
    }

    public void setPlayerAbility(Map<String, Long> playerAbility) {
        this.playerAbility = playerAbility;
    }

    public void addMember(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }
        this.members.add(playerIdx);
    }

    public int getMemberSize() {
        return this.members.size();
    }

    public void removeMember(String playerIdx) {
        members.remove(playerIdx);
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public long getNeedAbility() {
        return needAbility;
    }

    public void setNeedAbility(long needAbility) {
        this.needAbility = needAbility;
    }

    public String getLeaderIdx() {
        return leaderIdx;
    }

    public void setLeaderIdx(String leaderIdx) {
        this.leaderIdx = leaderIdx;
    }

    public long getLeaderAbility() {
        return leaderAbility;
    }

    public void setLeaderAbility(long leaderAbility) {
        this.leaderAbility = leaderAbility;
    }

    public int getTeamLv() {
        return teamLv;
    }

    public void setTeamLv(int teamLv) {
        this.teamLv = teamLv;
    }

    public boolean isActiveCopy() {
        return activeCopy;
    }

    public void setActiveCopy(boolean activeCopy) {
        this.activeCopy = activeCopy;
    }

    public void putAbility(String playerIdx, long ability) {
        playerAbility.put(playerIdx, ability);
    }


    public long getPlayerAbility(String playerIdx) {
        return this.playerAbility.get(playerIdx);
    }
}
