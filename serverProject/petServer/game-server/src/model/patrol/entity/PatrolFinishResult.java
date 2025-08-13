package model.patrol.entity;

import entity.CommonResult;
import lombok.Getter;
import lombok.Setter;

/**
 * 巡逻队是否结束
 *
 * @author xiao_FL
 * @date 2019/9/9
 */
@Getter
@Setter
public class PatrolFinishResult extends CommonResult {
    private int finish;

    private int todayCreateCount;
}
