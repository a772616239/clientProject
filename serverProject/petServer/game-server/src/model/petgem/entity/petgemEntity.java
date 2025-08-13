/**
 * created by tool DAOGenerate
 */
package model.petgem.entity;

import cfg.GameConfig;
import cfg.PetGemConfig;
import cfg.PetGemConfigObject;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import datatool.StringHelper;
import java.util.Collections;
import model.inscription.dbCache.petinscriptionCache;
import model.inscription.petinscriptionEntity;
import model.obj.BaseObj;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import platform.logs.ReasonManager;
import platform.logs.statistics.GemStatistics;
import protocol.Common;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.SC_PetGemGet_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetGemRemove_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetGemUpdate_VALUE;
import protocol.PetDB.SerializablePetGem;
import protocol.PetDB.SerializablePetGem.Builder;
import protocol.PetMessage;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetGemGet;
import protocol.PetMessage.SC_PetGemRemove;
import protocol.PetMessage.SC_PetGemUnEquip;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class petgemEntity extends BaseObj {
    private petgemEntity() {
    }

    @Override
    public String getClassType() {
        return "petgemEntity";
    }

    @Override
    public void putToCache() {
        petgemCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.gem = getGemListBuilder().build().toByteArray();
    }

    /**
     * 主键
     */
    private String idx;

    /**
     * 宠物所属玩家idx
     */
    private String playeridx;

    /**
     * 符文信息
     */
    private byte[] gem;

    /**
     * 符文背包扩容次数
     */
    private int bagenlarge;

    /**
     * 符文背包容量
     */
    private int capacity;

    /**
     * 获得主键
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置主键
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得宠物所属玩家idx
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置宠物所属玩家idx
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得符文信息
     */
    public byte[] getGem() {
        return gem;
    }

    /**
     * 设置符文信息
     */
    public void setGem(byte[] gem) {
        this.gem = gem;
    }

    /**
     * 获得符文背包扩容次数
     */
    public int getBagEnlarge() {
        return bagenlarge;
    }

    /**
     * 设置符文背包扩容次数
     */
    public void setBagEnlarge(int bagEnlarge) {
        this.bagenlarge = bagEnlarge;
    }

    /**
     * 获得符文背包容量
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 设置符文背包容量
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String getBaseIdx() {
        return idx;
    }

    /***************************分割**********************************/

    private Builder gemListBuilder;

    public Builder getGemListBuilder() {
        if (gemListBuilder == null) {
            if (this.gem != null) {
                try {
                    gemListBuilder = SerializablePetGem.parseFrom(this.gem).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.printStackTrace(e);
                    LogUtil.error("parse gem to gemList builder fail, return new Builder");
                    gemListBuilder = SerializablePetGem.newBuilder();
                }
            } else {
                gemListBuilder = SerializablePetGem.newBuilder();
            }
        }
        return gemListBuilder;
    }

    /**
     * 消息：删除符文
     *
     * @param playerId 玩家id
     * @param idList   移除符文id
     */
    public static void sendGemRemove(String playerId, List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        SC_PetGemRemove.Builder result = SC_PetGemRemove.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();
        retCode.setRetCode(RetCodeEnum.RCE_Success);
        result.addAllRemoveId(idList);
        result.setResult(retCode);
        GlobalData.getInstance().sendMsg(playerId, SC_PetGemRemove_VALUE, result);
    }

    /**
     * 消息：添加符文
     *
     * @param playerId 玩家id
     * @param gemList  符文
     */
    public static void sendGemGet(String playerId, List<Gem> gemList) {
        SC_PetGemGet.Builder result = SC_PetGemGet.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();
        retCode.setRetCode(RetCodeEnum.RCE_Success);
        result.setResult(retCode);
        result.setFinish(0);
        // 分批推送符文
        int pageNum = GameConfig.getById(GameConst.CONFIG_ID).getMsgmaxnum();
        if (gemList.size() > pageNum) {
            List<Gem> gemListTemp = new ArrayList<>();
            for (int i = 0; i < gemList.size(); ++i) {
                gemListTemp.add(gemList.get(i));
                if (i % pageNum == 0) {
                    result.addAllGem(gemListTemp);
                    if (i == gemList.size()) {
                        result.setFinish(1);
                    }
                    GlobalData.getInstance().sendMsg(playerId, SC_PetGemGet_VALUE, result);
                    gemListTemp.clear();
                    result.clearGem();
                }
            }
            if (gemList.size() != 0) {
                result.setFinish(1);
                result.addAllGem(gemListTemp);
                GlobalData.getInstance().sendMsg(playerId, SC_PetGemGet_VALUE, result);
            }
        } else {
            result.setFinish(1);
            result.addAllGem(gemList);
            GlobalData.getInstance().sendMsg(playerId, SC_PetGemGet_VALUE, result);
        }
    }

    public petgemEntity(String initPlayerId) {
        idx = IdGenerator.getInstance().generateId();
        playeridx = initPlayerId;
        // 背包扩容默认0
        bagenlarge = 0;
        // 背包总容量，读取配置
        capacity = GameConfig.getById(GameConst.CONFIG_ID).getPetgembaginit();
    }


    public void putGem(Gem gem) {
        if (gem == null) {
            return;
        }

        this.gemListBuilder.putGems(gem.getId(), gem);
    }

    public void putAllGem(Collection<Gem> gemList) {
        if (gemList == null || gemList.isEmpty()) {
            return;
        }

        for (Gem gem : gemList) {
            putGem(gem);
        }
    }

    public Gem getGemById(String gemId) {
        return getGemListBuilder().getGemsMap().get(gemId);
    }

    public void removeGemByIdList(List<String> gemIdxList) {
        if (gemIdxList == null || gemIdxList.isEmpty()) {
            return;
        }
        UpdateStaticsByRemoveGems(gemIdxList);

        for (String gemIdx : gemIdxList) {
            getGemListBuilder().removeGems(gemIdx);
        }
        sendGemRemove(getPlayeridx(), gemIdxList);


    }

    private void UpdateStaticsByRemoveGems(List<String> gemIdxList) {
        List<Gem> gems = gemIdxList.stream().map(this::getGemById).collect(Collectors.toList());
        Map<Integer, Long> rarityMap = new HashMap<>();
        for (Gem gem : gems) {
            PetGemConfigObject config = PetGemConfig.getById(gem.getGemConfigId());
            MapUtil.add2LongMapValue(rarityMap, config.getRarity(), -1L);
        }
        GemStatistics.getInstance().updateOwnGemRarityMap(rarityMap);
    }

    public void removeGemById(String gemIdx) {
        if (StringHelper.isNull(gemIdx)) {
            return;
        }

        getGemListBuilder().removeGems(gemIdx);
        sendGemRemove(getPlayeridx(), Arrays.asList(gemIdx));
    }

    public static RetCodeEnum gemCanRemove(Gem gem) {
        if (gem == null) {
            return RetCodeEnum.RCE_Pet_GemNotExist;
        }

        if (1 == gem.getGemLockStatus()) {
            return RetCodeEnum.RCE_Pet_PetGemLock;
        }
        if (!StringUtils.isEmpty(gem.getGemPet())) {
            return RetCodeEnum.RCE_Pet_PetGemAlreadyEquip;
        }

        return RetCodeEnum.RCE_Success;
    }


    /**
     * 此方法不会判断该符文是否已经被装备，装备前请判断之前的符文状态
     *
     * @param gemIdx
     * @param petIdx
     * @param needRefreshPet
     */
    public void equipGem(String gemIdx, String petIdx, boolean needRefreshPet) {
        if (StringHelper.isNull(gemIdx)) {
            return;
        }

        Gem gemById = getGemById(gemIdx);
        if (gemById == null) {
            return;
        }

        Gem build = gemById.toBuilder().setGemPet(petIdx).build();
        putGem(build);

        if (needRefreshPet) {
            // 重新计算宠物属性，通知
            petCache.getInstance().refreshPetProperty(getPlayeridx(), petIdx,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_UnEquipGem), true);
        }

        //  LogService.getInstance().submit(new PetGemEquipLog(getPlayeridx(), petIdx, null, gemById));
    }


    public void sendGemUnEquipMsg(String gemId) {
        if (StringUtils.isEmpty(gemId)) {
            return;
        }

        SC_PetGemUnEquip.Builder result = SC_PetGemUnEquip.newBuilder();
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        result.setGemId(gemId);
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_PetGemUnEquip_VALUE, result);
    }


    /**
     * 获得指定宠物装备的符文
     *
     * @param petId
     * @return
     */
    public List<Gem> getGemListByPet(String petId) {
        if (StringHelper.isNull(petId)) {
            return null;
        }
        List<Gem> result = new ArrayList<>();
        for (Gem value : getGemListBuilder().getGemsMap().values()) {
            if (petId.equals(value.getGemPet())) {
                result.add(value);
            }
        }
        return result;
    }

    public void sendGemUpdate(Gem gemInPet) {
        if (gemInPet == null) {
            return;
        }
        PetMessage.SC_PetGemUpdate.Builder msg = PetMessage.SC_PetGemUpdate.newBuilder().addGemUpdate(gemInPet);
        GlobalData.getInstance().sendMsg(getPlayeridx(), SC_PetGemUpdate_VALUE, msg);
    }

    public void sendGemUpdate(List<Gem> updateGems) {
        if (CollectionUtils.isEmpty(updateGems)) {
            return;
        }
        PetMessage.SC_PetGemUpdate.Builder msg = PetMessage.SC_PetGemUpdate.newBuilder().addAllGemUpdate(updateGems);
        GlobalData.getInstance().sendMsg(getPlayeridx(), SC_PetGemUpdate_VALUE, msg);
    }

    /**
     * 获得背包已使用容量
     */
    public int getOccupancy() {
        return getGemListBuilder().getGemsCount();
    }

    public List<Common.Reward> gemInscription2Rewards(List<String> removeGemIds) {
        if (CollectionUtils.isEmpty(removeGemIds)) {
            return Collections.emptyList();
        }
        Map<String, Gem> gemsMap = getGemListBuilder().getGemsMap();
        petinscriptionEntity petinscriptionEntity = petinscriptionCache.getInstance().getEntityByPlayer(playeridx);

        List<String> inscriptionIds = new ArrayList<>();

        for (String gemId : removeGemIds) {
            Gem gem = gemsMap.get(gemId);
            if (CollectionUtils.isEmpty(gem.getInscriptionIdList())) {
                continue;
            }
            inscriptionIds.addAll(gem.getInscriptionIdList());
        }
        if (CollectionUtils.isEmpty(inscriptionIds)) {
            return Collections.emptyList();
        }

        List<Common.Reward> rewards = petinscriptionEntity.inscriptionCfgIds2Reward(inscriptionIds);

        EventUtil.removeInscription(playeridx,inscriptionIds);

        return rewards;
    }
}