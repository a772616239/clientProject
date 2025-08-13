package cfg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import model.base.baseConfigObject;
import util.LogUtil;
import util.TimeUtil;

public class MistTimeLimitActivityObject implements baseConfigObject {


    private int id;

    private long starttime;

    private long endtime;

    private int activitytype;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setStarttime(String starttime) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(TimeUtil.defaultTimeZone));
        try {
            Date time = sdf.parse(starttime);
            this.starttime = time.getTime();
        } catch (ParseException e) {
            LogUtil.printStackTrace(e);
        }

    }

    public long getStarttime() {

        return this.starttime;

    }


    public void setEndtime(String endtime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(TimeUtil.defaultTimeZone));
        try {
            Date time = sdf.parse(endtime);
            this.endtime = time.getTime();
        } catch (ParseException e) {
            LogUtil.printStackTrace(e);
        }
    }

    public long getEndtime() {

        return this.endtime;

    }


    public void setActivitytype(int activitytype) {

        this.activitytype = activitytype;

    }

    public int getActivitytype() {

        return this.activitytype;

    }


}
