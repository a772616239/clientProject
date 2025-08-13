package server.handler.activity.signIn;

import cfg.CumuSignIn;
import cfg.CumuSignInObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity.CS_SignIn;
import protocol.Activity.SC_SignIn;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_CumuSignIn;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_SignIn_VALUE)
public class SignInHandler extends AbstractBaseHandler<CS_SignIn> {
    @Override
    protected CS_SignIn parse(byte[] bytes) throws Exception {
        return CS_SignIn.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SignIn req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_SignIn.Builder resultBuilder = SC_SignIn.newBuilder();
        if (target == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_SignIn_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(target, t -> {
            Builder db_builder = target.getDb_Builder();
            if (db_builder == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_SignIn_VALUE, resultBuilder);
                return;
            }
            long curTime = GlobalTick.getInstance().getCurrentTime();
            DB_CumuSignIn.Builder signInBuilder = db_builder.getSpecialInfoBuilder().getSignInBuilder();
            if (signInBuilder.getNextSignInTime() > curTime) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_RepeatedSignIn));
                gsChn.send(MsgIdEnum.SC_SignIn_VALUE, resultBuilder);
                return;
            }

            int nextDays = signInBuilder.getCumuDays() + 1;
            CumuSignInObject nextDay = CumuSignIn.getByDays(nextDays);
            List<Reward> rewards = null;
            if (nextDay == null) {
                LogUtil.warn("CumuSignIn cfg is already reach last days , please check cfg");
                CumuSignInObject curDay = CumuSignIn.getByDays(signInBuilder.getCumuDays());
                if (curDay != null) {
                    rewards = RewardUtil.parseRewardIntArrayToRewardList(curDay.getRewards());
                }
            } else {
                rewards = RewardUtil.parseRewardIntArrayToRewardList(nextDay.getRewards());
                signInBuilder.setCumuDays(nextDays);
            }

            RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CumuSignIn, String.valueOf(nextDays)), true);
            signInBuilder.setNextSignInTime(TimeUtil.getNextDayResetTime(curTime));

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setNextSignInTime(signInBuilder.getNextSignInTime());
            gsChn.send(MsgIdEnum.SC_SignIn_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CumuSignIn;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_SignIn_VALUE, SC_SignIn.newBuilder().setRetCode(retCode));
    }
}
