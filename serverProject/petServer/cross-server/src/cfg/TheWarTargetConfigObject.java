package cfg;

import java.util.ArrayList;
import java.util.List;
import model.base.baseConfigObject;
import protocol.Common.Reward;
import protocol.TheWar.WarReward;

public class TheWarTargetConfigObject implements baseConfigObject {


    private int id;

    private int missiontype;

    private int addtion;

    private int targetcount;

    private List<Reward> finishnormalreward;

    private List<WarReward> finishwarreward;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setMissiontype(int missiontype) {

        this.missiontype = missiontype;

    }

    public int getMissiontype() {

        return this.missiontype;

    }


    public void setAddtion(int addtion) {

        this.addtion = addtion;

    }

    public int getAddtion() {

        return this.addtion;

    }


    public void setTargetcount(int targetcount) {

        this.targetcount = targetcount;

    }

    public int getTargetcount() {

        return this.targetcount;

    }


    public void setFinishnormalreward(int[][] finishnormalreward) {
        if (finishnormalreward == null || finishnormalreward.length <= 0) {
            return;
        }
        this.finishnormalreward = new ArrayList<>();
        Reward.Builder builder = Reward.newBuilder();
        for (int i = 0; i < finishnormalreward.length; i++) {
            if (finishnormalreward[i] != null && finishnormalreward[i].length >= 3) {
                builder.setRewardTypeValue(finishnormalreward[i][0]);
                builder.setId(finishnormalreward[i][1]);
                builder.setCount(finishnormalreward[i][2]);
                this.finishnormalreward.add(builder.build());
            }
        }
    }

    public List<Reward> getFinishnormalreward() {

        return this.finishnormalreward;

    }


    public void setFinishwarreward(int[][] finishwarreward) {
        if (finishwarreward == null || finishwarreward.length <= 0) {
            return;
        }
        this.finishwarreward = new ArrayList<>();
        WarReward.Builder builder = WarReward.newBuilder();
        for (int i = 0; i < finishwarreward.length; i++) {
            if (finishwarreward[i] != null && finishwarreward[i].length >= 3) {
                builder.setRewardTypeValue(finishwarreward[i][0]);
                builder.setRewardId(finishwarreward[i][1]);
                builder.setRewardCount(finishwarreward[i][2]);
                this.finishwarreward.add(builder.build());
            }
        }
    }

    public List<WarReward> getFinishwarreward() {

        return this.finishwarreward;

    }


}
