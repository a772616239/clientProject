package server.handler.newbee;

import protocol.Common.EnumFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.Newbee.CS_GetNewbeeStep;
import protocol.Newbee.SC_GetNewbeeStep;
import protocol.RetCodeId.RetCodeEnum;
import common.AbstractBaseHandler;
import util.GameUtil;
import util.LogUtil;

/**
 * @author xiao_FL
 * @date 2019/9/17
 */
@MsgId(msgId = MsgIdEnum.CS_GetNewbeeStep_VALUE)
public class GetNewbeeHandler extends AbstractBaseHandler<CS_GetNewbeeStep> {
    @Override
    protected CS_GetNewbeeStep parse(byte[] bytes) throws Exception {
        return CS_GetNewbeeStep.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GetNewbeeStep req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerId);
        SC_GetNewbeeStep.Builder resultBuilder = SC_GetNewbeeStep.newBuilder();
        if (null == player) {
            LogUtil.error("server.handler.newbee.GetNewbeeHandler.execute, player idx =" + playerId + ",entity or dbData is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_GetNewbeeStep_VALUE, resultBuilder);
            return;
        }
        if (player.getDb_data().getNewBeeInfo().getPlayerNewbeeStepCount() > 0) {
            resultBuilder.addAllNewbeeStep(player.getDb_data().getNewBeeInfo().getPlayerNewbeeStepList());
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_GetNewbeeStep_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
