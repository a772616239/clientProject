package server.handler.gloryRoad;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.gloryroad.GloryRoadManager;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.CS_GloryRoadManualOperateBattle;
import protocol.GloryRoad.SC_GloryRoadManualOperateBattle;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/18
 */
@MsgId(msgId = MsgIdEnum.CS_GloryRoadManualOperateBattle_VALUE)
public class GloryRoadManualOperateBattleHandler extends AbstractBaseHandler<CS_GloryRoadManualOperateBattle> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_GloryRoad;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_GloryRoadManualOperateBattle.Builder resultBuilder = SC_GloryRoadManualOperateBattle.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_GloryRoadManualOperateBattle_VALUE, resultBuilder);
    }

    @Override
    protected CS_GloryRoadManualOperateBattle parse(byte[] bytes) throws Exception {
        return CS_GloryRoadManualOperateBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GloryRoadManualOperateBattle req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_GloryRoadManualOperateBattle.Builder resultBuilder = SC_GloryRoadManualOperateBattle.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_GloryRoad)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_GloryRoadManualOperateBattle_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum retCode = GloryRoadManager.getInstance().manualOperateBattle(playerIdx, req.getManual());

        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_GloryRoadManualOperateBattle_VALUE, resultBuilder);
    }
}
