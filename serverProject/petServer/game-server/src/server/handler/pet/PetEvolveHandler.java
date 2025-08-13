package server.handler.pet;

import cfg.GameConfig;
import cfg.LinkConfig;
import cfg.LinkConfigObject;
import cfg.PetEvolveCfg;
import cfg.PetEvolveCfgObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Collection;
import java.util.List;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_PetEvolve;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetEvolve;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetEvolve_VALUE;
import static protocol.RetCodeId.RetCodeEnum.RCE_Pet_PetNotExist;

/**
 * 宠物进化
 *
 * @date 2019/5/16
 */
@MsgId(msgId = CS_PetEvolve_VALUE)
public class PetEvolveHandler extends AbstractBaseHandler<CS_PetEvolve> {

    @Override
    protected CS_PetEvolve parse(byte[] bytes) throws Exception {
        return CS_PetEvolve.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetEvolve req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        PetMessage.SC_PetEvolve.Builder msg = PetMessage.SC_PetEvolve.newBuilder();
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        RetCodeEnum canPetEvolve;
        if (RetCodeEnum.RCE_Success != (canPetEvolve = canPetEvolve(req, playerId, entity))) {
            msg.setResult(GameUtil.buildRetCode(canPetEvolve));
            gsChn.send(MsgIdEnum.SC_PetEvolve_VALUE, msg);
            return;
        }
        doPetEvolve(req, entity);
        sendSuccessMsg(gsChn, req, msg, entity);

    }

    private RetCodeEnum canPetEvolve(CS_PetEvolve req, String playerId, petEntity entity) {
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        Pet pet;
        if ((pet = entity.getPetById(req.getPetId())) == null) {
            return RCE_Pet_PetNotExist;
        }
        int curEvolveLv = pet.getEvolveLv();
        PetEvolveCfgObject upCfg = PetEvolveCfg.getInstance().getByPetIdAndEvolveLv(pet.getPetBookId(), curEvolveLv);
        if (upCfg == null) {
            return RetCodeEnum.RCE_Pet_NotMathCondition;
        }
        int[] evolveNeedRarity = GameConfig.getById(GameConst.CONFIG_ID).getEvolveneedrarity();

        if (ArrayUtils.isEmpty(upCfg.getUpconsume()) || evolveNeedRarity.length <= curEvolveLv) {
            return RetCodeEnum.RCE_Pet_MaxEvolveLvLimit;
        }
        if (evolveNeedRarity[curEvolveLv] > pet.getPetRarity()) {
            return RetCodeEnum.RCE_Pet_NotMathCondition;
        }

        List<Consume> consumes = ConsumeUtil.parseToConsumeList(upCfg.getUpconsume());

        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes
                , ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetEvolve))) {
            return RetCodeEnum.RCE_Player_CurrencysNotEnought;
        }
        return RetCodeEnum.RCE_Success;
    }

    private void sendSuccessMsg(GameServerTcpChannel gsChn, CS_PetEvolve req, PetMessage.SC_PetEvolve.Builder msg, petEntity entity) {
        msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        msg.setPetId(req.getPetId());
        Pet pet = entity.getPetById(req.getPetId());
        int lv = pet == null ? 0 : pet.getEvolveLv();
        msg.setPetEvolveLv(lv);
        gsChn.send(MsgIdEnum.SC_PetEvolve_VALUE, msg);

    }

    private void doPetEvolve(CS_PetEvolve req, petEntity entity) {
        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            Pet lastPet = entity.getPetById(req.getPetId());
            if (lastPet == null) {
                LogUtil.error("player:{} doPetEvolve failed case by pet is null,petId:{}", entity.getPlayeridx(), req.getPetId());
                return;
            }
            Pet.Builder modifyPet = lastPet.toBuilder().setEvolveLv(lastPet.getEvolveLv() + 1);
            checkAndUpdatePetLink(modifyPet, entity);
            entity.refreshPetPropertyAndPut(modifyPet, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetEvolve), true);
            //通知战戈宠物更新
            EventUtil.triggerWarPetUpdate(entity.getPlayeridx(), req.getPetId(), GameConst.WarPetUpdate.MODIFY);
            doPetEvolveReward(entity.getPlayeridx(), lastPet);
        });
    }

    /**
     * 检查更新宠物链接
     *
     * @param modifyPet
     * @param entity
     */
    private void checkAndUpdatePetLink(Pet.Builder modifyPet, petEntity entity) {
        if (modifyPet.getEvolveLv() != 1) {
            return;
        }
        List<LinkConfigObject> links = LinkConfig.canTriggerLinkIds(modifyPet.getPetBookId());
        if (CollectionUtils.isEmpty(links)) {
            return;
        }
        Collection<Pet> pets = entity.peekAllPetByUnModify();
        int tempPetBookId;
        for (LinkConfigObject link : links) {
            if (canActiveLink(pets, link, modifyPet)) {
                for (int i = 0; i < link.getNeedpet().length; i++) {
                    tempPetBookId = link.getNeedpet()[i];
                    if (tempPetBookId == modifyPet.getPetBookId()) {
                        curEvolveUpPetActiveLink(modifyPet, link.getId());
                    } else {
                        petBagPetActiveLink(entity, pets, tempPetBookId, link.getId());
                    }

                }
            }
        }
    }

    private void petBagPetActiveLink(petEntity entity, Collection<Pet> pets, int tempPetBookId, int linkId) {
        pets.stream().filter(e -> e.getPetBookId() == tempPetBookId && !e.getActiveLinkList().contains(linkId) && e.getEvolveLv() > 0).
                forEach(pet -> {
                    Pet.Builder builder = doPetActiveLink(pet.toBuilder(), linkId);
                    entity.refreshPetPropertyAndPut(builder, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetEvolve), false);
                });
    }

    private void curEvolveUpPetActiveLink(Pet.Builder modifyPet, int linkId) {
        doPetActiveLink(modifyPet, linkId);
    }

    private boolean canActiveLink(Collection<Pet> pets, LinkConfigObject link, Pet.Builder modifyPet) {
        if (modifyPet.getActiveLinkList().contains(link.getId())) {
            return false;
        }
        int[] needPet = link.getNeedpet();
        for (int petBookId : needPet) {
            if (petBookId == modifyPet.getPetBookId()) {
                continue;
            }
            if (pets.stream().noneMatch(pet -> pet.getPetBookId() == petBookId && pet.getEvolveLv() > 0)) {
                return false;
            }

        }
        return true;
    }

    private Pet.Builder doPetActiveLink(Pet.Builder modifyPet, int linkId) {
        modifyPet.addActiveLink(linkId);
        return modifyPet;
    }

    private void doPetEvolveReward(String playerIdx, Pet lastPet) {
        PetEvolveCfgObject cfg = PetEvolveCfg.getInstance().getByPetIdAndEvolveLv(lastPet.getPetBookId(), lastPet.getEvolveLv());
        if (cfg == null) {
            return;
        }
        List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(cfg.getUpreward());
        if (CollectionUtils.isEmpty(rewards)) {
            return;
        }
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(
                RewardSourceEnum.RSE_PetEvolve), false);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetEvolve_VALUE, SC_PetEvolve.newBuilder().setResult(retCode));

    }


}
