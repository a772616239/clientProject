package model.cp;

import cfg.CpTeamCfg;
import cfg.CpTeamLvCfg;
import cfg.CpTeamRobotCfg;
import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import com.hyz.platform.sdk.utils.sensi.SensiWordsUtils;
import common.FunctionExclusion;
import common.GameConst;
import common.GlobalData;
import common.JedisUtil;
import common.tick.GlobalTick;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import static model.cp.CpRedisKey.CpTeamLock;
import model.cp.broadcast.CpTeamUpdate;
import model.cp.entity.CpCopyMap;
import model.cp.entity.CpTeamMember;
import model.cp.entity.CpTeamPublish;
import model.cp.factory.CpCopyMapFactory;
import model.cp.factory.CpTeamPublishFactory;
import model.crossarena.CrossArenaManager;
import model.player.util.PlayerUtil;
import model.redpoint.RedPointManager;
import model.redpoint.RedPointOptionEnum;
import model.team.dbCache.teamCache;
import model.team.entity.teamEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.CpFunction;
import protocol.MessageId;
import static protocol.MessageId.MsgIdEnum.SC_AddNewCpInvite_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_CpPlayerTeamUpdate_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateBuyCpTeamPlayTimes_VALUE;
import protocol.PrepareWar;
import protocol.RedPointIdEnum;
import static protocol.RedPointIdEnum.RedPointId.RP_CROSSARENA_CPTeam_NewJoin_LEAF_VALUE;
import protocol.RetCodeId;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_ApplyJoinTeam_ConditionNotMatch;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_CpInviteNotExists;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_FullTeamMember;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_NotJoinTeam;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_OnlyLeaderCanOperate;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_PlayTimesUseOut;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_RepeatApplyJoin;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_RepeatInvitePlayer;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_TeamNotExists;
import static protocol.RetCodeId.RetCodeEnum.RCE_Success;
import server.handler.cp.CpFunctionUtil;
import util.LogUtil;
import util.TimeUtil;

@Data
@Slf4j
public class CpTeamManger {

    @Getter
    private static CpTeamManger instance = new CpTeamManger();

    private static final int teamNameLength = 15;

    private static final int maxInviteNum = 50;

    private static final int minRandomNeedTeam = 60;

    //<玩家id,邀请的队伍>
    private Map<String, Set<Integer>> inviteTeamMap = new ConcurrentHashMap<>();
    private int fullMemberSize = 3;

    private CpTeamCache cache = CpTeamCache.getInstance();


    public boolean init() {
        CpTeamRobotCfg.getInstance().initRobot();
        return CpTeamMatchManger.getInstance().init()
                && CpCheckManger.getInstance().init()
                && CpBroadcastManager.getInstance().init()
                && CpPlayerKickManger.getInstance().init();

    }


    public CpTeamPublish createTeam(String playerIdx, CpFunction.CS_CreateCpTeam req) {
        CpTeamPublish cpTeamPublish = CpTeamPublishFactory.createCpTeamPublish(playerIdx
                , req.getTeamName(), req.getNeedAbility(), req.getAutoJoin());

        if (cpTeamPublish == null) {
            return null;
        }
        saveCreateTeamToCache(playerIdx, cpTeamPublish);

        sendTeamUpdate(playerIdx, cpTeamPublish.getTeamId());

        return cpTeamPublish;

    }


    private void sendTeamUpdate(String playerIdx, int teamId) {
        CpFunction.SC_CpPlayerTeamUpdate.Builder msg = CpFunction.SC_CpPlayerTeamUpdate.newBuilder();
        CpFunction.CpPlayerTeam.Builder team = CpFunctionUtil.buildCpPlayerTeam(teamId);
        if (team == null) {
            return;
        }
        msg.setTeam(team);
        GlobalData.getInstance().sendMsg(playerIdx, SC_CpPlayerTeamUpdate_VALUE, msg);

    }

    public long findTeamAutoDisbandTime(int teamId) {
        String teamsExpire = cache.findTeamsExpire(String.valueOf(teamId));
        if (teamsExpire == null) {
            return 0L;
        }
        return Long.parseLong(teamsExpire);
    }

    private void saveCreateTeamToCache(String playerIdx, CpTeamPublish cpTeamPublish) {
        CpTeamCache.savePlayerTeamMap(playerIdx, cpTeamPublish.getTeamId());
        cache.saveTeamInfo(cpTeamPublish.getTeamId(), cpTeamPublish);
        cache.saveTeamLevel(cpTeamPublish.getTeamId(), cpTeamPublish.getTeamLv());
        cache.savePlayerAbility(cpTeamPublish.getTeamId(), cpTeamPublish.getLeaderAbility());
        if (cpTeamPublish.isAutoJoin()) {
            cache.saveOpenTeamInfo(cpTeamPublish.getTeamId(), cpTeamPublish.getTeamLv());
        }
        cache.saveTeamsExpire(cpTeamPublish.getTeamId(), getCreateTeamExpire());
    }

    private long getCreateTeamExpire() {
        return GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_MIN * 30;
    }

    public RetCodeId.RetCodeEnum checkCreateTeam(String playerIdx, CpFunction.CS_CreateCpTeam req) {
        if (StringUtils.isEmpty(playerIdx)) {
            return RetCodeId.RetCodeEnum.RCE_ErrorParam;
        }
        if (findPlayerInfo(playerIdx)==null){
            return RetCodeId.RetCodeEnum.RCE_CP_PleaseReUploadTeam;
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(playerIdx);
        if (sf > 0) {
            return FunctionExclusion.getInstance().getRetCodeByType(sf);
        }
        String teamName = req.getTeamName();
        //名字非法
        if (!legalTeamName(teamName)) {
            return RetCodeId.RetCodeEnum.RCE_PrepareWar_IllegalTeamName;
        }
        //玩家在其他队伍中
        if (playerInTeam(playerIdx)) {
            return RetCodeId.RetCodeEnum.RCE_CP_PlayerInTeam;
        }
        if (playTimesUseOut(playerIdx)) {
            //次数使用完
            return RCE_CP_PlayTimesUseOut;
        }
        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    public boolean playerInTeam(String playerIdx) {
        return findPlayerTeamId(playerIdx) != null;
    }

    private boolean legalTeamName(String teamName) {
        return StringUtils.isNotBlank(teamName) && teamName.length() <= teamNameLength && SensiWordsUtils.isLegal(teamName);
    }


    private boolean fullMember(CpTeamPublish team) {
        return team.getMembers().size() >= 3;
    }

    public RetCodeId.RetCodeEnum checkAndApplyJoin(String playerIdx, CpFunction.CS_ApplyJoinCPTeam req) {
        if (playerInTeam(playerIdx)) {
            return RetCodeId.RetCodeEnum.RCE_CP_PlayerInTeam;
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(playerIdx);
        if (sf > 0) {
            return FunctionExclusion.getInstance().getRetCodeByType(sf);
        }
        return JedisUtil.syncExecSupplier(getLockTeamRedisKey(req.getTeamId()), () -> {
            CpTeamPublish team = findTeamByTeamId(req.getTeamId());
            if (team == null) {
                return RCE_CP_TeamNotExists;
            }
            if (team.getTeamLv() > CpTeamLvCfg.queryTeamLv(PlayerUtil.queryPlayerLv(playerIdx))) {
                //等级不足
                return RCE_CP_ApplyJoinTeam_ConditionNotMatch;
            }

            if (team.getNeedAbility() > queryLocalPlayerTeamAbility(playerIdx)) {
                //战力不足
                return RCE_CP_ApplyJoinTeam_ConditionNotMatch;

            }
            if (playTimesUseOut(playerIdx)) {
                //次数使用完
                return RCE_CP_PlayTimesUseOut;
            }

            if (team.getMemberSize() >= fullMemberSize) {
                //人数已满
                return RCE_CP_FullTeamMember;
            }
            if (team.isAutoJoin()) {
                addMember(playerIdx, team);
                return RCE_Success;
            }

            if (cache.existApplyJoinToDb(team.getTeamId(), playerIdx)) {
                return RCE_CP_RepeatApplyJoin;
            }

            cache.saveApplyJoinToDb(team.getTeamId(), playerIdx);

            String svrIndex = CpTeamCache.getInstance().findPlayerSvrIndex(team.getLeaderIdx());
            RedPointManager.getInstance().sendRedPointBS(team.getLeaderIdx(), svrIndex, RP_CROSSARENA_CPTeam_NewJoin_LEAF_VALUE, RedPointOptionEnum.ADD);

            sendApplyJoinTeam(playerIdx, team);
            return RetCodeId.RetCodeEnum.RCE_Success;
        });

    }

    private void sendApplyJoinTeam(String playerIdx, CpTeamPublish team) {
        CpBroadcastManager.getInstance().broadcastAddInvite(playerIdx, team.getLeaderIdx());
    }

    private boolean playTimesUseOut(String playerIdx) {
        if (CpFunctionUtil.isRobot(playerIdx)) {
            return false;
        }
        return CpCopyManger.getInstance().queryCpCopyRemainTimes(playerIdx) <= 0;
    }

    private long queryLocalPlayerTeamAbility(String playerIdx) {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity == null) {
            return 0L;
        }
        return teamEntity.getTeamFightAbility(PrepareWar.TeamNumEnum.TNE_LtCP_1);
    }

    public CpTeamPublish findPlayerJoinTeam(String playerIdx) {
        Integer teamId = findPlayerTeamId(playerIdx);
        if (teamId == null) {
            return null;
        }
        return findTeamByTeamId(teamId);

    }

    public CpTeamPublish findTeamByTeamId(int teamId) {
        return cache.loadTeamInfo(teamId);
    }

    private Integer findPlayerTeamId(String playerIdx) {
        return cache.getPlayerTeamId(playerIdx);
    }

    public Collection<CpTeamPublish> randomTeamsForPlayer(String playerIdx, int teamSize) {
        CpTeamMember cpTeamMember = cache.loadPlayerInfo(playerIdx);
        if (cpTeamMember == null) {
            return Collections.emptyList();
        }
        int teamLv = CpTeamLvCfg.queryTeamLv(cpTeamMember.getPlayerLv());
        Set<String> teamIdx = cache.findTeamIdByTeamLv(teamLv);
        List<String> teamIdxList = new ArrayList<>(teamIdx);
        Collections.shuffle(teamIdxList);
        List<CpTeamPublish> result = new LinkedList<>();
        for (String idx : teamIdxList) {
            CpTeamPublish team = findTeamByTeamId(Integer.parseInt(idx));
            if (team != null && !team.getMembers().contains(playerIdx)) {
                result.add(team);
                if (result.size() == teamSize) {
                    return result;
                }
            }
        }
        return result;
    }

    public boolean canInvite(String friendId) {
        if (playerInTeam(friendId)) {
            return false;
        }
        if (findPlayerInfo(friendId) == null) {
            return false;
        }
        //检查数量
        return true;
    }

    public String getLockTeamRedisKey(int teamId) {
        return CpTeamLock + teamId;
    }

    public RetCodeId.RetCodeEnum leaveTeam(String playerIdx) {
        Integer teamId1 = findPlayerTeamId(playerIdx);
        if (teamId1 == null) {
            return RCE_CP_NotJoinTeam;
        }
        return JedisUtil.syncExecSupplier(getLockTeamRedisKey(teamId1), () -> {
                    Integer teamId = findPlayerTeamId(playerIdx);
                    if (teamId == null) {
                        return RCE_CP_NotJoinTeam;
                    }
                    CpTeamPublish teamDb = cache.loadTeamInfo(teamId);
                    if (teamDb == null) {
                        return RCE_CP_TeamNotExists;
                    }
                    if (playerIdx.equals(teamDb.getLeaderIdx())) {
                        disbandTeam(teamDb);
                    } else {
                        kickPlayer(playerIdx, teamDb);
                        cache.saveTeamInfo(teamDb.getTeamId(), teamDb);
                        CpBroadcastManager.getInstance().broadcastTeamUpdate(new CpTeamUpdate(teamDb));
                    }
                    return RetCodeId.RetCodeEnum.RCE_Success;
                }
        );
    }

    public void sendMyTeamInfo(String playerIdx) {
        CpFunction.SC_ClaimMyCpTeam.Builder msg = CpFunction.SC_ClaimMyCpTeam.newBuilder();
        Integer teamId = findPlayerTeamId(playerIdx);
        if (teamId != null) {
            CpFunction.CpPlayerTeam.Builder team = CpFunctionUtil.buildCpPlayerTeam(findPlayerTeamId(playerIdx));
            if (team != null) {
                msg.setTeam(team);
            }
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_ClaimMyCpTeam_VALUE, msg);

    }

    public CpTeamMember findPlayerInfo(String playerIdx) {
        if (CpFunctionUtil.isRobot(playerIdx)) {
            return CpTeamRobotCfg.findRobotInfo(CpFunctionUtil.parseRobotId(playerIdx));
        }
        return cache.loadPlayerInfo(playerIdx);
    }

    public boolean isPlayerUploadTeam(String playerIdx) {
        CpTeamMember playerInfo = findPlayerInfo(playerIdx);
        if (playerInfo == null) {
            return false;
        }
        return playerInfo.isUploadTeam();
    }

    public void uploadPlayerInfo(String playerIdx) {
        CpTeamMember member = CpTeamPublishFactory.createCpTeamMember(playerIdx);
        if (member == null) {
            LogUtil.error("CpTeamManager uploadPlayerInfo error,playerIdx:{} ", playerIdx);
            return;
        }
        member.setUploadTeam(true);
        cache.savePlayerInfo(playerIdx, member);
        cache.savePlayerSvrIndex(playerIdx);
        CpCopyManger.getInstance().sendCopyInit(playerIdx);
    }


    public RetCodeId.RetCodeEnum checkAndDisbandTeam(String playerIdx) {
        CpTeamPublish playerJoinTeam = findPlayerJoinTeam(playerIdx);
        if (playerJoinTeam == null) {
            return RCE_CP_NotJoinTeam;
        }
        if (!teamLeader(playerIdx, playerJoinTeam)) {
            return RCE_CP_OnlyLeaderCanOperate;
        }
        disbandTeam(playerJoinTeam);

        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    /**
     * 解散队伍
     *
     * @param playerJoinTeam
     */
    public void disbandTeam(CpTeamPublish playerJoinTeam) {
        for (String member : new ArrayList<>(playerJoinTeam.getMembers())) {
            kickPlayer(member, playerJoinTeam);
            broadcastTeamDisband(member);
        }
        settleTeamDisband(playerJoinTeam.getTeamId(), playerJoinTeam.getTeamLv(), playerJoinTeam.isAutoJoin());
    }

    private void kickPlayer(String member, CpTeamPublish team) {
        cache.removePlayerTeamMap(member);
        team.removeMember(member);
    }

    private void broadcastTeamDisband(String member) {
        if (CpFunctionUtil.isRobot(member)) {
            return;
        }
        CpBroadcastManager.getInstance().broadcastTeamKickOut(member);
    }

    public RetCodeId.RetCodeEnum clearApplyJoinTeam(String playerIdx) {
        Integer teamId = findPlayerTeamId(playerIdx);
        if (teamId == null) {
            return RCE_CP_TeamNotExists;
        }

        return JedisUtil.syncExecSupplier(getLockTeamRedisKey(teamId), () -> {
            CpTeamPublish playerJoinTeam = findPlayerJoinTeam(playerIdx);
            if (playerJoinTeam == null) {
                return RCE_CP_TeamNotExists;
            }
            if (!playerIdx.equals(playerJoinTeam.getLeaderIdx())) {
                return RCE_CP_OnlyLeaderCanOperate;
            }
            clearApplyJoinTeamPlayers(playerJoinTeam.getTeamId());

            return RetCodeId.RetCodeEnum.RCE_Success;
        });
    }

    private void clearApplyJoinTeamPlayers(int teamId) {
        cache.clearApplyJoinTeamPlayers(teamId);
    }

    public RetCodeId.RetCodeEnum replyCpInvite(String playerIdx, String invitePlayerId, boolean accept) {
        if (!removeCpInvite(playerIdx, invitePlayerId)) {
            return RCE_CP_CpInviteNotExists;
        }
        if (!accept) {
            return RetCodeId.RetCodeEnum.RCE_Success;
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(playerIdx);
        if (sf > 0) {
            return FunctionExclusion.getInstance().getRetCodeByType(sf);
        }

        Integer teamId = findPlayerTeamId(invitePlayerId);
        if (teamId == null) {
            return RCE_CP_TeamNotExists;
        }

        if (playTimesUseOut(playerIdx)) {
            //次数使用完
            return RCE_CP_PlayTimesUseOut;
        }

        RetCodeId.RetCodeEnum codeEnum = JedisUtil.syncExecSupplier(getLockTeamRedisKey(teamId), () -> {
            CpTeamPublish playerJoinTeam = findPlayerJoinTeam(invitePlayerId);
            if (playerJoinTeam == null) {
                return RCE_CP_TeamNotExists;
            }
/*            if (playerJoinTeam.getTeamLv() > CpTeamLvCfg.queryTeamLv(PlayerUtil.queryPlayerLv(playerIdx))) {
                //等级不足
                return RetCodeId.RetCodeEnum.RCE_PlayerLvNotEnough;
            }

            if (playerJoinTeam.getNeedAbility() > queryLocalPlayerTeamAbility(playerIdx)) {
                //战力不足
                return RCE_CP_AbilityNotEnough;
            }*/
            if (fullMember(playerJoinTeam)) {
                return RCE_CP_FullTeamMember;
            }
            addMember(playerIdx, playerJoinTeam);
            return RetCodeId.RetCodeEnum.RCE_Success;
        });
        if (codeEnum == RetCodeId.RetCodeEnum.RCE_Success) {
            sendMyTeamInfo(playerIdx);
        }
        return codeEnum;

    }

    private boolean removeCpInvite(String playerIdx, String invitePlayerId) {
        return cache.removeCpInvite(playerIdx, invitePlayerId);
    }

    public Set<String> findAllPlayerInvite(String playerIdx) {
        return cache.loadPlayerCpInvite(playerIdx);
    }

    public RetCodeId.RetCodeEnum replyJoinTeamRequest(String playerIdx, String applyPlayer, boolean accept) {
        CpTeamPublish team = findPlayerJoinTeam(playerIdx);
        if (team == null) {
            return RCE_CP_TeamNotExists;
        }
        if (!removeApplyJoinTeam(team.getTeamId(), applyPlayer)) {
            return RCE_CP_CpInviteNotExists;
        }
        if (!accept) {
            return RCE_Success;
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(applyPlayer);
        if (sf > 0) {
            return FunctionExclusion.getInstance().getRetCodeByType(sf);
        }

        if (!teamLeader(playerIdx, team)) {
            return RCE_CP_OnlyLeaderCanOperate;
        }
        RetCodeId.RetCodeEnum codeEnum = JedisUtil.syncExecSupplier(getLockTeamRedisKey(team.getTeamId()), () -> {
            CpTeamPublish playerJoinTeam = findPlayerJoinTeam(playerIdx);
            if (playerJoinTeam == null) {
                return RCE_CP_TeamNotExists;
            }
            if (fullMember(playerJoinTeam)) {
                return RCE_CP_FullTeamMember;
            }
            addMember(applyPlayer, playerJoinTeam);
            return RCE_Success;
        });
        if (RCE_Success == codeEnum) {
            sendMyTeamInfo(playerIdx);
        }
        return codeEnum;
    }

    public void addMember(String playerIdx, CpTeamPublish team) {
        if (team.getMembers().contains(playerIdx)){
            return;
        }
        team.addMember(playerIdx);
        if (!CpFunctionUtil.isRobot(playerIdx)) {
            CpTeamCache.savePlayerTeamMap(playerIdx, team.getTeamId());
        }
        cache.saveTeamInfo(team.getTeamId(), team);
        //通知玩家
        CpBroadcastManager.getInstance().broadcastTeamUpdate(new CpTeamUpdate(team));
    }


    public CpCopyMap checkAndCreateMap(String playerIdx) {
        CpTeamPublish team = findPlayerJoinTeam(playerIdx);
        if (team == null) {
            return null;
        }
        if (!team.getLeaderIdx().equals(playerIdx)) {
            return null;
        }

        if (!fullMember(team)) {
            return null;
        }
        team.setActiveCopy(true);
        cache.saveTeamInfo(team.getTeamId(), team);
        CpCopyMap map = CpCopyMapFactory.createMap(team);
        saveCopyMapInfo(map);
        broadcastCopyActive(team);
        cache.saveTeamInfo(team.getTeamId(), team);
        for (String member : team.getMembers()) {
            CpCopyManger.getInstance().useCopyPlayTimes(member);
        }
        log.info("cp team leader open cp copy,teamId:{},openPlayer:{},mapId:{},allPlayers:{}", team.getTeamId(), playerIdx, map.getMapId(), map.getMembers());
        return map;
    }

    private void broadcastCopyActive(CpTeamPublish team) {
        List<String> playerIds = CpFunctionUtil.findPlayerIds(team.getMembers());
        if (CollectionUtils.isEmpty(playerIds)) {
            return;
        }
        CpBroadcastManager.getInstance().broadcastCopyActive(playerIds);
    }


    private void saveCopyMapInfo(CpCopyMap map) {
        cache.saveCopyMap(map);
        for (String playerIdx : map.getMembers()) {
            cache.savePlayerMapMap(playerIdx, map.getMapId());
            cache.savePlayerMapExpire(map.getMapId(), map.getExpireTime());
        }
    }

    private boolean removeApplyJoinTeam(int teamId, String applyPlayer) {
        return cache.removeApplyJoinTeamPlayer(teamId, applyPlayer);
    }

    public Set<String> findAllApplyJoinPlayer(String playerIdx) {
        CpTeamPublish playerJoinTeam = findPlayerJoinTeam(playerIdx);
        if (playerJoinTeam == null || !playerIdx.equals(playerJoinTeam.getLeaderIdx())) {
            return Collections.emptySet();
        }
        return cache.loadApplyJoinTeamPlayers(playerJoinTeam.getTeamId());
    }


    public boolean playerInCpCopy(String playerIdx) {
        return cache.findPlayerCopyMapId(playerIdx) != null;
    }

    public RetCodeId.RetCodeEnum invitePlayerJoinTeam(String playerIdx, String invitePlayerIdx) {
        Integer teamId = findPlayerTeamId(playerIdx);
        if (teamId == null) {
            return RCE_CP_TeamNotExists;
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(invitePlayerIdx);
        if (sf > 0) {
            return FunctionExclusion.getInstance().getRetCodeByType(sf);
        }
        return JedisUtil.syncExecSupplier(getLockTeamRedisKey(teamId), () -> {
            CpTeamPublish team = findTeamByTeamId(teamId);
            if (team == null) {
                return RCE_CP_TeamNotExists;
            }
            if (!teamLeader(playerIdx, team)) {
                return RCE_CP_OnlyLeaderCanOperate;
            }
            if (fullMember(team)) {
                return RCE_CP_FullTeamMember;
            }
            if (teamContainsPlayer(team, invitePlayerIdx)) {
                return RCE_CP_RepeatInvitePlayer;
            }
            //机器人直接加入组队
            if (isRobot(invitePlayerIdx)) {
                addMember(invitePlayerIdx, team);
                return RCE_Success;
            }

            if (playTimesUseOut(invitePlayerIdx)) {
                //次数使用完
                return RCE_CP_PlayTimesUseOut;
            }
            if (cache.existPlayerCpInvite(playerIdx, invitePlayerIdx)) {
                return RCE_CP_RepeatInvitePlayer;
            }
            cache.addPlayerCpInvite(playerIdx, invitePlayerIdx);

            String svrIndex = CpTeamCache.getInstance().findPlayerSvrIndex(invitePlayerIdx);
            RedPointManager.getInstance().sendRedPointBS(invitePlayerIdx, svrIndex, RedPointIdEnum.RedPointId.RP_CROSSARENA_CPTeam_NewInvite_LEAF_VALUE, RedPointOptionEnum.ADD);

            sendInvite(playerIdx, invitePlayerIdx);
            return RCE_Success;
        });
    }

    private void sendInvite(String playerIdx, String invitePlayerIdx) {
        CpFunction.InviteCpPlayer.Builder player = CpFunctionUtil.queryInviteCpPlayer(playerIdx);
        if (player == null) {
            return;
        }
        CpFunction.SC_AddNewCpInvite.Builder msg = CpFunction.SC_AddNewCpInvite.newBuilder();
        msg.setPlayer(player);
        CpBroadcastManager.getInstance().broadcastMsg(Collections.singletonList(invitePlayerIdx), SC_AddNewCpInvite_VALUE, msg.build());
    }

    private boolean teamContainsPlayer(CpTeamPublish team, String playerIdx) {
        if (team.getMembers().contains(playerIdx)) {
            return true;
        }
        return false;
    }

    private boolean teamLeader(String playerIdx, CpTeamPublish team) {
        return team.getLeaderIdx().equals(playerIdx);
    }


    private boolean isRobot(String invitePlayerIdx) {
        return invitePlayerIdx.startsWith(CpFunctionUtil.getRobotIdStart());
    }


    public RetCodeId.RetCodeEnum clearReceiveCpInvite(String playerIdx) {
        cache.clearReceiveCpInvite(playerIdx);
        return RCE_Success;
    }

    public void updateWeeklyData() {
        cache.clearUseTime();
    }

    public RetCodeId.RetCodeEnum autoMatchTeam(String playerIdx) {
        int sf = FunctionExclusion.getInstance().checkExclusionAll(playerIdx);
        if (sf > 0) {
            return FunctionExclusion.getInstance().getRetCodeByType(sf);
        }
        if (playerInTeam(playerIdx)) {
            return RetCodeId.RetCodeEnum.RCE_CP_PlayerInTeam;
        }
        CpTeamMatchManger.getInstance().addMatchPlayer(playerIdx);
        return RCE_Success;
    }


    public RetCodeId.RetCodeEnum cancelMatchPlayer(String playerIdx) {
        CpTeamMatchManger.getInstance().cancelAddMatchPlayer(playerIdx);
        return RCE_Success;

    }

    public List<String> findTeamPlayers(String playerIdx) {
        return CpTeamMatchManger.getInstance().findTeamPlayers(playerIdx);
    }

    public RetCodeId.RetCodeEnum kickOutTeammate(String playerIdx, String kickOutPlayer) {
        CpTeamPublish team = findPlayerJoinTeam(playerIdx);

        if (team == null) {
            return RCE_CP_TeamNotExists;
        }

        if (!teamLeader(playerIdx, team)) {
            return RCE_CP_OnlyLeaderCanOperate;
        }
        RetCodeId.RetCodeEnum codeEnum = JedisUtil.syncExecSupplier(getLockTeamRedisKey(team.getTeamId()), () -> {
            kickPlayer(kickOutPlayer, team);
            cache.saveTeamInfo(team.getTeamId(), team);
            return RCE_Success;
        });
        if (RCE_Success == codeEnum) {
            CpBroadcastManager.getInstance().broadcastTeamUpdate(new CpTeamUpdate(team));
            CpBroadcastManager.getInstance().broadcastTeamKickOut(kickOutPlayer);
        }
        return codeEnum;

    }

    public RetCodeId.RetCodeEnum buyCanPlayTimes(String playerIdx) {

        int totalCanPlayerBuyPlayTimes = getTotalCanPlayerBuyPlayTimes(playerIdx);

        int times = cache.queryPlayerBuyPlayerTimes(playerIdx);

        if (totalCanPlayerBuyPlayTimes <= times) {
            return RetCodeId.RetCodeEnum.RCE_MonthCard_LimitBuy;
        }

        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx,
                ConsumeUtil.parseConsume(CpTeamCfg.getById(GameConst.CONFIG_ID).getBuygameplayconsume()),
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_LT_CP))) {
            return RetCodeId.RetCodeEnum.RCE_MatieralNotEnough;
        }

        cache.savePlayerBuyPlayerTimes(playerIdx, times + 1);

        sendBuyCanPlayTimes(playerIdx, times + 1);

        return RCE_Success;

    }


    private void sendBuyCanPlayTimes(String playerIdx, int times) {
        CpFunction.SC_UpdateBuyCpTeamPlayTimes.Builder msg = CpFunction.SC_UpdateBuyCpTeamPlayTimes.newBuilder();
        msg.setTimes(times);
        GlobalData.getInstance().sendMsg(playerIdx, SC_UpdateBuyCpTeamPlayTimes_VALUE, msg);
    }

    private int getTotalCanPlayerBuyPlayTimes(String playerIdx) {
        int result = 0;

        VIPConfigObject vipCfg = VIPConfig.getById(PlayerUtil.queryPlayerVipLv(playerIdx));

        result += vipCfg == null ? 0 : vipCfg.getCpweeklybuycount();

        CrossArenaLvCfgObject caCfg = CrossArenaLvCfg.getByLv(CrossArenaManager.getInstance().findPlayerGradeLv(playerIdx));

        result += caCfg == null ? 0 : caCfg.getCpweeklybuycount();

        return result;
    }

    /**
     * 在组队中尚未开启副本
     *
     * @param playerIdx
     * @return
     */
    public boolean playerInMatchTeam(String playerIdx) {
        CpTeamPublish team = findPlayerJoinTeam(playerIdx);
        return team != null && !team.isActiveCopy();
    }

    public boolean getRedPointStateInvite(String playerId) {
        Set<String> inviteList = CpTeamCache.getInstance().loadPlayerCpInvite(playerId);
        return inviteList.size() > 0;
    }

    public boolean getRedPointStateApply(String playerId) {
        Integer teamId = findPlayerTeamId(playerId);
        if (teamId == null) {
            return false;
        }
        Set<String> players = CpTeamCache.getInstance().loadApplyJoinTeamPlayers(teamId);
        return players.size() > 0;
    }

    public void settleTeamDisband(int teamId, int teamLv, boolean autoJoin) {
        cache.removeTeam(teamId);
        cache.removeTeamExpire(teamId);
        cache.delLevelTeam(teamId, teamLv);
        if (autoJoin) {
            cache.removeOpenTeamInfo(teamLv, String.valueOf(teamId));
        }
        cache.removeTeamExpire(teamId);
    }


    public void removeTeamByAllPlayerLeave(int teamId) {
        CpTeamPublish team = findTeamByTeamId(teamId);
        if (team == null) {
            return;
        }
        settleTeamDisband(team.getTeamId(), team.getTeamLv(), team.isAutoJoin());
    }
}
