package server.handler.pet.gem;

import cfg.PetGemConfig;
import cfg.PetGemConfigLeve;
import cfg.PetGemConfigLeveObject;
import cfg.PetGemConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import org.apache.commons.lang.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.petGem.PetGemLvUpLog;
import platform.logs.statistics.GemStatistics;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetGemLvlUp;
import protocol.PetMessage.Gem;
import protocol.PetMessage.SC_PetGemLvlUp;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetGemLvlUp_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetGemLvlUp_VALUE;

@MsgId(msgId = CS_PetGemLvlUp_VALUE)
public class PetGemLvlUpHandler extends AbstractBaseHandler<CS_PetGemLvlUp> {

    @Override
    protected CS_PetGemLvlUp parse(byte[] bytes) throws Exception {
        return CS_PetGemLvlUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetGemLvlUp req, int i) {
        SC_PetGemLvlUp.Builder result = SC_PetGemLvlUp.newBuilder();
        String playerId = String.valueOf(gsChn.getPlayerId1());
        //强化
        petgemEntity petGem = petgemCache.getInstance().getEntityByPlayer(playerId);
        if (petGem == null) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetGemLvlUp_VALUE, result);
            return;
        }
        Gem gem = petGem.getGemById(req.getUpGemId());
        if (gem == null) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_GemNotExist));
            gsChn.send(SC_PetGemLvlUp_VALUE, result);
            return;
        }
        SyncExecuteFunction.executeConsumer(petGem, e -> {
            int curId = gem.getGemConfigId();
            int upLv = req.getUpLvNum();
            List<Consume> consumes = null;
            RetCodeEnum retCodeEnum = null;
            while (--upLv >= 0) {
                PetGemConfigObject config = PetGemConfig.getNextUpLvConfig(curId);
                if (config == null) {
                    retCodeEnum = RetCodeEnum.RSE_ConfigNotExist;
                    break;
                }
                PetGemConfigLeveObject lvConfig = PetGemConfigLeve.getByLv(config.getLv() - 1);
                if (lvConfig == null) {
                    retCodeEnum = RetCodeEnum.RSE_ConfigNotExist;
                    break;
                }
                consumes = ConsumeUtil.mergeConsumeByTypeAndId(ConsumeUtil.parseToConsumeList(lvConfig.getUplvconsume()), consumes);
                if (!ConsumeManager.getInstance().materialIsEnoughByList(playerId, consumes)) {
                    retCodeEnum = RetCodeEnum.RCE_MatieralNotEnough;
                    break;
                }
                curId = config.getId();
            }
            if (curId == gem.getGemConfigId()) {
                result.setResult(GameUtil.buildRetCode(retCodeEnum));
                gsChn.send(MsgIdEnum.SC_PetGemLvlUp_VALUE, result);
                return;
            }
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetGemLvUp);
            if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes, reason)) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
                gsChn.send(MsgIdEnum.SC_PetGemLvlUp_VALUE, result);
                EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_PopMission_MaterialNotEnough, 1, 1);
                return;
            }
            int finalCurId = curId;

            Gem nowGem = gem.toBuilder().setGemConfigId(finalCurId).build();
            petGem.putGem(nowGem);
            petGem.sendGemUpdate(nowGem);
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_PetGemLvlUp_VALUE, result);
            // 埋点日志
            LogService.getInstance().submit(new PetGemLvUpLog(playerId, gem.getId(), gem.getGemConfigId(), consumes, nowGem.getGemConfigId()));
            if (StringUtils.isNotBlank(gem.getGemPet())) {
                GemStatistics.getInstance().updateEquipEnhanceLv(req.getUpLvNum() - upLv - 1);
            }

            //目标：累积x个宝石到x等级(额外条件:宝石等级)
            EventUtil.triggerUpdateTargetProgress(petGem.getPlayeridx(), TargetTypeEnum.TEE_Gem_LvReach, 1
                    , PetGemConfig.queryEnhanceLv(finalCurId));
            petCache.settlePetUpdate(playerId, gem.getGemPet(), reason);
        });


    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetGemRefine;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetGemLvlUp_VALUE, SC_PetGemLvlUp.newBuilder().setResult(retCode));
    }


}
