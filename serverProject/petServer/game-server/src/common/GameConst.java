package common;

import common.load.ServerConfig;
import lombok.Getter;
import model.pet.entity.GemAdditionDto;
import util.TimeUtil;

public class GameConst {
	public static final int ROLE_NAME_MAX_LENGTH = 14;
	public static final int CONFIG_ID = 1;
	public static final int EACH_PET_MAX_EQUIP_RUNE_COUNT = 4;
	/**
	 * 无限制
	 */
	public static final int UN_LIMIT = -1;

	public static final long UpdateServerTime = 3000;
	public static final long BattleTimeout = 300000;
	public static final long NewBeeBattleTimeout = 1800000;

	public static int[][] empty2IntArray = new int[0][0];

	/**
	 * 通用铭文类型
	 */
	public static final int generalInscriptionType = -1;

	/**
	 * 魔灵种族
	 */
	public static final int NaturePetClass = 1;
	public static final int WildPetClass = 2;
	public static final int AbyssPetClass = 3;
	public static final int HellPetClass = 4;

	/**
	 * 核心魔灵
	 */
	public static final int CORE_PET = 4;

	/**
	 * 擂台赛货币积分道具id
	 */
	public static final  int CrossArenaScoreItemId = 1331;

	/**
	 * 跑马灯两次播放之间的最小间隔
	 */
	public static final long MARQUEE_MIN_INTERVAL_S = 60;
	public static final long MARQUEE_MIN_INTERVAL_MS = MARQUEE_MIN_INTERVAL_S * TimeUtil.MS_IN_A_S;

	/**
	 * // 抓鬼活动入场券道具id
	 */
	public static final int GhostBusterTicketItemId = 1199;
	/**
	 * // 迷雾森林最大房间数
	 */
	public static final int MistMaxRoomCount = 12;

	/**
	 * 擂台日连胜任务
	 */
	public static final int LtDailyWinTask = 1;

	/**
	 * 擂台周连胜任务
	 */
	public static final int LtWeeklyWinTask = 2;

	/**
	 * // 精炼石迷雾森林配置id
	 */
	public static final int MistRefinedStoneCfgId = 10000;

	/**
	 * // 友情卡迷雾森林配置id
	 */
	public static final int FriendPointMistCfgId = 2;

	/**
	 * // 矿区开采劵道具id
	 */
	public static final int MineExploitScrollItemId = 1032;
	/**
	 * // 矿区助阵劵道具id
	 */
	public static final int MineFriendHelpItemId = 1033;

	/**
	 * 流浪印记id
	 */
	public static final int ITEM_ID_STRAY_MARKS = 1003;
	/**
	 * 高级召唤卷
	 */
	public static final int ITEM_ID_HIGH_DRAW_CARD = 1005;
	/**
	 * 远古召唤卷
	 */
	public static final int ITEM_ID_HIGH_ANCIENT = 1006;
	/**
	 * 生命石
	 */
	public static final int ITEM_ID_LIFE_STONE = 1014;

	public static final int Artifact_Enhance_Point_Num = 6;

	/**
	 * 觉醒石
	 */
	public static final int ITEM_ID_AWAKE_STONE = 1019;

	/**
	 * 远古精华
	 */
	public static final int ITEM_ID_Ancient_Essence = 1007;

	/**
	 * 单张月卡使用天数
	 */
	public static final int ONE_MONTH_CARD_USE_DAY = 30;

	/**
	 * 宠物血量最大比例(千分比)
	 */
	public static final int PetMaxHpRate = 1000;

	/**
	 * 限时礼包默认当前达到次数
	 */
	public static final int TimeLimitGiftDefaultTarget = 0;

	/**
	 * 功勋id
	 */
	public static final int ITEM_ID_FEATS = 1115;


	/**
	 * 迷雾深林钻石宝箱id
	 */
	public static final int ITEM_ID_Diamond_Box = 1166;

	/**
	 * 商品默认折扣(千分比)
	 */
	public static final int SELL_DEFAULT_DISCOUNT = 1000;

	/**
	 * 迷宫碎片id
	 */
	public static final int ITEM_ID_MazeFragment = 1200;

	/**
	 * 玩家技能初始等级
	 */
	public static final int PlayerSkillDefaultStar = 0;

	/**
	 * 举报查询条数限制
	 */
	// TODO 原50
	public static final int REPORT_QUERY_SIZE = 50;

	/**
	 * 未被举报的评论查询页大小
	 */
	// TODO 原50
	public static final int UNREPORTED_COMMENT_QUERY_SIZE = 50;

	/**
	 * 自动处理累积的举报次数
	 */
	// TODO 原10
	public static final int REPORT_AUTO_DEAL_NEED_COUNT = 10;

	/**
	 * 宠物属性放大倍率
	 */
	public static final int petPropertyMagnification = 10000;

	/**
	 * 宠物加成放大倍率
	 */
	public static final double petAdditionMagnification = 1000.0;

	/**
	 * 配置中通用的的放大倍数
	 */
	public static final int commonMagnification = 1000;

	/**
	 * 符文经验转化率放大倍数
	 */
	public static final int runeExpAddProportionMagnification = 1000;

	/**
	 * 符文石
	 */
	public static final int RuneStoneItemId = 1127;
	/**
	 * 风暴之塔扫荡道具
	 */
	public static final int ITEM_ID_BOSSTOWER_SWEEP = 1212;
	// 迷雾森林道具最大个数
	public static final int MistItemSkillMaxCount = 4;

	// 活动boss奖励最大随机次数
	public static final int ActivityBossMaxRandomRewardTime = 12;

	public static final GemAdditionDto emptyGemAdditionDto = new GemAdditionDto();

	public static final int offerRewardTick = 3000;
	
	/**活跃功勋类型*/
	public static final int FEAT_TYPE_HUOYUE = 0;
	/**无尽功勋类型*/
	public static final int FEAT_TYPE_WUJIN = 1;
	/**虚空功勋类型*/
	public static final int FEAT_TYPE_XUKONG = 2;
	
	/**
	 * 宠物委托升级需要任务总数的key
	 */
	public static final int totalPetMissionNumKey = 0;

	public static class ServerState {
		public static final int ServerClosed = 0;
		public static final int ServerRunning = 1;
		public static final int ServerClosing = 2;
	}

	public static class PetUpType {
		public static final int Level = 1;
		public static final int Rarity = 2;
		public static final int Evolve = 3;
	}

	public static class ResourceCycleClaimType {
		public static final int base = 0;
		public static final int advanced = 1;
	}

	/**
	 * 放生类型
	 */
	public static class Discharge {
		public static final int free = 0; // 放生
		public static final int reset = 1; // 重订
		public static final int rarityReset = 2; // 品质重订
	}

	/**
	 * 時間最大值
	 */
	public static final long MAX_TIME = 9999999999999L;
	/**
	 * 排行榜时间做副分数时转化为小数的除数
	 */
	public static final  double RankTimeDivider = Math.pow(10,13);


	/**
	 * 排行榜枚举
	 */
	public static class RankingName {
		public static final String RN_MainLinePassed = "MainLinePassed"; // 主线闯关通关排行榜
		public static final String RN_ForInv_bossDamage = "ForInv_BossDamage"; // 外敌入侵boss伤害排行榜
		public static final String RN_New_ForInv_Score = "NewForInvScore"; // 新版外敌入侵积分
		public static final String RN_EndlessSpire = "EndlessSpire"; // 无尽尖塔排行榜
		public static final String RN_MistTransSvrRank = "MistTransSvrRank"; // 迷雾森林跨服排行榜
		public static final String RN_MistLocalSvrRank = "MistLocalSvrRank"; // 迷雾森林本服排行榜
		public static final String RN_MistKillMonster = "MistKillMonster"; // 迷雾森林本服排行榜
		public static final String RN_MistGainDiamondBox = "MistGainDiamondBox"; // 迷雾森林本服排行榜
		public static final String RN_Team1Ability = "Team1Ability"; // 主线编队1战力排行

		public static final String RN_NaturePet = "NaturePet"; // 自然魔灵排行
		public static final String RN_WildPet = "WildPet"; // 蛮荒魔灵排行
		public static final String RN_AbyssPet = "AbyssPet"; // 深渊魔灵排行
		public static final String RN_HellPet = "HellPe"; // 地狱魔灵排行

		public static final String RN_PET_ABILITY = "PetAbility";
		public static final String RN_PLAYER_LV = "PlayerLv";
		public static final String RN_ARENA_DAN_SCORE = "ArenaDanScore"; // 竞技场本服全段位排行
		public static final String RN_ARENA_DAN_SCORE_CROSS = "ArenaDanScoreCross"; // 竞技场全服全段位排行
		public static final String RN_MIST_KILL_PLAYER = "MistKillPlayer";
		public static final String RN_MIST_KILL_BOSS = "MistKillBoss";
		public static final String RN_ARENA_GAIN_SCORE = "ArenaGainScore"; // 竞技场房间内排行
		public static final String RN_ARENA_SCORE_LOCAL_DAN = "ArenaScoreLocalDan"; // 竞技场本服本段位排行
		public static final String RN_MINE_SCORE = "MineScore";
		public static final String RN_ActivityBoss_Damage = "ActivityBossDamage"; // 对活动boss造成的伤害
		public static final String RN_DEMON_DESCENDS_SCORE = "DemonDescendsScore";
		public static final String RN_TheWar_KillMonsterCount = "TheWarKillMonsterCount";
		public static final String RN_GloryRoad = "GloryRoad"; // 荣耀之路
		public static final String RN_RichMan = "RichMan"; // 大富翁

		public static final String RN_MatchArena_Local = "matchArenaLocal"; // 匹配竞技场本服
		public static final String RN_MatchArena_Cross = "matchArenaCross"; // 匹配竞技场跨服

		public static final String RN_Statistics_Ability = "Ability"; // 统计_战力
		public static final String RN_Statistics_Diamond = "Diamond"; // 统计_钻石
		public static final String RN_Statistics_Gold = "Gold"; // 统计_金币
		public static final String RN_Statistics_Coupon = "Coupon"; // 统计_点券
		public static final String RN_Statistics_StrayMarks = "StrayMarks"; // 统计_流浪印记
		public static final String RN_Statistics_HighDrawCard = "HighDrawCard"; // 统计_高级召唤卷
		public static final String RN_Statistics_Ancient = "AncientItem"; // 统计_远古召唤卷

		public static final String RN_CrazyDuel_Attack = "CrazyDuelAttack"; // 疯狂对决攻击方排行榜
		public static final String RN_CrazyDuel_Defend = "CrazyDuelDefend"; // 疯狂对决防守方排行榜

		public static final String RN_Lt_Score = "LeiTaiScore" ;		//擂台积分排行榜
		public static final String RN_Lt_Duel = "LeiTaiCrazyDuel";		//擂台疯狂对决
		public static final String RN_Lt_SerialWin = "LeiTaiSerialWin"; //擂台积分榜
		public static final String RN_FestivalBoss = "FestivalBoss"; //节日Boss
		public static final String RN_MagicThronMaxDamage = "MagicThronMaxDamage"; //魔法王座
		public static final String RN_PetAvoidance = "PetAvoidance"; //魔灵大躲避
		public static final String RN_ConsumeCoupon = "ConsumeCoupon"; //消耗魔晶
		public static final String RN_Recharge = "RechargeCoupon"; //充值魔晶


	}

	public static class EventType {
		public static final int ET_Login = 1;
		public static final int ET_Logout = 2;

		public static final int ET_AddCurrency = 3; // 增加货币 锁对象playerEntity 参数:货币类型（rewardTypeEnum中的货币）， 增加的数量
//        public static final int ET_ReduceCurrency = 4;         //减少货币  锁对象playerEntity  参数:货币类型， 减少的数量

		public static final int ET_RemoveItem = 5; // 参数Map<Integer, integer> itemCfgId,count
		public static final int ET_AddItem = 6; // 参数同上

		public static final int ET_AddAvatar = 9; // 添加头像， 锁对象playerEntity，参数 List<Integer> avatarCfgIdList
		public static final int ET_AddAvatarBorder = 10; // 添加头像框， 锁对象playerEntity，参数 List<Integer> avatarCfgIdList

		public static final int ET_UnlockTeamAndPosition = 11; // 解锁备战小队和位置 锁对象teamEntity， 参数：玩家等级

		public static final int ET_RemoveDisplayPet = 12; // 移除展示的宠物Idx 锁对象 playerEntity， 参数：宠物Idx List<String>

		/**
		 * 添加宠物，传入PetService.getPetByPlayer， 参数1 :Map：宠物合成Id,分为以下特殊两种宠物bookId:
		 *
		 * @see model.pet.entity.PetComposeHelper 程序生成bookId,标示了宠物种类,品质 规则见提示类
		 *      PetBaseProperties 中bookId
		 *      <p>
		 *      参数2获得宠物原因：RewardSourceEnum.Value
		 */
		public static final int ET_AddPet = 13;
		// 添加符文，传入PetRuneService.getRuneByPlayer，参数Map：图鉴id-数量
		public static final int ET_AddPetRune = 15;
		// 添加宠物碎片，传入PetFragmentService.getFragmentByPlayer，参数Map：碎片图鉴id，添加数量
		public static final int ET_AddPetFragment = 17;

		public static final int ET_AddMail = 19; // 锁对象 mailboxEntity 参数: DB_Mail

		// 清空符文状态，传入PetRuneService.getRuneByPlayer，参数List<String>宠物idList（符文被装备的宠物）
		public static final int ET_ResetRuneStatus = 21;
		// 设置宠物委托状态，传入PetService.getPetByPlayer，参数List<String>宠物idList
		public static final int ET_SetPetMissionStatus = 22;
		// 清空宠物委托状态，传入PetService.getPetByPlayer，参数List<String>宠物idList
		public static final int ET_ResetPetMissionStatus = 23;

		public static final int ET_AddVIPExp = 24; // 锁对象playerEntity，参数：添加的数量
		public static final int ET_AddExp = 25; // 锁对象playerEntity，参数：添加的数量

		public static final int ET_MainLineBattleSettle = 26; // 锁对象：mainlineEntity，参数1：nodeId，参数2：winnerCamp
		public static final int ET_UnlockMainLine = 27; // 玩家升级解锁新关卡，锁对象mainLineEntity, 参数1：玩家等级

		public static final int ET_VipLvUp = 30; // Vip升级，锁对象：空BaseObj，参数一：playerIdx， 参数二：升级前等级，参数三，升级后等级

		public static final int ET_RemoveTeamDeadPet = 32; // 移除勇气试炼阵亡的小队

		// 目标系统
		public static final int ET_UpdateTargetProgress = 36; // 更新目标系统进度
		public static final int ET_ClearAllMistTargetProgress = 37; // 清空所有迷雾森林目标进度
		public static final int ET_UpdateBossActivityTime = 38; // 更新参加boss活动次数(增加或者清空)
		public static final int ET_AddPointCopyScore = 39; // 添加积分副本分数

		// ----------迷雾森林Start(51-55)----------
		public static final int ET_MistForestSeasonEnd = 51; // 迷雾森林赛季结束清空玩家积分
		public static final int ET_ClearMistItem = 52; // 每日清除迷雾森林宝箱
		public static final int ET_MistForestServerClosed = 53; // 迷雾森林服务器关闭
		public static final int ET_MistBossActivityBegin = 54; // 迷雾森林boss活动开启
		// ----------迷雾森林End(51-55)----------

		// --------矿区争夺Start(56-70)--------------
//        public static final int ET_MineFightServerClosed = 56;              // 矿区服务器关闭
		public static final int ET_LockPetTeam = 57; // 加锁解锁小队
		public static final int ET_UpdateApplyFriendHelpList = 58; // 更新申请好友助阵信息
		public static final int ET_UpdateBeAppliedFriendHelpList = 59; // 更新被好友申请助阵信息
		public static final int ET_ExpireApplyFriendHelpList = 60; // 处理过期申请好友助阵信息
		public static final int ET_ExpireBeAppliedFriendHelpList = 61; // 处理过期被好友申请助阵信息
		public static final int ET_UpdateHelpingFriendIdx = 62; // 更新玩家助阵的好友id
		public static final int ET_RemoveBeHelpedFriendIdx = 63; // 删除被助阵的好友id

		public static final int ET_UpdateMineGiftData = 64; // 刷新矿区惊喜奖励
		public static final int ET_UpdateMineEffect = 65; // 刷新矿区额外buff效果

		public static final int ET_UpdateOwnedMineInfo = 66; // 缓存所占矿
		public static final int ET_UpdateAttackMineInfo = 67; // 缓存掠夺矿
		public static final int ET_SettleMineObj = 68; // 矿区战斗结算
		public static final int ET_AddMineReward = 69; // 缓存矿区奖励
		public static final int ET_RecordMineBattle = 70; // 记录矿区战斗
		// --------矿区争夺End(56-70)--------------
		public static final int ET_AddPlayerMineExp = 72; // 增加矿区经验

		public static final int ET_BattleServerClosed = 71; // 战斗服关闭

		public static final int ET_UPDATE_ACTIVITY_DROP_COUNT = 75; // 增量更新掉落的道具

		public static final int ET_TimerInvoke = 78; // 定时器时间

		public static final int ET_UPDATE_UNLOCK_MIST_LV = 79; // 更新迷雾深林解锁层级

		public static final int ET_RECORD_ARENA_BATTLE = 80; // 记录竞技场战斗

		public static final int ET_CLEAR_TEAM = 81; // 清空队伍

		public static final int ET_REMOVE_PET_FROM_TEAMS = 82; // 将指定宠物从编队中移除

		public static final int ET_UPDATE_PET_TEAM_STATE = 83; // 更新宠物编队状态

		public static final int ET_ENDLESS_SPIRE_BATTLE_SETTLE = 84; // 无尽尖塔战斗结算

		public static final int ET_FOREIGN_INVASION_BATTLE_SETTLE = 85; // 外敌入侵战斗结算

		public static final int ET_PATROL_BATTLE_SETTLE = 86; // 巡逻队战斗结算

		public static final int ET_RESOURCE_COPY_BATTLE_SETTLE = 88; // 资源副本战斗结算

		public static final int ET_BATTLE_PLAYER_LEAVE = 89; // 玩家离开

		public static final int ET_PLAYER_RECHARGE_ACTIVITY = 90; // 玩家充值

		public static final int ET_AUTO_REFRESH_SHOP = 100; // 商店自动刷新

		public static final int ET_Add_Limit_Purchase_Recharge_Id = 101; // 增加限制购买记录

		public static final int ET_ReCreateMonsterDiff = 102; // 重设怪物难度

		public static final int ET_CLEAR_ALL_PLAYER_ACTIVITY_INFO = 106; // 清除所有玩家指定活动id的活动数据

		public static final int ET_TIME_LIMIT_GIFT = 107; // 限时礼包

		public static final int ET_ADD_REPORT_TIMES = 108; // 增加已经举报的次数

		public static final int ET_BAN = 109; // 封禁

		public static final int ET_SHIELD_COMMENT = 110; // 屏蔽玩家摸一个评论模块的所有评论

		public static final int ET_TheWar_UpdateWarRoomIdx = 111; // 更新玩家战戈房间

		public static final int ET_WarPet_Update = 112; // 更新玩家战戈宠物

		public static final int ET_MIST_CLEAR_ALL_TIME_LIMIT_PROGRESS = 113; // 清空所有玩家迷雾深林限时任务进度

		public static final int ET_UnlockWishWell = 114; // 解锁许愿池

		public static final int ET_AllPetAdditionUpdate = 115; // 全体宠物加成事件

		public static final int ET_AddPetGem = 116; // 增加宠物宝石

		public static final int ET_ResetPetGemStatus = 117; // 清空宠物身上宝石状态

		public static final int ET_UpdatePlayerRecharge = 118; // 更新玩家充值金额

		public static final int ET_UnlockTimeLimitActivity = 119; // 解锁限时任务

		public static final int ET_NewForeignInvasionSendPlayerBuildingInfo = 120; // 发送玩家外敌入侵信息

		public static final int ET_ClearMistTimeLimitMission = 121; // 清空玩家外敌入侵信息

		public static final int ET_ClearAllPlayerTheWarSeasonMissionPro = 122; // 清空玩家远征任务进度

		public static final int ET_UpdatePetMissionLvUpPro = 123; // 更新宠物委托升级进度

		public static final int ET_DailyUpdatePlayerAllFunction = 124; // 每日更新玩家的所有功能
		public static final int ET_WeeklyUpdatePlayerAllFunction = 125; // 每周更新玩家的所有功能

		public static final int ET_UpdateRanking = 126; // 更新排行榜

		public static final int ET_UpdatePatrolMissionSwitch = 127; // 更新虚空秘境任务计时开关

		public static final int ET_AddNewTitle = 128; // 添加新称号

		public static final int ET_ClearClaimRecordOnPlayer = 129; // 清空playerEntity上的领取记录

		public static final int ET_AddGloryRoadQuizRecord = 130; // 添加竞猜记录

		public static final int ET_GloryRoadBattleResult = 131; // 荣耀之路战斗结算

		public static final int ET_UpdateStageRewardTarget = 132; // 更新活动连续奖励进度

		public static final int ET_RefreshMazeData = 133; // 刷新迷宫相关信息
		public static final int ET_SettleMazeActivity = 134; // 结算清空迷宫活动信息
		public static final int ET_CollectMazeItem = 135; // 累计收集迷宫碎片数量

		public static final int ET_AddInscription = 136; // 新增铭文

		public static final int ET_RefreshPetData = 137; // 刷新宠物属性

		public static final int ET_TRAIN_BATTLE_SETTLE = 138; // 训练场战斗结算
		public static final int ET_TRAIN_RANK_SETTLE = 139; // 训练场排行榜结算

		public static final int ET_UnLockFunction = 140; // 功能解锁

		public static final int ET_CoupTeamUpdate = 141; // 编队更新

		public static final int ET_ClearHelpPet = 142; // 清除助阵宠物

		public static final int ET_MAGICTHRON_PVE = 143; // 魔法王座pve

		public static final int ET_TrainItemAdd = 146; // 训练营道具添加

		public static final int ET_CollectArtifactExp = 147; // 收集神器经验

		public static int ET_CollectPet = 148; // 收集新宠物/解锁链接

		public static int ET_MagicThronRecord = 149; // 魔法王座战斗记录

		public static int ET_RemoveInscription = 150; // 移除铭文

		public static int ET_OpenTraining = 151; // 竞技场解锁

		public static int ET_OfferRewardFight = 152; // 悬赏任务挑战

		public static final int ET_CROSSARENAEVENT_BATTLE_SETTLE = 153; // 训练场战斗结算

		public static final int ET_AddCrossArenaGrade = 154; // 增加擂台荣誉积分
		
		public static final int ET_TrainScore = 155; // 训练营每层积分

		public static final int ET_CrossArenaBoss = 156; // 擂台赛boss

		public static final int ET_CompleteStoneRiftMission = 157; // 完成石头峡谷任务

		public static final int ET_UpdateStoneRiftAchievement = 158; // 更新石头峡谷成就

		public static final int ET_UpdateCrossArenaWeeklyTask = 159; // 更新竞技场任务

		public static final int ET_UpdateIncrRankingScore = 160; // 更新增量类排行榜积分

		public static final int ET_AddStoneRiftFactoryExp = 161; // 增加石头峡谷经验
        public static final int ET_UpdateStarCount = 162; // 更新玩家星元数量

		public static final int ET_PetAvoidanceGameTimeOver = 163; // 魔灵大躲避游戏时间结束

		public static final int ET_AddMoveEffect = 164;		 	   // 增加迷雾森林移动特效
	}

	public static class RedisKey {
		public static final String BattleServerInfo = "BattleServerInfo"; // 战斗服信息
		public static final String BattleServerIndexAddr = "BattleServerIndexAddr"; // 战斗服serverIndex对应ip
		public static final String BattleOnlineCount = "BattleOnlineCount"; // 战斗服在线人数

		public static final String CrossServerInfo = "CrossServerInfo"; // 跨服服务器地址信息

		public static final String CrossServerIndexAddr = "CrossServerIndexAddr"; // 跨服服务器serverIndex对应ip

		public static final String MistForestInfo = "MistForestInfo-"; // 迷雾森林相关信息

		public static final String MistMazeSyncData = "MistMazeSyncData"; // 迷宫相关信息
		public static final String MistMazeUpdateLock = "MistMazeUpdateLock"; // 迷宫数据更新锁

		public static final String MistGhostBusterMatchData = "MistGhostBusterMatchData"; // 抓鬼匹配数据
		public static final String MistGhostBusterMatchLock = "MistGhostBusterMatchLock"; // 抓鬼匹配数据更新锁

		public static final String MineFightInfo = "MineFightInfo"; // 矿区争夺相关信息

		public static final String ARENA_SERVER_INFO = "ArenaServerInfo"; // 竞技场服务器相关信息 hash

		public static final String TheWarRoomServerIndex = "TheWarRoomServerIndex"; // 战戈房间跨境服serverIndex信息
		public static final String TheWarServerLoadInfo = "TheWarServerLoadInfo"; // 战戈服务器负载信息(已排序)
		public static final String TheWarAvailableJoinRoomInfo = "TheWarAvailableJoinRoomInfo"; // 战戈当前可进入房间
		public static final String TheWarJoinedPlayerInfo = "TheWarJoinedPlayerInfo-"; // 当前赛季已参加的玩家信息

		public static final String TheWarRoomData = "TheWarRoomData"; // 战戈房间信息
		public static final String TheWarPlayerData = "TheWarPlayerData:"; // 战戈玩家信息
		public static final String TheWarGridData = "TheWarGridData:"; // 战戈格子信息

		public static final String MatchArena = "MatchArena:";
		public static final String MatchArenaRecentBattle = MatchArena + "RecentBattle";
		public static final String MatchArenaPlayerInfo = MatchArena + "PlayerInfo";
		public static final String MatchArenaPlayerScore = MatchArena + "PlayerScore";
		public static final String MatchArenaPlayerMatchInfo = MatchArena + "PlayerMatchInfo";
		public static final String MatchArenaClearSeasonCrossRanking = MatchArena + "ClearSeasonCrossRanking";
		public static final String MatchArenaMatchRobotInit = MatchArena + "MatchRobotInit";
		public static final String MatchArenaMatchRobotInfo = MatchArena + "MatchArenaMatchRobotInfo";

		public static final String MatchArenaLTTime = "MatchArenaLTTime"; // 擂台赛同步数据信息
		public static final String MatchArenaLTSyncData = "MatchArenaLTSyncData"; // 擂台赛同步数据信息
		public static final String MatchArenaLTSyncDataId = "MatchArenaLTSyncDataId"; // 擂台赛同步数据信息
		public static final String MatchArenaLTUpdateLock = "MatchArenaLTUpdateLock"; // 擂台赛同步数据信息更新锁

		public static final String CrossArenaTen = "CrossArena:ATen"; // 擂台赛同步数据信息
		public static final String CrossArenaTenPlayer = "CrossArena:ATenPlayer:"; // 擂台赛同步数据信息
		public static final String CrossArenaTime = "CrossArena:Time"; // 擂台赛同步数据信息
		public static final String CrossArenaTimeLock = "CrossArena:TimeLock"; // 擂台赛同步数据信息更新锁
		public static final String CrossArenaData = "CrossArena:Table"; // 单个擂台数据实际KEY=CrossArenaData+ID
		public static final String CrossArenaTableLock = "CrossArena:TableLock"; // 擂台赛队列信息
		public static final String CrossArenaBSSid = "CrossArena:BSSid:"; // 存储每个道场每个擂台逻辑执行服服务器ID
		public static final String CrossArenaPlayerTable = "CrossArena:PlayerTable"; // 存储玩家所在擂台ID
		public static final String CrossArenaTableNum = "CrossArena:TableNum"; // 擂台赛道场桌子数量
		public static final String CrossArenaQue = "CrossArena:Dui"; // 擂台赛队列信息
		public static final String CrossArenaQueLock = "CrossArena:DuiLock"; // 擂台赛队列信息
		public static final String CrossArenaPlayerInfo = "CrossArena:PlayerInfo"; // 擂台玩家战斗数据
		public static final String CrossArenaPlCache = "CrossArena:PlCache"; // 擂台玩家缓存数据
		public static final String CrossArenaPlOnline = "CrossArena:PlOnline"; // 擂台玩家在线玩家
		public static final String CrossArenaPlCacheOnline = "CrossArena:PlCacheOnline"; // 擂台玩家在线缓存数据
		public static final String CrossArenaPlCacheOnlineLock = "CrossArena:PlCacheOnlineLock"; // 擂台玩家在线缓存数据
		public static final String CrossArenaRBPlayerInfo = "CrossArena:RBPlayerInfo"; // 擂台机器玩家战斗数据
		public static final String CrossArenaProtectCard = "CrossArena:protectCard"; // 保护卡
		public static final String CrossArenaProtectRobotQue = "CrossArena:protectRobot:que:";  // 擂台赛保底机器人队列
		public static final String CrossArenaTableStateEndTime = "CrossArena:tableStateEndTime";           //桌子状态结束时间
		public static final String CrossArenaTableState = "CrossArena:tableSate:";  // 擂台状态

		public static final String TopPlayPlayer = "CrossArenaTopPlay:Player"; // 参赛者信息
		public static final String TopPlayGroup = "CrossArenaTopPlay:Group"; // 分组数据
		public static final String TopPlayTime = "CrossArenaTopPlay:Time"; // 时间数据
		public static final String TopPlayState = "CrossArenaTopPlay:State"; // 时间数据
		public static final String TopPlayLock = "CrossArenaTopPlay:Lock"; // 时间数据锁
		public static final String TopPlayBSSid = "CrossArenaTopPlay:BSSid"; // 逻辑服务器

		public static final String CrossArenaYear = "CrossArena:CrossArenaNoteYear";
		public static final String CrossArenaTopLoop = "CrossArena:CrossArenaTopLoop";
		public static final String CrossArenaNoteCotMap = "CrossArena:CrossArenaNoteCot"; // 10连胜前50名记录
		public static final String CrossArenaNoteInsMap = "CrossArena:CrossArenaNoteIns"; // 闯关记录
		public static final String CrossArenaNoteTopMap = "CrossArena:CrossArenaNoteTop"; // 巅峰赛记录

		public static final String BarragePrefix = "Barrage:"; // 弹幕前缀
		public static final String OFFER_REWARD = "OfferReward"; // 擂台悬赏数据
		public static final String OFFER_REWARD_LOCK = "OfferReward:Lock";
		public static final String OFFER_REWARD_Fight = "OfferRw:Fight"; // 悬赏任务战斗中人数
		public static final String OFFER_REWARD_WIN = "OfferRw:WIN"; // 悬赏任务挑战成功人数
		public static final String OFFER_REWARD_MOCK = "OfferRw:MOCK"; // 悬赏任务站位人数

		public static final String CpTeamPrefix = "CpTeam:"; // 组队玩法前缀

		public static final String CrazyDuelPrefix = "crazyDuel:";

		public static final String CrazyDuelLockPrefix = "crazyDuel:lock:";

		public static final String CrazyDuelRetLock = CrazyDuelLockPrefix + "reset";

		public static final String CrazyDuelRobot = CrazyDuelPrefix + "robotCreateTime";

		public static final String CROSSARENAPVP_ROOM = "CrossArenaPvpRoom"; // 切磋
		public static final String CROSSARENAPVP_LOCK = "CrossArenaPvpLock"; // 切磋房间锁
		public static final String CROSSARENAPVP_PLAYERROOM = "CrossArenaPvpPlayerInfo"; // 玩家切磋房缓存

		public static final String StoneRiftPrefix = "stoneRift:";

		public static final String MagicThron = "MagicThron:";

		public static final String CrossArenaRankPrefix = "CrossArenaRank:";

		public static final String CrossArenaRankSync = CrossArenaRankPrefix+"CrossArenaRankSync:";

		public static final String StarTreasureRecordList = "StarTreasureRecordList";


		public static byte[] getStarTreasureRecordKey(){
			return (RedisKey.StarTreasureRecordList+"_"+ ServerConfig.getInstance().getServer()).getBytes();
		}
	}

	public static class ActivityState {
		public static final int EndState = 0; // 活动结束状态
		public static final int OpenState = 1; // 活动开启状态
		public static final int SettleState = 2; // 活动结算状态
	}

	/**
	 * 平台聊天返回码
	 */
	public static class ChatRetCode {
		public static final int UNKNOWN_ERROR = 0;
		public static final int OPEN = 1;
		public static final int LV_NOT_ENOUGH = 2; // 等级不足
		public static final int BANNED_TO_POST = 3; // 禁言
	}

	public static class ServerStringConst {
		public static final int LV_NOT_ENOUGH = 61;
	}

	public static class Ban {
		public static final int ROLE = 1; // 角色
		public static final int CHAT = 2; // 聊天
		public static final int COMMENT = 3; // 禁止评论
	}

	public static class WarPetUpdate {
		public static final int MODIFY = 1; // 修改
		public static final int REMOVE = 2; // 移除
	}

	public static class PushType {
		public static final int INSTANT = 1; // 即时推送
		public static final int SCHEDULE = 2; // 定时推送
	}

	public enum PushSourceType {
		Platform(1), SDK(2);
		private int source;

		PushSourceType(int source) {
			this.source = source;
		}

		public int value() {
			return source;
		}
	}

	public enum PushTargetType {
		DEVICE("DEVICE"), USERID("USERID"), TAG("TAG"), ALL("ALL");
		private String type;

		PushTargetType(String type) {
			this.type = type;
		}

		public String value() {
			return type;
		}
	}

	public static class RechargeProduct {
		/**
		 * 1-6 魔晶充值Id
		 */
		public static final int RECHARGE_6 = 1; // 即时推送
		public static final int RECHARGE_30 = 2; // 定时推送
		public static final int RECHARGE_68 = 3;
		public static final int RECHARGE_128 = 4;
		public static final int RECHARGE_328 = 5;
		public static final int RECHARGE_648 = 6;

		// 基础月卡Id
		public static final int BaseMothCard = 8;
		// 高级月卡Id
		public static final int AdvancedMothCard = 9;
		// 高级功勋Id
		public static final int AdvancedFeats = 10;
		// 其他直购Id

	}

	public enum ArenaType {
		Null(0), Normal(1), Rank(2);
		@Getter
		private int code;

		ArenaType(int code) {
			this.code = code;
		}
	}

	public enum CpCopyMapPointType {
		Null(0), Battle(1), Treasure(2), Buff(3), LuckyStar(4), DoubleReward(5), Random(6);
		@Getter
		private int code;

		CpCopyMapPointType(int code) {
			this.code = code;
		}
	}

	public enum RechargeType {

		Coupon(1), BaseMonthCard(2), AdvancedMonthCard(3), AdvancedFeats(4), DirectGift(5), BusinessPopup(6),NormalItemCard(7), BetterItemCard(8),PrivilegedCard(9);

		@Getter
		private int code;

		RechargeType(int code) {
			this.code = code;
		}

	}

	public enum PlayerSex {
		Female(0), // 女性
		Male(1); // 男性

		@Getter
		private int code;

		PlayerSex(int code) {
			this.code = code;
		}

	}

	public static class StarTreasureConstant{
		public static final int MAX_ITEM_COUNT = 12;
		public static final int CHARGE_ADD_STAR_UNIT = 10;			//每充值 CHARGE_ADD_STAR_UNIT，获得 {活动配置数量} 个星元
	}

}
