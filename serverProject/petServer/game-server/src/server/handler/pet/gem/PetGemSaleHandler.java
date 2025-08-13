package server.handler.pet.gem;

import cfg.PetGemConfig;
import cfg.PetGemConfigAdvance;
import cfg.PetGemConfigAdvanceObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.petGem.PetGemDownLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetGemSale;
import protocol.PetMessage.Gem;
import protocol.PetMessage.SC_PetGemSale;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetGemSale_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetGemSale_VALUE;


@MsgId(msgId = CS_PetGemSale_VALUE)
public class PetGemSaleHandler extends AbstractBaseHandler<CS_PetGemSale> {

    @Override
    protected CS_PetGemSale parse(byte[] bytes) throws Exception {
        return CS_PetGemSale.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetGemSale req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        LogUtil.info("receive player:{} game sale,req:{}", playerId, req);
        petgemEntity entity = petgemCache.getInstance().getEntityByPlayer(playerId);
        SC_PetGemSale.Builder resultBuilder = SC_PetGemSale.newBuilder();
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetGemSale_VALUE, resultBuilder);
            return;
        }
        if (CollectionUtils.isEmpty(req.getIdsList())) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_PetGemSale_VALUE, resultBuilder);
            return;
        }

        AtomicReference<RetCodeEnum> ret = new AtomicReference<>();
        List<String> removeGemIds = new ArrayList<>();
        List<Reward> rewards = new ArrayList<>();
        SyncExecuteFunction.executeConsumer(entity, e -> {
            for (String gemId : req.getIdsList()) {
                Gem gemById = entity.getGemById(gemId);
                RetCodeEnum codeEnum = petgemEntity.gemCanRemove(gemById);
                if (RetCodeEnum.RCE_Success != codeEnum) {
                    ret.set(codeEnum);
                    continue;
                }
                PetGemConfigAdvanceObject config = PetGemConfigAdvance.getByGemConfigId(gemById.getGemConfigId());
                if (config == null) {
                    ret.set(RetCodeEnum.RSE_ConfigNotExist);
                    continue;
                }
                Consume consume = ConsumeUtil.parseConsume(config.getGemsaleconsume());
                if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetGemSale))) {
                    ret.set(RetCodeEnum.RCE_MatieralNotEnough);
                    continue;
                }

                List<Reward> saleResource = PetGemConfig.getSaleResource(gemById.getGemConfigId());
                if (CollectionUtils.isNotEmpty(saleResource)) {
                    rewards.addAll(saleResource);
                }
                removeGemIds.add(gemId);

                // 埋点日志
                LogService.getInstance().submit(new PetGemDownLog(playerId, gemById, saleResource));
                LogUtil.info(" player:{} game sale,gameId:{},gem cfgId:{}", playerId, gemId, config.getId());
            }

            if (CollectionUtils.isEmpty(removeGemIds)) {
                resultBuilder.setResult(GameUtil.buildRetCode(ret.get()));
                gsChn.send(SC_PetGemSale_VALUE, resultBuilder);
            }
            rewards.addAll(entity.gemInscription2Rewards(removeGemIds));
            entity.removeGemByIdList(removeGemIds);
            List<Reward> mergeReward = RewardUtil.mergeReward(rewards);
            RewardManager.getInstance().doRewardByList(playerId, mergeReward, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetGemSale), true);
        });
        LogUtil.info(" player:{} gem sale success", playerId);
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_PetGemSale_VALUE, resultBuilder);
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetGemReturn;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetGemSale_VALUE, SC_PetGemSale.newBuilder().setResult(retCode));
    }
}


