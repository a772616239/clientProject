package server.handler.pet.rune;

import cfg.PetRuneProperties;
import cfg.PetRuneWorth;
import cfg.PetRuneWorthObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeUtil;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PetMessage.CS_PetRuneSale;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneSale;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import java.util.List;

import static protocol.MessageId.MsgIdEnum.CS_PetRuneSale_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetRuneSale_VALUE;

/**
 * 处理客户端出售符文请求
 *
 * @author xiao_FL
 * @date 2019/5/31
 */
@MsgId(msgId = CS_PetRuneSale_VALUE)
public class PetRuneSaleHandler extends AbstractBaseHandler<CS_PetRuneSale> {

    @Override
    protected CS_PetRuneSale parse(byte[] bytes) throws Exception {
        return CS_PetRuneSale.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetRuneSale req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        LogUtil.info("receive player:{} rune sale,runeId:{}", playerId, req.getId());
        petruneEntity entity = petruneCache.getInstance().getEntityByPlayer(playerId);
        SC_PetRuneSale.Builder resultBuilder = SC_PetRuneSale.newBuilder();
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetRuneSale_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum ret = entity.runeCanRemove(req.getId());
        if (RetCodeEnum.RCE_Success != ret) {
            resultBuilder.setResult(GameUtil.buildRetCode(ret));
            gsChn.send(SC_PetRuneSale_VALUE, resultBuilder);
            return;
        }

        Rune runeById = entity.getRuneById(req.getId());
        if (runeById == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_RuneNotExist));
            gsChn.send(SC_PetRuneSale_VALUE, resultBuilder);
            return;
        }
        // 读取配置
        int runeRarity = PetRuneProperties.getByRuneid(runeById.getRuneBookId()).getRunerarity();
        PetRuneWorthObject sale = PetRuneWorth.getByRarityAndLevel(PetRuneProperties.getByRuneid(runeById.getRuneBookId()).getRunerarity(), runeById.getRuneLvl());
        if (sale == null) {
            LogUtil.error("sale petRune,PetRuneWorth config is null by rarity:{} and lv:{}", runeRarity, runeById.getRuneLvl());
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(SC_PetRuneSale_VALUE, resultBuilder);
            return;
        }
        //符文经验返还
        int runeExp = petruneCache.getInstance().calculateRuneTotalExp(runeById);
        List<Reward> reward = calculateSaleReward(runeById, sale, runeExp);


        RewardManager.getInstance().doRewardByList(playerId, reward,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_SellRune), true);

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.removeRune(runeById);
        });

        LogUtil.info("player:{} sale rune success,runeId:{},runeBookId:{},runeLv:{}", playerId, req.getId(), runeById.getRuneBookId(), runeById.getRuneLvl());

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_PetRuneSale_VALUE, resultBuilder);
    }

    private List<Reward> calculateSaleReward(Rune runeById, PetRuneWorthObject sale, int runeExp) {
        List<Reward> expReward = petruneCache.runeExpToReward(runeExp);

        List<Reward> reward = RewardUtil.parseRewardIntArrayToRewardList(sale.getRunesale());
        reward.addAll(expReward);
        List<Reward> blessReward = ConsumeUtil.parseConsumeToReward(runeById.getBlessRating().getConsumesList());
        if (!CollectionUtils.isEmpty(blessReward)) {
            reward.addAll(blessReward);
            reward = RewardUtil.mergeReward(reward);
        }
        return reward;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetRuneSale;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetRuneSale_VALUE, SC_PetRuneSale.newBuilder().setResult(retCode));
    }


}
