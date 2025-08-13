package util;

import cfg.*;
import common.GameConst.EventType;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import datatool.StringHelper;
import entity.UpdateActivityDropCount;
import model.crossarena.CrossArenaManager;
import model.crossarena.entity.playercrossarenaEntity;
import model.foreignInvasion.newVersion.NewForeignInvasionManager;
import model.gloryroad.GloryRoadManager;
import model.gloryroad.dbCache.gloryroadCache;
import model.gloryroad.entity.gloryroadEntity;
import model.inscription.dbCache.petinscriptionCache;
import model.inscription.petinscriptionEntity;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.entity.mailboxEntity;
import model.mailbox.util.MailUtil;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mistforest.MistConst;
import model.mistforest.MistTimeLimitMissionManager;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.petfragment.entity.petfragmentEntity;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.ranking.AbstractRanking;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.stoneriftEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.team.dbCache.teamCache;
import model.team.entity.teamEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager.Reason;
import platform.purchase.PurchaseManager;
import protocol.*;
import protocol.Activity.EnumRankingType;
import protocol.Comment.CommentTypeEnum;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.GloryRoad.GloryRoadQuizRecord;
import protocol.MailDB.DB_MailInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TargetSystem.TimeLimitGiftType;
import server.event.Event;
import server.event.EventManager;

import java.util.*;
import java.util.Collection;
import java.util.Map.Entry;

/**
 * 抛事件的工具类
 */
public class EventUtil {


    /**
     * 不上锁的事件
     */
    public static void unlockObjEvent(int eventType, Object... params) {
        Event event = Event.valueOf(eventType, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        if (params != null) {
            event.pushParam(params);
        }
        EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * 触发等级提升相关事件
     *
     * @param playerIdx
     * @param curLv
     * @return
     */
    public static void triggerLevelUpEvent(String playerIdx, int beforeLv, int curLv) {
        if (playerIdx == null || curLv <= 0) {
            return;
        }

        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity != null) {
            Event event = Event.valueOf(EventType.ET_UnlockTeamAndPosition, GameUtil.getDefaultEventSource(), teamEntity);
            event.pushParam(curLv);
            EventManager.getInstance().dispatchEvent(event);
        }

        //解锁主线
        mainlineEntity mainLine = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLine != null) {
            Event event = Event.valueOf(EventType.ET_UnlockMainLine, GameUtil.getDefaultEventSource(), mainLine);
            EventManager.getInstance().dispatchEvent(event);
        }

        //触发玩家等级成就
        triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_PlayerLvReach, curLv, 0);
        //升级触发外敌入侵
//        ForeignInvasionManager.getInstance().onPlayerLogin(playerIdx);
        NewForeignInvasionManager.getInstance().onPlayerLogIn(playerIdx);
        //升级触发荣耀之路
        GloryRoadManager.getInstance().onPlayerLogin(playerIdx, true);
        //玩家等级礼包
        triggerTimeLimitGift(playerIdx, TimeLimitGiftType.TLG_PlayerLv, curLv);


        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target != null) {
            //解锁许愿池
         /*   if (needUnLockWishWell(target)) {
                playerEntity player = playerCache.getByIdx(playerIdx);
                if (player!=null){
                    player.unlockFunction(EnumFunction.WishingWell);
                }
            }*/

            //解锁限时任务
            Event unlockTimeLimitActivityEvent = Event.valueOf(EventType.ET_UnlockTimeLimitActivity,
                    GameUtil.getDefaultEventSource(), target);
            unlockTimeLimitActivityEvent.pushParam(beforeLv, curLv);
            EventManager.getInstance().dispatchEvent(unlockTimeLimitActivityEvent);

            //迷雾深林限时任务任务楼层切换(任务所属于楼层变化) 如切换楼层从限时任务切换到大乱斗,则直接清空玩家当前的任务进度,从新任务开始做
            int beforeMistLv = MistConst.getPlayerLvBelongMistLv(beforeLv);
            int afterMistLv = MistConst.getPlayerLvBelongMistLv(curLv);
            if (!MistTimeLimitMissionManager.getInstance().inSameTimeLimitActivity(beforeMistLv, afterMistLv)) {
                LogUtil.info("util.EventUtil.triggerLevelUpEvent, player lv up," + playerIdx + " beforeMistLv:"
                        + beforeMistLv + ", afterMistLv:" + afterMistLv + ",is not belong the same activity," +
                        " clear player mist time limit mission");
                Event clearMistTimeLimitMission = Event.valueOf(EventType.ET_ClearMistTimeLimitMission,
                        GameUtil.getDefaultEventSource(), target);
                EventManager.getInstance().dispatchEvent(clearMistTimeLimitMission);
            }
        }

        //boss塔解锁目标
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_BossTower_UnlockLvReach
                , BossTowerConfig.getMaxUnlockLv(curLv), 0);

    }

    private static boolean needUnLockWishWell(targetsystemEntity target) {
        return PlayerUtil.queryFunctionLock(target.getIdx(), EnumFunction.WishingWell) && target.getDb_Builder().getSpecialInfo().getWishingWell().getWishMapCount() <= 0;
    }

    /**
     * 触发添加道具事件，
     *
     * @param playerIdx
     * @param addMap    <itemCfgId, addCount>
     */
    public static void triggerAddItemEvent(String playerIdx, Map<Integer, Integer> addMap, Reason sourceEnum) {
        if (playerIdx == null || addMap == null || addMap.isEmpty()) {
            return;
        }

        itembagEntity itemBagByPlayerIdx = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBagByPlayerIdx != null) {
            Event event = Event.valueOf(EventType.ET_AddItem, GameUtil.getDefaultEventSource(), itemBagByPlayerIdx);
            event.pushParam(addMap);
            event.pushParam(sourceEnum);
            EventManager.getInstance().dispatchEvent(event);

        }
    }

    /**
     * 触发删除道具事件，
     *
     * @param playerIdx
     * @param removeMap <itemCfgId, removeCount>
     */
    public static boolean triggerRemoveItemEvent(String playerIdx, Map<Integer, Integer> removeMap, Reason reason) {
        if (playerIdx == null) {
            return false;
        }

        if (removeMap == null || removeMap.isEmpty()) {
            return true;
        }

        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBag == null) {
            return false;
        }

        boolean enough = SyncExecuteFunction.executeFunction(itemBag, item -> {
            for (Entry<Integer, Integer> entry : removeMap.entrySet()) {
                if (itemBag.getItemCount(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
            return true;
        });

        if (!enough) {
            return false;
        }

        Event event = Event.valueOf(EventType.ET_RemoveItem, GameUtil.getDefaultEventSource(), itemBag);
        event.pushParam(removeMap);
        event.pushParam(reason);
        return EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * 触发删除道具事件，
     */
    public static boolean triggerRemoveItemEvent(String playerIdx, int removeId, int removeCount, Reason sourceEnum) {
        Map<Integer, Integer> removeMap = new HashMap<>();
        removeMap.put(removeId, removeCount);
        return triggerRemoveItemEvent(playerIdx, removeMap, sourceEnum);
    }


//    public static boolean addMailByPlayerIdxList(Collection<String> playerIdxList, int mailTemplateId, List<Reward> rewards,
//                                                 Reason reason, String... bodyParam) {
//        if (CollectionUtils.isEmpty(playerIdxList)) {
//            return false;
//        }
//        playerIdxList.forEach(e -> {
//            triggerAddMailEvent(e, mailTemplateId, rewards, reason, bodyParam);
//        });
//        return true;
//    }

    /**
     * 触发添加邮件事件
     *
     * @param playerIdx
     * @param mailTemplateId
     * @param rewards        如果添加reward 则以添加的rewards为准,此字段null时以模板附件为准，要想邮件没有附件，使用空List
     * @param bodyParam      用于邮件体字符串拼接
     * @return
     */
    public static boolean triggerAddMailEvent(String playerIdx, int mailTemplateId, List<Reward> rewards,
                                              Reason reason, String... bodyParam) {
        DB_MailInfo.Builder builder = MailUtil.fillDBMailByTemplateId(mailTemplateId, rewards, bodyParam);
        if (builder == null) {
            return false;
        }
        triggerAddMailEvent(playerIdx, builder, reason);
        return true;
    }

    /**
     * 触发添加邮件事件
     *
     * @param playerIdx
     * @return
     */
    public static boolean triggerAddMailEvent(String playerIdx, DB_MailInfo.Builder mailInfo, Reason reason) {
        mailboxEntity entity = mailboxCache.getInstance().getMailBoxByPlayerIdx(playerIdx);
        if (entity == null) {
            return false;
        }
        Event event = Event.valueOf(EventType.ET_AddMail, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(mailInfo, reason);
        EventManager.getInstance().dispatchEvent(event);
        return true;
    }

    /**
     * 更新目标系统进度
     *
     * @param playerIdx
     * @param type
     * @param addPro    添加进度
     * @param param     参数
     */
    public static void triggerUpdateTargetProgress(String playerIdx, TargetTypeEnum type, int addPro, int param) {
        if (playerIdx == null || type == null || type == TargetTypeEnum.TTE_NULL || addPro <= 0) {
            LogUtil.warn("util.EventUtil.triggerUpdateTargetProgress, error param, playerIdx = " + playerIdx +
                    ", type = " + type + ", addPro = " + addPro + ", param =" + param);
            return;
        }

        targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (targetEntity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_UpdateTargetProgress, GameUtil.getDefaultEventSource(), targetEntity);
        event.pushParam(type, addPro, param);
        EventManager.getInstance().dispatchEvent(event);
    }


    /**
     * 增量更新玩家活动掉落数量
     *
     * @param playerIdx
     */
    public static void triggerUpdateDropItemCount(String playerIdx, List<UpdateActivityDropCount> dropSource) {
        if (playerIdx == null || dropSource == null || dropSource.isEmpty()) {
            LogUtil.warn("util.EventUtil.triggerUpdateDropItemCount, error param, playerIdx = "
                    + playerIdx + ", dropSource size =  " + CollectionUtils.size(dropSource));
            return;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_UPDATE_ACTIVITY_DROP_COUNT, GameUtil.getDefaultEventSource(), target);
        event.pushParam(dropSource);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddCurrency(String playerIdx, RewardTypeEnum type, int count, Reason reason) {
        if (playerIdx == null) {
            LogUtil.error("util.EventUtil.triggerAddCurrency, error param, reason=" + reason);
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddCurrency, GameUtil.getDefaultEventSource(), player);
        event.pushParam(type, count, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddPetFragment(String playerIdx, Map<Integer, Integer> cfgIdCountMap, Reason reason) {
        if (playerIdx == null || cfgIdCountMap == null || cfgIdCountMap.isEmpty()) {
            LogUtil.error("util.EventUtil.triggerAddPetFragment, error param, reason=" + reason);
            return;
        }

        petfragmentEntity fragmentByPlayer = PetFragmentServiceImpl.getInstance().getFragmentByPlayer(playerIdx);
        if (fragmentByPlayer == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddPetFragment, GameUtil.getDefaultEventSource(), fragmentByPlayer);
        event.pushParam(cfgIdCountMap, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddPet(String playerIdx, Map<Integer, Integer> cfgIdCountMap, Reason reason) {
        if (playerIdx == null || cfgIdCountMap == null || cfgIdCountMap.isEmpty()) {
            LogUtil.error("util.EventUtil.triggerAddPet, error param, reason=" + reason);
            return;
        }

        petEntity petCacheTemp = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (petCacheTemp == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddPet, GameUtil.getDefaultEventSource(), petCacheTemp);
        event.pushParam(cfgIdCountMap, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddPetRune(String playerIdx, Map<Integer, Integer> cfgIdCountMap, Reason reason) {
        if (playerIdx == null || cfgIdCountMap == null || cfgIdCountMap.isEmpty()) {
            LogUtil.error("util.EventUtil.triggerAddPetRune, error param, reason=" + reason);
            return;
        }

        petruneEntity runeCacheTemp = petruneCache.getInstance().getEntityByPlayer(playerIdx);
        if (runeCacheTemp == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddPetRune, GameUtil.getDefaultEventSource(), runeCacheTemp);
        event.pushParam(cfgIdCountMap, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddAvatar(String playerIdx, Collection<Integer> cfgIdList, Reason reason) {
        if (playerIdx == null || cfgIdList == null || cfgIdList.isEmpty()) {
            LogUtil.error("util.EventUtil.triggerAddAvatar, error param, reason=" + reason);
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddAvatar, GameUtil.getDefaultEventSource(), player);
        event.pushParam(cfgIdList, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddAvatarBorder(String playerIdx, Collection<Integer> cfgIdList, Reason reason) {
        if (playerIdx == null || cfgIdList == null || cfgIdList.isEmpty()) {
            LogUtil.error("util.EventUtil.triggerAddAvatarBorder, error param, reason=" + reason);
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddAvatarBorder, GameUtil.getDefaultEventSource(), player);
        event.pushParam(cfgIdList, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddVIPExp(String playerIdx, int exp) {
        if (playerIdx == null) {
            LogUtil.error("util.EventUtil.triggerAddVIPExp, error param");
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddVIPExp, GameUtil.getDefaultEventSource(), player);
        event.pushParam(exp);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddExp(String playerIdx, int exp) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddExp, GameUtil.getDefaultEventSource(), player);
        event.pushParam(exp);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void cleatTeam(String playerIdx, TeamNumEnum team, boolean sendMsg) {
        if (StringHelper.isNull(playerIdx) || null == team || TeamNumEnum.TNE_Team_Null == team) {
            return;
        }

        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_CLEAR_TEAM, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(team);
        event.pushParam(sendMsg);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void removePetFromTeams(String playerIdx, Set<String> petIdxSet) {
        if (StringHelper.isNull(playerIdx) || GameUtil.collectionIsEmpty(petIdxSet)) {
            return;
        }

        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_REMOVE_PET_FROM_TEAMS, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(petIdxSet);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void resetRuneStatus(String playerIdx, List<String> petIdxList) {
        if (GameUtil.collectionIsEmpty(petIdxList)) {
            return;
        }

        petruneEntity entity = petruneCache.getInstance().getEntityByPlayer(playerIdx);
        if (entity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_ResetRuneStatus, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(petIdxList);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void removeDisPet(String playerIdx, List<String> petIdxList) {
        if (GameUtil.collectionIsEmpty(petIdxList)) {
            return;
        }

        //移除宠物后需要移除展示的宠物
        playerEntity playerEntity = playerCache.getByIdx(playerIdx);
        if (playerEntity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_RemoveDisplayPet, GameUtil.getDefaultEventSource(), playerEntity);
        event.pushParam(petIdxList);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void updatePetTeamState(String playerIdx, Collection<String> petIdxList, boolean inOrNot, boolean sendMsg) {
        if (GameUtil.collectionIsEmpty(petIdxList)) {
            return;
        }

        //移除宠物后需要移除展示的宠物
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (entity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_UPDATE_PET_TEAM_STATE, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(petIdxList);
        event.pushParam(inOrNot);
        event.pushParam(sendMsg);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerReChargeActivity(String playerId, int rechargeScore) {
        if (playerId == null || rechargeScore <= 0) {
            LogUtil.error("util.EventUtil.triggerReChargeActivity, error param");
            return;
        }
        targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (targetEntity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_PLAYER_RECHARGE_ACTIVITY, GameUtil.getDefaultEventSource(), targetEntity);
        event.pushParam(playerId, rechargeScore, playerCache.getInstance().queryTodayRecharge(playerId));
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddLimitPurchaseId(String playerId, int rechargeId) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (target == null) {
            return;
        }
        if (target.getDb_Builder().getLimitPurchaseRechargeIdsList().contains(rechargeId)) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_Add_Limit_Purchase_Recharge_Id, GameUtil.getDefaultEventSource(), target);
        event.pushParam(rechargeId);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void recreateMonsterDiff(String playerIdx, EnumFunction function) {
        if (StringUtils.isBlank(playerIdx) || function == null) {
            return;
        }

        recreateMonsterDiff(playerIdx, function, mainlineCache.getInstance().getPlayerCurNode(playerIdx));
    }

    public static void recreateMonsterDiff(String playerIdx, EnumFunction function, int nodeId) {
        if (StringUtils.isBlank(playerIdx) || function == null) {
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("util.EventUtil.recreateMonsterDiff, player is null, playerIdx:" + playerIdx);
            return;
        }

        Event event = Event.valueOf(EventType.ET_ReCreateMonsterDiff, GameUtil.getDefaultEventSource(), player);
        event.pushParam(function, nodeId);
        EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * 清空所有玩家的指定活动数据
     *
     * @param activity
     */
    public static void clearAllPlayerActivityInfo(ServerActivity activity) {
        if (activity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_CLEAR_ALL_PLAYER_ACTIVITY_INFO, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(activity);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerTimeLimitGift(String playerIdx, TimeLimitGiftType timeLimitGiftType, int curtTarget) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("util.EventUtil.recreateMonsterDiff, targetSystemEntity is null, playerIdx:" + playerIdx);
            return;
        }
        Event event = Event.valueOf(EventType.ET_TIME_LIMIT_GIFT, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(timeLimitGiftType);
        event.pushParam(curtTarget);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void openTraining(String playerIdx, int id) {
        Event event = Event.valueOf(EventType.ET_OpenTraining, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(playerIdx);
        event.pushParam(id);
        EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * 默认添加一次
     *
     * @param playerIdx
     */
    public static void addReportTimes(String playerIdx) {
        addReportTimes(playerIdx, 1);
    }

    public static void addReportTimes(String playerIdx, int addTimes) {
        if (StringUtils.isBlank(playerIdx) || addTimes <= 0) {
            return;
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_ADD_REPORT_TIMES, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(addTimes);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void ban(List<String> playerIdx, int banType, long endTime, String banMsg) {
        if (CollectionUtils.isEmpty(playerIdx) || endTime <= GlobalTick.getInstance().getCurrentTime()) {
            return;
        }
        unlockObjEvent(EventType.ET_BAN, playerIdx, banType, endTime, banMsg);
    }

    public static void shieldComment(List<CommentTypeEnum> typeList, String playerIdx) {
        if (StringUtils.isBlank(playerIdx) || CollectionUtils.isEmpty(typeList)) {
            return;
        }
        unlockObjEvent(EventType.ET_SHIELD_COMMENT, typeList, playerIdx);
    }


    /**
     * @param playerIdx
     * @param petIdx
     * @param warUpdateType
     * @see common.GameConst.WarPetUpdate
     */
    public static void triggerWarPetUpdate(String playerIdx, String petIdx, int warUpdateType) {
        if (StringUtils.isBlank(petIdx) || StringUtils.isBlank(petIdx)) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null || StringHelper.isNull(player.getDb_data().getTheWarRoomIdx())) {
            return;
        }
        if (!player.getDb_data().getTheWarData().containsInWarPets(petIdx)) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_WarPet_Update, GameUtil.getDefaultEventSource(), player);
        event.pushParam(petIdx, warUpdateType);
        EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * @param playerId
     * @param lastAddition
     * @param nowAddition
     * @param type         0 其他 1神器, 2称号,3图鉴
     */
    public static void triggerAllPetAdditionUpdate(String playerId, Map<Integer, Integer> lastAddition, Map<Integer, Integer> nowAddition, int type) {
        if (StringUtils.isEmpty(playerId)) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AllPetAdditionUpdate, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(playerId, lastAddition, nowAddition, type);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddInscription(String playerIdx, Map<Integer, Integer> cfgIdCountMap, Reason reason) {
        if (playerIdx == null || cfgIdCountMap == null || cfgIdCountMap.isEmpty()) {
            LogUtil.error("util.EventUtil.triggerAddGem, error param, reason=" + reason);
            return;
        }

        petinscriptionEntity inscriptionEntity = petinscriptionCache.getInstance().getEntityByPlayer(playerIdx);
        if (inscriptionEntity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddInscription, GameUtil.getDefaultEventSource(), inscriptionEntity);
        event.pushParam(cfgIdCountMap, reason);
        EventManager.getInstance().dispatchEvent(event);

    }

    public static void triggerAddGem(String playerIdx, Map<Integer, Integer> cfgIdCountMap, Reason reason) {
        if (playerIdx == null || cfgIdCountMap == null || cfgIdCountMap.isEmpty()) {
            LogUtil.error("util.EventUtil.triggerAddGem, error param, reason=" + reason);
            return;
        }

        petgemEntity gemCacheTemp = petgemCache.getInstance().getEntityByPlayer(playerIdx);
        if (gemCacheTemp == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddPetGem, GameUtil.getDefaultEventSource(), gemCacheTemp);
        event.pushParam(cfgIdCountMap, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void resetPetGemStatus(String playerIdx, List<String> gemIds) {
        if (GameUtil.collectionIsEmpty(gemIds)) {
            return;
        }

        petgemEntity entity = petgemCache.getInstance().getEntityByPlayer(playerIdx);
        if (entity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_ResetPetGemStatus, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(gemIds);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerUpdatePlayerRecharge(String playerIdx, int addCoupon) {
        if (StringUtils.isEmpty(playerIdx) || addCoupon == 0) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_UpdatePlayerRecharge, GameUtil.getDefaultEventSource(), player);
        event.pushParam(addCoupon);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerUpdatePlayerStar(String playerIdx, int rechargeValue) {
        if (StringUtils.isEmpty(playerIdx) || rechargeValue == 0) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_UpdateStarCount, GameUtil.getDefaultEventSource(), player);
        event.pushParam(playerIdx);
        event.pushParam(rechargeValue);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void removeDeadPetFromTeam(String playerIdx, TeamTypeEnum teamType) {
        if (StringUtils.isEmpty(playerIdx) || teamType == null || teamType == TeamTypeEnum.TTE_Null) {
            return;
        }

        teamEntity teams = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teams != null) {
            Event event = Event.valueOf(EventType.ET_RemoveTeamDeadPet, GameUtil.getDefaultEventSource(), teams);
            event.pushParam(teamType);
            EventManager.getInstance().dispatchEvent(event);
        }
    }

    public static void triggerUpdatePetMissionLvUpPro(String playerIdx, Map<Integer, Integer> missionPro) {
        petmissionEntity petmissionEntity = petmissionCache.getInstance().getEntityByPlayerIdx(playerIdx);
        if (petmissionEntity == null) {
            return;
        }

        if (petmissionEntity.getMissionListBuilder().getMissionLv() >= PetMissionLevel.getMaxMissionLv()) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_UpdatePetMissionLvUpPro, GameUtil.getDefaultEventSource(), petmissionEntity);
        event.pushParam(missionPro);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void addPointInstance(String playerIdx, int addCount, Reason reason) {
        if (StringUtils.isEmpty(playerIdx) || addCount <= 0) {
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            Event event = Event.valueOf(EventType.ET_AddPointCopyScore, GameUtil.getDefaultEventSource(), entity);
            event.pushParam(addCount, reason);
            EventManager.getInstance().dispatchEvent(event);
        }
    }

    public static void updateRanking(AbstractRanking ranking) {
        if (ranking == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_UpdateRanking, GameUtil.getDefaultEventSource(), ranking);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerPausePatrolMission(String playerIdx, boolean pause) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return;
        }
        TargetSystemDB.DB_PatrolMission patrolMission = target.getDb_Builder().getPatrolMission();
        if (patrolMission.getEndTime() <= 0 || Common.MissionStatusEnum.MSE_Finished == patrolMission.getMission().getStatus()) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_UpdatePatrolMissionSwitch, GameUtil.getDefaultEventSource(), target);
        event.pushParam(pause);
        EventManager.getInstance().dispatchEvent(event);
    }


    public static void triggerRechargeProduct(String playerIdx, Map<Integer, Integer> cfgIdCountMap, Reason reason) {
        LogUtil.info("playerIdx:{} triggerRechargeProduct:{},reason:{}", playerIdx, cfgIdCountMap, reason);
        if (MapUtils.isEmpty(cfgIdCountMap)) {
            LogUtil.error("playerIdx:{},triggerRechargeProduct cfgIdCountMap is empty", playerIdx);
            return;
        }
        for (Entry<Integer, Integer> entry : cfgIdCountMap.entrySet()) {
            if (entry.getValue() < 1) {
                continue;
            }
            if (entry.getValue() > 1) {
                LogUtil.warn("playerIdx:{},triggerRechargeProduct cfgId:{},count:{} more than 1,but only active 1", playerIdx, entry.getKey(), entry.getValue());
            }
            PurchaseManager.getInstance().settlePurchaseByRechargeProduct(playerIdx, entry.getKey(), reason);
        }
    }

    public static void addNewTitles(String playerIdx, List<Integer> newTitleList, Reason reason) {
        if (StringUtils.isEmpty(playerIdx) || CollectionUtils.isEmpty(newTitleList)) {
            LogUtil.info("util.EventUtil.addNewTitles, error param, playerIdx:" + playerIdx + ", newTitleList:"
                    + GameUtil.collectionToString(newTitleList));
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.info("util.EventUtil.addNewTitles, error param, playerIdx:" + playerIdx + " entity is null");
            return;
        }

        Event event = Event.valueOf(EventType.ET_AddNewTitle, GameUtil.getDefaultEventSource(), player);
        event.pushParam(newTitleList, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void addTrainItem(String playerIdx, Map<Integer, Integer> cfgIdCountMap, Reason reason) {
        LogUtil.info("playerIdx:{} addTrainItem:{},reason:{}", playerIdx, cfgIdCountMap, reason);
        if (MapUtils.isEmpty(cfgIdCountMap)) {
            LogUtil.error("playerIdx:{},addTrainItem cfgIdCountMap is empty", playerIdx);
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.info("util.EventUtil.addTrainItem, error param, playerIdx:" + playerIdx + " entity is null");
            return;
        }
        Event event = Event.valueOf(EventType.ET_TrainItemAdd, GameUtil.getDefaultEventSource(), player);
        event.pushParam(cfgIdCountMap, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void addCrossGradeGrade(String playerIdx, int count, Reason reason) {
        if (StringHelper.isNull(playerIdx)) {
            LogUtil.error("util.EventUtil.triggerAddCurrency, error param, reason=" + reason);
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddCrossArenaGrade, GameUtil.getDefaultEventSource(), player);
        event.pushParam(count, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void addMistMoveEffect(String playerIdx, List<Integer> cfgIdList, Reason reason) {
        if (StringHelper.isNull(playerIdx)) {
            LogUtil.error("util.EventUtil.triggerAddCurrency, error param, reason=" + reason);
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddCrossArenaGrade, GameUtil.getDefaultEventSource(), player);
        event.pushParam(cfgIdList, reason);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerClearBuyRecordOnPlayer(String playerIdx, long activityId) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_ClearClaimRecordOnPlayer, GameUtil.getDefaultEventSource(), player);
        event.pushParam(activityId);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void addGloryRoadQuizRecord(String playerIdx, GloryRoadQuizRecord record) {
        gloryroadEntity entity = gloryroadCache.getInstance().getEntity(playerIdx);
        if (entity == null || record == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_AddGloryRoadQuizRecord, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(record);
        EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * @param playerIdx1
     * @param playerIdx2
     * @param player1Result
     * @param linkBattleRecordId
     */
    public static void gloryRoadBattleResult(String playerIdx1, String playerIdx2, int player1Result, String linkBattleRecordId) {
        if (StringUtils.isEmpty(playerIdx1)) {
            return;
        }
        unlockObjEvent(EventType.ET_GloryRoadBattleResult, playerIdx1, playerIdx2 == null ? "" : playerIdx2,
                player1Result, linkBattleRecordId == null ? "" : linkBattleRecordId);
    }

    public static void triggerUpdateStageRewardProgress(String playerIdx, long activityId, int add) {
        if (StringUtils.isEmpty(playerIdx) || add == 0) {
            return;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        Event event = Event.valueOf(EventType.ET_UpdateStageRewardTarget, GameUtil.getDefaultEventSource(), target);
        event.pushParam(activityId, add);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerCollectMazeItemCount(String playerIdx, int itemCount) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_CollectMazeItem, GameUtil.getDefaultEventSource(), player);
        event.pushParam(itemCount);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerRefreshPetData(String playerId, String petId, Reason reason) {
        if (StringUtils.isEmpty(playerId) || StringUtils.isEmpty(petId)) {
            return;
        }
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        if (entity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_RefreshPetData, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(petId).pushParam(reason);
        EventManager.getInstance().dispatchEvent(event);

    }

    public static void triggerUnlockFunction(String playerIdx, List<EnumFunction> functions) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null || CollectionUtils.isEmpty(functions)) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_UnLockFunction, GameUtil.getDefaultEventSource(), player);
        event.pushParam(functions);
        EventManager.getInstance().dispatchEvent(event);

    }

    public static void triggerCoupTeamUpdate(String playerIdx) {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_CoupTeamUpdate, GameUtil.getDefaultEventSource(), teamEntity);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void clearHelpPet(String playerIdx, EnumFunction function) {
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (petEntity == null) {
            return;
        }
        PetMessage.HelpPetBagItem helpPetBagItem = petEntity.getDbPetsBuilder().getHelpPetMap().get(function.getNumber());
        if (helpPetBagItem == null || helpPetBagItem.getPetCount() <= 0) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_ClearHelpPet, GameUtil.getDefaultEventSource(), petEntity);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerCollectArtifactExp(String playerId, int artifactId, int lastSkillLv, int nowSkillLv) {
        if (lastSkillLv >= nowSkillLv) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_CollectArtifactExp, GameUtil.getDefaultEventSource(), player);
        event.pushParam(artifactId, lastSkillLv, nowSkillLv);
        EventManager.getInstance().dispatchEvent(event);

    }

    public static void triggerCollectPets(String playerIdx, Collection<PetMessage.Pet> pets) {
        if (StringUtils.isBlank(playerIdx) || CollectionUtils.isEmpty(pets)) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_CollectPet, GameUtil.getDefaultEventSource(), player);
        event.pushParam(pets);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void removeInscription(String playerIdx, List<String> inscriptionIds) {
        if (CollectionUtils.isEmpty(inscriptionIds) || StringUtils.isBlank(playerIdx)) {
            return;
        }
        petinscriptionEntity entity = petinscriptionCache.getInstance().getEntityByPlayer(playerIdx);
        if (entity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_RemoveInscription, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(inscriptionIds);
        EventManager.getInstance().dispatchEvent(event);

    }

    public static void triggerCompleteStoneRiftUnlockMission(String playerIdx, List<Integer> completeMissionIds) {
        if (CollectionUtils.isEmpty(completeMissionIds) || StringUtils.isBlank(playerIdx)) {
            return;
        }
        stoneriftEntity entity = stoneriftCache.getByIdx(playerIdx);
        if (entity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_CompleteStoneRiftMission, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(completeMissionIds);
        EventManager.getInstance().dispatchEvent(event);

    }

    public static void triggerUpdateStoneRiftAchievement(String playerIdx, List<TargetSystem.TargetMission> completeMissionIds) {
        if (CollectionUtils.isEmpty(completeMissionIds) || StringUtils.isBlank(playerIdx)) {
            return;
        }
        stoneriftEntity entity = stoneriftCache.getByIdx(playerIdx);
        if (entity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_UpdateStoneRiftAchievement, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(completeMissionIds);
        EventManager.getInstance().dispatchEvent(event);

    }

    public static void triggerUpdateCrossArenaWeeklyTask(String playerIdx, CrossArena.CrossArenaGradeType type, int value) {
        if (StringUtils.isBlank(playerIdx) || type == null) {
            return;
        }
        playercrossarenaEntity entity = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        if (entity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_UpdateCrossArenaWeeklyTask, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(type, value);
        EventManager.getInstance().dispatchEvent(event);

    }

    public static void updateIncrRankingScore(String playerIdx, EnumRankingType rankingType, int removeCount) {
        if (removeCount <= 0 || StringUtils.isEmpty(playerIdx)) {
            return;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target==null){
            return;
        }
        Event event = Event.valueOf(EventType.ET_UpdateIncrRankingScore, GameUtil.getDefaultEventSource(), target);
        event.pushParam(rankingType, removeCount);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void triggerAddStoneRiftFactoryExp(String playerId, int factoryId, Reward stealReward) {
        if (StringUtils.isEmpty(playerId) || factoryId <= 0 || stealReward == null) {
            return;
        }
        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_AddStoneRiftFactoryExp, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(factoryId, stealReward);
        EventManager.getInstance().dispatchEvent(event);
    }
}
