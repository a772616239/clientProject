package petrobot.system.bossTower;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import protocol.BossTower.CS_ClaimBossTowerInfo;
import protocol.BossTower.CS_SweepBossTower;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/12/25
 */
public class BossTowerManager {

    @Index(IndexConst.CLAIM_BOSS_TOWER_INFO)
    public void claimBossTower(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_ClaimBossTowerInfo_VALUE, CS_ClaimBossTowerInfo.newBuilder());
    }

    @Index(IndexConst.SWEEP_BOSS_TOWER)
    public void sweepBossTower(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_SweepBossTower_VALUE, CS_SweepBossTower.newBuilder());
    }
}
