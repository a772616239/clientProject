package cfg;

import model.base.baseConfigObject;

public class MainLineNodeObject implements baseConfigObject {


    private int id;

    private int nodetype;

    private int[][] reward;

    private int[] rewarplot;

    private int fightmakeid;

    private int[] prevnodeid;

    private int[] afternodeid;

    private int param;

    private int[][] enhance;

    private int[][] weaken;

    private boolean onhookable;

    private int[][] onhookresourceoutput;

    private int[][] onhookrandompool;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setNodetype(int nodetype) {

        this.nodetype = nodetype;

    }

    public int getNodetype() {

        return this.nodetype;

    }


    public void setReward(int[][] reward) {

        this.reward = reward;

    }

    public int[][] getReward() {

        return this.reward;

    }


    public void setRewarplot(int[] rewarplot) {

        this.rewarplot = rewarplot;

    }

    public int[] getRewarplot() {

        return this.rewarplot;

    }


    public void setFightmakeid(int fightmakeid) {

        this.fightmakeid = fightmakeid;

    }

    public int getFightmakeid() {

        return this.fightmakeid;

    }


    public void setPrevnodeid(int[] prevnodeid) {

        this.prevnodeid = prevnodeid;

    }

    public int[] getPrevnodeid() {

        return this.prevnodeid;

    }


    public void setAfternodeid(int[] afternodeid) {

        this.afternodeid = afternodeid;

    }

    public int[] getAfternodeid() {

        return this.afternodeid;

    }


    public void setParam(int param) {

        this.param = param;

    }

    public int getParam() {

        return this.param;

    }


    public void setEnhance(int[][] enhance) {

        this.enhance = enhance;

    }

    public int[][] getEnhance() {

        return this.enhance;

    }


    public void setWeaken(int[][] weaken) {

        this.weaken = weaken;

    }

    public int[][] getWeaken() {

        return this.weaken;

    }


    public void setOnhookable(boolean onhookable) {

        this.onhookable = onhookable;

    }

    public boolean getOnhookable() {

        return this.onhookable;

    }


    public void setOnhookresourceoutput(int[][] onhookresourceoutput) {

        this.onhookresourceoutput = onhookresourceoutput;

    }

    public int[][] getOnhookresourceoutput() {

        return this.onhookresourceoutput;

    }


    public void setOnhookrandompool(int[][] onhookrandompool) {

        this.onhookrandompool = onhookrandompool;

    }

    public int[][] getOnhookrandompool() {

        return this.onhookrandompool;

    }

    /**
     * 返回当前节点是否是战斗节点
     * @return
     */
    public boolean isBattleNode() {
        return getNodetype() == 1 || getNodetype() == 2;
    }

}
