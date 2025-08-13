package cfg;

import model.base.baseConfigObject;

public class TheWarGridStationConfigObject implements baseConfigObject {


    private int id;

    private int group;

    private int quality;

    private int wargoldplus;

    private int holywaterplus;

    private int dpplus;

    private int commonbuffid;

    private int[][] racebuffid;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setGroup(int group) {

        this.group = group;

    }

    public int getGroup() {

        return this.group;

    }


    public void setQuality(int quality) {

        this.quality = quality;

    }

    public int getQuality() {

        return this.quality;

    }


    public void setWargoldplus(int wargoldplus) {

        this.wargoldplus = wargoldplus;

    }

    public int getWargoldplus() {

        return this.wargoldplus;

    }


    public void setHolywaterplus(int holywaterplus) {

        this.holywaterplus = holywaterplus;

    }

    public int getHolywaterplus() {

        return this.holywaterplus;

    }


    public void setDpplus(int dpplus) {

        this.dpplus = dpplus;

    }

    public int getDpplus() {

        return this.dpplus;

    }


    public void setCommonbuffid(int commonbuffid) {

        this.commonbuffid = commonbuffid;

    }

    public int getCommonbuffid() {

        return this.commonbuffid;

    }


    public void setRacebuffid(int[][] racebuffid) {

        this.racebuffid = racebuffid;

    }

    public int[][] getRacebuffid() {

        return this.racebuffid;

    }


}
