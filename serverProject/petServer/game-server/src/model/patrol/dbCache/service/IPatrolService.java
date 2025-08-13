package model.patrol.dbCache.service;

import model.patrol.entity.*;
import protocol.Battle;
import protocol.Common.Reward;
import protocol.Patrol;
import protocol.Patrol.PatrolPoint;
import protocol.Patrol.PatrolSearchEvent;
import protocol.Patrol.PatrolStatus;
import protocol.PetMessage.Pet;
import entity.CommonResult;
import entity.RewardResult;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/8/1
 */
public interface IPatrolService {
    /**
     * 开启巡逻队游戏
     *
     * @param playerId   玩家id
     * @return 地图/玩家信息
     */
    PatrolInitResult patrolMapInit(String playerId);

    /**
     * 玩家移动
     *
     * @param playerId 玩家id
     * @param point    前进点位置
     * @return 移动成功/失败
     */
    PatrolMoveResult move(String playerId, PatrolPoint point);

    /**
     * 手动结束本次巡逻
     *
     * @param playerId 玩家id
     * @return 操作结果
     */
    CommonResult finish(String playerId);

    /**
     * 查询游戏难度和进度
     *
     * @param playerId 玩家id
     * @return 返回结果
     */
    PatrolFinishResult patrolStatus(String playerId);

    /**
     * 玩家失败，使用道具继续游戏
     *
     * @param playerId 玩家id
     * @param result
     * @return 操作结果
     */
    void reborn(String playerId, Patrol.SC_PatrolReborn.Builder result);

    /**
     * 玩家移动触发探索事件
     *
     * @param forward  前进位置（仅含xy坐标信息）
     * @param event    触发方式
     * @param playerId 玩家id
     * @return 效果状态
     */
    PatrolExploreResult explore(String playerId, PatrolSearchEvent event, PatrolPoint forward);

    /**
     * 玩家移动打开宝藏
     *
     * @param playerId 玩家id
     * @param forward  前进位置（仅含xy坐标信息）
     * @param buy
     * @return 开启结果
     */
    PatrolMoveResult treasure(String playerId, PatrolPoint forward, boolean buy);

    /**
     * 玩家进入战斗
     *
     * @param playerId 玩家id
     * @param irritate 是否激怒：0否1是
     * @param x        前进x
     * @param y        前进y
     * @return 战斗id
     */
    PatrolBattleResult getFightMakeId(String playerId, int x, int y, int irritate);

    /**
     * 获得战斗奖励
     *
     * @param playerId 玩家id
     * @return 奖励列表
     */
    List<Reward> getBattleReward(String playerId);

    /**
     * 结算玩家战斗
     *  @param playerId 玩家id
     * @param battleResult 战斗结果
     * @param rewardList  奖励列表
     * @param location
     */
    void battleSettle(String playerId, int battleResult, List<Reward> rewardList, PatrolTree location);

    /**
     * 购买时光卷轴
     *
     * @param playerId 玩家id
     * @return 返回次数
     */
    PatrolPurchaseResult patrolPurchase(String playerId);

    /**
     * vip升级获取免费重生次数
     *
     * @param playerId 玩家id
     * @param beforeVip 升级前vip等级
     * @param finalAfterAdd 当前vip等级
     * @return 升级结果
     */
    boolean vipLevelUp(String playerId, int beforeVip, int finalAfterAdd);

    /**
     * 推送通关奖励信息
     *
     * @param playerId 玩家id
     * @return 推送结果
     */
    RewardResult displayReward(String playerId);

    /**
     * 查询玩家的巡逻队状态
     *
     * @param playerIdx
     * @return
     */
    PatrolStatus getStateByPlayerIdx(String playerIdx);

    /**
     * 处理玩家密室请求
     *
     * @param playerId 玩家id
     * @param forward  前进位置
     * @param event    触发事件
     * @return 查询结果
     */
    PatrolMoveResult chooseChamber(String playerId, PatrolPoint forward, Patrol.PatrolChamberEvent event);

    /**
     * 验证助战宠物是否存在
     *
     * @param playerId 玩家id
     * @param petId    宠物id
     * @return 验证结果
     */
    Pet getVirtualPet(String playerId, String petId);

    /**
     * 插入/更新 巡逻队entity
     * @param playerIdx 玩家id
     * @param patrolExist 当前玩家patrolEntity
     * @param patrolCfg PatrolConfig--Id
     * @param mapId MapConfig--Id
     * @return
     */
    patrolEntity upsertPatrolEntity(String playerIdx, patrolEntity patrolExist, int patrolCfg, int mapId);

    void sendPatrolInit(String playerId);

    PatrolBattleResult preBattleInfo(String playerIdx, int irritate);

    boolean todayFirstPlay(patrolEntity patrolEntity);
}
