package cfg;

import model.base.baseConfigObject;
import org.apache.commons.lang.math.RandomUtils;
import protocol.TransServerCommon.MineConstructInfo;
import protocol.TransServerCommon.TypeOdds;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import static util.TimeUtil.getTodayStamp;


public class MineConstructConfigObject implements baseConfigObject {


    private int id;

    private List<MineConstructInfo> minetypedata;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setMinetype(int[][] minetype) {
        if (minetypedata == null) {
            minetypedata = new ArrayList<>();
        }
        for (int i = 0; i < minetype.length; i++) {
            if (minetype[i].length < 6) {
                throw new IndexOutOfBoundsException("MineConstructConfig type error, id=" + getId());
            }
            MineConstructInfo.Builder builder = MineConstructInfo.newBuilder();
            builder.setDailyStartTime(minetype[i][0] * TimeUtil.MS_IN_A_HOUR + minetype[i][1] * TimeUtil.MS_IN_A_MIN);
            builder.setDailyEndTime(minetype[i][2] * TimeUtil.MS_IN_A_HOUR + minetype[i][3] * TimeUtil.MS_IN_A_MIN);
            int weightNum = 0;
            for (int j = 4; j + 1 < minetype[i].length; j += 2) {
                TypeOdds.Builder builder1 = TypeOdds.newBuilder();
                builder1.setType(minetype[i][j]);
                builder1.setWeight(minetype[i][j + 1]);
                builder.addTypeOdds(builder1);
                weightNum += minetype[i][j + 1];
            }
            builder.setTotalWeight(weightNum);
            minetypedata.add(builder.build());
        }
    }

    public List<MineConstructInfo> getMinetypedata() {

        return this.minetypedata;

    }

    public int getResetType(long curTime) {
        long todayStamp = getTodayStamp(curTime);
        long dailyTimeStamp = curTime - todayStamp;
        for (int i = 0; i < minetypedata.size(); i++) {
            MineConstructInfo constructInfo = minetypedata.get(i);
            if (dailyTimeStamp < constructInfo.getDailyStartTime() || dailyTimeStamp >= constructInfo.getDailyEndTime()) {
                continue;
            }
            int randVal = RandomUtils.nextInt(constructInfo.getTotalWeight());
            int weight = 0;
            for (int j = 0; j < constructInfo.getTypeOddsList().size(); j++) {
                TypeOdds typeOdds = constructInfo.getTypeOddsList().get(j);
                weight += typeOdds.getWeight();
                if (randVal < weight) {
                    return typeOdds.getType();
                }
            }
            break;
        }
        LogUtil.error("ResetMineType error id = " + id);
        return 1; // 默认返回第1个类型
    }

}
