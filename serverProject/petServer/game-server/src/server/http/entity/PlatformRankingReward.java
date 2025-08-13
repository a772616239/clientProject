package server.http.entity;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/06/02
 */
@Getter
@Setter
public class PlatformRankingReward {
    private int startRanking;
    private int endRanking;
    private List<PlatformReward> rewards;
}
