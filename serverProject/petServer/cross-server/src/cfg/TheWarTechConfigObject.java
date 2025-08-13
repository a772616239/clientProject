package cfg;

import model.base.baseConfigObject;

public class TheWarTechConfigObject implements baseConfigObject {


    private int id;

    private int race;

    private int level;

    private int[] basebuffid;

    private int[][] skilllist;

    private int needquality;

    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setRace(int race) {

        this.race = race;

    }

    public int getRace() {

        return this.race;

    }


    public void setLevel(int level) {

        this.level = level;

    }

    public int getLevel() {

        return this.level;

    }


    public void setBasebuffid(int[] basebuffid) {

        this.basebuffid = basebuffid;

    }

    public int[] getBasebuffid() {

        return this.basebuffid;

    }


    public void setSkilllist(int[][] skilllist) {

        this.skilllist = skilllist;

    }

    public int[][] getSkilllist() {

        return this.skilllist;

    }

    public void setNeedquality(int needquality) {

        this.needquality = needquality;

    }

    public int getNeedquality() {

        return this.needquality;

    }

}
