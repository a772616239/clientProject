package model.battle;

import cfg.FightMake;
import cfg.FightMakeObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import model.mainLine.dbCache.mainlineCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePetData.Builder;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetPropertyDict;
import protocol.Battle.PlayerBaseInfo;
import protocol.BattleCMD.BattleFrameParam_DragDropPet;
import protocol.BattleCMD.BattleFrameParam_Finish;
import protocol.BattleCMD.BattleFrameParam_UseAssistance;
import protocol.BattleCMD.BattleFrameParam_UsePetUltimateSkill;
import protocol.BattleCMD.BattleFrameParam_UsePlayerSkill;
import protocol.BattleMono.BattleOperation;
import protocol.BattleMono.FightParamDict;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.Common.EnumFunction;
import protocol.PetMessage.PetProperty;

/**
 * @author huhan
 * @date 2020/04/23
 */
public class BattleUtil {

    /**
     * @param camp
     * @param params
     * @param factory 百分比
     * @return
     */
    public static ExtendProperty.Builder builderMonsterExtendProperty(int camp, int[][] params, int factory) {
        if (params == null || (camp != 1 && camp != 2) || factory <= 0) {
            return null;
        }

        ExtendProperty.Builder exProBuilder = ExtendProperty.newBuilder();

        exProBuilder.setCamp(camp);
        PetPropertyDict.Builder dictBuilder = PetPropertyDict.newBuilder();
        for (int[] ints : params) {
            if (ints.length == 2) {
                dictBuilder.addKeys(PetProperty.forNumber(ints[0]));
                dictBuilder.addValues((ints[1] * factory) / 100);
            }
        }
        exProBuilder.setPropDict(dictBuilder);

        return exProBuilder;
    }

    /**
     * 初始化怪物难度修正
     *
     * @param playerIdx
     * @param camp
     * @return
     */
    public static ExtendProperty.Builder initMonsterExPropertyAdjust(String playerIdx, int camp) {
        return BattleUtil.builderMonsterExtendProperty(camp, getMonsterMainLineAdjust(playerIdx));
    }

    public static int[][] getMonsterMainLineAdjust(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }
        int curNode = mainlineCache.getInstance().getPlayerCurNode(playerIdx);
        return getMonsterMainLineAdjustByNodeId(curNode);
    }

    /**
     * 初始化怪物难度修正
     *
     * @param camp
     * @return
     */
    public static ExtendProperty.Builder initMonsterExPropertyAdjustByNodeId(int nodeId, int camp) {
        return BattleUtil.builderMonsterExtendProperty(camp, getMonsterMainLineAdjustByNodeId(nodeId));
    }

    public static int[][] getMonsterMainLineAdjustByNodeId(int nodeId) {
        if (nodeId <= 0) {
            return null;
        }

        MainLineNodeObject nodeCfg = null;
        for (int i = nodeId; i > 0; i--) {
            MainLineNodeObject tempNodeCfg = MainLineNode.getById(i);
            if (tempNodeCfg != null && tempNodeCfg.isBattleNode()) {
                nodeCfg = tempNodeCfg;
                break;
            }
        }

        if (nodeCfg == null) {
            return null;
        }

        FightMakeObject fightObj = FightMake.getById(nodeCfg.getFightmakeid());
        return fightObj == null ? null : fightObj.getMonsterpropertyext();
    }

    public static ExtendProperty.Builder builderMonsterExtendProperty(int camp, int[][] params) {
        if (params == null || (camp != 1 && camp != 2)) {
            return null;
        }

        ExtendProperty.Builder exProBuilder = ExtendProperty.newBuilder();

        exProBuilder.setCamp(camp);
        PetPropertyDict.Builder dictBuilder = PetPropertyDict.newBuilder();
        for (int[] ints : params) {
            if (ints.length == 2) {
                dictBuilder.addKeys(PetProperty.forNumber(ints[0]));
                dictBuilder.addValues(ints[1]);
            }
        }
        exProBuilder.setPropDict(dictBuilder);

        return exProBuilder;
    }

    /**
     * 构建玩家战斗基本信息
     *
     * @param playerIdx
     * @return
     */
    public static PlayerBaseInfo.Builder buildPlayerBattleBaseInfo(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return null;
        }
        return player.getBattleBaseData();
    }

    /**
     * @param fightParams
     * @param fightParamType
     * @return
     */
    public static long getFightParamsValue(List<FightParamDict> fightParams, FightParamTypeEnum fightParamType) {
        if (fightParams == null || fightParams.isEmpty()) {
            return 0;
        }

        for (FightParamDict fightParam : fightParams) {
            if (fightParam.getKey() == fightParamType) {
                return fightParam.getValue();
            }
        }
        return 0;
    }

    public static void addHeldMailToFriend(String playerIdx) {
//        playerEntity player = playerCache.getByIdx(playerIdx);
//        if (player == null) {
//            return;
//        }
//        playermineEntity playerMine = playermineCache.getInstance().getMineByPlayerIdx(playerIdx);
//        if (playerMine == null) {
//            return;
//        }
//        String friendIdx = playerMine.getMineFightData().getFriendHelpInfo().getBeHelpFriendIdx();
//        LogUtil.info("FriendHelpMail idx=" + playerIdx + ",targetIdx=" + friendIdx);
//        playerEntity friend = playerCache.getByIdx(friendIdx);
//        if (friend == null) {
//            LogUtil.error("FriendHelpMail targetNotFound idx=" + playerIdx + ",targetIdx=" + friendIdx);
//            return;
//        }
//        if (!friend.isFriend(playerIdx)) {
//            LogUtil.error("FriendHelpMail is not Friend idx=" + playerIdx + ",targetIdx=" + friendIdx);
//            return;
//        }
//        EventUtil.triggerAddMailEvent(friend.getIdx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getFriendhelpreward(), null,
//                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_FriendHelp), player.getName());
    }

    public static boolean checkSerializedFrame(BattleOperation operation) {
        try {
            switch (operation.getFramType()) {
                case BFTE_Null:
                    break;
                case BFTE_DragDropPet:
                    BattleFrameParam_DragDropPet.parseFrom(operation.getFramParam());
                    break;
                case BFTE_UsePlayerSkill:
                    BattleFrameParam_UsePlayerSkill.parseFrom(operation.getFramParam());
                    break;
                case BFTE_UseAssistance:
                    BattleFrameParam_UseAssistance.parseFrom(operation.getFramParam());
                    break;
                case BFTE_UsePetUltimateSkill:
                    BattleFrameParam_UsePetUltimateSkill.parseFrom(operation.getFramParam());
                    break;
                case BFTE_Finish:
                    BattleFrameParam_Finish.parseFrom(operation.getFramParam());
                    break;
                default:
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 战斗类型功能枚举映射关系
     */
    private static final Map<BattleSubTypeEnum, EnumFunction> BATTLE_TYPE_FUNCTION_MAP;

    static {
        Map<BattleSubTypeEnum, EnumFunction> tempMap = new EnumMap<>(BattleSubTypeEnum.class);

        tempMap.put(BattleSubTypeEnum.BSTE_MainLineCheckPoint, EnumFunction.MainLine);
        tempMap.put(BattleSubTypeEnum.BSTE_EndlessSpire, EnumFunction.Endless);
        tempMap.put(BattleSubTypeEnum.BSTE_ForeignInvasion, EnumFunction.ForeignInvasion);
        tempMap.put(BattleSubTypeEnum.BSTE_MistForest, EnumFunction.MistForest);
        tempMap.put(BattleSubTypeEnum.BSTE_Patrol, EnumFunction.Patrol);
        tempMap.put(BattleSubTypeEnum.BSTE_BreaveChallenge, EnumFunction.CourageTrial);
        tempMap.put(BattleSubTypeEnum.BSTE_MineFight, EnumFunction.MiningArea);
        tempMap.put(BattleSubTypeEnum.BSTE_ResourceCopy, EnumFunction.ResCopy);
//        tempMap.put(BattleSubTypeEnum.BSTE_PointCopy, EnumFunction.);
        tempMap.put(BattleSubTypeEnum.BSTE_Arena, EnumFunction.Arena);
//        tempMap.put(BattleSubTypeEnum.BSTE_ActivityBoss, EnumFunction);
        tempMap.put(BattleSubTypeEnum.BSTE_BossTower, EnumFunction.BossTower);
        tempMap.put(BattleSubTypeEnum.BSTE_TheWar, EnumFunction.TheWar);
        tempMap.put(BattleSubTypeEnum.BSTE_Training, EnumFunction.Training);
        tempMap.put(BattleSubTypeEnum.BSTE_magicthron, EnumFunction.MagicThron);
        tempMap.put(BattleSubTypeEnum.BSTE_OfferReward, EnumFunction.OfferReward);
        tempMap.put(BattleSubTypeEnum.BSTE_CrossArenaEvent, EnumFunction.CrossArena);
        tempMap.put(BattleSubTypeEnum.BSTE_CrossArenaPvp, EnumFunction.QIECUO);

        BATTLE_TYPE_FUNCTION_MAP = Collections.unmodifiableMap(tempMap);
    }

    public static EnumFunction getFunctionTypeByBattleType(BattleSubTypeEnum battleType) {
        if (battleType == null || battleType == BattleSubTypeEnum.BSTE_Null) {
            return null;
        }

        return BATTLE_TYPE_FUNCTION_MAP.get(battleType);
    }

    /**
     * 计算宠物战斗数据的总战力
     *
     * @param list
     * @return
     */
    public static long calculateTotalAbility(Collection<BattlePetData> list) {
        long result = 0;
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        return list.stream().map(BattlePetData::getAbility).reduce(Long::sum).orElse(0L);
    }

    public static List<BattlePetData> setMonsterRemainHp(List<BattlePetData> monsterPet, Map<String, BattleRemainPet> monsterRemainHp) {
        if (CollectionUtils.isEmpty(monsterPet) || MapUtils.isEmpty(monsterRemainHp)) {
            return monsterPet;
        }

        List<BattlePetData> result = new ArrayList<>();
        for (BattlePetData battlePetData : monsterPet) {
            if (!monsterRemainHp.containsKey(battlePetData.getPetId())) {
                continue;
            }

            BattleRemainPet remainHp = monsterRemainHp.get(battlePetData.getPetId());
            if (remainHp.getRemainHpRate() <= 0) {
                continue;
            }

            Builder petBuilder = battlePetData.toBuilder();
            int hpPropertyIndex = 0;
            List<PetProperty> keysList = petBuilder.getPropDictBuilder().getKeysList();
            for (int i = 0; i < keysList.size(); i++) {
                if (keysList.get(i) == PetProperty.Current_Health) {
                    hpPropertyIndex = i;
                    break;
                }
            }

            petBuilder.getPropDictBuilder().setValues(hpPropertyIndex, remainHp.getRemainHpRate());

            result.add(petBuilder.build());
        }
        return result;
    }
}
