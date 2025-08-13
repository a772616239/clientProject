/**
 * created by tool DAOGenerate
 */
package model.mistplayer.entity;

import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import cfg.CrossConstConfig;
import cfg.GameConfig;
import cfg.MistCommonConfig;
import cfg.MistCommonConfigObject;
import cfg.MistDropItemConfig;
import cfg.MistLootPackCarryConfig;
import cfg.MistLootPackCarryConfigObject;
import cfg.MistMapObjConfigObject;
import cfg.PlayerLevelConfig;
import cfg.PlayerLevelConfigObject;
import com.google.protobuf.GeneratedMessageV3.Builder;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalData;
import common.GlobalTick;
import common.load.ServerConfig;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import model.mistforest.MistConst;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.obj.BaseObj;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetBuffData;
import protocol.Battle.PlayerBaseInfo;
import protocol.Battle.SkillBattleDict;
import protocol.Common.LanguageEnum;
import protocol.Common.SC_RetCode;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistAlchemyData;
import protocol.MistForest.MistPlayerInfo;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.SC_ChooseAlchemyReward;
import protocol.MistForest.SC_StartAlchemy;
import protocol.PetMessage.PetProperty;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_ExchangeMistRoom;
import protocol.ServerTransfer.CS_GS_MistForestRoomInfo;
import protocol.ServerTransfer.CS_GS_UpdateAlchemyData;
import protocol.ServerTransfer.CS_GS_UpdateCarryReward;
import protocol.ServerTransfer.CS_GS_UpdateOffPropData;
import protocol.ServerTransfer.GS_CS_JoinMistForest;
import protocol.ServerTransfer.PlayerOffline;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import protocol.TransServerCommon.EnumMistSelfOffPropData;
import protocol.TransServerCommon.MistGhostBusterSyncData;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;
import util.ObjUtil;
import util.TimeUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class MistPlayer extends BaseObj {


    public String getClassType() {
        return "player";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private String userid;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private int avatar;

    /**
     *
     */
    private int avatarBorder;

    /**
     *
     */
    private int avatarBorderRank;

    /**
     *
     */
    private int level;

    /**
     *
     */
    private int experience;

    /**
     *
     */
    private int vip;

    /**
     *
     */
    private int vipexperience;



    /**
     * 获得
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得
     */
    public String getUserid() {
        return userid;
    }

    /**
     * 设置
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * 获得
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获得
     */
    public int getAvatar() {
        return avatar;
    }

    /**
     * 设置
     */
    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    /**
     * 获得
     */
    public int getAvatarBorder() {
        return avatarBorder;
    }

    /**
     * 设置
     */
    public void setAvatarBorder(int avatarBorder) {
        this.avatarBorder = avatarBorder;
    }

    /**
     * 获得
     */
    public int getAvatarBorderRank() {
        return avatarBorderRank;
    }

    /**
     * 设置
     */
    public void setAvatarBorderRank(int avatarBorderRank) {
        this.avatarBorderRank = avatarBorderRank;
    }

    /**
     * 获得
     */
    public int getLevel() {
        return level;
    }

    /**
     * 设置
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 获得
     */
    public int getExperience() {
        return experience;
    }

    /**
     * 设置
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * 获得
     */
    public int getVip() {
        return vip;
    }

    /**
     * 设置
     */
    public void setVip(int vip) {
        this.vip = vip;
    }

    /**
     * 获得
     */
    public int getVipexperience() {
        return vipexperience;
    }

    /**
     * 设置
     */
    public void setVipexperience(int vipexperience) {
        this.vipexperience = vipexperience;
    }


    public String getBaseIdx() {

        return idx;
    }

    @Override
    public void putToCache() {

    }

    @Override
    public void transformDBData() {

    }

    /**
     * ==================================自动生成分割线================================================
     */

    public MistPlayer() {
        this.skillList = new ArrayList<>();
        this.petDataList = new ArrayList<>();
        this.acceptTeamInvite = true; // 默认为接受
        this.remainPetHpMap = new HashMap<>();
        this.playerBaseAdditions = new HashMap<>();
        this.ownedRewardMap = new HashMap<>();
        this.gainRewardMap = new HashMap<>();
        this.alchemyDataMap = new HashMap<>();
    }

    private long offlineTime;
    private boolean online;

    private int serverIndex;
    private MistRoom mistRoom;
    private long fighterId;
    private int camp;
    private long fightPower;
    private boolean acceptTeamInvite;

    private int mainLineUnlockLevel;

    private int mistStamina;

    private int titleId;
    private int newTitleId;

    private List<SkillBattleDict> skillList;
    private List<BattlePetData> petDataList;
    private Map<Integer, Integer> playerBaseAdditions;

    private long lastUpdateRemainHpTime;
    private Map<String, Integer> remainPetHpMap;

    private Map<Integer, Integer> ownedRewardMap;
    private Map<Integer, Integer> gainRewardMap;

    private boolean isRobot;

    private boolean readyState;

    private boolean gainActivityBossBoxFlag;

    private Map<Integer, MistAlchemyData> alchemyDataMap;

    private int crossVipLv;

    private int eliteMonsterRewardTimes;

    private int recoverIntervalExtRate; // 回血间隔额外比例

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getOfflineTime() {
        return offlineTime;
    }

    public void setOfflineTime(long offlineTime) {
        this.offlineTime = offlineTime;
    }

    public int getServerIndex() {
        return serverIndex;
    }

    public void setServerIndex(int serverIndex) {
        this.serverIndex = serverIndex;
    }

    public MistRoom getMistRoom() {
        return mistRoom;
    }

    public void setMistRoom(MistRoom mistRoom) {
        this.mistRoom = mistRoom;
    }

    public long getFighterId() {
        return fighterId;
    }

    public void setFighterId(long fighterId) {
        this.fighterId = fighterId;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public boolean isRobot() {
        return isRobot;
    }

    public void setRobot(boolean robot) {
        isRobot = robot;
    }

    public boolean isReadyState() {
        return readyState;
    }

    public void setReadyState(boolean readyState) {
        this.readyState = readyState;
    }

    public long getFightPower() {
        return fightPower;
    }

    public void setFightPower(long fightPower) {
        this.fightPower = fightPower;
    }

    public int getMainLineUnlockLevel() {
        return mainLineUnlockLevel;
    }

    public void setMainLineUnlockLevel(int mainLineUnlockLevel) {
        this.mainLineUnlockLevel = mainLineUnlockLevel;
    }

    public int getMistStamina() {
        return mistStamina;
    }

    public void setMistStamina(int mistStamina) {
        this.mistStamina = mistStamina;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public int getNewTitleId() {
        return newTitleId;
    }

    public void setNewTitleId(int newTitleId) {
        this.newTitleId = newTitleId;
    }

    public boolean getGainActivityBossBoxFlag() {
        return gainActivityBossBoxFlag;
    }

    public void setGainActivityBossBoxFlag(boolean gainActivityBossBoxFlag) {
        this.gainActivityBossBoxFlag = gainActivityBossBoxFlag;
    }

    public int getDailyBattleRewardCount() {
        PlayerLevelConfigObject config = PlayerLevelConfig.getByLevel(getLevel());
        if (config == null) {
            return 0;
        }
        Integer rewardCountObj = ownedRewardMap.get(config.getMistbattlerewarditemid());
        return rewardCountObj != null ? config.getMistbattlerewarditemid() : 0;
    }

    public boolean isAcceptTeamInvite() {
        return acceptTeamInvite;
    }

    public void setAcceptTeamInvite(boolean acceptTeamInvite) {
        this.acceptTeamInvite = acceptTeamInvite;
    }

    public Map<Integer, MistAlchemyData> getAlchemyDataMap() {
        return alchemyDataMap;
    }

    public List<SkillBattleDict> getSkillList() {
        return skillList;
    }

    public int getCrossVipLv() {
        return crossVipLv;
    }

    public void setCrossVipLv(int crossVipLv) {
        this.crossVipLv = crossVipLv;
    }

    public int getEliteMonsterRewardTimes() {
        return eliteMonsterRewardTimes;
    }

    public void setEliteMonsterRewardTimes(int eliteMonsterRewardTimes) {
        this.eliteMonsterRewardTimes = eliteMonsterRewardTimes;
    }

    public int getRecoverIntervalExtRate() {
        return recoverIntervalExtRate;
    }

    public void setRecoverIntervalExtRate(int recoverIntervalExtRate) {
        this.recoverIntervalExtRate = recoverIntervalExtRate;
    }

    public void updateSkillList(List<SkillBattleDict> skillList) {
        if (skillList == null) {
            return;
        }
        this.skillList.clear();
        this.skillList.addAll(skillList);
    }

    public List<BattlePetData> getPetDataList() {
        List<BattlePetData> battlePetList = new ArrayList<>();
        for (BattlePetData petData : petDataList) {
            if (playerBaseAdditions.isEmpty() && !remainPetHpMap.containsKey(petData.getPetId())) {
                battlePetList.add(petData);
                continue;
            }
            BattlePetData.Builder petBuilder = BattlePetData.newBuilder().mergeFrom(petData);
            for (int index = 0; index < petBuilder.getPropDict().getKeysCount(); index++) {
                int key = petBuilder.getPropDictBuilder().getKeysValue(index);
                if (key == PetProperty.Current_Health_VALUE && remainPetHpMap.containsKey(petData.getPetId())) {
                    petBuilder.getPropDictBuilder().setValues(index, remainPetHpMap.get(petData.getPetId()));
                }
                Integer addition = playerBaseAdditions.get(key);
                if (addition != null) {
                    long propVale = petBuilder.getPropDictBuilder().getValues(index) + addition;
                    petBuilder.getPropDictBuilder().setValues(index, propVale);
                }
            }
            battlePetList.add(petBuilder.build());
        }
        return battlePetList;
    }

    public void updatePetDataList(List<BattlePetData> petDataList) {
        if (petDataList == null) {
            return;
        }
        this.petDataList.clear();
        this.petDataList.addAll(petDataList);
        this.fightPower = 0;
        for (BattlePetData petData : petDataList) {
            this.fightPower += petData.getAbility();
        }
    }

    public void updateBaseAdditions(Map<Integer, Integer> additions) {
        if (additions == null) {
            return;
        }
        playerBaseAdditions.clear();
        playerBaseAdditions.putAll(additions);
    }

    public List<PetBuffData> getExtendBuffList() {
        if (getMistRoom() == null) {
            return null;
        }
        MistFighter fighter = getMistRoom().getObjManager().getMistObj(getFighterId());
        if (fighter == null) {
            return null;
        }
        return fighter.getExtendBuffList(false);
    }

    public void setPetRemainHp(int camp, List<BattleRemainPet> remainPetList) {
//        remainPetHpMap.clear();
        int failedHpRate = CrossConstConfig.getById(GameConst.ConfigId).getBattlefailedhprate();
        if (camp == 0) { // 阵营为0 表示失败，直接设置生命值
            for (BattlePetData petData : petDataList) {
                remainPetHpMap.put(petData.getPetId(), failedHpRate);
            }
        } else if (!CollectionUtils.isEmpty(remainPetList)) {
            for (BattleRemainPet remainPet : remainPetList) {
                if (remainPet.getCamp() != camp) {
                    continue;
                }
                for (BattlePetData petData : petDataList) {
                    if (petData.getPetId().equals(remainPet.getPetId())) {
                        int remainHpRate = Math.max(remainPet.getRemainHpRate(), failedHpRate);
                        remainPetHpMap.put(petData.getPetId(), remainHpRate);
                    }
                }
            }
        }
        Event event = Event.valueOf(EventType.ET_CalcFighterRemainHpRate, this, getMistRoom());
        EventManager.getInstance().dispatchEvent(event);
    }

    public void updateRemainPetHp(long curTime) {
        if (remainPetHpMap.isEmpty()) {
            return;
        }
        MistFighter fighter = getMistRoom().getObjManager().getMistObj(getFighterId());
        if (fighter == null || fighter.isBattling()) {
            return;
        }
        if (lastUpdateRemainHpTime == 0) {
            lastUpdateRemainHpTime = curTime;
            return;
        }
        boolean inSafeRegion = fighter.isInSafeRegion();
        long recoverInterval = inSafeRegion ? CrossConstConfig.getById(GameConst.ConfigId).getSaferegionrecoverhpinterval()
                : CrossConstConfig.getById(GameConst.ConfigId).getOutersaferegionrecoverhpinterval();

        recoverInterval = (recoverInterval * TimeUtil.MS_IN_A_S * (1000 + recoverIntervalExtRate)) / 1000;
        if (lastUpdateRemainHpTime + recoverInterval > curTime) {
            return;
        }
        lastUpdateRemainHpTime = curTime;

        int recoverRate = inSafeRegion ? CrossConstConfig.getById(GameConst.ConfigId).getSaferegionrecoverhprate()
                : CrossConstConfig.getById(GameConst.ConfigId).getOutersaferegionrecoverhprate();
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(getCrossVipLv());
        if (cfg != null) {
            if (cfg.getMisthprecoverrate() > 0) {
                recoverRate += recoverRate * cfg.getMisthprecoverrate() / 1000;
            }
        }
        Iterator<Entry<String, Integer>> iter = remainPetHpMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Integer> entry = iter.next();
            if (entry.getValue() >= GameConst.PetMaxHpRate) {
                iter.remove();
                continue;
            }
            int newHpRate = Math.min(GameConst.PetMaxHpRate, entry.getValue() + recoverRate);
            remainPetHpMap.put(entry.getKey(), newHpRate);
        }

        Event event = Event.valueOf(EventType.ET_CalcFighterRemainHpRate, this, getMistRoom());
        EventManager.getInstance().dispatchEvent(event);
    }

    public long calcTotalRemainHpRate() {
        long totalHp = 0;
        long remainHp = 0;
        long petMaxHp;
        for (BattlePetData petData : petDataList) {
            for (int index = 0; index < petData.getPropDict().getKeysCount(); index++) {
                PetProperty propType = petData.getPropDict().getKeys(index);
                if (propType == PetProperty.HEALTH) {
                    petMaxHp = petData.getPropDict().getValues(index);
                    totalHp += petMaxHp;
                    Integer remainHpRate = remainPetHpMap.get(petData.getPetId());
                    if (remainHpRate != null) {
                        remainHp += remainHpRate * petMaxHp / 1000;
                    } else {
                        remainHp += petMaxHp;
                    }
                    break;
                }
            }
        }
        return totalHp > 0 ? remainHp * 1000 / totalHp : 0;
    }

    public void changeCurrentHp(int rate) {
        if (rate == 0 || rate > GameConst.PetMaxHpRate || rate < -GameConst.PetMaxHpRate) {
            return;
        }
        Integer newHpRate;
        int failedHpRate = CrossConstConfig.getById(GameConst.ConfigId).getBattlefailedhprate();
        for (BattlePetData petData : petDataList) {
            newHpRate = remainPetHpMap.get(petData.getPetId());
            if (newHpRate != null) {
                newHpRate = Math.max(failedHpRate, Math.min(GameConst.PetMaxHpRate, newHpRate + rate));
                remainPetHpMap.put(petData.getPetId(), newHpRate);
            } else if (rate < 0) {
                newHpRate = Math.max(failedHpRate, GameConst.PetMaxHpRate + rate);
                remainPetHpMap.put(petData.getPetId(), newHpRate);
            }
        }
        if (getMistRoom() != null) {
            Event event = Event.valueOf(EventType.ET_CalcFighterRemainHpRate, this, getMistRoom());
            EventManager.getInstance().dispatchEvent(event);
        }
    }

    public Map<Integer, Integer> addNewGainRewardMap(Map<Integer, Integer> newRewardMap) {
        PlayerLevelConfigObject plyLvCfg = PlayerLevelConfig.getByLevel(getLevel());
        if (plyLvCfg == null) {
            return null;
        }
        int realGainCount;
        Integer tmpGainCountObj;
        MistLootPackCarryConfigObject config;
        List<Integer> removeRewards = null; // 移除到上限的奖励
        for (Entry<Integer, Integer> entry : newRewardMap.entrySet()) {
            config = MistLootPackCarryConfig.getById(entry.getKey());
            if (config == null) {
                continue;
            }
            int gainCount = 0;
            realGainCount = entry.getValue();
            tmpGainCountObj = ownedRewardMap.get(entry.getKey());
            if (tmpGainCountObj != null) {
                gainCount += tmpGainCountObj;
            }
            tmpGainCountObj = gainRewardMap.get(entry.getKey());
            if (tmpGainCountObj != null) {
                gainCount += tmpGainCountObj;
            }
            if (config.getId() == MistConst.FriendPointItemId) {
                realGainCount = Math.min(realGainCount, MistConst.getGaineFrienPointDailyLimit(getVip()) - gainCount);
            } else {
                int limit = config.getLimitByRule(mistRoom.getMistRule());
                if (limit < 0) {
                    limit = plyLvCfg.getLimitByRule(mistRoom.getMistRule());
                }
                if (limit > 0) {
                    realGainCount = Math.min(realGainCount, limit - gainCount);
                }
            }

            if (realGainCount <= 0) {
                if (removeRewards == null) {
                    removeRewards = new ArrayList<>();
                }
                removeRewards.add(entry.getKey());
                continue;
            }
            if (realGainCount != entry.getValue()) {
                newRewardMap.put(entry.getKey(), realGainCount);
            }

            gainRewardMap.merge(entry.getKey(), realGainCount, (oldNum, newNum) -> oldNum + newNum);
        }

        if (removeRewards != null) {
            for (Integer removeReward : removeRewards) {
                newRewardMap.remove(removeReward);
            }
        }
        updateCarryRewards(newRewardMap);
        return newRewardMap;
    }

    public boolean checkRewardEnough(int rewardId, int checkCount) {
        Integer count = gainRewardMap.get(rewardId);
        return count != null && count >= checkCount;
    }

    public void consumeGainReward(int rewardId, int consumeCount) {
        Integer count = gainRewardMap.get(rewardId);
        if (count == null ) {
            return;
        }
        count -= consumeCount;
        if (count > 0) {
            gainRewardMap.put(rewardId, count);
        } else {
            gainRewardMap.remove(rewardId);
            count = 0;
        }
        CS_GS_UpdateCarryReward.Builder builder = CS_GS_UpdateCarryReward.newBuilder();
        builder.setIdx(getIdx());
        builder.putDeltaCarryRewards(rewardId, count);
        builder.setMistRuleValue(mistRoom.getMistRule());
        GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_UpdateCarryReward_VALUE, builder);
    }

    public void updateCarryRewards(Map<Integer, Integer> deltaRewardMap) {
        CS_GS_UpdateCarryReward.Builder builder = CS_GS_UpdateCarryReward.newBuilder();
        builder.setIdx(getIdx());
        for (Entry<Integer, Integer> entry : deltaRewardMap.entrySet()) {
            Integer countObj = gainRewardMap.get(entry.getKey());
            int count = countObj != null ? countObj : 0;
            builder.putDeltaCarryRewards(entry.getKey(), count);
        }
        builder.setMistRuleValue(mistRoom.getMistRule());
        GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_UpdateCarryReward_VALUE, builder);
    }

    public Map<Integer, Integer> dropGainReward(boolean isPkMode) {
        if (gainRewardMap.isEmpty()) {
            return null;
        }
        int dropNum = MistDropItemConfig.getInstance().calcDropNum(isPkMode);
        if (dropNum == 0) {
            return null;
        }
        PlayerLevelConfigObject plyLvCfg = PlayerLevelConfig.getByLevel(getLevel());
        if (plyLvCfg == null) {
            return null;
        }
        int perRewardCount = plyLvCfg.getLimitByRule(mistRoom.getMistRule()) * GameConfig.getById(GameConst.ConfigId).getMistrefiningstonespercent() / 1000;
        List<Integer> calcRewardList = new LinkedList<>();
        MistLootPackCarryConfigObject config;
        for (Entry<Integer, Integer> entry : gainRewardMap.entrySet()) {
            config = MistLootPackCarryConfig.getById(entry.getKey());
            if (config == null) {
                continue;
            }
            int limit = config.getLimitByRule(mistRoom.getMistRule());
            if (limit < 0) {
                int rewardCopies = entry.getValue() / perRewardCount;
                int tmpCount = entry.getValue() % perRewardCount == 0 ? rewardCopies : rewardCopies + 1;
                for (int i = 0; i < tmpCount; i++) {
                    calcRewardList.add(entry.getKey());
                }
            } else {
                calcRewardList.add(entry.getKey());
            }
        }

        HashMap<Integer, Integer> tmpRewardMap = new HashMap<>();
        Random rand = new Random();
        int calcCount = Math.min(dropNum, calcRewardList.size());
        for (int i = 0; i < calcCount; i++) {
            int index = rand.nextInt(calcRewardList.size());
            Integer itemId = calcRewardList.get(index);
            if (itemId == null) {
                continue;
            }
            tmpRewardMap.merge(itemId, 1, (oldNum, newNum) -> oldNum + newNum);
            calcRewardList.remove(index);
        }

        for (Entry<Integer, Integer> entry : tmpRewardMap.entrySet()) {
            if (!gainRewardMap.containsKey(entry.getKey())) {
                continue;
            }
            config = MistLootPackCarryConfig.getById(entry.getKey());
            int limit = config.getLimitByRule(mistRoom.getMistRule());
            if (limit < 0) {
                gainRewardMap.merge(entry.getKey(), entry.getValue(), (oldVal, newVal) -> {
                    int count = oldVal - newVal * perRewardCount;
                    if (count > 0) {
                        tmpRewardMap.put(entry.getKey(), newVal * perRewardCount);
                        return count;
                    } else {
                        tmpRewardMap.put(entry.getKey(), oldVal);
                        return null;
                    }
                });
            } else {
                gainRewardMap.merge(entry.getKey(), entry.getValue(), (oldVal, newVal) -> {
                    int count = oldVal - newVal;
                    if (count > 0) {
                        return count;
                    } else {
                        tmpRewardMap.put(entry.getKey(), oldVal);
                        return null;
                    }
                });
            }
        }

        updateCarryRewards(tmpRewardMap);
        return tmpRewardMap;
    }

    public void startAlchemyReward(int rewardId) {
        SC_StartAlchemy.Builder builder = SC_StartAlchemy.newBuilder();
        MistLootPackCarryConfigObject cfg = MistLootPackCarryConfig.getById(rewardId);
        if (cfg == null || cfg.getAlchemyexhangereward() == null) {
            builder.setRetCode(MistRetCode.MRC_ErrorParam);
            sendMsgToServer(MsgIdEnum.SC_StartAlchemy_VALUE, builder);
            return;
        }
        MistCommonConfigObject cmCfg = MistCommonConfig.getByMistlevel(getMistRoom().getLevel());
        if (cmCfg == null) {
            return;
        }
        boolean flag = false;
        for (int canAlchemyRewardId : cmCfg.getAlchemylist()) {
            if (canAlchemyRewardId == rewardId) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            builder.setRetCode(MistRetCode.MRC_ErrorParam);
            sendMsgToServer(MsgIdEnum.SC_StartAlchemy_VALUE, builder);
            return;
        }
        if (!checkRewardEnough(rewardId, 1)) {
            builder.setRetCode(MistRetCode.MRC_NotFountCarryReward);
            sendMsgToServer(MsgIdEnum.SC_StartAlchemy_VALUE, builder);
            return;
        }
        if (cfg.getNeedalchemynum() != null && cfg.getNeedalchemynum().length > 1){
            int needNum = cfg.getNeedalchemynum()[1];
            CrossArenaLvCfgObject crossVipLvCfg = CrossArenaLvCfg.getByLv(getCrossVipLv());
            if (crossVipLvCfg != null) {
                needNum -= needNum * crossVipLvCfg.getMist_pointgoldstonereduction() / 1000;
                needNum = Integer.max(0, needNum);
            }
            if (!checkRewardEnough(cfg.getNeedalchemynum()[0], needNum)) {
                builder.setRetCode(MistRetCode.MRC_CurrencyNotEnough);
                sendMsgToServer(MsgIdEnum.SC_StartAlchemy_VALUE, builder);
                return;
            }
            consumeGainReward(cfg.getNeedalchemynum()[0], needNum);
        }
        int rand;
        int sum = 0;
        int count = Math.min(cfg.getAlchemyrewardcount(), cfg.getAlchemyexhangereward().length);
        MistAlchemyData.Builder alchemyData = MistAlchemyData.newBuilder();
        alchemyData.setExchangeRewardId(rewardId);
        alchemyData.addRewardIdList(rewardId);
        for (int i = 0; i < count; i++) {
            rand = RandomUtils.nextInt(1000);
            for (int j = 0; j < cfg.getAlchemyexhangereward().length; j++) {
                if (cfg.getAlchemyexhangereward()[j].length < 2) {
                    continue;
                }
                sum += cfg.getAlchemyexhangereward()[j][0];
                if (rand < sum) {
                    alchemyData.addRewardIdList(cfg.getAlchemyexhangereward()[j][1]);
                    break;
                }
            }
        }
        consumeGainReward(rewardId, 1);
        alchemyDataMap.put(rewardId, alchemyData.build());
        builder.setRetCode(MistRetCode.MRC_Success);
        builder.addAllRewardId(alchemyData.getRewardIdListList());
        sendMsgToServer(MsgIdEnum.SC_StartAlchemy_VALUE, builder);

        CS_GS_UpdateAlchemyData.Builder builder1 = CS_GS_UpdateAlchemyData.newBuilder();
        builder1.setPlayerIdx(getIdx());
        builder1.setBAdd(true);
        builder1.setExchangeRewardId(rewardId);
        builder1.addAllRewardIdList(alchemyData.getRewardIdListList());
        GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_UpdateAlchemyData_VALUE, builder1);
    }

    public boolean chooseAlchemyReward(int exchangeId, int rewardId) {
        SC_ChooseAlchemyReward.Builder builder = SC_ChooseAlchemyReward.newBuilder();
        MistAlchemyData alchemyData = alchemyDataMap.get(exchangeId);
        if (alchemyData == null) {
            builder.setRetCode(MistRetCode.MRC_NotAlchemy);
            sendMsgToServer(MsgIdEnum.SC_ChooseAlchemyReward_VALUE, builder);
            return false;
        }
        HashMap<Integer, Integer> reward = null;
        for (Integer alchemyRewardId : alchemyData.getRewardIdListList()) {
            if (alchemyRewardId == rewardId) {
                if (reward == null) {
                    reward = new HashMap<>();
                }
                reward.put(alchemyRewardId, 1);
                break;
            }
        }
        if (reward == null) {
            builder.setRetCode(MistRetCode.MRC_ErrorParam);
            sendMsgToServer(MsgIdEnum.SC_ChooseAlchemyReward_VALUE, builder);
            return false;
        }
        alchemyDataMap.remove(exchangeId);

        addNewGainRewardMap(reward);
        builder.setRetCode(MistRetCode.MRC_Success);
        sendMsgToServer(MsgIdEnum.SC_ChooseAlchemyReward_VALUE, builder);

        CS_GS_UpdateAlchemyData.Builder builder1 = CS_GS_UpdateAlchemyData.newBuilder();
        builder1.setPlayerIdx(getIdx());
        builder1.setBAdd(false);
        builder1.setExchangeRewardId(exchangeId);
        GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_UpdateAlchemyData_VALUE, builder1);
        return true;
    }

    public void clear() {
        setServerIndex(0);
        setMistRoom(null);
        setName("");
        setLevel(0);
        setVip(0);
        setAvatar(0);
        setAvatarBorder(0);
        setAvatarBorderRank(0);
        setCamp(0);
        setFighterId(0);
        setFightPower(0);
        setOnline(false);
        setOfflineTime(0);
        setMainLineUnlockLevel(0);
        setTitleId(0);
        setNewTitleId(0);
        skillList.clear();
        petDataList.clear();
        playerBaseAdditions.clear();
//        dailyBagRewardCountList.clear();
        remainPetHpMap.clear();

        gainRewardMap.clear();
        ownedRewardMap.clear();
        alchemyDataMap.clear();
    }

    public void onPlayerLogin(GS_CS_JoinMistForest req, boolean isResume) {
        setName(req.getPlayerBaseData().getPlayerName());
        setAvatar(req.getPlayerBaseData().getAvatar());
        setAvatarBorder(req.getPlayerBaseData().getAvatarBorder());
        setAvatarBorderRank(req.getPlayerBaseData().getAvatarBorderRank());
        setLevel(req.getPlayerBaseData().getLevel());
        setVip(req.getPlayerBaseData().getVipLevel());
        setServerIndex(req.getServerIndex());
        setCrossVipLv(req.getCrossArenaVipLv());
        setMainLineUnlockLevel(req.getMainLineUnlockLevel());
        setTitleId(req.getPlayerBaseData().getTitleId());
        setNewTitleId(req.getPlayerBaseData().getNewTitleId());
        updateSkillList(req.getPlayerSkillIdListList());
        updatePetDataList(req.getPetListList());
        updateBaseAdditions(req.getPlayerBaseAdditionsMap());
//        addDailyBagRewardCountList(req.getBagRewardCountList());
        gainRewardMap.putAll(req.getCarryRewardsMap());
        initDailyOwnedRewards(req.getDailyOwnedRewardsMap());
        setMistStamina(req.getMistStamina());
        setGainActivityBossBoxFlag(req.getGainBossActivityBoxFlag());
        setOnline(true);
        setOfflineTime(0);
        setEliteMonsterRewardTimes(req.getEliteMonsterRewardTimes());

        for (MistAlchemyData alchemyData : req.getAlchemyDataList()) {
            alchemyDataMap.put(alchemyData.getExchangeRewardId(), alchemyData);
        }

        LogUtil.info("player login id=" + getIdx() + ",name=" + getName());
    }

    protected void initRobotPlayerData() {
        setName(ObjUtil.createRandomName(LanguageEnum.forNumber(ServerConfig.getInstance().getLanguage())));
        setLevel(RandomUtils.nextInt(PlayerLevelConfig._ix_level.size()));
        setAvatar(GameConfig.getById(GameConst.ConfigId).getDefaultavatarid());
    }

    public void initGhostBusterPlayer(MistGhostBusterSyncData ghostBusterSyncData) {
        if (ghostBusterSyncData == null) {
            setRobot(true);
            initRobotPlayerData();
        } else {
            setName(ghostBusterSyncData.getPlayerInfo().getPlayerName());
            setAvatar(ghostBusterSyncData.getPlayerInfo().getAvatar());
            setAvatarBorder(ghostBusterSyncData.getPlayerInfo().getAvatarBorder());
            setAvatarBorderRank(ghostBusterSyncData.getPlayerInfo().getAvatarBorderRank());
            setLevel(ghostBusterSyncData.getPlayerInfo().getLevel());
            setVip(ghostBusterSyncData.getPlayerInfo().getVipLevel());
            setTitleId(ghostBusterSyncData.getPlayerInfo().getTitleId());
            setNewTitleId(ghostBusterSyncData.getPlayerInfo().getNewTitleId());

            setServerIndex(ghostBusterSyncData.getFromSvrIndex());

            initDailyOwnedRewards(ghostBusterSyncData.getDailyOwnedRewardsMap());
        }
        setOnline(true);

        LogUtil.info("Init GhostPlayer id={},name={},isRobot={}", getIdx(), getName(), isRobot());
    }

    public void onPlayerLogout(boolean toGS) {
        LogUtil.info("player logout id=" + getIdx() + ",name=" + getName());

        if (toGS) {
            PlayerOffline.Builder builder = PlayerOffline.newBuilder();
            builder.setPlayerIdx(getIdx());
            GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.PlayerOffline_VALUE, builder);
        }
        clear();
    }

    public void offline() {
        if (isOnline()) {
            setOnline(false);
            setOfflineTime(GlobalTick.getInstance().getCurrentTime());
            LogUtil.info("player offline from mistforest id=" + getIdx() + ",name=" + getName());
        }
    }

    public void updateOfflineData(Map<Integer, Long> offPropData, Map<Integer, Long> selfOffData) {
        if (offPropData == null && selfOffData == null) {
            return;
        }
        CS_GS_UpdateOffPropData.Builder builder = CS_GS_UpdateOffPropData.newBuilder();
        builder.setPlayerIdx(getIdx());
        if (offPropData != null) {
            builder.putAllOffPropData(offPropData);
        }
        if (selfOffData != null) {
            builder.putAllSelfOffPropData(selfOffData);
        }
        GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_UpdateOffPropData_VALUE, builder);
    }

    public void removeOfflineData(MistUnitPropTypeEnum propType, EnumMistSelfOffPropData selfPropType) {
        if (propType == null && propType == null) {
            return;
        }
        CS_GS_UpdateOffPropData.Builder builder = CS_GS_UpdateOffPropData.newBuilder();
        builder.setPlayerIdx(getIdx());
        builder.addRemoveOffPropData(propType.getNumber());
        builder.addRemoveSelfOffPropData(selfPropType.getNumber());
        GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_UpdateOffPropData_VALUE, builder);
    }

    public void initByMistFighter(MistFighter fighter) {
        if (fighter == null) {
            return;
        }
        setMistRoom(fighter.getRoom());
        setCamp((int) fighter.getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE));
        setFighterId(fighter.getId());
    }

    public void initDailyOwnedRewards(Map<Integer, Integer> gainRewardMap) {
        ownedRewardMap.putAll(gainRewardMap);
    }

    public PlayerBaseInfo.Builder builderBaseInfo() {
        PlayerBaseInfo.Builder playerInfo = PlayerBaseInfo.newBuilder();
        playerInfo.setPlayerId(getIdx());
        playerInfo.setPlayerName(getName());
        playerInfo.setLevel(getLevel());
        playerInfo.setAvatar(getAvatar());
        playerInfo.setAvatarBorder(getAvatarBorder());
        playerInfo.setAvatarBorderRank(getAvatarBorderRank());
        playerInfo.setTitleId(getTitleId());
        return playerInfo;
    }

    public BattlePlayerInfo.Builder buildPveBattleData() {
        BattlePlayerInfo.Builder builder = BattlePlayerInfo.newBuilder();
        builder.setPlayerInfo(builderBaseInfo());
        builder.setCamp(1);
        builder.addAllPetList(getPetDataList());
        builder.addAllPlayerSkillIdList(getSkillList());
        return builder;
    }

    public PvpBattlePlayerInfo.Builder buildPvpBattleData(int battleCamp) {
        PvpBattlePlayerInfo.Builder builder = PvpBattlePlayerInfo.newBuilder();
        builder.setPlayerInfo(builderBaseInfo());
        builder.setCamp(battleCamp);
        builder.setFromSvrIndex(getServerIndex());
        builder.addAllPetList(getPetDataList());
        builder.addAllPlayerSkillIdList(getSkillList());

        List<PetBuffData> extBuff = getExtendBuffList();
        if (extBuff != null) {
            ExtendProperty.Builder builder1 = ExtendProperty.newBuilder();
            builder1.setCamp(battleCamp);
            builder1.addAllBuffData(extBuff);

            builder.addExtendProp(builder1);
        }
        return builder;
    }

    public MistPlayerInfo.Builder buildMistPlayerInfo() {
        MistPlayerInfo.Builder playerInfo = MistPlayerInfo.newBuilder();
        playerInfo.setId(getIdx());
        playerInfo.setName(getName() != null ? getName() : "");
        playerInfo.setAvatar(getAvatar());
        playerInfo.setAvatarBorder(getAvatarBorder());
        playerInfo.setAvatarBorderRank(getAvatarBorderRank());
        playerInfo.setLevel(getLevel());
        playerInfo.setFightPower(getFightPower());
        if (mistRoom != null) {
            MistFighter fighter = mistRoom.getObjManager().getMistObj(getFighterId());
            if (fighter != null) {
                playerInfo.setTeamId(fighter.getTeamId());
            }
        }
        playerInfo.setNewTitleId(getNewTitleId());
        return playerInfo;
    }

    public boolean isGainBagBeyondCount() {
        PlayerLevelConfigObject plyLvCfg = PlayerLevelConfig.getByLevel(getLevel());
        return plyLvCfg != null ? getDailyBattleRewardCount() >= plyLvCfg.getLimitByRule(mistRoom.getMistRule()) : false;
    }

    public void exchangeMistForest(MistMapObjConfigObject newMapCfg) {
        if (newMapCfg == null) {
            return;
        }
        LogUtil.info("player exchange mistForest id=" + getIdx() + ",newMapId=" + newMapCfg.getId() + ",rule=" + newMapCfg.getMaprule() + ",roomLevel=" + newMapCfg.getMaplevel());
        CS_GS_ExchangeMistRoom.Builder builder = CS_GS_ExchangeMistRoom.newBuilder();
        builder.setIdx(getIdx());
        builder.setMistRuleValue(newMapCfg.getMaprule());
        builder.setNewMistLevel(newMapCfg.getMaplevel());
        GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_ExchangeMistRoom_VALUE, builder);

        clear();
    }

    public void sendRetCodeMsg(RetCodeEnum retCode) {
        if (isRobot()) {
            return;
        }
        SC_RetCode.Builder builder = SC_RetCode.newBuilder();
        builder.setRetCode(GameUtil.buildRetCode(retCode));
        sendMsgToServer(MsgIdEnum.SC_RetCode_VALUE, builder);
    }

    public boolean sendMsgToServer(int msgId, Builder<?> builder) {
        if (getServerIndex() <= 0) {
            return false;
        }
        CS_GS_MistForestRoomInfo.Builder builder1 = CS_GS_MistForestRoomInfo.newBuilder();
        builder1.addPlayerId(getIdx());
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        return GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_MistForestRoomInfo_VALUE, builder1);
    }

    public void onTick(long curTime) {
        updateRemainPetHp(curTime);
    }

}