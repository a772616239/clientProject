/**
 * created by tool DAOGenerate
 */
package model.petmission.entity;

import cfg.GameConfig;
import cfg.PetMissionLevel;
import cfg.PetMissionLevelObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.mainLine.dbCache.mainlineCache;
import model.obj.BaseObj;
import model.petmission.dbCache.petmissionCache;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.CollectionUtils;
import protocol.PetDB.SerializableAcceptedPetMission;
import protocol.PetDB.SerializablePetMission;
import protocol.PetMessage;
import protocol.PetMessage.AcceptedPetMission;
import protocol.PetMessage.PetMission;
import protocol.RetCodeId;
import util.GameUtil;
import util.LogUtil;
import util.ObjUtil;
import util.RandomUtil;

import static protocol.MessageId.MsgIdEnum.SC_PetMissionInit_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionLvUp_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionUpProUpdate_VALUE;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class petmissionEntity extends BaseObj {

    @Override
    public String getClassType() {
        return "petmissionEntity";
    }

    @Override
    public void putToCache() {
        petmissionCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.mission = getMissionListBuilder().build().toByteArray();
        this.acceptedmission = getAcceptedMissionListBuilder().build().toByteArray();
    }

    private static final int defaultMissionLv = 1;

    private static final int initLimitMissionCount = 1;

    //总刷新次数
    private int totalRefreshCount = 0;
    //总委托限制次数
    private int totalLimitMissionCount = 0;


    /**
     * 主键
     */
    private String idx;

    /**
     * 对应玩家id
     */
    private String playeridx;

    /**
     * 未接受任务id
     */
    private byte[] mission;

    /**
     * 执行中任务
     */
    private byte[] acceptedmission;

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
     * 获得对应玩家id
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置对应玩家id
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得未接受任务id
     */
    public byte[] getMission() {
        return mission;
    }

    /**
     * 设置未接受任务id
     */
    public void setMission(byte[] mission) {
        this.mission = mission;
    }

    /**
     * 获得执行中任务
     */
    public byte[] getAcceptedmission() {
        return acceptedmission;
    }

    /**
     * 设置执行中任务
     */
    public void setAcceptedmission(byte[] acceptedmission) {
        this.acceptedmission = acceptedmission;
    }

    @Override
    public String getBaseIdx() {
        return idx;
    }

    private petmissionEntity() {
    }

    /***************************分割**********************************/
    private SerializablePetMission.Builder missionListBuilder;
    private SerializableAcceptedPetMission.Builder acceptedMissionListBuilder;

    public SerializablePetMission.Builder getMissionListBuilder() {
        if (missionListBuilder == null) {
            if (this.mission == null) {
                missionListBuilder = SerializablePetMission.newBuilder();
            } else {
                try {
                    missionListBuilder = SerializablePetMission.parseFrom(this.mission).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.printStackTrace(e);
                    LogUtil.error("model.petmission.entity.petmissionEntity.getMissionList, param mission list fail, " +
                            "return new SerializablePetMission.Builder");
                    missionListBuilder = SerializablePetMission.newBuilder();
                }
            }
        }
        return missionListBuilder;
    }

    public SerializableAcceptedPetMission.Builder getAcceptedMissionListBuilder() {
        if (acceptedMissionListBuilder == null) {
            if (this.acceptedmission == null) {
                acceptedMissionListBuilder = SerializableAcceptedPetMission.newBuilder();
            } else {
                try {
                    acceptedMissionListBuilder = SerializableAcceptedPetMission.parseFrom(this.acceptedmission).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.printStackTrace(e);
                    LogUtil.error("model.petmission.entity.petmissionEntity.getMissionList, param  accept mission list fail, " +
                            "return new SerializablePetMission.Builder");
                    acceptedMissionListBuilder = SerializableAcceptedPetMission.newBuilder();
                }
            }
        }
        return acceptedMissionListBuilder;
    }

    public petmissionEntity(String initPlayerId) {
        this.idx = IdGenerator.getInstance().generateId();
        this.playeridx = initPlayerId;

        //玩家初始任务
        int petMissionCount = VIPConfig.getById(PlayerUtil.queryPlayerVipLv(initPlayerId)).getPetmissiondailytimes();

        getMissionListBuilder().setMissionLv(defaultMissionLv);

        initDailyMission(petMissionCount);
        getMissionListBuilder().setNextRefreshConsume(PetMissionHelper.calculateRefreshNeed(playeridx, 0));
    }

    private void initDailyMission(int petMissionCount) {
        addNewMission(RandomUtil.randomPetMissionWithType(mainlineCache.getInstance().getCurOnHookNode(getPlayeridx()),
                getMissionListBuilder().getMissionLv(), PetMessage.PetMissionType.PMT_Limit));
        for (int i = 0; i < petMissionCount - 1; i++) {
            addNewMission(RandomUtil.randomPetMissionWithType(mainlineCache.getInstance().getCurOnHookNode(getPlayeridx()),
                    getMissionListBuilder().getMissionLv(), PetMessage.PetMissionType.PMT_Normal));
        }
    }

    public int getMaxMissionCount() {
        int playerVipLv = PlayerUtil.queryPlayerVipLv(getPlayeridx());
        VIPConfigObject object = VIPConfig.getById(playerVipLv);
        if (object == null) {
            object = VIPConfig.getById(0);
        }
        return object.getPetmissiondailytimes();
    }

    public void addNewMissionList(List<PetMission> missionList) {
        if (missionList == null || missionList.isEmpty()) {
            return;
        }

        for (PetMission petMission : missionList) {
            addNewMission(petMission);
        }
    }

    public void addNewMission(PetMission mission) {
        if (mission == null) {
            return;
        }
        getMissionListBuilder().putMissions(mission.getMissionId(), mission);
    }

    /**
     * 判断是否还可以添加新任务
     *
     * @return
     */
    public boolean isFull() {
        return getMissionListBuilder().getMissionsCount() >= GameConfig.getById(GameConst.CONFIG_ID).getPetmissionmaxnumber();
    }

    /**
     * 更新玩家每日的任务个数，未接受的任务补充到玩家vip等级对应的任务个数
     */
    public void updateDailyData(boolean sendMsg) {
        getMissionListBuilder().clearRefreshCount().setNextRefreshConsume(PetMissionHelper.calculateRefreshNeed(getPlayeridx(), 0));

        int needAddCount = getMaxMissionCount() - getMissionListBuilder().getMissionsCount();
        if (needAddCount <= 0) {
            if (sendMsg) {
                sendPetMissionInit();
            }
            return;
        }
        initDailyMission(needAddCount);

        if (sendMsg) {
            sendPetMissionInit();
        }

    }

    public void sendPetMissionInit() {
        PetMessage.SC_PetMissionInit.Builder resultBuilder = PetMessage.SC_PetMissionInit.newBuilder();
        SerializablePetMission.Builder missionListBuilder = getMissionListBuilder();
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        resultBuilder.addAllMission(missionListBuilder.getMissionsMap().values());
        resultBuilder.setNextRefreshConsume(missionListBuilder.getNextRefreshConsume());
        resultBuilder.addAllAcceptedMission(getAcceptedMissionListBuilder().getAcceptedMissionsMap().values());
        resultBuilder.setMissionLv(missionListBuilder.getMissionLv());
        GlobalData.getInstance().sendMsg(getPlayeridx(), SC_PetMissionInit_VALUE, resultBuilder);
    }

    public PetMission getMissionById(String id) {
        return getMissionListBuilder().getMissionsMap().get(id);
    }

    public void removeMission(String id) {
        getMissionListBuilder().removeMissions(id);
    }


    public void addAcceptMission(AcceptedPetMission acceptedPetMission) {
        if (acceptedPetMission == null) {
            return;
        }
        getAcceptedMissionListBuilder().putAcceptedMissions(acceptedPetMission.getMissionId(), acceptedPetMission);
    }

    public AcceptedPetMission getAcceptMissionById(String id) {
        return getAcceptedMissionListBuilder().getAcceptedMissionsMap().get(id);
    }

    public void removeAcceptMissionById(String id) {
        getAcceptedMissionListBuilder().removeAcceptedMissions(id);
    }

    public void sendPetMissionLvUp(int upLv) {
        PetMessage.SC_PetMissionLvUp.Builder msg = PetMessage.SC_PetMissionLvUp.newBuilder().setCurLv(upLv);
        GlobalData.getInstance().sendMsg(getPlayeridx(), SC_PetMissionLvUp_VALUE, msg);
    }

    public void sendPetMissionUpProUpdate(Map<Integer, Integer> upLvProMap) {
        PetMessage.SC_PetMissionUpProUpdate.Builder msg = PetMessage.SC_PetMissionUpProUpdate.newBuilder();
        msg.addAllUpMissionStar(upLvProMap.keySet());
        msg.addAllUpMissionPro(upLvProMap.values());
        GlobalData.getInstance().sendMsg(getPlayeridx(), SC_PetMissionUpProUpdate_VALUE, msg);
    }

    public void updatePetMissionLvUpPro(Map<Integer, Integer> adds) {
        SerializablePetMission.Builder missionListBuilder = getMissionListBuilder();
        int playerMissionLv = missionListBuilder.getMissionLv();

        if (playerMissionLv >= PetMissionLevel.getMaxMissionLv()) {
            return;
        }

        addMissionPro(adds);

        int upMissionLv = playerMissionLv;

        while (true) {
            PetMissionLevelObject missionConfig = PetMissionLevel.getByMissionlv(upMissionLv);
            if (missionConfig == null) {
                LogUtil.error("PetMissionLevelObject is null by playerMissionLv:{}", upMissionLv);
                return;
            }
            boolean lvUp = tryLvUp(missionConfig);
            if (!lvUp) {
                if (upMissionLv > playerMissionLv) {
                    clearMissionPro(missionConfig);
                }
                break;
            }
            upMissionLv++;
        }

        if (upMissionLv > playerMissionLv) {
            missionListBuilder.setMissionLv(upMissionLv);
            sendPetMissionLvUp(upMissionLv);
        }
        sendPetMissionUpProUpdate(missionListBuilder.getUpLvProMap());
    }

    private void clearMissionPro(PetMissionLevelObject missionConfig) {
        Map<Integer, Integer> targetNeed = PetMissionLevel.getInstance().getTargetNeed(missionConfig.getMissionlv());
        Map<Integer, Integer> upLvProMap = new HashMap<>(missionListBuilder.getUpLvProMap());
        for (Map.Entry<Integer, Integer> entry : upLvProMap.entrySet()) {
            Integer need = targetNeed.get(entry.getKey());
            if (need == null) {
                missionListBuilder.removeUpLvPro(entry.getKey());
            } else {
                missionListBuilder.putUpLvPro(entry.getKey(), Math.min(need, entry.getValue()));
            }
        }
    }


/*    private void addMissionPro(PetMissionLevelObject missionConfig, int missionStar, int adds) {
        int totalNeed = 0;
        for (int[] upNeed : missionConfig.getUptarget()) {
            if (upNeed.length < 2) {
                continue;
            }
            if (upNeed[0] == GameConst.totalPetMissionNumKey) {
                totalNeed = upNeed[1];
                continue;
            }
            if (upNeed[0] > missionStar) {
                continue;
            }
            Integer curTimes = missionListBuilder.getUpLvProMap().get(upNeed[0]);
            int curMissionStartNum = curTimes == null ? adds : curTimes + adds;
            curMissionStartNum = Math.min(curMissionStartNum, upNeed[1]);
            missionListBuilder.putUpLvPro(upNeed[0], curMissionStartNum);
        }

        int totalCompleteNum = ObjUtil.requireIntOrDefault(missionListBuilder.getUpLvProMap().get(GameConst.totalPetMissionNumKey), 0) + 1;
        totalCompleteNum = Math.min(totalNeed, totalCompleteNum);
        missionListBuilder.putUpLvPro(GameConst.totalPetMissionNumKey, totalCompleteNum);
    }*/

    private void addMissionPro(Map<Integer, Integer> adds) {
        Map<Integer, Integer> upLvProMap = missionListBuilder.getUpLvProMap();

        adds.forEach((k, v) -> missionListBuilder.putUpLvPro(k, upLvProMap.getOrDefault(k, 0) + v));

        int totalCompleteNum = ObjUtil.requireIntOrDefault(upLvProMap.get(GameConst.totalPetMissionNumKey), 0);
        totalCompleteNum = totalCompleteNum + adds.values().stream().mapToInt(Integer::intValue).sum();
        missionListBuilder.putUpLvPro(GameConst.totalPetMissionNumKey, totalCompleteNum);
    }

    private boolean tryLvUp(PetMissionLevelObject missionConfig) {
        if (missionConfig == null || ArrayUtils.isEmpty(missionConfig.getUptarget())) {
            return false;
        }
        for (int[] upNeed : missionConfig.getUptarget()) {
            if (upNeed.length < 2) {
                return false;
            }
            Integer cur = missionListBuilder.getUpLvProMap().get(upNeed[0]);
            if (cur == null || cur < upNeed[1]) {
                return false;
            }
        }
        for (int[] upNeed : missionConfig.getUptarget()) {
            missionListBuilder.putUpLvPro(upNeed[0], missionListBuilder.getUpLvProMap().get(upNeed[0]) - upNeed[1]);
        }
        return true;
    }

    //刷的越多,限定任务概率出现越大
    public List<PetMission> randomPetMission(int curOnHookNode, int missionLv, int needRefreshCount) {
        int[] missionLimit = GameConfig.getById(GameConst.CONFIG_ID).getMissionlimit();
        if (missionLimit.length < 2) {
            return Collections.emptyList();
        }
        int var1 = missionLimit[0];
        int var2 = missionLimit[1];
        int maxLimitCount = ((this.totalRefreshCount + needRefreshCount) / var1 + 1) * var2 - this.totalLimitMissionCount;
        List<PetMission> resultMission = RandomUtil.randomPetMission(curOnHookNode, missionLv, needRefreshCount, maxLimitCount);
        long thisTimeRefreshCount = resultMission.stream().filter(m -> PetMessage.PetMissionType.PMT_Limit == m.getMissionType()).count();
        this.totalRefreshCount += resultMission.size();
        this.totalLimitMissionCount += thisTimeRefreshCount;
        return resultMission;
    }

    public PetMission randomOnePetMission(int curOnHookNode, int missionLv) {
        List<PetMission> petMissions = randomPetMission(curOnHookNode, missionLv, 1);
        if (CollectionUtils.isEmpty(petMissions)) {
            return null;
        }
        return petMissions.get(0);
    }
}