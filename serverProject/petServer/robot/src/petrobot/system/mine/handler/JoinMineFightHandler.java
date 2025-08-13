package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.system.mine.RobotMine;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.MineGiftObj;
import protocol.MineFight.MineInfo;
import protocol.MineFight.MineRewardInfo;
import protocol.MineFight.SC_JoinMineFight;
import protocol.RetCodeId.RetCodeEnum;

@MsgId(msgId = MsgIdEnum.SC_JoinMineFight_VALUE)
public class JoinMineFightHandler extends AbstractHandler<SC_JoinMineFight> {
    @Override
    protected SC_JoinMineFight parse(byte[] bytes) throws Exception {
        return SC_JoinMineFight.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_JoinMineFight ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            RobotMine robotMine = r.getData().getMineInfo();
            if (robotMine == null) {
                robotMine = new RobotMine(r);
            } else {
                robotMine.clear();
            }
            for (MineInfo mineInfo : ret.getTotalMinesList()) {
                robotMine.getMineInfoMap().put(mineInfo.getId(), mineInfo.toBuilder());
            }
            for (MineGiftObj mineGift : ret.getMineGiftObjList()) {
                robotMine.getMineGiftObjMap().put(mineGift.getGiftId(), mineGift);
            }

            for (MineRewardInfo mineReward : ret.getRewardDataList()) {
                r.getData().getMineInfo().getMineRewardMap().put(mineReward.getFromIndex(), mineReward);
            }

            for (int index = 0; index < ret.getUsingPetForms().getKeysCount(); index++) {
                int formIndex = ret.getUsingPetForms().getKeys(index);
                String mineIdx = ret.getUsingPetForms().getValues(index);
                r.getData().getMineInfo().getOwnedMineInfo().put(formIndex, mineIdx);
            }
        });
        if (ret.getRetCode().getRetCode() != RetCodeEnum.RCE_Success) {
            LogUtil.warn("JoinMineFight failed,retCode=" + ret.getRetCode().getRetCode());
            return;
        }

    }
}
