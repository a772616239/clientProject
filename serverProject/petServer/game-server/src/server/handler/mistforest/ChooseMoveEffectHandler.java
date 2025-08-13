package server.handler.mistforest;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_ChooseCurMoveEffect;
import protocol.PlayerInfo.MistMoveEffectInfo;
import protocol.PlayerInfo.SC_ChooseCurMoveEffect;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_ChooseMoveEffect;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ChooseCurMoveEffect_VALUE)
public class ChooseMoveEffectHandler extends AbstractBaseHandler<CS_ChooseCurMoveEffect> {
    @Override
    protected CS_ChooseCurMoveEffect parse(byte[] bytes) throws Exception {
        return CS_ChooseCurMoveEffect.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChooseCurMoveEffect req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        SC_ChooseCurMoveEffect.Builder ret = SC_ChooseCurMoveEffect.newBuilder();
        if (!player.getDb_data().getMistForestData().getMoveEffectInfoList().contains(req.getChosenEffectId())) {
            ret.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Failure)); // TODO 未拥有
            gsChn.send(MsgIdEnum.SC_ChooseCurMoveEffect_VALUE, ret);
            return;
        }
        RetCodeEnum retCode = SyncExecuteFunction.executeFunction(player, entity -> {
            for (int index = 0; index < entity.getDb_data().getMistForestDataBuilder().getMoveEffectInfoCount(); index++) {
                MistMoveEffectInfo effect = entity.getDb_data().getMistForestDataBuilder().getMoveEffectInfo(index);
                if (effect == null) {
                    continue;
                }
                if (effect.getMoveEffectId() == req.getChosenEffectId()) {
                    if (effect.getExpireTime() > 0 && effect.getExpireTime() > GlobalTick.getInstance().getCurrentTime()) {
                        return RetCodeEnum.RCE_ErrorParam;  // TODO 已过期
                    }
                    entity.getDb_data().getMistForestDataBuilder().setCurMistEffectId(effect.getMoveEffectId());
                    return RetCodeEnum.RCE_Success;
                }
            }
            return RetCodeEnum.RCE_ErrorParam;
        });
        if (retCode == RetCodeEnum.RCE_Success) {
            if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(playerId) > 0) {
                GS_CS_ChooseMoveEffect.Builder builder = GS_CS_ChooseMoveEffect.newBuilder().setPlayerIdx(playerId).setMoveEffectId(req.getChosenEffectId());
                CrossServerManager.getInstance().sendMsgToMistForest(playerId, MsgIdEnum.GS_CS_ChooseMoveEffect_VALUE, builder, false);
            }
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ChooseCurMoveEffect_VALUE, SC_ChooseCurMoveEffect.newBuilder().setRetCode(
                GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance)));
    }
}
