package cfg;

import model.base.baseConfigObject;
import protocol.Common.Reward;

import java.util.ArrayList;
import java.util.List;

public class MineBaseConfigObject implements baseConfigObject {


    private int type;

    private int quality;

    private int level;

    private int refreshtime;

    private int needexploittime;

    private int decexploittime;

    private int pvefightmakeid;

    private int pvpfightmakeid;

    private List<Reward> rewardlist;


    public void setType(int type) {

        this.type = type;

    }

    public int getType() {

        return this.type;

    }


    public void setQuality(int quality) {

        this.quality = quality;

    }

    public int getQuality() {

        return this.quality;

    }


    public void setLevel(int level) {

        this.level = level;

    }

    public int getLevel() {

        return this.level;

    }


    public void setRefreshtime(int refreshtime) {

        this.refreshtime = refreshtime;

    }

    public int getRefreshtime() {

        return this.refreshtime;

    }

    public void setNeedexploittime(int needexploittime) {

        this.needexploittime = needexploittime;

    }

    public int getNeedexploittime() {

        return this.needexploittime;

    }


    public void setDecexploittime(int decexploittime) {

        this.decexploittime = decexploittime;

    }

    public int getDecexploittime() {

        return this.decexploittime;

    }

    public void setPvefightmakeid(int pvefightmakeid) {

        this.pvefightmakeid = pvefightmakeid;

    }

    public int getPvefightmakeid() {

        return this.pvefightmakeid;

    }


    public void setPvpfightmakeid(int pvpfightmakeid) {

        this.pvpfightmakeid = pvpfightmakeid;

    }

    public int getPvpfightmakeid() {

        return this.pvpfightmakeid;

    }


    public void setRewardlist(int[][] rewardlistArr) {
        if (rewardlist == null) {
            rewardlist = new ArrayList<>();
        }
        for (int i = 0; i < rewardlistArr.length; i++) {
            if (rewardlistArr[i].length < 3) {
                throw new IndexOutOfBoundsException("MineBaseConfig reward error type=" + type);
            } else {
                Reward.Builder builder = Reward.newBuilder();
                builder.setRewardTypeValue(rewardlistArr[i][0]);
                builder.setId(rewardlistArr[i][1]);
                builder.setCount(rewardlistArr[i][2]);
                rewardlist.add(builder.build());
            }
        }

    }

    public List<Reward> getRewardlist() {

        return this.rewardlist;

    }

    public Reward getRewardByIndex(int rewardIndex) {
        if (rewardIndex < 0 || rewardIndex >= rewardlist.size()) {
            return null;
        }
        return rewardlist.get(rewardIndex);
    }


}
