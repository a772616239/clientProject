package cfg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import model.base.baseConfigObject;
import petrobot.util.LogUtil;

public class TheWarSeasonConfigObject implements baseConfigObject {


    private int id;

    private String openmapname;

    private long startplaytime;

    private long endplaytime;

    private int[] missions;

    private Map<Integer, Integer> seasoncamprankreward;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setOpenmapname(String openmapname) {

        this.openmapname = openmapname;

    }

    public String getOpenmapname() {

        return this.openmapname;

    }


    public void setStartplaytime(String startplaytime) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        try {
            Date time = sdf.parse(startplaytime);
            this.startplaytime = time.getTime();
        } catch (ParseException e) {
            LogUtil.printStackTrace(e);
        }

    }

    public long getStartplaytime() {

        return this.startplaytime;

    }


    public void setEndplaytime(String endplaytime) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        try {
            Date time = sdf.parse(endplaytime);
            this.endplaytime = time.getTime();
        } catch (ParseException e) {
            LogUtil.printStackTrace(e);
        }

    }

    public long getEndplaytime() {

        return this.endplaytime;

    }


    public void setMissions(int[] missions) {

        this.missions = missions;

    }

    public int[] getMissions() {

        return this.missions;

    }


    public void setSeasoncamprankreward(int[][] seasoncamprankreward) {
        if (seasoncamprankreward == null || seasoncamprankreward.length <= 0) {
            return;
        }
        this.seasoncamprankreward = new HashMap<>();
        for (int i = 0; i < seasoncamprankreward.length; i++) {
            if (seasoncamprankreward[i] == null || seasoncamprankreward[i].length < 2) {
                continue;
            }
            this.seasoncamprankreward.put(seasoncamprankreward[i][0], seasoncamprankreward[i][1]);
        }

    }

    public Map<Integer, Integer> getSeasoncamprankreward() {

        return this.seasoncamprankreward;

    }


}
