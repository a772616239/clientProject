package server.http.entity;

import lombok.Data;

import java.util.List;

@Data
public class PlatformFestivalBossTreasure {

    private int id ;

    /**
     * 宝箱达成进度
     */
    private int target;
    /**
     * 宝箱奖励
     */
    private List<PlatformReward> rewards;

}
