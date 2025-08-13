package server.handler.bravechallenge;

import cfg.GameConfig;
import cfg.VIPConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.ReasonManager;
import protocol.BraveChallenge;
import protocol.BraveChallenge.SC_BraveChallengeReborn;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @Description
 * @Author hanx
 * @Date2020/5/9 0009 13:48
 **/
@MsgId(msgId = MessageId.MsgIdEnum.CS_BraveChallengeReborn_VALUE)
public class BraveChallengeReBornHandler extends AbstractBaseHandler<BraveChallenge.CS_BraveChallengeReborn> {

    @Override
    protected BraveChallenge.CS_BraveChallengeReborn parse(byte[] bytes) throws Exception {
        return BraveChallenge.CS_BraveChallengeReborn.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BraveChallenge.CS_BraveChallengeReborn req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        BraveChallenge.SC_BraveChallengeReborn.Builder result = BraveChallenge.SC_BraveChallengeReborn.newBuilder();
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_BraveChallengeReborn_VALUE, result);
            return;
        }
        int rebornTimes = VIPConfig.getById(player.getVip()).getBravechallengereborntimes();
        bravechallengeEntity entity = bravechallengeCache.getInstance().getEntityByPlayer(playerId);
        //机会用完
        if (entity.getProgressBuilder().getTodayRebornTimes() >= rebornTimes) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_BraveChallenge_RebornTimesUseUp));
            gsChn.send(MessageId.MsgIdEnum.SC_BraveChallengeReborn_VALUE, result);
        }

        Common.Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getBravechallengerebornprice());
        //购买复活消耗
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_BraveChallenge))) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MessageId.MsgIdEnum.SC_BraveChallengeReborn_VALUE, result);
            return;
        }

        //重置宠物血量
        entity.rebornPets();
        entity.sendProgress();
        result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));

        //没用完 恢复所有宠物状态
        gsChn.send(MessageId.MsgIdEnum.SC_BraveChallengeReborn_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CourageTrial;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BraveChallengeReborn_VALUE, SC_BraveChallengeReborn.newBuilder().setRetCode(retCode));
    }
}
