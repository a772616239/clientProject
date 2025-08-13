package cfg;

import model.base.baseConfigObject;

public class MistSeasonConfigObject implements baseConfigObject {


    private int id;

    private long starttime;

    private long endtime;


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
}
