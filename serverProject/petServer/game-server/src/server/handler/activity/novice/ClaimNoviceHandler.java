package server.handler.activity.novice;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.CS_ClaimNovice;
import protocol.Activity.NoviceCredit;
import protocol.Activity.SC_ClaimNovice;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_NoviceCredit;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimNovice_VALUE)
public class ClaimNoviceHandler extends AbstractBaseHandler<CS_ClaimNovice> {

    @Override
    protected CS_ClaimNovice parse(byte[] bytes) throws Exception {
        return CS_ClaimNovice.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimNovice req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_ClaimNovice.Builder resultBuilder = SC_ClaimNovice.newBuilder();
        if (target == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimNovice_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(target, t -> {
            if (target.noviceIsValid(2)) {
                resultBuilder.setFinished(false);

                Builder db_builder = target.getDb_Builder();
                if (db_builder == null) {
                    resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                    gsChn.send(MsgIdEnum.SC_ClaimNovice_VALUE, resultBuilder);
                    return;
                }
                DB_NoviceCredit.Builder noviceBuilder = db_builder.getSpecialInfoBuilder().getNoviceBuilder();

                NoviceCredit.Builder newBuilder = NoviceCredit.newBuilder();
                newBuilder.setStartTime(noviceBuilder.getStartTime());
                newBuilder.setCurPoint(noviceBuilder.getCurPoint());
                newBuilder.addAllClaimReward(noviceBuilder.getClaimRewardList());
                newBuilder.addAllMissionPro(noviceBuilder.getMissionProMap().values());
                resultBuilder.setNovice(newBuilder);
            } else {
                resultBuilder.setFinished(true);
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimNovice_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NullFuntion;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimNovice_VALUE, SC_ClaimNovice.newBuilder().setRetCode(retCode));
    }
}
