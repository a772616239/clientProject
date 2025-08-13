package server.http.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/11/26
 */
@Getter
@Setter
public class PlatformRuneTreasurePool {
    PlatformRandomReward reward;
    /**
     * 是否限定
     */
    boolean limited;
}
