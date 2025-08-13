package common;

public class GameConst {
    public static final int MaxPlayerCount = 1010;

    public static final long UpdateServerTime = 3000;

    public static final int ConfgId = 1;

    public static final String ROOBOTID = "111";

    public static class EventType {
        public static final int ET_Login = 1;
        public static final int ET_Logout = 2;
        public static final int ET_Offline = 3;

        public static final int ET_AddSubmitResultCount = 5;
        public static final int ET_BattleCheck = 6;

    }

    public static class RedisKey {
        public static final String BattleServerInfo = "BattleServerInfo"; // 战斗服信息
        public static final String BattleServerIndexAddr = "BattleServerIndexAddr"; // 战斗服serverIndex对应ip

        public static final String BattleOnlineCount = "BattleOnlineCount"; // 战斗服在线人数

        public static final String MatchArena = "MatchArena:";
        public static final String MatchArenaPlayerInfo = MatchArena + "PlayerInfo";
        public static final String MatchArenaPlayerScore = MatchArena + "PlayerScore";
        public static final String MatchArenaPlayerMatchInfo = MatchArena + "PlayerMatchInfo";

        public static final String MatchArenaLTTime = "MatchArenaLTTime";               // 擂台赛同步数据信息
        public static final String MatchArenaLTSyncData = "MatchArenaLTSyncData";               // 擂台赛同步数据信息
        public static final String MatchArenaLTSyncDataId = "MatchArenaLTSyncDataId";               // 擂台赛同步数据信息
        public static final String MatchArenaLTUpdateLock = "MatchArenaLTUpdateLock";

        public static final String CROSSARENAPVP_ROOM = "CrossArenaPvpRoom";           // 切磋
        public static final String CROSSARENAPVP_LOCK = "CrossArenaPvpLock";           // 切磋房间锁
        public static final String CROSSARENAPVP_PLAYERROOM = "CrossArenaPvpPlayerInfo";

        public static final String CrossArenaTen = "CrossArena:ATen";               // 擂台赛是否是10连胜时间段
        public static final String CrossArenaTableStateEndTime = "CrossArena:tableStateEndTime";           //桌子状态结束时间
        public static final String CrossArenaTenPlayer = "CrossArena:ATenPlayer:";               // 擂台赛10连胜时间段中，完成过1次10连的列表
        public static final String CrossArenaTime = "CrossArena:Time";               // 擂台赛开启时间
        public static final String CrossArenaTimeLock = "CrossArena:TimeLock";           // 擂台赛开启时间更新锁
        public static final String CrossArenaData = "CrossArena:Table";               // 单个擂台数据实际KEY=CrossArenaData+ID(擂台ID)
        public static final String CrossArenaTableLock = "CrossArena:TableLock";  // 单个擂台数据操作锁
        public static final String CrossArenaBSSid = "CrossArena:BSSid:";             // 存储每个道场每个擂台逻辑执行服服务器ID
        public static final String CrossArenaPlayerTable = "CrossArena:PlayerTable";               // 存储玩家所在擂台ID
        public static final String CrossArenaTableNum = "CrossArena:TableNum";  // 擂台赛道场桌子数量
        public static final String CrossArenaPlayerInfo = "CrossArena:PlayerInfo";  // 擂台玩家参与数据
        public static final String CrossArenaQue = "CrossArena:Dui";  // 擂台赛队列信息
        public static final String CrossArenaQueLock = "CrossArena:DuiLock";  // 擂台赛队列信息锁
        public static final String CrossArenaPlOnline = "CrossArena:PlOnline";  // 擂台玩家在线玩家(参与玩法的玩家)
        public static final String CrossArenaTempWinCot = "CrossArena:TempWinCot";  // 擂台回收的时候，临时存储有连胜的玩家数据
        public static final String CrossArenaRBPlayerInfo = "CrossArena:RBPlayerInfo"; // 擂台机器玩家战斗数据
        public static final String CrossArenaProtectCard = "CrossArena:protectCard"; // 保护卡
        public static final String CrossArenaProtectRobotQue = "CrossArena:protectRobot:que:";  // 擂台赛保底机器人队列
        public static final String CrossArenaTableState = "CrossArena:tableSate:";  // 擂台状态
        public static final String CrossArenaCreateTableLock = "CrossArena:createTableLock:";  // 创建擂台锁

        public static final String CrossArenaProtectRobotQueLock = "CrossArena:protectRobot:queLock:";  // 擂台赛保护机器人队列锁

        public static final String CrossArenaPlayerSerialFail = "CrossArena:player:serialFail:";  // 擂台赛保护机器人队列锁

        public static final String TopPlayPlayer = "CrossArenaTopPlay:Player"; // 参赛者信息
        public static final String TopPlayGroup = "CrossArenaTopPlay:Group"; // 分组数据
        public static final String TopPlayTime = "CrossArenaTopPlay:Time"; // 时间数据
        public static final String TopPlayState = "CrossArenaTopPlay:State"; // 时间数据
        public static final String TopPlayLock = "CrossArenaTopPlay:Lock"; // 时间数据锁
        public static final String TopPlayBSSid = "CrossArenaTopPlay:BSSid"; // 逻辑服务器
        public static final String TopPlaySidLock = "CrossArenaTopPlay:BSSidLock"; // 时间数据

        public static final String SYNCACHE = "99999999";

    }
}
