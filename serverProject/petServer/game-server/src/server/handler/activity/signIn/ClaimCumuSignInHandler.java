package server.handler.activity.signIn;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.CS_ClaimCumuSignIn;
import protocol.Activity.SC_ClaimCumuSignIn;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_CumuSignIn;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimCumuSignIn_VALUE)
public class ClaimCumuSignInHandler extends AbstractBaseHandler<CS_ClaimCumuSignIn> {
    @Override
    protected CS_ClaimCumuSignIn parse(byte[] bytes) throws Exception {
        return CS_ClaimCumuSignIn.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimCumuSignIn req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_ClaimCumuSignIn.Builder resultBuilder = SC_ClaimCumuSignIn.newBuilder();
        if (target == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimCumuSignIn_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(target, t -> {
            Builder db_builder = target.getDb_Builder();
            if (db_builder == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimCumuSignIn_VALUE, resultBuilder);
                return;
            }

            DB_CumuSignIn.Builder signInBuilder = db_builder.getSpecialInfoBuilder().getSignInBuilder();
            resultBuilder.setCumuDays(signInBuilder.getCumuDays());
            resultBuilder.setNextSignInTime(signInBuilder.getNextSignInTime());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimCumuSignIn_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CumuSignIn;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimCumuSignIn_VALUE, SC_ClaimCumuSignIn.newBuilder().setRetCode(retCode));
    }
}
