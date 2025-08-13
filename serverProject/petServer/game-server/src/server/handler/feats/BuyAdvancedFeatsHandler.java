/*
package server.handler.feats;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.PayActivityLog;
import platform.logs.entity.PayActivityLog.PayActivityEnum;
import protocol.Common;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import protocol.TargetSystemDB;
import util.GameUtil;


@MsgId(msgId = MsgIdEnum.CS_BuyAdvanceFeats_VALUE)
public class BuyAdvancedFeatsHandler extends AbstractBaseHandler<TargetSystem.CS_GetFeatsInfo> {
    @Override
    protected TargetSystem.CS_GetFeatsInfo parse(byte[] bytes) throws Exception {
        return TargetSystem.CS_GetFeatsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, TargetSystem.CS_GetFeatsInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        TargetSystem.SC_GetFeatsInfo.Builder resultBuilder = TargetSystem.SC_GetFeatsInfo.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (target == null || player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyAdvanceFeats_VALUE, resultBuilder);
            return;
        }
        TargetSystemDB.DB_Feats featsInfo = target.getDb_Builder().getFeatsInfo();
        //高级功勋未过期
        if (hasActiveAdvancedFeats(featsInfo)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Feats_HasBuyAdvancedFeats));
            gsChn.send(MsgIdEnum.SC_BuyAdvanceFeats_VALUE, resultBuilder);
            return;

        }
        Common.Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getAdvancedfeatsprice());
        //购买消耗
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FeatsReward))) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_BuyAdvanceFeats_VALUE, resultBuilder);
            return;
        }
        SyncExecuteFunction.executeConsumer(target, entity -> target.getDb_Builder().getFeatsInfoBuilder()
                .setFeatsType(1));

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_BuyAdvanceFeats_VALUE, resultBuilder);
        if (consume != null && consume.getCount() > 0) {
            LogService.getInstance().submit(new PayActivityLog(playerIdx, consume, PayActivityEnum.Feats));
        }
    }

    private boolean hasActiveAdvancedFeats(TargetSystemDB.DB_Feats featsInfo) {
        return featsInfo.getFeatsType() == 1;
    }

}
*/
