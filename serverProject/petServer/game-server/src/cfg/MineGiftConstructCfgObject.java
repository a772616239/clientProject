package cfg;

import model.base.baseConfigObject;
import org.apache.commons.lang.math.RandomUtils;
import protocol.TransServerCommon.MineGiftInfo;
import protocol.TransServerCommon.TypeOdds;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class MineGiftConstructCfgObject implements baseConfigObject {


    private int id;

    private int refreshtime;

    private List<MineGiftInfo> giftlistdata;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setRefreshtime(int refreshtime) {

        this.refreshtime = refreshtime;

    }

    public int getRefreshtime() {

        return this.refreshtime;

    }


    public void setGiftlist(int[][] giftlist) {
        if (giftlistdata == null) {
            giftlistdata = new ArrayList<>();
        }
        for (int i = 0; i < giftlist.length; i++) {
            if (giftlist[i].length < 7) {
                throw new IndexOutOfBoundsException("MineGiftList config error, id=" + getId());
            }
            MineGiftInfo.Builder builder = MineGiftInfo.newBuilder();
            builder.setDailyStartTime(giftlist[i][0] * TimeUtil.MS_IN_A_HOUR + giftlist[i][1] + TimeUtil.MS_IN_A_MIN);
            builder.setDailyEndTime(giftlist[i][1]);
            builder.setGenerateOdds(giftlist[i][2]);
            int weightNum = 0;
            for (int j = 5; j + 1 < giftlist[i].length; j += 2) {
                TypeOdds.Builder builder1 = TypeOdds.newBuilder();
                builder1.setType(giftlist[i][j]);
                builder1.setWeight(giftlist[i][j + 1]);
                builder.addTypeOdds(builder1);
                weightNum += giftlist[i][j + 1];
            }
            builder.setTotalWeight(weightNum);
            giftlistdata.add(builder.build());
        }
    }

    public List<MineGiftInfo> getGiftlist() {

        return this.giftlistdata;

    }

    public int getGenerateGiftId(long curTime) {
        long todayStamp = TimeUtil.getTodayStamp(curTime);
        long dailyTimeStamp = curTime - todayStamp;
        for (int i = 0; i < giftlistdata.size(); i++) {
            MineGiftInfo giftInfo = giftlistdata.get(i);
            if (dailyTimeStamp < giftInfo.getDailyStartTime() || dailyTimeStamp >= giftInfo.getDailyEndTime()) {
                continue;
            }
            // 优先判断是否会产出
            if (RandomUtils.nextInt(1000) >= giftInfo.getGenerateOdds()) {
                break;
            }
            int randVal = RandomUtils.nextInt(giftInfo.getTotalWeight());
            int weight = 0;
            for (int j = 0; j < giftInfo.getTypeOddsList().size(); j++) {
                TypeOdds typeOdds = giftInfo.getTypeOddsList().get(j);
                weight += typeOdds.getWeight();
                if (randVal < weight) {
                    return typeOdds.getType();
                }
            }
            break;
        }
        return 0;
    }

}
