package server.http.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author hanx
 * @Date2020/10/12 0012 10:38
 **/
@Getter
@Setter
public class PlatformRichMan {
    //所有点位
    List<RichManPoint> points = new ArrayList<>();


    @Getter
    @Setter
    public static class RichManPoint {
        //点位id
        private int pointId;
        //@see RichManPointType
        private int pointType;

        //免费奖励
        private List<PlatformReward> freeRewards;

        //商店
        private List<PlatformBuyMission> buyMissions;

        //充值返利百分比
        private int rebate;

    }

    public static class RichManPointType {
        public static final int Null = 0;  //空点
        public static final int Start = 1;     //起始点
        public static final int FreeReward = 2;    //免费奖励
        public static final int Back = 3;          //回退
        public static final int Store = 4;         //商店
        public static final int RechargeRebate = 5;    //充值返利
        public static final int BigReward = 6;    //大奖
        public static final int DoubleReward = 7; //奖励翻倍
    }


}

