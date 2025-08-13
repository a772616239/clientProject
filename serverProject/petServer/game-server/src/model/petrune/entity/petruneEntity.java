/**
 * created by tool DAOGenerate
 */
package model.petrune.entity;

import cfg.GameConfig;
import cfg.PetRuneProperties;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.WarPetUpdate;
import common.GlobalData;
import common.IdGenerator;
import datatool.StringHelper;
import model.obj.BaseObj;
import model.pet.dbCache.petCache;
import model.petrune.dbCache.petruneCache;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.PetRuneEquipLog;
import platform.logs.statistics.RuneStatistics;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;

import static protocol.MessageId.MsgIdEnum.SC_PetRuneGet_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetRuneRemove_VALUE;

import protocol.PetDB.SerializablePetRune;
import protocol.PetDB.SerializablePetRune.Builder;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneBagRefresh;
import protocol.PetMessage.SC_PetRuneGet;
import protocol.PetMessage.SC_PetRuneRemove;
import protocol.PetMessage.SC_PetRuneUnEquip;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class petruneEntity extends BaseObj {
    private petruneEntity() {
    }

    @Override
    public String getClassType() {
        return "petruneEntity";
    }

    @Override
    public void putToCache() {
        petruneCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.rune = getRuneListBuilder().build().toByteArray();
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
    private byte[] rune;

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
    public byte[] getRune() {
        return rune;
    }

    /**
     * 设置符文信息
     */
    public void setRune(byte[] rune) {
        this.rune = rune;
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

    private SerializablePetRune.Builder runeListBuilder;

    public SerializablePetRune.Builder getRuneListBuilder() {
        if (runeListBuilder == null) {
            if (this.rune != null) {
                try {
                    runeListBuilder = SerializablePetRune.parseFrom(this.rune).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.printStackTrace(e);
                    LogUtil.error("parse rune to runeList builder fail, return new Builder");
                    runeListBuilder = SerializablePetRune.newBuilder();
                }
            } else {
                runeListBuilder = SerializablePetRune.newBuilder();
            }
        }
        return runeListBuilder;
    }

    /**
     * 消息：删除符文
     *
     * @param playerId 玩家id
     * @param idList   移除符文id
     */
    public static void sendRuneRemove(String playerId, List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        SC_PetRuneRemove.Builder result = SC_PetRuneRemove.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();
        retCode.setRetCode(RetCodeEnum.RCE_Success);
        result.addAllRemoveId(idList);
        result.setResult(retCode);
        GlobalData.getInstance().sendMsg(playerId, SC_PetRuneRemove_VALUE, result);
    }

    /**
     * 消息：添加符文
     *
     * @param playerId 玩家id
     * @param runeList 符文
     */
    public static void sendRuneGet(String playerId, List<Rune> runeList) {
        SC_PetRuneGet.Builder result = SC_PetRuneGet.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();
        retCode.setRetCode(RetCodeEnum.RCE_Success);
        result.setResult(retCode);
        result.setFinish(0);
        // 分批推送符文
        if (runeList.size() > GameConfig.getById(1).getMsgmaxnum()) {
            List<Rune> runeListTemp = new ArrayList<>();
            for (int i = 0; i < runeList.size(); ++i) {
                runeListTemp.add(runeList.get(i));
                if (i % GameConfig.getById(1).getMsgmaxnum() == 0) {
                    result.addAllRune(runeListTemp);
                    if (i == runeList.size()) {
                        result.setFinish(1);
                    }
                    GlobalData.getInstance().sendMsg(playerId, SC_PetRuneGet_VALUE, result);
                    runeListTemp.clear();
                    result.clearRune();
                }
            }
            if (runeList.size() != 0) {
                result.setFinish(1);
                result.addAllRune(runeListTemp);
                GlobalData.getInstance().sendMsg(playerId, SC_PetRuneGet_VALUE, result);
            }
        } else {
            result.setFinish(1);
            result.addAllRune(runeList);
            GlobalData.getInstance().sendMsg(playerId, SC_PetRuneGet_VALUE, result);
        }
    }

    /**
     * 消息：重设符文状态
     *
     * @param playerId 玩家id
     * @param idList   符文
     */
    public static void sendRuneReset(String playerId, List<String> idList) {
        if (playerId != null && idList != null && idList.size() > 0) {
            SC_PetRuneUnEquip.Builder result = SC_PetRuneUnEquip.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            retCode.setRetCode(RetCodeEnum.RCE_Success);
            result.setResult(retCode);
            result.addAllRuneId(idList);
            GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_PetRuneUnEquip_VALUE, result);
        }
    }

    /**
     * 推送符文背包容量更新
     *
     * @param playerId 玩家id
     * @param capacity 新容量
     */
    public static void sendPetRuneBagRefresh(String playerId, int capacity) {
        SC_PetRuneBagRefresh.Builder result = SC_PetRuneBagRefresh.newBuilder();
        result.setCapacity(capacity);
        RetCode.Builder retCode = RetCode.newBuilder();
        retCode.setRetCode(RetCodeEnum.RCE_Success);
        result.setResult(retCode);
        GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_PetRuneBagRefresh_VALUE, result);
    }

    public petruneEntity(String initPlayerId) {
        idx = IdGenerator.getInstance().generateId();
        playeridx = initPlayerId;
        // 背包扩容默认0
        bagenlarge = 0;
        // 背包总容量，读取配置
        capacity = GameConfig.getById(GameConst.CONFIG_ID).getPetrunebaginit();
    }

    /**
     * 获得所有的相同id的符文数量
     *
     * @param runeCfgId
     * @return
     */
    public int getSameRuneBookIdCountByBookId(int runeCfgId) {
        Builder runeListBuilder = getRuneListBuilder();
        int count = 0;
        for (Rune rune : runeListBuilder.getRuneMap().values()) {
            if (runeCfgId == rune.getRuneBookId()) {
                count++;
            }
        }
        return count;
    }

    public void putRune(Rune rune) {
        if (rune == null) {
            return;
        }

        this.runeListBuilder.putRune(rune.getId(), rune);
    }

    public void putAllRune(Collection<Rune> runeList) {
        if (runeList == null || runeList.isEmpty()) {
            return;
        }

        for (Rune rune : runeList) {
            putRune(rune);
        }
    }

    public Rune getRuneById(String runeId) {
        if (StringHelper.isNull(runeId)) {
            return null;
        }
        return getRuneListBuilder().getRuneMap().get(runeId);
    }


    public void removeRune(Rune rune) {
        if (rune == null) {
            return;
        }
        getRuneListBuilder().removeRune(rune.getId());
        sendRuneRemove(getPlayeridx(), Arrays.asList(rune.getId()));

        statisticsByRuneRemove(rune);

    }

    /**
     * 只是移除了符文
     */
    public void removeRuneWithoutSendCli(Rune rune) {
        if (rune == null) {
            return;
        }
        getRuneListBuilder().removeRune(rune.getId());
    }

    private void statisticsByRuneRemove(Rune rune) {
        int runRarity = PetRuneProperties.getQualityByCfgId(rune.getRuneBookId());
        Map<Integer, Long> map = Collections.singletonMap(runRarity, -1L);
        RuneStatistics.getInstance().updateOwnRarityMap(map);
        if (!StringUtils.isEmpty(rune.getRunePet())) {
            RuneStatistics.getInstance().updateEquipRarityMap(map);
        }
    }


    public void removeRuneByRuneList(List<Rune> runeList) {
        if (CollectionUtils.isEmpty(runeList)) {
            return;
        }
        List<String> runeIdxList = new LinkedList<>();
        runeList.stream().map(Rune::getId).
                filter(runeId -> !StringUtils.isEmpty(runeId)).
                forEach(runeId -> {
                    getRuneListBuilder().removeRune(runeId);
                    runeIdxList.add(runeId);
                });
        sendRuneRemove(getPlayeridx(), runeIdxList);
        statisticsByRunesRemove(runeList);
    }

    private void statisticsByRunesRemove(List<Rune> runes) {
        Map<Integer, Long> ownRarityUpdate = new HashMap<>();
        Map<Integer, Long> equipRarityUpdate = new HashMap<>();
        for (Rune rune : runes) {
            int runRarity = PetRuneProperties.getQualityByCfgId(rune.getRuneBookId());
            ownRarityUpdate.put(runRarity, -1L);
            if (!StringUtils.isEmpty(rune.getRunePet())) {
                equipRarityUpdate.put(runRarity, -1L);
            }
        }
        RuneStatistics.getInstance().updateOwnRarityMap(ownRarityUpdate);
        if (!CollectionUtils.isEmpty(equipRarityUpdate)) {
            RuneStatistics.getInstance().updateEquipRarityMap(equipRarityUpdate);
        }
    }

    public static RetCodeEnum runeCanRemove(String playerIdx, Rune rune) {
        if (rune == null) {
            return RetCodeEnum.RCE_Pet_RuneNotExist;
        }

        if (1 == rune.getRuneLockStatus()) {
            return RetCodeEnum.RCE_Pet_PetRuneLock;
        }

        if (petCache.getInstance().getPetById(playerIdx, rune.getRunePet()) != null) {
            return RetCodeEnum.RCE_Pet_PetRuneAlreadyEquiped;
        }

        return RetCodeEnum.RCE_Success;
    }

    /**
     * 判断符文是否可以移除
     *
     * @param runeIdx
     * @return
     */
    public RetCodeEnum runeCanRemove(String runeIdx) {
        return runeCanRemove(getPlayeridx(), getRuneById(runeIdx));
    }

    /**
     * 判断符文是否可以移除
     *
     * @param runeIdxList
     * @return
     */
    public boolean runeListCanRemove(List<String> runeIdxList) {
        if (runeIdxList == null || runeIdxList.isEmpty()) {
            return false;
        }

        for (String idx : runeIdxList) {
            if (RetCodeEnum.RCE_Success != runeCanRemove(idx)) {
                return false;
            }
        }

        return true;
    }

    public List<Rune> getRuneByList(List<String> idList) {
        if (idList == null || idList.isEmpty()) {
            return null;
        }

        List<Rune> result = new ArrayList<>();
        for (String idx : idList) {
            Rune runeById = getRuneById(idx);
            if (runeById != null) {
                result.add(runeById);
            }
        }
        return result;
    }

    /**
     * 判断符文是否已经被装备
     *
     * @return
     */
    public boolean runeIsEquipped(String runeIdx) {
        Rune runeById = getRuneById(runeIdx);
        if (runeById == null) {
            return false;
        }
        return !"".equals(runeById.getRunePet());
    }

    public boolean runeIsEquippedByPet(String runeIdx, String petIdx) {
        if (StringHelper.isNull(runeIdx) || StringHelper.isNull(petIdx)) {
            return false;
        }
        Rune runeById = getRuneById(runeIdx);
        if (runeById == null) {
            return false;
        }
        return petIdx.equals(runeById.getRunePet());
    }

    /**
     * 此方法不会判断该符文是否已经被装备，装备前请判断之前的符文状态
     *
     * @param runeIdx
     * @param petIdx
     * @param needRefreshPet
     */
    public void equipRune(String runeIdx, String petIdx, boolean needRefreshPet) {
        if (StringHelper.isNull(runeIdx)) {
            return;
        }

        Rune runeById = getRuneById(runeIdx);
        if (runeById == null) {
            return;
        }

        Rune build = runeById.toBuilder().setRunePet(petIdx).build();
        putRune(build);

        if (needRefreshPet) {
            // 重新计算宠物属性，通知
            petCache.getInstance().refreshPetProperty(getPlayeridx(), petIdx,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_UnEquipRune), true);
        }

        LogService.getInstance().submit(new PetRuneEquipLog(getPlayeridx(), petIdx, null, runeById));
        RuneStatistics.getInstance().updateEquipRarityMap(Collections.singletonMap(PetRuneProperties.getQualityByCfgId(runeById.getRuneBookId()), 1L));
    }

    /**
     * 卸下符文装备
     */
    public RetCodeEnum unEquipAllRuneById(Collection<String> runeIdxList, boolean needRefreshPet) {
        if (GameUtil.collectionIsEmpty(runeIdxList)) {
            return RetCodeEnum.RCE_Success;
        }

        List<Rune> allRune = runeIdxList.stream().map(this::getRuneById).collect(Collectors.toList());
        unEquipAllRune(allRune, needRefreshPet, true);
        return RetCodeEnum.RCE_Success;
    }

    /**
     * 卸下符文
     *
     * @param needRefreshPet 需要刷新宠物属性
     * @param sendUnEquipMsg
     */
    public RetCodeEnum unEquipRuneById(String runeIdx, boolean needRefreshPet, boolean sendUnEquipMsg) {
        Rune petRune = getRuneById(runeIdx);
        if (petRune == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        unEquipRune(petRune, needRefreshPet, sendUnEquipMsg);
        return RetCodeEnum.RCE_Success;
    }

    /**
     * 卸下符文
     *
     * @param needRefreshPet 需要刷新宠物属性
     * @param sendUnEquipMsg
     */
    private RetCodeEnum unEquipRune(Rune rune, boolean needRefreshPet, boolean sendUnEquipMsg) {
        if (rune == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        unEquipAllRune(Collections.singletonList(rune), needRefreshPet, sendUnEquipMsg);
        return RetCodeEnum.RCE_Success;
    }

    public void unEquipAllRune(Collection<Rune> runeList, boolean needRefreshPet, boolean sendUnEquipMsg) {
        if (GameUtil.collectionIsEmpty(runeList)) {
            return;
        }

        List<String> needUpdate = new ArrayList<>();
        Map<Integer, Long> rarityNum = new HashMap<>();
        Set<String> needUpdatePet = new HashSet<>();
        for (Rune rune : runeList) {
            String petIdx = rune.getRunePet();
            if (Objects.equals(petIdx, "")) {
                continue;
            }

            needUpdatePet.add(petIdx);
            Rune build = rune.toBuilder().setRunePet("").build();
            putRune(build);

            needUpdate.add(rune.getId());

            // 埋点日志
            LogService.getInstance().submit(new PetRuneEquipLog(getPlayeridx(), petIdx, rune, null));
            MapUtil.add2LongMapValue(rarityNum, PetRuneProperties.getQualityByCfgId(rune.getRuneBookId()), -1L);
        }

        if (!needUpdate.isEmpty() && sendUnEquipMsg) {
            sendRuneUnEquipMsg(needUpdate);
        }

        if (needRefreshPet && !needUpdatePet.isEmpty()) {
            needUpdatePet.forEach(petIdx -> {
                // 重新计算宠物属性，通知
                petCache.getInstance().refreshPetProperty(getPlayeridx(), petIdx,
                        ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_UnEquipRune), true);
                //通知战戈宠物更新
                EventUtil.triggerWarPetUpdate(getPlayeridx(), petIdx, WarPetUpdate.MODIFY);
            });
        }

        RuneStatistics.getInstance().updateEquipRarityMap(rarityNum);
    }

    public void sendRuneUnEquipMsg(List<String> runeIdList) {
        if (GameUtil.collectionIsEmpty(runeIdList)) {
            return;
        }

        SC_PetRuneUnEquip.Builder result = SC_PetRuneUnEquip.newBuilder();
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        result.addAllRuneId(runeIdList);
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_PetRuneUnEquip_VALUE, result);
    }


    public boolean runeIsExist(String runeIdx) {
        if (StringHelper.isNull(runeIdx)) {
            return false;
        }

        return getRuneById(runeIdx) != null;
    }


    /**
     * 移除指定宠物指定位置的符文
     *
     * @param petId
     * @param runeType
     * @return 返回卸下的符文id
     */
    public String unEquipSameTypeRune(String petId, int runeType) {
        List<Rune> runeListByPet = getRuneListByPet(petId);
        if (runeListByPet == null || runeListByPet.isEmpty()) {
            return null;
        }

        for (Rune rune : runeListByPet) {
            if (runeType == PetRuneProperties.getRuneType(rune.getRuneBookId())) {
                unEquipRune(rune, false, false);
                return rune.getId();
            }
        }
        return null;
    }

    public int getRuneType(String runeIdx) {
        Rune runeById = getRuneById(runeIdx);
        if (runeById == null) {
            return 0;
        }
        return PetRuneProperties.getRuneType(runeById.getRuneBookId());
    }

    /**
     * 获得指定宠物装备的符文
     *
     * @param petId
     * @return
     */
    public List<Rune> getRuneListByPet(String petId) {
        if (StringHelper.isNull(petId)) {
            return null;
        }
        List<Rune> result = new ArrayList<>();
        for (Rune value : getRuneListBuilder().getRuneMap().values()) {
            if (petId.equals(value.getRunePet())) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * 获得背包已使用容量
     */
    public int getOccupancy() {
        return getRuneListBuilder().getRuneCount();
    }

    /**
     * 移除某个宠物装备的所有符文
     *
     * @param petIdx
     */
    public void unEquipAllPetRune(String petIdx, boolean needRefresh) {
        List<Rune> runeListByPet = getRuneListByPet(petIdx);
        if (GameUtil.collectionIsEmpty(runeListByPet)) {
            return;
        }
        unEquipAllRune(runeListByPet, needRefresh, true);
    }

    /**
     * 移除宠物装备的所有符文
     *
     * @param petIdxList
     */
    public void unEquipAllPetListRune(List<String> petIdxList, boolean needRefresh) {
        if (GameUtil.collectionIsEmpty(petIdxList)) {
            return;
        }

        for (String petIdx : petIdxList) {
            unEquipAllPetRune(petIdx, needRefresh);
        }
    }
}