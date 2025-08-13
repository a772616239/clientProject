package cfg;

import model.base.baseConfigObject;

public class MatchArenaSeasonConfigObject implements baseConfigObject {


    private int id;

    private long starttime;

    private long endtime;

    private int[][] timescope;

    private int[] crossrankingrewards;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setStarttime(long starttime) {

        this.starttime = starttime;

    }

    public long getStarttime() {

        return this.starttime;

    }


    public void setEndtime(long endtime) {

        this.endtime = endtime;

    }

    public long getEndtime() {

        return this.endtime;

    }


    public void setTimescope(int[][] timescope) {

        this.timescope = timescope;

    }

    public int[][] getTimescope() {

        return this.timescope;

    }


    public void setCrossrankingrewards(int[] crossrankingrewards) {

        this.crossrankingrewards = crossrankingrewards;

    }

    public int[] getCrossrankingrewards() {

        return this.crossrankingrewards;

    }


}
