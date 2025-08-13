package server.handler.bravechallenge;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.player.util.PlayerUtil;
import protocol.BraveChallenge.CS_BraveChallengeInit;
import protocol.BraveChallenge.ChallengeProgress;
import protocol.BraveChallenge.SC_BraveChallengeInit;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 处理客户端初始化游戏请求
 *
 * @author xiao_FL
 * @date 2019/8/29
 */
@MsgId(msgId = MsgIdEnum.CS_BraveChallengeInit_VALUE)
public class BraveChallengeInitHandler extends AbstractBaseHandler<CS_BraveChallengeInit> {

    @Override
    protected CS_BraveChallengeInit parse(byte[] bytes) throws Exception {
        return CS_BraveChallengeInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BraveChallengeInit req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_BraveChallengeInit.Builder resultBuilder = SC_BraveChallengeInit.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerId, EnumFunction.CourageTrial)) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_BraveChallengeInit_VALUE, resultBuilder);
            return;
        }

        bravechallengeEntity entity = bravechallengeCache.getInstance().getEntityByPlayer(playerId);
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BraveChallengeInit_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            ChallengeProgress clientProgress = entity.getClientProgress();
            if (clientProgress == null) {
                resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_BraveChallengeInit_VALUE, resultBuilder);
                return;
            }

            resultBuilder.setProgressMsg(entity.getClientProgress());
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_BraveChallengeInit_VALUE, resultBuilder);
            entity.getProgressBuilder().setNewGame(false);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CourageTrial;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BraveChallengeInit_VALUE, SC_BraveChallengeInit.newBuilder().setResult(retCode));
    }
}
