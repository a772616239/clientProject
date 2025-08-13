package model.cp;

import static common.GameConst.RedisKey.CpTeamPrefix;

public class CpRedisKey {

    public static final String CpTeamId = CpTeamPrefix + "teamId:"; // 组队玩法队伍id

    public static final String CpTeamLock = CpTeamPrefix + "teamLock:"; // 组队加入队伍

    public static final String CpTeamPlayerInfo = CpTeamPrefix + "CpTeamMemberInfo"; // 组队中玩家信息

    public static final String CpTeamInfo = CpTeamPrefix + "CpTeamInfo"; // 组队中编队信息

    public static final String CpBattleRecord = CpTeamPrefix + "CpBattleRecord"; // 组队中编队信息

    public static final String CpTeamBuyPlayTimes = CpTeamPrefix + "buyPlayTimes:"; // 玩家所在服务器

    public static final String CpTeamPlayerMap = CpTeamPrefix + "PlayerMap"; // 组队中编队信息

    public static final String CpTeamApplyJoinPlayer = CpTeamPrefix + "ApplyJoinPlayer:"; // 申请加入队伍的玩家Idx

    public static final String CpTeamAbility = CpTeamPrefix + "teamAbility"; // 玩家组队战力

    public static final String CpPlayerInvite = CpTeamPrefix + "PlayerInvite:"; // 玩家受邀信息

    public static final String CpTeamMapInfo = CpTeamPrefix + "MapInfo"; // 组队中编队信息

    public static final String CpTeamPlayerMapId = CpTeamPrefix + "PlayerMapId";

    public static final String CpPlayCopyLock = CpTeamPrefix + "CpPlayCopyLock:"; // 组队副本玩法锁

    public static final String CpCopyUpdate = CpTeamPrefix + "CpCopyUpdate:"; // 组队副本玩法锁

    public static final String CpCopyPlayerOffline = CpTeamPrefix + "playerCopyOffline:"; // 组队副本玩法锁

    public static final String CpCopyPlayerPlayTimes = CpTeamPrefix + "PlayerPlayTimes"; // 玩家副本使用次数

    public static final String CpTeamLv = CpTeamPrefix + "CpTeamLv:"; // 队伍等级

    public static final String CpOpenTeamLv = CpTeamPrefix + "CpOpenTeamLv:"; // 开放队伍等级

    public static final String CpBroadcast = CpTeamPrefix + "Broadcast:"; // 组队副本玩法锁

    public static final String CpBattleSettle = CpBroadcast + "CpBattleSettle"; // 战斗结算

    public static final String CpCopyUpdateBroadcast = CpBroadcast + "CpCopyUpdate"; // 战斗结算


    public static final String CpTeamUpdate = CpBroadcast + "CpTeamUpdate:"; // 队伍等级

    public static final String CpCopyExpireTime = CpBroadcast + "CpCopyExpire:"; // 队伍等级

    public static final String CpPlayerLeaveCopy = CpBroadcast + "CpPlayerLeaveCopy:"; // 玩家离开组队玩法

    public static final String CpCopyActive = CpBroadcast + "CpCopyActive:"; // 副本激活

    public static final String CpCopyApplyJoinTeam = CpBroadcast + "applyJoinTeam:"; // 申请加入组队

    public static final String CpTeamDailyData = CpTeamPrefix + "dailyData:"; // 玩家每日数据

    public static final String CpPlayerIpPort = CpTeamPrefix + "playerIpPort:"; // 玩家所在服务器

    public static final String CpTeamExpire = CpTeamPrefix + "cpTeamExpire:"; // 玩家所在服务器

    public static final String CpFree = CpTeamPrefix + "cpFree:"; // 玩家所在服务器

    public static final String CpBroadcastCopyReset = CpBroadcast + "copyReset:";   //副本重置广播

    public static final String CpBroadcastCopyUpdate = CpBroadcast + "copyUpdate:";   //副本更新广播

    public static final String CpBroadcastTeamUpdate = CpBroadcast + "teamUpdate:";   //副本更新广播

    public static final String CpBroadcastPlayerLeaveTeam = CpBroadcast + "playerLeaveTeam:";   //玩家离开队伍

    public static final String CpBroadcastCopyActive = CpBroadcast + "copyActive";   //玩家离开队伍

    public static final String CpBroadcastExpireCheck = CpBattleSettle + "expireCheck";     //过期检查

}
