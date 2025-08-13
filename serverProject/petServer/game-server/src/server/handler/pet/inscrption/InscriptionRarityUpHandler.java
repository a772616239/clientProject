package server.handler.pet.inscrption;

import cfg.InscriptionCfg;
import cfg.InscriptionCfgObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import helper.StringUtils;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import model.inscription.dbCache.petinscriptionCache;
import model.inscription.petinscriptionEntity;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_InscriptionRarityUp;
import protocol.PetMessage.SC_InscriptionRarityUp;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;

import static protocol.MessageId.MsgIdEnum.CS_InscriptionRarityUp_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_InscriptionRarityUp_VALUE;

@MsgId(msgId = CS_InscriptionRarityUp_VALUE)
public class InscriptionRarityUpHandler extends AbstractBaseHandler<CS_InscriptionRarityUp> {

    @Override
    protected CS_InscriptionRarityUp parse(byte[] bytes) throws Exception {
        return CS_InscriptionRarityUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_InscriptionRarityUp req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        LogUtil.info("receive player:{} InscriptionRarityUp,req:{}", playerId, req);
        SC_InscriptionRarityUp.Builder result = SC_InscriptionRarityUp.newBuilder();
        if (!checkParams(playerId, req)) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_InscriptionRarityUp_VALUE, result);
            return;
        }
        petinscriptionEntity entity = petinscriptionCache.getInstance().getEntityByPlayer(playerId);
        if (entity == null) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_InscriptionRarityUp_VALUE, result);
            return;
        }
        List<InscriptionCfgObject> cfgList = material2Cfg(req, entity);
        if (CollectionUtils.isEmpty(cfgList)) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Inscription_MaterialNotExists));
            gsChn.send(SC_InscriptionRarityUp_VALUE, result);
            return;
        }
        int newInscriptionId = composeNewInscription(entity, cfgList);
        if (newInscriptionId == -1) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_InscriptionRarityUp_VALUE, result);
            return;
        }
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_InscriptionCompose);
        RetCodeEnum retCodeEnum = SyncExecuteFunction.executeFunction(entity, cx -> {
            if (!consumeMaterial(entity, req)) {
                return RetCodeEnum.RCE_MatieralNotEnough;
            }
            entity.playerObtainInscription(Collections.singletonMap(newInscriptionId, 1), reason);
            sendRewardDisplay(playerId, newInscriptionId, reason);
            return RetCodeEnum.RCE_Success;
        });
        LogUtil.info(" player:{} InscriptionRarityUp,result:{}", playerId, retCodeEnum);
        result.setResult(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(SC_InscriptionRarityUp_VALUE, result);

    }

    private void sendRewardDisplay(String playerId, int newInscriptionId, ReasonManager.Reason reason) {
        GlobalData.getInstance().sendDisRewardMsg(playerId, Common.Reward.newBuilder()
                .setCount(1).setId(newInscriptionId).setRewardType(Common.RewardTypeEnum.RTE_Inscription).build(), reason.getSourceEnum());
    }

    private boolean consumeMaterial(petinscriptionEntity entity, CS_InscriptionRarityUp req) {
        Map<Integer, Integer> itemNum = new HashMap<>();
        for (Integer cfg : req.getCfgIdList()) {
            MapUtil.add2IntMapValue(itemNum, cfg, 1);
        }
        return entity.consumeInscription(req.getInscriptionIdList(), itemNum);
    }

    private int composeNewInscription(petinscriptionEntity entity, List<InscriptionCfgObject> cfgList) {
        int curRarity = checkAndReturnSameRarity(cfgList);
        if (curRarity == -1) {
            LogUtil.info("player:{} composeNewInscription ,mater rarity not same", entity.getPlayeridx());
            return -1;
        }
        int totalProbability = calculateTotalProbability(cfgList);
        int newInscriptionRarity = randomNewRarity(curRarity, totalProbability);
        return randomNewCfgId(newInscriptionRarity);
    }

    private int randomNewCfgId(int newInscriptionRarity) {
        return InscriptionCfg.getInstance().randomByRarity(newInscriptionRarity);
    }

    private int randomNewRarity(int curRarity, int totalProbability) {
        int maxRarity = InscriptionCfg.getInstance().getInscriptionMaxRarity();
        if (curRarity >= maxRarity) {
            return maxRarity;
        }
        int random = ThreadLocalRandom.current().nextInt(1000);
        if (random < totalProbability) {
            return InscriptionCfg.getNextRarity(curRarity);
        }
        return curRarity;
    }

    private int calculateTotalProbability(List<InscriptionCfgObject> cfgList) {
        return cfgList.stream().mapToInt(InscriptionCfgObject::getProbability).sum();
    }

    private List<InscriptionCfgObject> material2Cfg(CS_InscriptionRarityUp req, petinscriptionEntity entity) {
        List<InscriptionCfgObject> result = new ArrayList<>();
        InscriptionCfgObject cfgObject;
        for (Integer cfgId : req.getCfgIdList()) {
            cfgObject = InscriptionCfg.getById(cfgId);
            if (cfgObject == null) {
                return Collections.EMPTY_LIST;
            }
            result.add(cfgObject);
        }
        for (String inscriptionId : req.getInscriptionIdList()) {
            PetMessage.Inscription inscription = entity.getDb_data().getInscriptionEntityMap().get(inscriptionId);
            cfgObject = InscriptionCfg.getById(inscription.getCfgId());
            if (cfgObject == null) {
                return Collections.EMPTY_LIST;
            }
            result.add(cfgObject);
        }
        return result;
    }

    /**
     * 如果当前品质不等则返回负一 ,相同则返回任一配置品质
     *
     * @param cfgList
     * @return
     */
    private int checkAndReturnSameRarity(List<InscriptionCfgObject> cfgList) {
        int rarity = cfgList.get(0).getRarity();
        for (InscriptionCfgObject cfg : cfgList) {
            if (cfg.getRarity() != rarity) {
                return -1;
            }
        }
        return rarity;
    }

    private boolean checkParams(String playerId, CS_InscriptionRarityUp req) {
        if (StringUtils.isEmpty(playerId)) {
            return false;
        }
        int materialCount = req.getInscriptionIdCount() + req.getCfgIdCount();
        if (materialCount <= 1 || materialCount > 5) {
            return false;
        }
        return true;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_Inscription;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_InscriptionRarityUp_VALUE, SC_InscriptionRarityUp.newBuilder().setResult(retCode));
    }


}
