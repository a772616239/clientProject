package model.mistforest;

import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import cfg.MistCommonConfig;
import cfg.MistCommonConfigObject;
import cfg.MistGhostConfig;
import cfg.MistGhostConfigObject;
import cfg.MistJewelryConfig;
import cfg.MistJewelryConfigObject;
import cfg.MistLootPackCarryConfig;
import cfg.MistLootPackCarryConfigObject;
import cfg.MistMonsterFightConfig;
import cfg.MistMonsterFightConfigObject;
import cfg.MistTimeLimitActivity;
import cfg.MistTimeLimitActivityObject;
import cfg.PlayerLevelConfig;
import cfg.PlayerLevelConfigObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.GameConst.EventType;
import common.GlobalTick;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import model.mistforest.mistobj.MistFighter;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.ServerTransfer.PairValue;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.TimeUtil;

public class MistConst {
    public static final int MistRoomMaxPlayerCount = 100;
    public static final int MistRoomMaxTeamMemberSize = 5;

    public static final int MistObjMoveInterval = 100;

    public static final int MistItemSkillMaxCount = 4;

    public static final int MistRobotPursueDistance = 10; // 抓鬼机器人追击最远距离

    public static final int MistDynamicTouchMaxDistance = 3; // 判定运动玩家距离

    public static final int MistStaticTouchMaxDistance = 1; // 判定静态玩家距离

    public static final int MistTouchShopMaxDistance = 3; // 判定商店距离

    public static final int MistCageBuffId = 2; //  画地为牢出生buff写死

    public static final int ContinualKillCount = 5;

    public static final int EmptyRoomTime = 600000; // 清除空房间时间

    public static final long BattleTimeout = 300000;
    public static final long NewbieBattleTimeout = 1830000;

    public static final long MistKeyBroadcastTime = 30000;

    public static final long MaxOfflineTime = 300000;

    public static final long CheckClientClockTime = 2100; // 加速检测时间

    public static final long MistInvokeScrollItemId = 16; // 召唤卷轴道具id
    public static final int RefinedStoneItemId = 10000; // 精炼石道具id
    public static final int FriendPointItemId = 2; // 友情卡道具id

    public static final int MistDelayRemoveTime = 5; // 迷雾森林死亡延迟移除时间


    public static final int MistWaitBossBattleBuffId = 15; // 迷雾森林等待boss战斗buffId
    public static final int MistVolcanoBuffId = 59; // 迷雾森林火山喷发buffId
    public static final int MistVipRebornBuffId = 70; // 迷雾森林待复活状态buffId
    public static final int MistRebornProtectedBuffId = 17; // 迷雾森林出生保护buffId

    public static class MistTriggerParamType {
        public final static int BuffId = 1;
        public final static int ItemId = 2;
        public final static int KeyId = 3;
        public final static int TreasureBoxId = 4;
        public final static int TreasureBagId = 5;
        public final static int TrapId = 6;
        public final static int DoorId = 7;
        public final static int ItemSkillId = 8;
        public final static int UseItemSkillIndex = 9;
        public final static int BuffTime = 10;
        public final static int TransInvokerId = 11; // 发起召唤传送令id
        public final static int BlinkGridTransToward = 12; // 迷宫传送格子传送方向
        public final static int BlinkGridLevel = 13; // 迷宫传送格子层数
        public final static int BlinkGridTransLevel = 14; // 迷宫传送格子目标层数
        public final static int GhostObjId = 15;
        public final static int BeatWantedPlayerFlag = 16; // 是否击败通缉状态玩家标识
        public final static int SettleJewelryCount = 17; // 结算时宝珠数量
        public final static int RemoveObjId = 18; // 通用移除对象id
        public final static int TranPosData = 19; // 传送位置信息
        public final static int SnowBallId = 20;  // 雪球id
        public final static int LavaBadgeId = 21;  // 熔岩徽章对象id
        public final static int LavaBadgeCount = 22;  // 熔岩徽章数量
        public final static int VipSkillType = 23;  // 特权技能类型
        public final static int DirectSettleBattleFlag = 24;  // 直接结算战斗标识
        public final static int ChangeStaminaVal = 25;  // 改变体力所需体力值
        public final static int CommonRewardObjId = 26; // 通用奖励对象id
    }

    public static class MistMonsterState {
        public final static int idle = 0;       // 普通状态
        public final static int pursue = 1;     // 追击状态
        public final static int attack = 2;     // 攻击状态
        public final static int endPursue = 3;  // 结束追击返回出生点状态
    }

    public static class MistGhostState {
        public final static int idle = 0;           // 普通状态
        public final static int escape = 1;         // 逃跑状态
        public final static int pursue = 2;         // 追击状态(仅守卫鬼魂)
        public final static int endPursue = 3;      // 结束追击返回出生点状态(仅守卫鬼魂)
    }

    public static class MistRobotState {
        public final static int idle = 0;           // 普通状态
        public final static int escape = 1;         // 逃跑状态
        public final static int pursue = 2;         // 追击状态
    }

    public static class MistGuardMonsterState {
        public final static int idle = 0;           // 站立状态
        public final static int patrol = 1;         // 巡逻状态
        public final static int arrest = 2;         // 逮捕状态
    }

    public static class MistNormalState {
        public final static int idle = 0;           // 站立状态
        public final static int patrol = 1;         // 巡逻状态
    }

    public static class MistBattleSide {
        public final static int notBattle = 0;
        public final static int leftSide = 1;
        public final static int rightSide = 2;
    }

    public static class MistBossKeyState {
        public final static int keyNotBorn = 0; // boss钥匙未出现状态
        public final static int keyNotPicked = 1; // boss钥匙出现未拾取状态
        public final static int keyPicked = 2; // boss钥匙被拾取状态
    }

    public static class MistActivityBossStage {
        public final static int initStage = 0; // 活动boss初始状态
        public final static int furyStage = 1; // 活动boss狂暴状态
        public final static int weakStage = 2; // 活动boss最终虚弱状态
    }

    public static class MistEnmityState {
        public final static int normal = 0;     // 正常状态
        public final static int warning = 1;    // 警惕状态
        public final static int fury = 2;       // 愤怒状态
        public final static int attack = 3;     // 攻击状态
        public final static int reback = 4;     // 返回原点状态
    }

    public static class MistGambleResult {
        public final static int smaller = 0;     // 小
        public final static int bigger = 1;    // 大
    }

    public static class MistVipSkillType {
        public final static int Transport = 1;          // 传送技能
        public final static int SearchTreasure = 2;     // 寻宝技能
        public final static int RebornInSituPlace = 3;  // 原地复活技能
    }

    public static class MistConditionType {
        public final static int CheckProperty = 1;				    // 判断属性
        public final static int CheckObjType = 2;					// 判断目标类型
        public final static int CheckOwnedBuff = 3;				    // 判断是否有buff
        public final static int CheckGroup = 4;					    // 判断阵营
        public final static int CheckTargetPos = 5;				    // 判断与目标距离
        public final static int CheckFixPos = 6;					// 判断与固定点距离
        public final static int CheckItemType = 7;				    // 判断道具类型(仅道具)
        public final static int CheckItemSkillFull = 8;			    // 判断道具是否已满(仅玩家)
        public final static int CheckProbability = 9;				// 判断概率
        public final static int CheckImmune = 10;					// 判断是否免疫负面效果
        public final static int CheckPlayerMode = 11;				// 判断玩家模式
        public final static int CheckBeatWantedPlayerBattle = 12;   // 判断是否击败通缉状态玩家
        public final static int CheckIsSharedBox = 13;              // 判断是否时共享宝箱
        public final static int CheckJewelrySettle = 14;        	// 判断是否含有宝珠数量结算
        public final static int CheckLavaBadgeSettle = 15;          // 判断是否含有熔岩徽章结算
        public final static int CheckInScheduleSection = 16;        // 判断是否在时间封闭区间内
        public final static int CheckVipSkillType = 17;      		// 判断特权技能类型
        public final static int CheckDirectSettleBattle = 18;      	// 判断是否是直接结算战斗

        public final static int CheckBeingPicking = 100;			// 判断是否正在被拾取(仅宝箱、资源带) 待优化
    }

    public static class MistCammondType {
        public final static int ChangeProperty = 1;				    // 改变属性
        public final static int ChangeBuff = 2;					    // 添加、删除buff
        public final static int CreateTrap = 3;					    // 创建陷阱
        public final static int RemoveTrap = 4;					    // 删除陷阱
        public final static int ChangePos = 5;					    // 改变位置
        public final static int ChangeSpeedRate = 6;			    // 改变移速千分比
        public final static int AddItemSkill = 7;				    // 拾取道具技能
        public final static int RemovePickedItem = 8;			    // 删除被拾取的道具
        public final static int EnterPveBattle = 9;				    // 进入pve战斗(仅玩家)
        public final static int EnterPvpBattle = 10;		        // 进入pvp战斗(仅玩家)
        public final static int ChangeMistFrostLevel = 11;		    // 进入其他等级的迷雾森林
        public final static int GainTreasureBox = 12;			    // 获得宝箱(仅玩家)
        public final static int RemoveGhost = 13;				    // 删除鬼魂指令(仅玩家)
        public final static int Blink = 14;						    // 闪现
        public final static int ChangeBattleBuff = 15;			    // 改变战斗buff(仅玩家，带入战斗)
        public final static int ChangeDefenceRate = 16;			    // 改变防御百分比(仅玩家)
        public final static int RemoveKey = 17;					    // 删除钥匙
        public final static int RemoveBox = 18;					    // 删除宝箱
        public final static int RemoveBag = 19;					    // 删除资源带
        public final static int BroadcastTips = 20;				    // 广播消息
        public final static int UseItem = 21;					    // 使用道具
        public final static int ChangeKeyWaitingBossState = 22;	    // 改变钥匙等待Boss状态
        public final static int ChangeStunState = 23;			    // 改变眩晕状态
        public final static int FlickAway = 24;					    // 弹开目标
        public final static int CreateCage = 25;				    // 创建牢笼(画地为牢牢笼)
        public final static int ChangeInvokerId = 26;			    // 改变召唤者id
        public final static int ClearDeBuff = 27;				    // 清除负面buff
        public final static int ChangePlayerHp = 28;			    // 改变玩家当前血量千分比(仅玩家)
        public final static int PlayEffect = 29;				    // 播放特效
        public final static int MazeTransPos = 30;				    // 迷宫传送
        public final static int ApplyExitMistRoom = 31;			    // 申请退出迷雾森林
        public final static int RecordOrClearOptionalBoxId = 32;    // 记录或移除可选宝箱标记
        public final static int ChangeJewelryCount = 33;		    // 改变宝珠数量
        public final static int ClearHiddenEvilState = 34;		    // 清除隐藏魔物状态
        public final static int BroadCastBossEffect = 35;		    // 广播boss全屏动画
        public final static int RemoveObject = 36;				    // 通用移除对象(目前用于移除活动boss)
        public final static int FlagBeTouchSnowBallId = 37;		    // 标记是否被雪球击中
        public final static int CreateVolcanoStone = 38;		    // 创建火山岩石
        public final static int SummonHiddenEvil = 39;			    // 召唤隐藏魔物
        public final static int ChangeLavaBadgeCount = 40;		    // 增加熔岩徽章数量
        public final static int ChangeShowDataState = 41;		    // 改变探宝状态
        public final static int FighterWaitingForReborn = 42;		// 玩家待复活
        public final static int FighterRebornForDefeated = 43;		// 玩家复活
        public final static int ChangePlayerStamina = 44;		    // 改变玩家体力
        public final static int GainCommonRewardObjReward = 45;		// 通用奖励对象获得奖励
        public final static int ChangeRecoverHpIntervalRate = 46;   // 改变玩家回血间隔比例
        public final static int ChangeAdditionBuffRate = 47;        // 附加buff效果比例

        public final static int ChangeBePickingState = 100;		    // 改变目标正在被拾取状态(仅宝箱、资源带) 待优化
    }

    public static class MistSkillTiming {
        public final static int CastItemSkill = 1;				// 使用道具时
        public final static int AddBuff = 2;					// 添加buff时
        public final static int RemoveBuff = 3;				    // 移除buff时
        public final static int InterruptBuff = 4;				// 打断buff时
        public final static int TouchPlayer = 5;				// 触碰其他玩家时
        public final static int TouchItem = 6;					// 触碰道具时
        public final static int TouchKey = 7;					// 触碰钥匙时
        public final static int TouchTrap = 8;					// 触碰陷阱时
        public final static int TouchTreasureBox = 9;			// 触碰宝箱时
        public final static int TouchTreasureBag = 10;			// 触碰资源带时
        public final static int TouchBuilding = 11;			    // 触碰出入口时
        public final static int WinBossBattle = 12;			    // BOSS战胜利时
        public final static int LossBossBattle = 13;			// BOSS战失败时
        public final static int WinPvpBattle = 14;				// pvp战胜利时
        public final static int LossPvpBattle = 15;			    // pvp战失败时
        public final static int JoinRoom = 16;					// 加入迷雾森林时
        public final static int ExitRoom = 17;					// 退出迷雾森林时
        public final static int TouchMonster = 18;				// 触碰野怪时
        public final static int WinMonsterBattle = 19;			// 野怪战斗胜利时
        public final static int LossMonsterBattle = 20;		    // 野怪战斗失败时
        public final static int BackToSafeRegion = 21;			// 点击传回安全区时
        public final static int TouchTransGrid = 22;			// 进入传送格子时
        public final static int TouchChaoticGrid = 23;			// 进入混沌格子时
        public final static int TouchTreatGrid = 24;			// 进入治疗格子时
        public final static int TouchBlinkGrid = 25;			// 进入闪现格子时
        public final static int TouchGhost = 26;				// 触碰鬼魂时
        public final static int ApplyExitRoom = 27;			    // 申请退出迷雾森林时
        public final static int TouchOptionalBox = 28;	    	// 触碰可选宝箱时
        public final static int SummonEvilBattle = 29;	   		// 召唤隐藏宝珠时
        public final static int WinSummonEvilBattle = 30;		// 召唤宝珠战斗胜利时
        public final static int LossSummonEvilBattle = 31;		// 召唤宝珠战斗失败时
        public final static int ArrestedByGuardMonster = 32;	// 被守卫宝箱怪逮捕时
        public final static int TouchPoisonousMushroom = 33;	// 触碰毒蘑菇时
        public final static int TouchDriftSand = 34;			// 触碰流沙时
        public final static int EnterFireCluster = 35;			// 进入火堆时
        public final static int LeaveFireCluster = 36;			// 离开火堆时
        public final static int TouchSnowBall = 37;			    // 触碰雪球时
        public final static int TouchCactus = 38;				// 触碰仙人掌时
        public final static int ClickOasis = 39;				// 点击绿洲时
        public final static int TouchWolf = 40;				    // 触碰狼时
        public final static int TouchVolcanoStone = 41;		    // 触碰火山岩时
        public final static int TouchCave = 42;				    // 触碰洞穴时
        public final static int TouchLavaBadge = 43;			// 触碰熔岩徽章时
        public final static int TouchLavaLord = 44;			    // 触碰熔岩领主时
        public final static int ScheduleOpen = 45;	    		// 事件开始时
        public final static int UseVipSkill = 46;	    		// 使用特权技能时
        public final static int TouchCommonRewardObj = 47;	    // 触碰通用奖励对象时
    }

    public static class MistBuffInterruptType {
        public final static int Move = 1;						// 移动打断
        public final static int CastItemSkill = 2;			    // 使用道具打断
        public final static int EnterPveBattle = 3;			    // 进入pve战打断
        public final static int BeenPlayerTouched = 4;		    // 被其他玩家挑战打断
        public final static int ExitMistRoom = 5;				// 退出迷雾森林打断
        public final static int BeUnderControl = 6;			    // 被控制打断
        public final static int ResponseTeamInvoke = 7;		    // 回复召唤令打断(仅打断召唤队友buff)
        public final static int BackToSafeRegion = 8;			// 点击传回安全区打断
        public final static int ChangeMoveEffect = 9;			// 切换移动特效打断
    }

    // 附加buff加成类型
    public static class MistAdditionBuffType {
        public final static int attackRate = 1;					// 攻击
        public final static int defendRate = 2;					// 防御
        public final static int weekRate = 3;					// 虚弱
    }

    public static int getDistanceSqr(ProtoVector sourcePos, ProtoVector targetPos) {
        return getDistanceSqr(sourcePos.getX(), sourcePos.getY(), targetPos.getX(), targetPos.getY());
    }

    public static int getDistanceSqr(int sourceX, int sourceY, int targetX, int targetY) {
        return (sourceX - targetX) * (sourceX - targetX) + (sourceY - targetY) * (sourceY - targetY);
    }

    public static boolean checkInDistanceSqr(int checkDisSqr, ProtoVector sourcePos, ProtoVector targetPos) {
        int dis = getDistanceSqr(sourcePos, targetPos);
        return dis <= checkDisSqr;
    }

    public static boolean checkInDistance(int checkDis, ProtoVector.Builder sourcePos, ProtoVector.Builder targetPos) {
        int dis = getDistanceSqr(sourcePos.getX(), sourcePos.getY(), targetPos.getX(), targetPos.getY());
        return dis <= checkDis * checkDis;
    }

    public static boolean checkInDistance(int checkDis, ProtoVector sourcePos, ProtoVector targetPos) {
        int dis = getDistanceSqr(sourcePos, targetPos);
        return dis <= checkDis * checkDis;
    }

    public static boolean checkInDistance(int checkDis, int sourceX, int sourceY, int targetX, int targetY) {
        int dis = getDistanceSqr(sourceX, sourceY, targetX, targetY);
        return dis <= checkDis * checkDis;
    }

    public static boolean checkInRoughDistance(int checkDis, ProtoVector sourcePos, ProtoVector targetPos) {
        return checkInRoughDistance(checkDis, sourcePos.getX(), sourcePos.getY(), targetPos.getX(), targetPos.getY());
    }

    public static boolean checkInRoughDistance(int checkDis, int sourceX, int sourceY, int targetX, int targetY) {
        if (checkDis <= 0) {
            return false;
        }
        int xDis = Math.abs(sourceX - targetX);
        if (xDis > checkDis) {
            return false;
        }
        int yDis = Math.abs(sourceY - targetY);
        return yDis <= checkDis;
    }

    public static boolean checkSamePos(ProtoVector.Builder sourcePos, ProtoVector.Builder targetPos) {
        return checkSamePos(sourcePos.getX(), sourcePos.getY(), targetPos.getX(), targetPos.getY());
    }

    public static boolean checkSamePos(int sourceX, int sourceY, int targetX, int targetY) {
        return sourceX == targetX && sourceY == targetY;
    }

    public static ProtoVector.Builder calcStanderCoordVector(int sourceX, int sourceY, int targetX, int targetY) {
        // (根号2)分之1 约为0.7
        ProtoVector.Builder builder = ProtoVector.newBuilder();
        if (targetX - sourceX > 0) {
            if (targetY - sourceY > 0) {
                builder.setX(7);
                builder.setY(7);
            } else if (targetY - sourceY < 0) {
                builder.setX(7);
                builder.setY(-7);
            } else {
                builder.setX(10);
                builder.setY(0);
            }
        } else if (targetX - sourceX < 0) {
            if (targetY - sourceY > 0) {
                builder.setX(-7);
                builder.setY(7);
            } else if (targetY - sourceY < 0) {
                builder.setX(-7);
                builder.setY(-7);
            } else {
                builder.setX(-10);
                builder.setY(0);
            }
        } else {
            if (targetY - sourceY > 0) {
                builder.setX(0);
                builder.setY(10);
            } else if (targetY - sourceY < 0) {
                builder.setX(0);
                builder.setY(-10);
            } else {
                builder.setX(0);
                builder.setY(0);
            }
        }
        return builder;
    }

    public static boolean isOnlyServerUseProp(int propIdx) {
        return propIdx == MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_BornPosId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_MultiRewardRate_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_ExploitingResource_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_GrassGroup_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_SilentState_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_PreDeadBuffId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_BossRebornPosCfgId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_CreateBuffId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_BossEscapeBuffId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_NotRemoveWhenDead_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_IsBossActivityBox_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_Monster_WarnDis_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_Monster_PursueDis_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_CactusBeTouchTimes_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_OasisRecoverInterval_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_TransPosCfgId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_PatrolPosCfgId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_WolfWarningTime_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_WolfFuryTime_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_WolfAttackDis_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_WolfPursueSpeed_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_OasisExtBuffId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_SealBoxCfgId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_IsLavaImmuneState_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_MagicBoxConfig_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_WolfAttackTimes_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_MagicCyclePlayConfig_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_GhostType_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_GuardMonsterBuffId_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_VolcanoStoneLandTime_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_NewbieTaskFlag_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_ShowInVipMiniMap_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_UsingShowObjState_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_PlayerCalcParam1_VALUE
                || propIdx == MistUnitPropTypeEnum.MUPT_PlayerCalcParam2_VALUE;
    }

    public static boolean objCanMove(int objType) {
        return objType == MistUnitTypeEnum.MUT_Player_VALUE
                || objType == MistUnitTypeEnum.MUT_Monster_VALUE;
    }

    public static long protoPosToLongPos(ProtoVector pos) {
        if (pos == null) {
            return 0;
        }
        return ((long) pos.getX() * 1000) << 32 | (pos.getY() * 1000);
    }

    public static void mergePlayerMap(final Map<Integer, Set<String>> playerMap1, Map<Integer, Set<String>> playerMap2) {
        if (playerMap1 == null) {
            return;
        }
        if (playerMap2 == null) {
            return;
        }
        MistPlayer player;
        for (Entry<Integer, Set<String>> entry : playerMap2.entrySet()) {
            Set<String> playerSet = playerMap1.get(entry.getKey());
            for (String playerIdx : entry.getValue()) {
                player = MistPlayerCache.getInstance().queryObject(playerIdx);
                if (player == null) {
                    continue;
                }
                if (playerSet == null) {
                    playerSet = new HashSet<>();
                    playerMap1.put(entry.getKey(), playerSet);
                }
                if (!playerSet.contains(playerIdx)) {
                    playerSet.add(playerIdx);
                }
            }
        }
    }

    public static boolean isPvpState(int[][] pvpTimeList, PairValue.Builder pvpTimePair) {
        pvpTimePair.clear();
        if (pvpTimeList == null || pvpTimeList.length <= 0) {
            return false;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        long todayTime = TimeUtil.getTodayStamp(curTime);
        PairValue.Builder minPvpTime = PairValue.newBuilder();
        for (int i = 0; i < pvpTimeList.length; i++) {
            if (pvpTimeList[i].length < 2) {
                continue;
            }
            long startTime = pvpTimeList[i][0] * TimeUtil.MS_IN_A_MIN;
            long endTime = pvpTimeList[i][1] * TimeUtil.MS_IN_A_MIN;

            if (minPvpTime.getLeft() <= 0 || minPvpTime.getLeft() > startTime) {
                minPvpTime.setLeft(startTime);
                minPvpTime.setRight(endTime);
            }
            if (curTime >= todayTime + endTime) {
                continue;
            }
            long startTimeStamp = todayTime + pvpTimeList[i][0] * TimeUtil.MS_IN_A_MIN;
            long endTimeStamp = todayTime + pvpTimeList[i][1] * TimeUtil.MS_IN_A_MIN;
            if (pvpTimePair.getRight() <= 0 || pvpTimePair.getRight() > endTimeStamp) {
                pvpTimePair.setLeft(startTimeStamp);
                pvpTimePair.setRight(endTimeStamp);
                if (pvpTimePair.getLeft() >= startTimeStamp) {
                    return true;
                }
            }
        }
        if (pvpTimePair.getLeft() <= 0) {
            pvpTimePair.setLeft(minPvpTime.getLeft() + todayTime + TimeUtil.MS_IN_A_DAY);
            pvpTimePair.setRight(minPvpTime.getRight() + todayTime + TimeUtil.MS_IN_A_DAY);
        }
        return false;
    }

    public static Map<Integer, Integer> buildMonsterBattleReward(int monsterFightCfgId, int mistRule, int playerLevel) {
        MistMonsterFightConfigObject monsterFightCfg = MistMonsterFightConfig.getById(monsterFightCfgId);
        if (monsterFightCfg == null || monsterFightCfg.getBattlereward() == null || monsterFightCfg.getBattlereward().length <= 0) {
            return null;
        }
        PlayerLevelConfigObject plyLvConfig = PlayerLevelConfig.getByLevel(playerLevel);
        if (plyLvConfig == null) {
            return null;
        }
        MistLootPackCarryConfigObject mistPackCarryCfg;

        Random random = new Random();
        Map<Integer, Integer> rewardMap = new HashMap<>();
        for (int i = 0; i < monsterFightCfg.getBattlereward().length; i++) {
            if (monsterFightCfg.getBattlereward()[i] == null || monsterFightCfg.getBattlereward()[i].length < 3) {
                continue;
            }
            mistPackCarryCfg = MistLootPackCarryConfig.getById(monsterFightCfg.getBattlereward()[i][1]);
            if (mistPackCarryCfg == null) {
                continue;
            }
            if (random.nextInt(1000) >= monsterFightCfg.getBattlereward()[i][0]) {
                continue;
            }
            if (mistPackCarryCfg.getLimitByRule(mistRule) < 0) {
                rewardMap.merge(mistPackCarryCfg.getId(), plyLvConfig.getMistbattlerewarcount(), (oldVal, newVal) -> oldVal + newVal);
            } else {
                rewardMap.merge(mistPackCarryCfg.getId(), monsterFightCfg.getBattlereward()[i][2], (oldVal, newVal) -> oldVal + newVal);
            }
        }
        return rewardMap;
    }

    public static long getMonsterBattleJewelryCount(int monsterFightCfgId) {
        MistMonsterFightConfigObject monsterFightCfg = MistMonsterFightConfig.getById(monsterFightCfgId);
        if (monsterFightCfg == null || monsterFightCfg.getRewardjewelrycount() == null || monsterFightCfg.getRewardjewelrycount().length <= 0) {
            return 0;
        }
        int sum = 0;
        int rand = RandomUtils.nextInt(1000);
        for (int i = 0; i < monsterFightCfg.getRewardjewelrycount().length; i++) {
            if (monsterFightCfg.getRewardjewelrycount()[i] == null || monsterFightCfg.getRewardjewelrycount()[i].length < 2) {
                continue;
            }
            sum += monsterFightCfg.getRewardjewelrycount()[i][0];
            if (rand < sum) {
                return monsterFightCfg.getRewardjewelrycount()[i][1];
            }
        }
        return 0;
    }

    public static Map<Integer,Integer> buildJewelryReward(int cfgId) {
        MistJewelryConfigObject cfg = MistJewelryConfig.getById(cfgId);
        if (cfg == null || cfg.getReward()==null) {
            return null;
        }
        Map<Integer, Integer> reward = null;
        for (int i = 0; i < cfg.getReward().length; i++) {
            if (cfg.getReward()[i] == null || cfg.getReward()[i].length < 2) {
                continue;
            }
            if (reward == null) {
                reward = new HashMap<>();
            }
            reward.put(cfg.getReward()[i][0], cfg.getReward()[i][1]);
        }
        return reward;
    }

    public static long getFightLavaBadgeCount(int monsterFightCfgId) {
        MistMonsterFightConfigObject monsterFightCfg = MistMonsterFightConfig.getById(monsterFightCfgId);
        if (monsterFightCfg == null || monsterFightCfg.getRewardlavabadgecount() == null || monsterFightCfg.getRewardlavabadgecount().length <= 0) {
            return 0;
        }
        int sum = 0;
        int rand = RandomUtils.nextInt(1000);
        for (int i = 0; i < monsterFightCfg.getRewardlavabadgecount().length; i++) {
            if (monsterFightCfg.getRewardlavabadgecount()[i] == null || monsterFightCfg.getRewardlavabadgecount()[i].length < 2) {
                continue;
            }
            sum += monsterFightCfg.getRewardlavabadgecount()[i][0];
            if (rand < sum) {
                return monsterFightCfg.getRewardlavabadgecount()[i][1];
            }
        }
        return 0;
    }

    public static Map<Integer, Integer> buildMonsterBattleTeamReward(int monsterFightCfgId) {
        Map<Integer, Integer> teamRewardMap = new HashMap<>();
        MistMonsterFightConfigObject monsterFightCfg = MistMonsterFightConfig.getById(monsterFightCfgId);
        if (monsterFightCfg == null || monsterFightCfg.getBattleteamreward() == null || monsterFightCfg.getBattleteamreward().length <= 0) {
            return teamRewardMap;
        }
        if (monsterFightCfg.getBattleteamreward() != null && monsterFightCfg.getBattleteamreward().length > 0) {
            for (int i = 0; i < monsterFightCfg.getBattleteamreward().length; i++) {
                if (monsterFightCfg.getBattleteamreward()[i] == null || monsterFightCfg.getBattleteamreward()[i].length < 2) {
                    continue;
                }
                teamRewardMap.put(monsterFightCfg.getBattleteamreward()[i][0], monsterFightCfg.getBattleteamreward()[i][1]);
            }
        }

        return teamRewardMap;
    }

    public static Map<Integer, Integer> buildGostConfigReward(int cfgId) {
        MistGhostConfigObject cfg = MistGhostConfig.getById(cfgId);
        if (cfg == null || cfg.getGhosttouchreward() == null || cfg.getGhosttouchreward().length <= 0) {
            return null;
        }
        Map<Integer, Integer> rewardMap = new HashMap<>();
        for (int i = 0; i < cfg.getGhosttouchreward().length; i++) {
            rewardMap.put(cfg.getGhosttouchreward()[i], 1);
        }
        return rewardMap;
    }

    public static int getGainMistBagExtraRateByVipLv(int vipLv) {
        VIPConfigObject config = VIPConfig.getById(vipLv);
        return config != null ? config.getGainmistbagextrarate() : 0;
    }

    public static int getGaineFrienPointDailyLimit(int vipLv) {
        VIPConfigObject config = VIPConfig.getById(vipLv);
        return config != null ? config.getFrienditemgainlimit() : 0;
    }

    public static MistTimeLimitActivityObject getNextMistOpenActivityCfg() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        MistTimeLimitActivityObject tmpCfg = null;
        for (MistTimeLimitActivityObject cfg : MistTimeLimitActivity._ix_id.values()) {
            if (cfg.getEndtime() <= curTime) {
                continue;
            }
            if (null == tmpCfg || tmpCfg.getStarttime() > cfg.getStarttime()) {
                tmpCfg = cfg;
            }
        }
        return tmpCfg;
    }

    public static int getMistActivityBossCfgId(MistTimeLimitActivityObject activityCfg, int mistLevel) {
        if (null == activityCfg) {
            return 0;
        }

        MistCommonConfigObject cmmCfg = MistCommonConfig.getByMistlevel(mistLevel);
        if (null == cmmCfg && null == cmmCfg.getActivtiybosscfgid()) {
            return 0;
        }
        for (int i = 0; i < cmmCfg.getActivtiybosscfgid().length; i++) {
            if (null == cmmCfg.getActivtiybosscfgid()[i] || cmmCfg.getActivtiybosscfgid()[i].length < 2) {
                continue;
            }
            if (activityCfg.getActivitytype() == cmmCfg.getActivtiybosscfgid()[i][0]) {
                return cmmCfg.getActivtiybosscfgid()[i][1];
            }
        }
        return 0;
    }

    public static Map<Integer, Integer> buildCommonRewardMap(int[][] rewardList, int mistRule, int playerLevel) {
        if (rewardList == null || rewardList.length == 0) {
            return null;
        }
        PlayerLevelConfigObject plyLvConfig = PlayerLevelConfig.getByLevel(playerLevel);
        if (plyLvConfig == null) {
            return null;
        }
        MistLootPackCarryConfigObject mistPackCarryCfg;

        Map<Integer, Integer> rewardMap = new HashMap<>();
        for (int i = 0; i < rewardList.length; i++) {
            if (rewardList[i] == null || rewardList[i].length < 2) {
                continue;
            }
            mistPackCarryCfg = MistLootPackCarryConfig.getById(rewardList[i][0]);
            if (mistPackCarryCfg == null) {
                continue;
            }
            if (mistPackCarryCfg.getLimitByRule(mistRule) < 0) {
                rewardMap.merge(mistPackCarryCfg.getId(), plyLvConfig.getMistbattlerewarcount(), (oldVal, newVal) -> oldVal + newVal);
            } else {
                rewardMap.merge(mistPackCarryCfg.getId(), rewardList[i][1], (oldVal, newVal) -> oldVal + newVal);
            }
        }
        return rewardMap;
    }

    public static int directSettlePveBattle(MistFighter fighter, int monsterFightCfgId) {
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return -1;
        }
        MistMonsterFightConfigObject cfg = MistMonsterFightConfig.getById(monsterFightCfgId);
        if (cfg == null) {
            return -1;
        }
        MistCommonConfigObject cmCfg = MistCommonConfig.getByMistlevel(fighter.getRoom().getLevel());
        if (cmCfg == null || cmCfg.getDirectsettlefightparam2() == 0 || cfg.getDirectsettlefightpower() == 0) {
            return -1;
        }
        long pveFightDecHpRate = cfg.getDirectsettledecreasehp();
        long pveFightPower = cfg.getDirectsettlefightpower();

        long plyMainLineLevel = player.getMainLineUnlockLevel();
        long plyFightPower = player.getFightPower();

        long plyCalcParam1 = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerCalcParam1_VALUE);
        long plyCalcParam2 = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerCalcParam2_VALUE);

        long curHp = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
        long tmpVal1 = 1000 - ((plyMainLineLevel - cmCfg.getDirectsettlefightparam1()) * 1000 / cmCfg.getDirectsettlefightparam2() / 1000);

        long tmpVal2 = plyFightPower < pveFightPower ? 0 : (plyFightPower - pveFightPower) * 1000 / pveFightPower / 1000;

        long checkVal1 = pveFightDecHpRate * (300 + 350 * Math.max(0, tmpVal1) / 1000 + 350 * (1000 - Math.min(tmpVal2, 1000)) / 1000) / 1000;
        double calcVal = (double) checkVal1;
        if (plyCalcParam1 > 0) {
            calcVal *= Math.pow(0.8, plyCalcParam1);
        }
        if (plyCalcParam2 > 0) {
            calcVal *= Math.pow(1.25, plyCalcParam2);
        }
        checkVal1 = (long) calcVal;
        return curHp > checkVal1 ? (int) checkVal1 : -1;
    }

    public static int calcPvpPlayerFightPower(MistFighter fighter1, MistFighter fighter2) {
        if (fighter1 == null && fighter2 == null) {
            return -1; // 平局
        }

        if (fighter1 == null) {
            return 2; // 玩家2胜利
        }
        if (fighter2 == null) {
            return 1; // 玩家1胜利
        }
        long fighter1Power = fighter1.getPvpFightPower();
        long fighter2Power = fighter2.getPvpFightPower();
        if (fighter1Power == fighter2Power) {
            return -1; // 平局
        }
        MistPlayer player;
        int winnerCamp;
        long changeRate = 1; // 至少为千分之1
        if (fighter1Power > fighter2Power) {
            winnerCamp = 1; // 玩家1胜利
            player = fighter1.getOwnerPlayerInSameRoom();
            if (fighter1Power != 0) {
                changeRate = fighter2Power * 1000 / fighter1Power * fighter1.getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
            }
        } else {
            winnerCamp = 2; // 玩家2胜利
            player = fighter2.getOwnerPlayerInSameRoom();
            if (fighter2Power != 0) {
                changeRate = fighter1Power * 1000 / fighter2Power * fighter2.getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
            }
        }
        if (player != null) {
            Event event = Event.valueOf(EventType.ET_ChangePlayerHpRate, fighter1.getRoom(), player);
            event.pushParam(changeRate);
            EventManager.getInstance().dispatchEvent(event);
        }
        return winnerCamp;
    }

    public static Long buildComboRebornPos(int cfgId) {
        MistComboBornPosConfigObject posCfg = MistComboBornPosConfig.getById(cfgId);
        Long posData = null;
        if (posCfg != null && posCfg.getPlayerrebornposlist() != null && posCfg.getPlayerrebornposlist().length > 0) {
            int randIndex = RandomUtils.nextInt(posCfg.getPlayerrebornposlist().length);
            if (posCfg.getPlayerrebornposlist()[randIndex].length > 1) {
                posData = GameUtil.mergeIntToLong(posCfg.getPlayerrebornposlist()[randIndex][0], posCfg.getPlayerrebornposlist()[randIndex][1]);
            }
        }
        return posData;
    }

    public static MistLootPackCarryConfigObject getConfigIdByReward(int rewardType, int rewardId) {
        for (MistLootPackCarryConfigObject config : MistLootPackCarryConfig._ix_id.values()) {
            if (config.getRewardtype() == rewardType && config.getRewardid() == rewardId) {
                return config;
            }
        }
        return null;
    }

    public static int calcCrossVipDropExtBox(int crossVipLv, int boxQuality) {
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(crossVipLv);
        if (cfg == null) {
            return 0;
        }
        int[][] boxRateCfg = cfg.getMistextraboxrate();
        if (boxRateCfg == null || boxRateCfg.length <= 0) {
            return 0;
        }
        for (int i = 0; i < boxRateCfg.length; i++) {
            if (boxRateCfg[i] == null || boxRateCfg[i].length < 3) {
                continue;
            }
            if (boxRateCfg[i][0] == boxQuality) {
                return boxRateCfg[i][1] > RandomUtils.nextInt(1000) ? boxRateCfg[i][2] : 0;
            }
        }
        return 0;
    }
}
