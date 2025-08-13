package model.reward;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import protocol.Common.Reward;

/**
 * @author huhan
 * @date 2020/06/12
 */
@Getter
@Setter
@AllArgsConstructor
public class IndexReward {
    private int index;
    private Reward reward;
}
