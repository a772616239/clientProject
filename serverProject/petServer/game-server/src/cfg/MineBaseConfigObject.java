package cfg;

import model.base.baseConfigObject;

public class MineBaseConfigObject implements baseConfigObject {


    private int type;

    private int quality;

    private int level;

    private int refreshtime;

    private int needexploittime;

    private int decexploittime;

    private int pvefightmakeid;

    private int pvpfightmakeid;

    private int[] dailyminerewardlist;

    private int[][] normalrewardlist;

    private int[] defendfailreward;


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


    public void setDailyminerewardlist(int[] dailyminerewardlist) {

        this.dailyminerewardlist = dailyminerewardlist;

    }

    public int[] getDailyminerewardlist() {

        return this.dailyminerewardlist;

    }


    public void setNormalrewardlist(int[][] normalrewardlist) {

        this.normalrewardlist = normalrewardlist;

    }

    public int[][] getNormalrewardlist() {

        return this.normalrewardlist;

    }


    public void setDefendfailreward(int[] defendfailreward) {

        this.defendfailreward = defendfailreward;

    }

    public int[] getDefendfailreward() {

        return this.defendfailreward;

    }


}
