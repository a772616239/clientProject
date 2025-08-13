package cfg;

import model.base.baseConfigObject;

public class PollingActivityObject implements baseConfigObject {


    private int id;

    private int pollingtype;

    private int[] openday;

    private int opentime;

    private int durationtime;

    private int consumeid;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setPollingtype(int pollingtype) {

        this.pollingtype = pollingtype;

    }

    public int getPollingtype() {

        return this.pollingtype;

    }


    public void setOpenday(int[] openday) {

        this.openday = openday;

    }

    public int[] getOpenday() {

        return this.openday;

    }


    public void setOpentime(int opentime) {

        this.opentime = opentime;

    }

    public int getOpentime() {

        return this.opentime;

    }


    public void setDurationtime(int durationtime) {

        this.durationtime = durationtime;

    }

    public int getDurationtime() {

        return this.durationtime;

    }


    public void setConsumeid(int consumeid) {

        this.consumeid = consumeid;

    }

    public int getConsumeid() {

        return this.consumeid;

    }


}
