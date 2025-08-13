
package model.inscription;

import cfg.InscriptionCfg;
import cfg.InscriptionCfgObject;
import common.GlobalData;
import common.IdGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Data;
import model.inscription.dbCache.petinscriptionCache;
import model.obj.BaseObj;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager.Reason;
import protocol.Common;
import protocol.PetDB;
import protocol.PetDB.SerializablePetInscription;
import protocol.PetDB.SerializablePetInscription.Builder;
import protocol.PetMessage.Inscription;
import protocol.PetMessage.SC_AddInscription;
import protocol.PetMessage.SC_RemoveInscription;
import util.LogUtil;
import util.MapUtil;

@Data
public class petinscriptionEntity extends BaseObj {
    private String idx;
    private String playeridx;
    private byte[] inscription;
    private final Map<Integer, Integer> itemNum = new ConcurrentHashMap();
    private Builder db_data = null;

    private petinscriptionEntity() {
    }

    public petinscriptionEntity(String playerIdx) {
        this.idx = IdGenerator.getInstance().generateId();
        this.playeridx = playerIdx;
        this.putToCache();
    }

    public String getClassType() {
        return "petinscriptionEntity";
    }

    public void putToCache() {
        petinscriptionCache.put(this);
    }

    public void transformDBData() {
        this.getDb_data().clearInscriptionItem().putAllInscriptionItem(this.itemNum);
        this.inscription = this.getDb_data().build().toByteArray();
    }


    public String getBaseIdx() {
        return this.idx;
    }

    public void addItemNum(int cfgId, int num) {
        MapUtil.add2IntMapValue(this.itemNum, cfgId, num);
    }

    public boolean removeItemNum(int cfgId, int num) {
        Integer existNum = this.itemNum.get(cfgId);
        if (existNum != null && existNum >= num) {
            if (existNum == num) {
                this.itemNum.remove(cfgId);
            } else {
                this.itemNum.put(cfgId, existNum - num);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean inscriptionItemEnough(int cfgId, int num) {
        Integer existNum = this.itemNum.get(cfgId);
        return existNum != null && existNum >= num;
    }

    public Builder getDb_data() {
        if (db_data == null) {
            this.db_data = getDBInscriptionData();
        }
        return db_data;
    }

    private PetDB.SerializablePetInscription.Builder getDBInscriptionData() {
        if (this.inscription != null) {
            synchronized (this) {
                if (this.db_data != null) {
                    return this.db_data;
                }
                Builder builder;
                try {
                    builder = SerializablePetInscription.parseFrom(this.inscription).toBuilder();
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                    return null;
                }
                initItemNumMap(builder);
                return builder;
            }
        } else {
            return PetDB.SerializablePetInscription.newBuilder();
        }
    }


    private void initItemNumMap(Builder builder) {
        this.itemNum.clear();
        this.itemNum.putAll(builder.getInscriptionItemMap());
        builder.clearInscriptionItem();
    }

    public void playerObtainInscription(Map<Integer, Integer> cfgIdMap, Reason reason) {
        if (CollectionUtils.isEmpty(cfgIdMap)) {
            return;
        }
        LogUtil.info("player:{} obtain inscription ,cfgIdMap:{},reason:{} ", this.getPlayeridx(), cfgIdMap, reason);
        for (Entry<Integer, Integer> entry : cfgIdMap.entrySet()) {
            this.addItemNum(entry.getKey(), entry.getValue());
        }
        this.sendAddInscription(cfgIdMap);
    }

    public void sendAddInscription(List<Inscription> inscriptionEntities, Map<Integer, Integer> inscriptionItems) {
        protocol.PetMessage.SC_AddInscription.Builder msg = SC_AddInscription.newBuilder();
        if (!CollectionUtils.isEmpty(inscriptionItems)) {
            msg.addAllCfgId(inscriptionItems.keySet()).addAllNum(inscriptionItems.values());
        }

        if (!CollectionUtils.isEmpty(inscriptionEntities)) {
            msg.addAllInscriptions(inscriptionEntities);
        }

        GlobalData.getInstance().sendMsg(this.playeridx, 345, msg);
    }

    public void sendAddInscription(Map<Integer, Integer> inscriptionMap) {
        this.sendAddInscription(null, inscriptionMap);
    }

    public Inscription buildNewInscription(int cfgId) {
        protocol.PetMessage.Inscription.Builder entity = Inscription.newBuilder();
        return entity.setId(IdGenerator.getInstance().generateId()).setCfgId(cfgId).build();
    }

    public Inscription InscriptionItemConvert2Entity(int cfgId) {
        if (!this.removeItemNum(cfgId, 1)) {
            return null;
        } else {
            Inscription entity = this.buildNewInscription(cfgId);
            this.getDb_data().putInscriptionEntity(entity.getId(), entity);
            return entity;
        }
    }

    /**
     * @param inscriptionIds 消耗铭文对象的id list
     * @param consumeItem    消耗铭文道具的 <道具id,数量>
     * @return
     */
    public boolean consumeInscription(List<String> inscriptionIds, Map<Integer, Integer> consumeItem) {
        if (!consumeEnough(inscriptionIds, consumeItem)) {
            return false;
        }
        LogUtil.info("player:{} consumeInscription inscriptionIds:{},consumeItem:{}", this.playeridx, inscriptionIds, consumeItem);
        if (!CollectionUtils.isEmpty(consumeItem)) {
            for (Entry<Integer, Integer> entry : consumeItem.entrySet()) {
                this.removeItemNum(entry.getKey(), entry.getValue());
            }
        }
        if (!CollectionUtils.isEmpty(inscriptionIds)) {
            for (String inscriptionId : inscriptionIds) {
                this.removeEntity(inscriptionId);
            }
        }

        this.sendRemoveInscription(inscriptionIds, consumeItem);
        return true;
    }

    public void sendRemoveInscription(List<String> entityIds, Map<Integer, Integer> itemIds) {
        protocol.PetMessage.SC_RemoveInscription.Builder msg = SC_RemoveInscription.newBuilder();
        if (!CollectionUtils.isEmpty(entityIds)) {
            msg.addAllIds(entityIds);
        }

        if (!CollectionUtils.isEmpty(itemIds)) {
            msg.addAllNum(itemIds.values()).addAllCfgId(itemIds.keySet());
        }

        GlobalData.getInstance().sendMsg(this.getPlayeridx(), 346, msg);
    }

    private boolean removeEntity(String inscriptionId) {
        return this.getDb_data().removeInscriptionEntity(inscriptionId) != null;
    }

    public boolean consumeEnough(List<String> inscriptionIds, Map<Integer, Integer> consumeItem) {
        if (!CollectionUtils.isEmpty(inscriptionIds) &&
                !this.getDb_data().getInscriptionEntityMap().keySet().containsAll(inscriptionIds)) {
            return false;
        }
        for (Entry<Integer, Integer> entry : consumeItem.entrySet()) {
            if (!this.inscriptionItemEnough(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public void addAndNewInscription(int inscriptionId) {
        this.addItemNum(inscriptionId, 1);
        this.sendAddInscription(Collections.singletonMap(inscriptionId, 1));
    }

    public void entity2Item(String inscriptionId) {
        Inscription remove = (Inscription) this.getDb_data().getInscriptionEntityMap().get(inscriptionId);
        if (remove == null) {
            LogUtil.info("player:{} inscription entity2Item error cause by entity is null by inscriptionId:{}", new Object[]{this.getPlayeridx(), inscriptionId});
        } else {
            this.getDb_data().removeInscriptionEntity(inscriptionId);
            this.addItemNum(remove.getCfgId(), 1);
            this.sendRemoveInscription(Collections.singletonList(inscriptionId), null);
            this.sendAddInscription(Collections.singletonMap(remove.getCfgId(), 1));
        }
    }

    public List<Integer> getInscriptionCfgIds(List<String> inscriptionIdList) {
        if (CollectionUtils.isEmpty(inscriptionIdList)) {
            return Collections.emptyList();
        } else {
            Map<String, Inscription> entityMap = this.getDb_data().getInscriptionEntityMap();
            List<Integer> cfgIds = new ArrayList();

            for (String insId : inscriptionIdList) {
                Inscription inscription = entityMap.get(insId);
                if (inscription != null) {
                    InscriptionCfgObject cfg = InscriptionCfg.getById(inscription.getCfgId());
                    if (cfg != null) {
                        cfgIds.add(cfg.getId());
                    }
                }
            }

            return cfgIds;
        }
    }

    public Map<Integer, Integer> getItemNum() {
        return this.itemNum;
    }

    public List<Common.Reward> inscriptionCfgIds2Reward(List<String> inscriptionIds) {
        List<Integer> inscriptionCfgIds = getInscriptionCfgIds(inscriptionIds);
        Map<Integer, Long> collect = inscriptionCfgIds.stream().collect(Collectors.groupingBy(a -> a, Collectors.counting()));

        List<Common.Reward> rewards = new ArrayList<>();

        for (Entry<Integer, Long> entry : collect.entrySet()) {

            rewards.add(Common.Reward.newBuilder().setRewardType(Common.RewardTypeEnum.RTE_Inscription)
                    .setId(entry.getKey()).setCount(Math.toIntExact(entry.getValue())).build());
        }

        return rewards;

    }

    public void removeByIds(List<String> idxList) {
        for (String s : idxList) {
            this.getDb_data().getInscriptionEntityMap().remove(s);
        }
        sendRemoveInscription(idxList, Collections.emptyMap());
    }
}
