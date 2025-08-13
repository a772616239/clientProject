package server.handler.activity.boss;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activityboss.ActivityBossManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.CS_ActivityBossInit;
import protocol.Activity.SC_ActivityBossInit;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * boss战活动
 *
 * @author xiao_FL
 * @date 2020/3/18
 */
@MsgId(msgId = MsgIdEnum.CS_ActivityBossInit_VALUE)
public class ActivityBossHandler extends AbstractBaseHandler<CS_ActivityBossInit> {

    @Override
    protected CS_ActivityBossInit parse(byte[] bytes) throws Exception {
        return CS_ActivityBossInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ActivityBossInit csActivityBossInit, int i) {
        SC_ActivityBossInit.Builder result = SC_ActivityBossInit.newBuilder();
        String playerId = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (entity == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ActivityBoss_Closed));
            gsChn.send(MsgIdEnum.SC_ActivityBossInit_VALUE, result);
            return;
        }

        if (ActivityBossManager.getInstance().isDisplayed()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            result.setTimes(entity.getDb_Builder().getSpecialInfo().getActivityBoss().getTimes());
        } else {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ActivityBoss_Closed));
        }
        gsChn.send(MsgIdEnum.SC_ActivityBossInit_VALUE, result);

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ActivityBoss;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ActivityBossInit_VALUE, SC_ActivityBossInit.newBuilder().setRetCode(retCode));
    }
}
