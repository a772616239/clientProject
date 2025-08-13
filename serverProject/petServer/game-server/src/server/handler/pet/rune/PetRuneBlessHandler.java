package server.handler.pet.rune;

import cfg.GameConfig;
import cfg.PetRuneBlessPropertyCfg;
import cfg.PetRuneBlessPropertyCfgObject;
import cfg.PetRuneBlessRatingCfg;
import cfg.PetRuneBlessUpCfg;
import cfg.PetRuneExp;
import cfg.PetRuneProperties;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import org.springframework.util.CollectionUtils;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.PetMessage;
import protocol.PetMessage.CS_PetRuneBless;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneBless;
import protocol.RetCodeId.RetCodeEnum;
import util.ArrayUtil;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetRuneBless_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetRuneBless_VALUE;


@MsgId(msgId = CS_PetRuneBless_VALUE)
public class PetRuneBlessHandler extends AbstractBaseHandler<CS_PetRuneBless> {

    @Override
    protected CS_PetRuneBless parse(byte[] bytes) throws Exception {
        return CS_PetRuneBless.parseFrom(bytes);
    }

    private static final int blessStartRarity = 9;

    @Override
    public void execute(GameServerTcpChannel gsChn, CS_PetRuneBless req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PetRuneBless.Builder msg = SC_PetRuneBless.newBuilder();
        petruneEntity entity = petruneCache.getInstance().getEntityByPlayer(playerId);
        if (entity == null) {
            msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_PetRuneBless_VALUE, msg);
            return;
        }
        Rune rune = entity.getRuneById(req.getRuneId());
        if (rune == null) {
            msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_PetRuneBless_VALUE, msg);
            return;
        }
        int runRarity = PetRuneProperties.getRuneRarity(rune.getRuneBookId());
        if (!canRuneBless(rune)) {
            msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_PetRuneBless_VALUE, msg);
            return;
        }
        List<Common.Consume> blessConsume = PetRuneBlessPropertyCfg.getBlessConsume(runRarity);
        if (CollectionUtils.isEmpty(blessConsume)) {
            msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_PetRuneBless_VALUE, msg);
            return;
        }
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, blessConsume, null)) {
            msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MessageId.MsgIdEnum.SC_PetRuneBless_VALUE, msg);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            Rune.Builder newRune = rune.toBuilder();
            //祝福副属性
            blessRuneExProperty(newRune, msg);
            //属性评级
            blessRatingForRune(newRune);
            //累积消耗
            cumuBlessConsume(newRune, blessConsume);
            //save
            entity.putRune(newRune.build());
            //更新符文相关的其他
            petruneCache.getInstance().settleRuneUpdate(playerId, newRune);
            msg.setNewRune(newRune);
            msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        });

        gsChn.send(SC_PetRuneBless_VALUE, msg);


    }

    private void cumuBlessConsume(Rune.Builder newRune, List<Common.Consume> blessConsume) {
        PetMessage.BlessRating.Builder blessRating = newRune.getBlessRatingBuilder();
        blessRating.addAllConsumes(blessConsume);
        List<Common.Consume> consumes = ConsumeUtil.mergeConsume(blessRating.getConsumesList());
        blessRating.clearConsumes().addAllConsumes(consumes);
    }

    private void blessRatingForRune(Rune.Builder newRune) {
        PetMessage.BlessRating.Builder blessRating = newRune.getBlessRatingBuilder().clearItemLv();
        int runRarity = PetRuneProperties.getRuneRarity(newRune.getRuneBookId());
        int totalLevel = 0;
        for (PetMessage.RunePropertieyEntity exProperty : newRune.getRuneExProperty().getPropertyList()) {
            int level = calculatePropertyRatingLevel(runRarity, exProperty);
            blessRating.addItemLv(level);
            totalLevel += level;
        }
        int avgLv = totalLevel / newRune.getRuneExProperty().getPropertyCount();
        blessRating.setTotalLv(avgLv);
    }

    /**
     * 分别祝福(提升符文副属性)
     */
    private void blessRuneExProperty(Rune.Builder rune, SC_PetRuneBless.Builder msg) {
        int runRarity = PetRuneProperties.getRuneRarity(rune.getRuneBookId());
        PetRuneBlessPropertyCfgObject propertyCfg = PetRuneBlessPropertyCfg.getByRunerarity(PetRuneProperties.getRuneRarity(rune.getRuneBookId()));
        PetMessage.RuneProperties.Builder properties = PetMessage.RuneProperties.newBuilder();

        int cfgMaxValue;
        for (PetMessage.RunePropertieyEntity exProperty : rune.getRuneExProperty().getPropertyList()) {
            int propertyType = exProperty.getPropertyType();
            int propertyValue = exProperty.getPropertyValue();
            cfgMaxValue = getRuneMaxBlessValue(propertyCfg, propertyType);
            if (propertyValue >= cfgMaxValue) {
                properties.addProperty(exProperty);
                msg.addPropertyType(propertyType).addPropertyRating(0);
                continue;
            }

            //随机一个副属性增量<评级,增量>
            Pair<Integer, Integer> pair = randomBless(runRarity, propertyType);
            propertyValue = Math.min(pair.getValue() + propertyValue, cfgMaxValue);

            properties.addProperty(exProperty.toBuilder().setPropertyValue(propertyValue));
            msg.addPropertyType(propertyType).addPropertyRating(pair.getKey());
        }
        rune.setRuneExProperty(properties);
    }

    private int calculatePropertyRatingLevel(int runRarity, PetMessage.RunePropertieyEntity exProperty) {
        return PetRuneBlessRatingCfg.getInstance().ratingProperty(runRarity, exProperty.getPropertyType(), exProperty.getPropertyValue());
    }

    private Pair<Integer, Integer> randomBless(int runRarity, int propertyType) {
        return PetRuneBlessUpCfg.getInstance().randomBless(runRarity, propertyType);
    }

    private boolean canRuneBless(Rune rune) {
        if (rune.getRuneLvl() < PetRuneExp.queryRuneMaxLv(PetRuneProperties.getRuneRarity(rune.getRuneBookId()))) {
            return false;
        }
        if (PetRuneProperties.getRuneRarity(rune.getRuneBookId()) < blessStartRarity) {
            return false;
        }
        return !blessMax(rune);
    }

    private boolean blessMax(Rune rune) {
        PetRuneBlessPropertyCfgObject cfg = PetRuneBlessPropertyCfg.getByRunerarity(PetRuneProperties.getRuneRarity(rune.getRuneBookId()));
        if (cfg == null) {
            return true;
        }
        for (PetMessage.RunePropertieyEntity property : rune.getRuneExProperty().getPropertyList()) {
            if (property.getPropertyValue() < getRuneMaxBlessValue(cfg, property.getPropertyType())) {
                return false;
            }
        }
        return true;
    }

    private int getRuneMaxBlessValue(PetRuneBlessPropertyCfgObject cfg, int propertyType) {
        return ArrayUtil.getValueFromKeyValueIntArray(cfg.getFinalproperty(), propertyType);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetRune;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetRuneBless_VALUE, SC_PetRuneBless.newBuilder().setResult(retCode));
    }


}
