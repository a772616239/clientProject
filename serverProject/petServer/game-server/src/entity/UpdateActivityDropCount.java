package entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 此类用于掉落道具数量更新
 * @author huhan
 * @date 2020/03/20
 */
@Getter
@Setter
@AllArgsConstructor
public class UpdateActivityDropCount {
    private long activityId;
    private int itemId;
    private int count;
}