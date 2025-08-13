package cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfigObject;
import protocol.TransServerCommon.MistDailyInfo;
import util.LogUtil;
import util.TimeUtil;

public class MistDailyObjConfigObject implements baseConfigObject {


    private int id;

    private int maprule;

    private int maplevel;

    private int objtype;

    private List<MistDailyInfo> createtimedata;

    private Map<Integer, Long> initprop;

    private int[] initRandProp;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setMaprule(int maprule) {

        this.maprule = maprule;

    }

    public int getMaprule() {

        return this.maprule;

    }


    public void setMaplevel(int maplevel) {

        this.maplevel = maplevel;

    }

    public int getMaplevel() {

        return this.maplevel;

    }


    public void setObjtype(int objtype) {

        this.objtype = objtype;

    }

    public int getObjtype() {

        return this.objtype;

    }


    public void setCreatetimedata(int[][] createtimedata) {
        this.createtimedata = new ArrayList<>();
        long beginTime;
        long endTime;
        long interval;
        int maxObjCount;
        long tmpTime = 0;
        for (int i = 0; i < createtimedata.length; ++i) {
            if (createtimedata[i].length < 6) {
                throw new IndexOutOfBoundsException();
            }
            beginTime = createtimedata[i][0] * TimeUtil.MS_IN_A_HOUR + createtimedata[i][1] * TimeUtil.MS_IN_A_MIN;
            endTime = createtimedata[i][2] * TimeUtil.MS_IN_A_HOUR + createtimedata[i][3] * TimeUtil.MS_IN_A_MIN;
            interval = createtimedata[i][4] * TimeUtil.MS_IN_A_S;
            maxObjCount = createtimedata[i][5];
            if (beginTime > endTime) {
                throw new RuntimeException("MistDailyTime Error type 0,index=" + i);
            }
            if (endTime - beginTime < interval) {
                LogUtil.warn("MistDailyTime Error type 1,index=" + i);
            }
            if (tmpTime > 0 && tmpTime > beginTime) {
                throw new RuntimeException("MistDailyTime Error type 2,index=" + i);
            }
            if (interval <= 0) {
                continue;
            }
            tmpTime = beginTime;
            do {
                MistDailyInfo.Builder builder = MistDailyInfo.newBuilder();
                builder.setCreateTime(tmpTime);
                builder.setMaxObjCount(maxObjCount);
                this.createtimedata.add(builder.build());
                tmpTime += interval;
            } while (tmpTime < endTime);

            tmpTime = endTime;
        }


    }

    public List<MistDailyInfo> getCreatetimedata() {

        return this.createtimedata;

    }


    public void setInitprop(int[][] initprop) {

        if (this.initprop == null) {
            this.initprop = new HashMap<>();
        }
        for (int i = 0; i < initprop.length; ++i) {
            if (initprop[i].length < 2) {
                throw new IndexOutOfBoundsException();
            } else {
                this.initprop.put(initprop[i][0], (long) initprop[i][1]);
            }
        }

    }

    public Map<Integer, Long> getInitprop() {

        return this.initprop;

    }


    public void setInitRandProp(int[] initrandprop) {
        this.initRandProp = initrandprop;
    }

    public int[] getInitRandProp() {

        return this.initRandProp;

    }

    // 生成下次创建时间
    public long generateNextCreateTime(long curTime) {
        if (getCreatetimedata().isEmpty()) {
            return 0;
        }
        long tmpTime = 0;
        long todayBeginTime = TimeUtil.getTodayStamp(curTime);
        for (MistDailyInfo cfg : getCreatetimedata()) {
            long createTime = todayBeginTime + cfg.getCreateTime();
            if (curTime > createTime) {
                continue;
            }
            if (tmpTime == 0 || tmpTime > createTime) {
                tmpTime = createTime;
            }
        }

        if (tmpTime == 0) {
            tmpTime = TimeUtil.getNextDayStamp(curTime) + getCreatetimedata().get(0).getCreateTime();
        }
        return tmpTime;
    }

    // 获取当前可创建每日对象的最大数量
    public int getCurMaxDailyObjCount(long curTime) {
        if (getCreatetimedata().isEmpty()) {
            return 0;
        }
        int maxObjCount = 0;
        long todayBeginTime = TimeUtil.getTodayStamp(curTime);
        for (MistDailyInfo cfg : getCreatetimedata()) {
            long createTime = todayBeginTime + cfg.getCreateTime();
            if (curTime <= createTime) {
                maxObjCount = cfg.getMaxObjCount();
                break;
            }
        }
        return maxObjCount;
    }

}
