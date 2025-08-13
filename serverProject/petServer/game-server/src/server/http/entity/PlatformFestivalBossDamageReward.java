package server.http.entity;

import lombok.Data;

import java.util.List;
@Data
public class PlatformFestivalBossDamageReward {
    private int id ;
    /**
     * 最低伤害
     */
    private long damageStart ;
    /**
     * 最高伤害
     */
    private int damageEnd ;
    /**
     * 奖励
     */
    private List<PlatformReward> rewards;
}
