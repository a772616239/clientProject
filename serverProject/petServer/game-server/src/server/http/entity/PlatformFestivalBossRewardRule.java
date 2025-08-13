package server.http.entity;

import lombok.Data;

import java.util.List;

@Data
public class PlatformFestivalBossRewardRule {

    private int id ;

    /**
     * 玩家伤害
     */
    private long damage ;

    /**
     * 奖励
     */
    private List<PlatformReward> rewards;

}
