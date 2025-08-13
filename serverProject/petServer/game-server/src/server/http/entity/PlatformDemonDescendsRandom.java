package server.http.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020.10.12
 */
@Getter
@Setter
public class PlatformDemonDescendsRandom {
    private PlatformRandomReward randomRewards;
    private int rewardLv;
    private boolean grandPrize;
}
