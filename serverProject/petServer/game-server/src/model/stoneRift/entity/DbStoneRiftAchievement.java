package model.stoneRift.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DbStoneRiftAchievement implements Serializable {
    private static final long serialVersionUID = 8247277284605933886L;
    private Set<Integer> claimedIds = new HashSet<>();
    private Set<Integer> completeAchievementIds = new HashSet<>();

    public Set<Integer> getClaimedIds() {
        return claimedIds;
    }

    public void setClaimedIds(Set<Integer> claimedIds) {
        this.claimedIds = claimedIds;
    }

    public Set<Integer> getCompleteAchievementIds() {
        return completeAchievementIds;
    }

    public void setCompleteAchievementIds(Set<Integer> completeAchievementIds) {
        this.completeAchievementIds = completeAchievementIds;
    }
}
