package cfg;

import java.util.ArrayList;
import java.util.List;
import model.base.baseConfigObject;
import protocol.TheWar.WarReward;

public class TheWarBattleRewardObject implements baseConfigObject {


    private int id;

    private List<WarReward> surrenderrewrad;

    private List<WarReward> failedrewrad;

    private List<WarReward> zerowinrewrad;

    private List<WarReward> onestarwinrewrad;

    private List<WarReward> twostarwinrewrad;

    private List<WarReward> threestarwinrewradwarrewrad;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setSurrenderrewrad(int[][] surrenderrewrad) {
        if (surrenderrewrad == null || surrenderrewrad.length <= 0) {
            return;
        }
        this.surrenderrewrad = new ArrayList<>();
        WarReward.Builder builder = WarReward.newBuilder();
        for (int i = 0; i < surrenderrewrad.length; i++) {
            if (surrenderrewrad[i] != null && surrenderrewrad[i].length >= 3) {
                builder.setRewardTypeValue(surrenderrewrad[i][0]);
                builder.setRewardId(surrenderrewrad[i][1]);
                builder.setRewardCount(surrenderrewrad[i][2]);
                this.surrenderrewrad.add(builder.build());
            }

        }
    }

    public List<WarReward> getSurrenderrewrad() {

        return this.surrenderrewrad;

    }


    public void setFailedrewrad(int[][] failedrewrad) {
        if (failedrewrad == null || failedrewrad.length <= 0) {
            return;
        }
        this.failedrewrad = new ArrayList<>();
        WarReward.Builder builder = WarReward.newBuilder();
        for (int i = 0; i < failedrewrad.length; i++) {
            if (failedrewrad[i] != null && failedrewrad[i].length >= 3) {
                builder.setRewardTypeValue(failedrewrad[i][0]);
                builder.setRewardId(failedrewrad[i][1]);
                builder.setRewardCount(failedrewrad[i][2]);
                this.failedrewrad.add(builder.build());
            }
        }
    }

    public List<WarReward> getFailedrewrad() {

        return this.failedrewrad;

    }


    public void setZerowinrewrad(int[][] zerowinrewrad) {
        if (zerowinrewrad == null || zerowinrewrad.length <= 0) {
            return;
        }
        this.zerowinrewrad = new ArrayList<>();
        WarReward.Builder builder = WarReward.newBuilder();
        for (int i = 0; i < zerowinrewrad.length; i++) {
            if (zerowinrewrad[i] != null && zerowinrewrad[i].length >= 3) {
                builder.setRewardTypeValue(zerowinrewrad[i][0]);
                builder.setRewardId(zerowinrewrad[i][1]);
                builder.setRewardCount(zerowinrewrad[i][2]);
                this.zerowinrewrad.add(builder.build());
            }
        }
    }

    public List<WarReward> getZerowinrewrad() {

        return this.zerowinrewrad;

    }


    public void setOnestarwinrewrad(int[][] onestarwinrewrad) {
        if (onestarwinrewrad == null || onestarwinrewrad.length <= 0) {
            return;
        }
        this.onestarwinrewrad = new ArrayList<>();
        WarReward.Builder builder = WarReward.newBuilder();
        for (int i = 0; i < onestarwinrewrad.length; i++) {
            if (onestarwinrewrad[i] != null && onestarwinrewrad[i].length >= 3) {
                builder.setRewardTypeValue(onestarwinrewrad[i][0]);
                builder.setRewardId(onestarwinrewrad[i][1]);
                builder.setRewardCount(onestarwinrewrad[i][2]);
                this.onestarwinrewrad.add(builder.build());
            }
        }
    }

    public List<WarReward> getOnestarwinrewrad() {

        return this.onestarwinrewrad;

    }


    public void setTwostarwinrewrad(int[][] twostarwinrewrad) {
        if (twostarwinrewrad == null || twostarwinrewrad.length <= 0) {
            return;
        }
        this.twostarwinrewrad = new ArrayList<>();
        WarReward.Builder builder = WarReward.newBuilder();
        for (int i = 0; i < twostarwinrewrad.length; i++) {
            if (twostarwinrewrad[i] != null && twostarwinrewrad[i].length >= 3) {
                builder.setRewardTypeValue(twostarwinrewrad[i][0]);
                builder.setRewardId(twostarwinrewrad[i][1]);
                builder.setRewardCount(twostarwinrewrad[i][2]);
                this.twostarwinrewrad.add(builder.build());
            }
        }
    }

    public List<WarReward> getTwostarwinrewrad() {

        return this.twostarwinrewrad;

    }


    public void setThreestarwinrewradwarrewrad(int[][] threestarwinrewradwarrewrad) {
        if (threestarwinrewradwarrewrad == null || threestarwinrewradwarrewrad.length <= 0) {
            return;
        }
        this.threestarwinrewradwarrewrad = new ArrayList<>();
        WarReward.Builder builder = WarReward.newBuilder();
        for (int i = 0; i < threestarwinrewradwarrewrad.length; i++) {
            if (threestarwinrewradwarrewrad[i] != null && threestarwinrewradwarrewrad[i].length >= 3) {
                builder.setRewardTypeValue(threestarwinrewradwarrewrad[i][0]);
                builder.setRewardId(threestarwinrewradwarrewrad[i][1]);
                builder.setRewardCount(threestarwinrewradwarrewrad[i][2]);
                this.threestarwinrewradwarrewrad.add(builder.build());
            }
        }
    }

    public List<WarReward> getThreestarwinrewradwarrewrad() {

        return this.threestarwinrewradwarrewrad;

    }


}
