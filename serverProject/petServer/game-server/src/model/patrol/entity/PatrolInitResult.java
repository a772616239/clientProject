package model.patrol.entity;

import protocol.Patrol.PatrolMap;
import protocol.Patrol.PatrolStatus;
import entity.CommonResult;

/**
 * @author xiao_FL
 * @date 2019/8/2
 */
public class PatrolInitResult extends CommonResult {
    private PatrolMap patrolMap;

    private PatrolStatus patrolStatus;

    private boolean newGame;

    public PatrolMap getPatrolMap() {
        return patrolMap;
    }

    public void setPatrolMap(PatrolMap patrolMap) {
        this.patrolMap = patrolMap;
    }

    public PatrolStatus getPatrolStatus() {
        return patrolStatus;
    }

    public void setPatrolStatus(PatrolStatus patrolStatus) {
        this.patrolStatus = patrolStatus;
    }

    public boolean isNewGame() {
        return newGame;
    }

    public void setNewGame(boolean newGame) {
        this.newGame = newGame;
    }
}
