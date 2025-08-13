package cfg;

import java.util.ArrayList;
import java.util.List;
import model.base.baseConfigObject;
import protocol.TheWar.WarReward;

public class TheWarPersueConfigObject implements baseConfigObject {


    private int roomlevel;

    private int rewardstamina;

    private int[][] warrewrad;


    public void setRoomlevel(int roomlevel) {

        this.roomlevel = roomlevel;

    }

    public int getRoomlevel() {

        return this.roomlevel;

    }


    public void setRewardstamina(int rewardstamina) {

        this.rewardstamina = rewardstamina;

    }

    public int getRewardstamina() {

        return this.rewardstamina;

    }


    public void setWarrewrad(int[][] warrewrad) {

        this.warrewrad = warrewrad;

    }

    public List<WarReward> getWarrewrad() {
        if (warrewrad == null) {
            return null;
        }
        WarReward.Builder reward = WarReward.newBuilder();
        List<WarReward> warRewardList = new ArrayList<>();
        for (int i = 0; i < warrewrad.length; i++) {
            if (warrewrad[i] == null || warrewrad[i].length < 3) {
                continue;
            }
            reward.setRewardTypeValue(warrewrad[i][0]);
            reward.setRewardId(warrewrad[i][1]);
            reward.setRewardCount(warrewrad[i][2]);
            warRewardList.add(reward.build());
        }
        return warRewardList;
    }
}
