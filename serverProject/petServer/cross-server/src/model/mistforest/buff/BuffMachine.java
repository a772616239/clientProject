package model.mistforest.buff;

import cfg.MistBuffConfig;
import cfg.MistBuffConfigObject;
import common.GlobalData;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.UnitBuffData;
import protocol.ServerTransfer.CS_GS_UpdateOfflineBuffs;
import protocol.ServerTransfer.MistOfflineBuffData;

public class BuffMachine {
    private ConcurrentHashMap<Integer, Buff> buffList;
    private MistObject owner;

    public BuffMachine(MistObject owner) {
        this.owner = owner;
        buffList = new ConcurrentHashMap<>();
    }

    public void clear() {
        buffList.clear();
        owner = null;
    }

    public void revertOfflineBuffList(List<MistOfflineBuffData> offlineBuffList) {
        if (CollectionUtils.isEmpty(offlineBuffList)) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        for (MistOfflineBuffData offBuff : offlineBuffList) {
            if (offBuff.getBuffExpireTime() > curTime) {
                revertOfflineBuff(offBuff.getBuffId(), offBuff.getBuffLayer(), offBuff.getBuffExpireTime());
            }
        }
    }

    public void revertOfflineBuff(int buffId, int buffLayer, long expireTime) {
        Buff buff = new Buff(buffId, owner, owner);
        if (buff.getBuffCfg() != null) {
            HashMap<Integer, Long> params = new HashMap<>();
            params.put(MistTriggerParamType.BuffId, Long.valueOf(buffId));
            buffList.put(buff.getBuffId(), buff);
            if (buff.getBuffCfg().getMaxstackcount() > 0) {
                for (int i = 0; i < buffLayer; i++) {
                    buff.addEffect(params);
                }
            } else {
                buff.addEffect(params);
            }
            buff.addBuffData(0);
            buff.setExpireTime(expireTime);
            buff.setStackCount((byte) buffLayer);
            buff.cacheBuffTriggerParams(params);
        }
    }

    public ConcurrentHashMap<Integer, Buff> getBuffList() {
        return buffList;
    }

    public MistObject getOwner() {
        return owner;
    }

    public Buff getBuff(int buffId) {
        return buffList.get(buffId);
    }

    public Buff addBuff(int buffId, MistObject makerObj, HashMap<Integer, Long> params) {
        if (!checkDebuff(buffId)) {
            return null;
        }
        if (params == null) {
            params = new HashMap<>();
        }
        Long lifeTimeObj = params.get(MistTriggerParamType.BuffTime);
        long lifeTime = lifeTimeObj != null ? lifeTimeObj : 0;
        params.put(MistTriggerParamType.BuffId, Long.valueOf(buffId));
        if (!buffList.containsKey(buffId)) {
            Buff buff = new Buff(buffId, owner, makerObj);
            if (lifeTime > 0) {
                buff.setLifeTime(lifeTime);
            }
            if (buff.getBuffCfg() != null) {
                buffList.put(buff.getBuffId(), buff);
                buff.addEffect(params);
                buff.addBuffData(0);
                buff.cacheBuffTriggerParams(params);
                owner.addAddBuffCmd(buff, 0l);
                if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                    owner.getRoom().updateMistKeyHolder(owner, true);
                }
                transSaveOfflineBuff(buff, buff.getStackCount(), buff.getExpireTime());
                return buff;
            }
        } else {
            Buff buff = buffList.get(buffId);
            MistBuffConfigObject buffCfg = buff.getBuffCfg();
            if (buff.getHostObj() == null || buffCfg == null) {
                return null;
            }
            long curTime = GlobalTick.getInstance().getCurrentTime();
            long remainTime= buff.getBuffRemainTime(curTime);
            long pastTime = Math.max(0, Math.min(buff.getLifeTime(), buff.getLifeTime() - remainTime));
            if (lifeTime <= 0) {
                lifeTime = buffCfg.getLifetime();
            }
            if (buffCfg.getMaxstackcount() > 0) {
                pastTime = 0;
            } else {
                lifeTime += buff.getLifeTime();
            }
//            if (lifeTime <= 0) {
//                lifeTime = buffCfg.getLifetime();
//            }
//            if (buffCfg.getMaxstackcount() > 0) {
//                long maxLifeTime = buffCfg.getMaxstackcount() * buffCfg.getLifetime();
//                long tmpLifeTime = lifeTime + remainTime;
//                long oldStackCount = remainTime / buffCfg.getLifetime() + 1;
//                long newStackCount = tmpLifeTime / buffCfg.getLifetime() + 1;
//                if (oldStackCount > buffCfg.getMaxstackcount() || newStackCount > buffCfg.getMaxstackcount()) {
//                    pastTime = 0;
//                    lifeTime = maxLifeTime;
//                    newStackCount = buffCfg.getMaxstackcount();
//                } else if (newStackCount > oldStackCount){
//                    lifeTime = newStackCount * buffCfg.getLifetime();
//                    pastTime = lifeTime - tmpLifeTime;
//                } else {
//                    newStackCount = oldStackCount;
//                    lifeTime = buff.getLifeTime();
//                    pastTime = lifeTime - tmpLifeTime;
//                }
//                buff.setStackCount((byte) newStackCount);
//            } else {
//                lifeTime += buff.getLifeTime();
//            }

            buff.setLifeTime(lifeTime);
            if (buffCfg.getMaxstackcount() > 0 && buff.getStackCount() < buffCfg.getMaxstackcount()) {
                buff.addEffect(params);
            }
            if (buff.getStackCount() < buffCfg.getMaxstackcount()) {
                buff.setStackCount((byte) (buff.getStackCount() + 1));
            }
            buff.addBuffData(pastTime);
            buff.cacheBuffTriggerParams(params);
            owner.addUpdateBuffCmd(buff, curTime);

            if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                owner.getRoom().updateMistKeyHolder(owner, true);
            }
            transSaveOfflineBuff(buff, buff.getStackCount(), buff.getExpireTime());
            return buff;
        }
        return null;
    }

    public Buff addBuffWithPause(int buffId, MistObject makerObj, HashMap<Integer, Long> params) {
        if (!checkDebuff(buffId)) {
            return null;
        }
        if (params == null) {
            params = new HashMap<>();
        }
        Long lifeTimeObj = params.get(MistTriggerParamType.BuffTime);
        long lifeTime = lifeTimeObj != null ? lifeTimeObj : 0;
        params.put(MistTriggerParamType.BuffId, Long.valueOf(buffId));
        if (!buffList.containsKey(buffId)) {
            Buff buff = new Buff(buffId, owner, makerObj);
            if (lifeTime > 0) {
                buff.setLifeTime(lifeTime);
            }
            if (buff.getBuffCfg() != null) {
                buffList.put(buff.getBuffId(), buff);
                buff.addEffect(params);
                buff.addBuffData(0);
                buff.cacheBuffTriggerParams(params);
                long curTime = GlobalTick.getInstance().getCurrentTime();
                buff.pause(curTime);
                owner.addAddBuffCmd(buff, curTime);
                transSaveOfflineBuff(buff, buff.getStackCount(), buff.getExpireTime());
                return buff;
            }
        } else {
            Buff buff = buffList.get(buffId);
            MistBuffConfigObject buffCfg = buff.getBuffCfg();
            if (buff.getHostObj() == null || buffCfg == null) {
                return null;
            }
            long curTime = GlobalTick.getInstance().getCurrentTime();
            long remainTime= buff.getBuffRemainTime(curTime);
            long pastTime = Math.max(0, Math.min(buff.getLifeTime(), buff.getLifeTime() - remainTime));
            if (lifeTime <= 0) {
                lifeTime = buffCfg.getLifetime();
            }
            if (buffCfg.getMaxstackcount() > 0) {
                pastTime = 0;
            } else {
                lifeTime += buff.getLifeTime();
            }
            buff.setLifeTime(lifeTime);
            if (buffCfg.getMaxstackcount() > 0 && buff.getStackCount() < buffCfg.getMaxstackcount()) {
                buff.addEffect(params);
            }
            if (buff.getStackCount() < buffCfg.getMaxstackcount()) {
                buff.setStackCount((byte) (buff.getStackCount() + 1));
            }
            buff.addBuffData(pastTime);
            buff.cacheBuffTriggerParams(params);
            buff.pause(curTime);
            owner.addUpdateBuffCmd(buff, curTime);
            transSaveOfflineBuff(buff, buff.getStackCount(), buff.getExpireTime());
            return buff;
        }
        return null;
    }

    public Buff robBuff(Buff buff, long curTime) {
        if (buff == null) {
            return null;
        }
        Buff ownedBuff = getBuff(buff.getBuffId());
        if (ownedBuff != null) {
            ownedBuff.resume(curTime);
            owner.addUpdateBuffCmd(buff, curTime);
            return ownedBuff;
        }
        if (buff.getHostObj() != null){
            buff.getHostObj().getBufMachine().beenRobbedBuff(buff.getBuffId());
        }
        buff.setMakerObj(owner);
        buff.setHostObj(owner);
        buff.resume(curTime);
        buffList.put(buff.getBuffId(), buff);
        buff.robEffect();
        owner.addUpdateBuffCmd(buff, curTime);
        transSaveOfflineBuff(buff, buff.getStackCount(), buff.getExpireTime());
        return buff;
    }

    public void removeBuff(int buffId) {
        Buff buff = buffList.get(buffId);
        if (buff != null) {
            buff.removeEffect();
            owner.addRemoveBuffCmd(buffId);
            buffList.remove(buffId);
            buff.clear();
            if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                owner.getRoom().updateMistKeyHolder(null, true);
            }
            transSaveOfflineBuff(buff, 0, 0l);
        }
    }

    public void beenRobbedBuff(int buffId) {
        Buff buff = buffList.get(buffId);
        if (buff != null) {
            buff.beenRobbedEffect();
            owner.addRemoveBuffCmd(buffId);
            buffList.remove(buffId);
            transSaveOfflineBuff(buff, 0, 0l);
        }
    }

    public void interruptBuffById(int buffId) {
        Buff buff = buffList.get(buffId);
        if (buff != null) {
            buff.interruptBuff();
            owner.addRemoveBuffCmd(buffId);
            buffList.remove(buffId);
            buff.clear();
            if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                owner.getRoom().updateMistKeyHolder(null, true);
            }
        }
    }

    public void interruptBuffByType(int interruptType) {
        if (buffList.isEmpty()) {
            return;
        }
        List<Integer> interruptedBuffList = new ArrayList<>();
        for (Buff buff : buffList.values()) {
            if (buff == null || buff.getBuffCfg() == null) {
                continue;
            }
            for (Integer type : buff.getBuffCfg().getInterrupttype()) {
                if (type == interruptType) {
                    buff.interruptBuff();
                    owner.addRemoveBuffCmd(buff.getBuffId());
                    interruptedBuffList.add(buff.getBuffId());
                    buff.clear();
                    if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                        owner.getRoom().updateMistKeyHolder(null, true);
                    }
                    break;
                }
            }
        }
        for (Integer buffId : interruptedBuffList) {
            buffList.remove(buffId);
        }
    }

    public void pauseBuff(long curTime) {
        for (Buff buff : buffList.values()) {
            if (buff.getBuffCfg() == null) {
                continue;
            }
            int pauseTime = buff.getBuffCfg().getPausedecreasetime();
            if (pauseTime == 0) {
                continue;
            }
            buff.pause(curTime);
            owner.addUpdateBuffCmd(buff, curTime);
            if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                owner.getRoom().updateMistKeyHolder(owner, true);
            }
        }
    }

    public void pauseBuffById(int buffId) {
        Buff buff = getBuff(buffId);
        if (buff == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (!buff.pause(curTime)) {
            return;
        }
        owner.addUpdateBuffCmd(buff, curTime);
    }

    public void resumeBuffById(int buffId) {
        Buff buff = getBuff(buffId);
        if (buff == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (!buff.resume(curTime)) {
            return;
        }
        owner.addUpdateBuffCmd(buff, curTime);
    }

    public void resumeBuffByTarget(long curTime, boolean beenRobbed) {
        long targetId = owner.getAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE);
        MistFighter target = owner.getRoom().getObjManager().getMistObj(targetId);
        beenRobbed = beenRobbed && target != null && target.getId() != owner.getId();
        for (Buff buff : buffList.values()) {
            if (buff.getBuffCfg() == null) {
                continue;
            }
            int pauseTime = buff.getBuffCfg().getPausedecreasetime();
            if (pauseTime == 0) {
                continue;
            }
            if (!buff.isBuffPaused(curTime)) {
                continue;
            }
            if (beenRobbed) {
                target.getBufMachine().robBuff(buff, curTime);
                if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                    target.getRoom().updateMistKeyHolder(target, true);
                }
            } else {
                buff.resume(curTime);
                owner.addUpdateBuffCmd(buff, curTime);
                if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                    owner.getRoom().updateMistKeyHolder(owner, true);
                }
            }

        }
    }

    public void ownerDead() {
        Iterator<Entry<Integer, Buff>> iter = buffList.entrySet().iterator();
        while (iter.hasNext()) {
            Buff buff = iter.next().getValue();
            if (buff == null) {
                continue;
            }
            buff.removeEffect();
            owner.addRemoveBuffCmd(buff.getBuffId());
            buff.clear();
            if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                owner.getRoom().updateMistKeyHolder(null, true);
            }
        }
        buffList.clear();
    }

    public void clearDeBuff() {
        if (buffList.isEmpty()) {
            return;
        }
        List<Integer> removeList = new ArrayList<>();
        for (Buff buff : buffList.values()) {
            if (buff == null || buff.getBuffCfg() == null || !buff.getBuffCfg().getIsdebuff()) {
                continue;
            }
            buff.interruptBuff();
            owner.addRemoveBuffCmd(buff.getBuffId());
            removeList.add(buff.getBuffId());
            buff.clear();
            if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                owner.getRoom().updateMistKeyHolder(null, true);
            }
        }
        for (Integer buffId : removeList) {
            buffList.remove(buffId);
        }
    }

    public boolean checkDebuff(int buffId) {
        if (owner.getAttribute(MistUnitPropTypeEnum.MUPT_IsImmuneState_VALUE) <= 0) {
            return true;
        }
        MistBuffConfigObject buffCfg = MistBuffConfig.getById(buffId);
        if (buffCfg == null) {
            return true;
        }
        return !buffCfg.getIsdebuff();
    }

    public void onTick(long curTime) {
        if (buffList.isEmpty()) {
            return;
        }
        Iterator<Entry<Integer, Buff>> iter = buffList.entrySet().iterator();
        Buff buff;
        while (iter.hasNext()) {
            Entry<Integer, Buff> entry = iter.next();
            buff = entry.getValue();
            if (owner == null || !owner.isAlive()) {
                buff.removeEffect();
                iter.remove();
                buff.clear();
                continue;
            }
            if (buff.isBuffPaused(curTime)) {
                continue;
            }
            if (buff.isBuffExpired(curTime)) {
                buff.removeEffect();
                owner.addRemoveBuffCmd(buff.getBuffId());
                iter.remove();
                buff.clear();
                if (buff.getBuffId() == MistConst.MistWaitBossBattleBuffId) {
                    owner.getRoom().updateMistKeyHolder(null, true);
                }
            } else {
                buff.periodicEffect(curTime);
            }
        }
    }

    public List<UnitBuffData> getAllBuffData() {
        if (buffList.isEmpty()) {
            return null;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        List<UnitBuffData> buffDtaList = new ArrayList<>();
        for (Buff buff : buffList.values()) {
            buffDtaList.add(buff.buildBuffData(curTime));
        }
        return buffDtaList;
    }

    public long getBossKeyBuffRemainTime(long curTime) {
        Buff buff = getBuff(MistConst.MistWaitBossBattleBuffId);
        if (buff == null) {
            return 0;
        }
        return (int) buff.getBuffRemainTime(GlobalTick.getInstance().getCurrentTime());
    }

    public void transSaveOfflineBuff(Buff buff, int buffLayer, long expireTime) {
        if (!(owner instanceof MistFighter)) {
            return;
        }
        if (buff.getBuffCfg() == null || !buff.getBuffCfg().getIsofflinebuff()) {
            return;
        }
        MistPlayer player = ((MistFighter) owner).getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        CS_GS_UpdateOfflineBuffs.Builder builder = CS_GS_UpdateOfflineBuffs.newBuilder();
        builder.setIdx(player.getIdx());
        builder.getOfflineBuffBuilder().setBuffId(buff.getBuffId()).setBuffLayer(buffLayer).setBuffExpireTime(expireTime);
        GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_UpdateOfflineBuffs_VALUE, builder);
    }
}
