package common;

public class GameConst {
    public static final int ROLE_NAME_MAX_LENGTH = 14;

    public static final int CROSS_RANKING_SERVER_INDEX = -1;

    public static final int ConfigId = 1;

    public static final int Max_Online_Player_Count = 1000;

    public static final long UpdateServerTime = 3000;

    public static final int MistMaxRoomCount = 12; // 迷雾森林最大房间数
    /**
     * 宠物血量最大比例(千分比)
     */
    public static final int PetMaxHpRate = 1000;

    /**
     * 宠物属性放大倍率
     */
    public static final int petPropertyMagnification = 10000;

    /**
     * 宠物加成放大倍率
     */
    public static final double petAdditionMagnification = 1000.0;



    public static class ObjType {
        public static final int PLAYER = 1;
        public static final int PET = 2;
    }

    public static class EventType {
        public static final int ET_Login = 1;
        public static final int ET_Logout = 2;
        public static final int ET_Offline = 3;

        // 迷雾森林
        public static final int ET_ExchangeMistForest = 10;
        public static final int ET_EnterMistPveBattle = 11;
        public static final int ET_SetMistPlayerPetRemainHp = 12;
        public static final int ET_EnterMistPvpBattle = 13;
        public static final int ET_SettleMistPvpBattle = 14;
        public static final int ET_RemoveMistPlayer = 15;

        public static final int ET_ConsumeLootPackReward = 16;

        public static final int ET_MonsterBattleCarryReward = 20;
        public static final int ET_GainMistCarryReward = 21;
        public static final int ET_CalcFighterRemainHpRate = 22;
        public static final int ET_ChangePlayerHpRate = 23;

        public static final int ET_CalcPlayerDropItem = 24;

        public static final int ET_CreateGhostBusterRoom = 25;

        public static final int ET_UpdateEliteMonsterRewardTimes = 26;

        public static final int ET_ChangeRecoverHpIntervalRate = 27;

        // 矿区争夺
        public static final int ET_AttackerGiveup = 31; // 玩家放弃掠夺或掉线

        public static final int ET_TimerInvoke = 32;               //定时器时间

        public static final int ET_RANKING_UPDATE = 34;                 //更新排行榜

        public static final int ET_RANKING_CLEAR = 35;                  //清空排行榜

        public static final int ET_QUERY_ARENA_ROOM_RANKING = 36;       //查询房间排行榜

        // ==========================战戈 start(50-100)===================
        public static final int ET_TheWar_AddFootHoldGrid = 50;         // 玩家占据据点
        public static final int ET_TheWar_AddUnsettledCurrency = 51;    // 缓存采集货币(采集至最大时间时不清楚据点)

        public static final int ET_TheWar_SettleAfkReward = 53;         // 玩家结算一次挂机奖励
        public static final int ET_TheWar_AddPlayerCache = 54;          // 缓存玩家信息
        public static final int ET_TheWar_AddGridCache = 55;            // 缓存格子信息

        public static final int ET_TheWar_AddWarGridRecord = 56;        // 增加格子记录

        public static final int ET_TheWar_RemoveDpResource = 57;        // 扣除开门资源(并获得兑换金币)

        public static final int ET_TheWar_AddPosGroupGrid = 58;         // 占领格子计算阵营格子组
        public static final int ET_TheWar_RemovePosGroupGrid = 59;      // 清除格子计算阵营格子组

        public static final int ET_TheWar_AddTargetProgress = 60;       // 增加职位任务进度
        public static final int ET_TheWar_AddPortalGridProgress = 61;   // 增加传送门开启进度
        public static final int ET_TheWar_AddRefreshMonsterGrid = 62;       // 刷新空地格子成野怪格子
        public static final int ET_TheWar_RemoveRefreshMonsterGrid = 63;    // 刷新野怪格子成空地格子(采集至最大时间时,room->grid)
        public static final int ET_TheWar_AddRefreshMonsterCount = 64;      // 增加野怪格子刷新数
        public static final int ET_TheWar_DecRefreshMonsterCount = 65;      // 减少野怪格子刷新数

        public static final int ET_TheWar_ChangePlayerStamina = 66;         // 改变玩家体力

        public static final int ET_TheWar_ChangeTargetGridProperty = 68;    // 改变目标格子属性

        public static final int ET_TheWar_AddPlayerBattleState = 69;        // 玩家进入战斗

        public static final int ET_TheWar_TakeUpRoom = 70;                  // 重新创建房间
        public static final int ET_TheWar_RoomSettle = 71;                  // 结算房间

        public static final int ET_TheWar_ClearFootHoldGrid = 72;           // 玩家清除格子
        public static final int ET_TheWar_ClearStationPetGrid = 73;         // 清除格子驻防信息

        public static final int ET_TheWar_InitWarRoom = 74;                 // 初始化房间

        public static final int ET_TheWar_ModifyPetTroopsData = 75;         // 修改宠物驻防信息
        public static final int ET_TheWar_RemovePetTroopsData = 76;         // 移除宠物驻防信息

        public static final int ET_TheWar_UpdateRemainPetHp = 77;           // 保留剩余血量
        public static final int ET_TheWar_UpdateBossPetHp = 78;             // 更新boss格子血量

        public static final int ET_TheWar_RemovePetFromTeam = 79;           // 移除队伍宠物
        public static final int ET_TheWar_RemoveTroopsPetFromGrid = 80;     // 移除格子中驻防宠物

        public static final int ET_TheWar_AddUnclaimedReward = 81;          // 添加未结算奖励
        public static final int ET_TheWar_SettleBattleReward = 82;          // 结算战斗奖励
        // ==========================战戈 end(50-100)=====================
    }

    public static class RedisKey {
        public static final String BattleServerInfo = "BattleServerInfo";   // 战斗服信息
        public static final String BattleServerIndexAddr = "BattleServerIndexAddr"; // 战斗服serverIndex对应ip
        public static final String BattleOnlineCount = "BattleOnlineCount"; // 战斗服在线人数

        public static final String CrossServerInfo = "CrossServerInfo";     // 跨服服务器地址信息

        public static final String CrossServerIndexAddr = "CrossServerIndexAddr";     // 跨服服务器serverIndex对应ip

        public static final String MistForestInfo = "MistForestInfo-";      // 迷雾森林相关信息

        public static final String MistMazeSyncData = "MistMazeSyncData";               // 迷宫相关信息
        public static final String MistMazeUpdateLock = "MistMazeUpdateLock";           // 迷宫数据更新锁

        public static final String MistGhostBusterMatchData = "MistGhostBusterMatchData";   // 抓鬼匹配数据
        public static final String MistGhostBusterMatchLock = "MistGhostBusterMatchLock";   // 抓鬼匹配数据更新锁

        //========================竞技场相关key start========================
        // 竞技场服务器相关信息 hash
        public static final String ARENA_SERVER_INFO = "ArenaServerInfo";

        //竞技场房间  hash  key:RoomId
        public static final String ARENA_ROOM_INFO = "ArenaRoomInfo";
        //竞技场玩家基本信息信息  hash   DB_ArenaPlayerInfo
        public static final String ARENA_PLAYER_BASE_INFO = "ArenaPlayerBaseInfo";

        //竞技场玩家防御队伍信息信息  hash   DB_ArenaDefinedTeamsInfo
        public static final String ARENA_PLAYER_DEFINED_TEAMS_INFO = "ArenaPlayerDefinedTeamsInfo";

        //玩家段位晋升队列 list playerIdx
        public static final String ARENA_PLAYER_DAN_UP = "ArenaPlayerDanUp";
        //玩家房间分配队列key  list<playerIdx>
        public static final String ARENA_ALLOCATION_PLAYER_ROOM = "ArenaAllocationPlayerRoom";
        //移除处理队列 list
        public static final String ARENA_REMOVE_PLAYER_FROM_ROOM = "ArenaRemoveFormRoom";

        /**
         * 房间排行榜结算处理临时列表, 服务器抛出每日段位结算时间后, 所有服务器竞争上锁并设置该字段
         * 房间信息内保存一个下次结算时间,服务器竞争到房间锁后,结算并从temp删除房间id和设置房间的下次结算时间
         *           服务器tick逻辑后判断为空后删除   使用此方式
         */
        public static final String ARENA_ROOM_RANKING_SETTLE_LIST = "ArenaRoomRankingSettleList";

        //房间段位结算
        public static final String ARENA_ROOM_DAN_SETTLE_LIST = "ArenaRoomDanSettleList";

        public static final String ARENA_LOCK_DAN_CREATE_ROOM = "ArenaLockDanCreateRoom-";          //创建房间锁

        //========================竞技场相关key end========================

        //========================战戈key start========================
        public static final String TheWarAvailableJoinRoomInfo = "TheWarAvailableJoinRoomInfo";    // 战戈当前可进入房间
        public static final String TheWarRoomServerIndex = "TheWarRoomServerIndex";                 // 战戈房间跨境服serverIndex信息
        public static final String TheWarServerLoadInfo = "TheWarServerLoadInfo";                   // 战戈服务器负载信息(已排序)

        public static final String TheWarCreateRoomLock = "TheWarCreateRoomLock";                   // 创建房间锁

        public static final String TheWarRoomData = "TheWarRoomData";                               // 战戈房间信息
        public static final String TheWarPlayerData = "TheWarPlayerData:";                          // 战戈玩家信息
        public static final String TheWarGridData = "TheWarGridData:";                              // 战戈格子信息

        public static final String TheWarUpdateRoomLock = "TheWarUpdateRoomLock";                   // 接管房间锁
        //========================战戈key end========================
    }

    public static class RankingName {
        public static final String ARENA_ROOM_RANKING = "ArenaRoomRanking-";
    }
}
