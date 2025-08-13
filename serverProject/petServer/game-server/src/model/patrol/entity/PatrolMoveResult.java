package model.patrol.entity;

import protocol.Patrol.PatrolPoint;
import entity.CommonResult;

/**
 * @author xiao_FL
 * @date 2019/8/9
 */
public class PatrolMoveResult extends CommonResult {
    private PatrolPoint location;

    public PatrolPoint getLocation() {
        return location;
    }

    public void setLocation(PatrolPoint location) {
        this.location = location;
    }
}
