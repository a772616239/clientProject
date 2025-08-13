package server.handler.player;


import cfg.GameConfig;
import cfg.MailTemplateUsed;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.ServerState;
import common.GlobalData;
import common.HttpRequestUtil;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.PlatformManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.CreateRoleLog;
import platform.logs.entity.LogInLog;
import platform.logs.entity.LogOutLog;
import platform.logs.entity.OnLineTimeLog;
import protocol.Common;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.LoginProto.CS_Login;
import protocol.LoginProto.SC_KickOut;
import protocol.LoginProto.SC_Login;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.PetAPP;
import server.event.Event;
import server.event.EventManager;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.ObjUtil;
import util.TimeUtil;


@MsgId(msgId = MsgIdEnum.CS_Login_VALUE)
public class LoginHandler extends AbstractHandler<CS_Login> {

    @Override
    protected CS_Login parse(byte[] msgByte) throws Exception {
        return CS_Login.parseFrom(msgByte);
    }

    @Override
    protected void execute(GameServerTcpChannel channel, CS_Login req, int codeNum) {
        String addr = channel.channel.remoteAddress().toString();
        LogUtil.info("recv login msg from" + addr + ",userId=" + req.getUserId());
        SC_Login.Builder builder = SC_Login.newBuilder();
        playerEntity player = null;
        try {
            if (GlobalData.getInstance().isServerFull()) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Login_FullPlayer));
                channel.send(MsgIdEnum.SC_Login_VALUE, builder);
                return;
            }
            if (PetAPP.serverState != ServerState.ServerRunning) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Login_ServerNotOpen));
                channel.send(MsgIdEnum.SC_Login_VALUE, builder);
                return;
            }
            String userId = req.getUserId();
        /*    if (!checkVersion(userId, req.getClientData())) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Login_ClientVersionError));
                channel.send(MsgIdEnum.SC_Login_VALUE, builder);
                return;
            }

         */
            //验证账号
            RetCodeEnum authenticateRet = authenticateUser(req);
            String tmpPlayerId = playerCache.getInstance().getIdxByUserId(userId);
            player = playerCache.getByIdx(tmpPlayerId);
            if (RetCodeEnum.RCE_Success != authenticateRet) {
                if (RetCodeEnum.RCE_Login_ServerBusy != authenticateRet || player == null) {
                    builder.setRetCode(GameUtil.buildRetCode(authenticateRet));
                    channel.send(MsgIdEnum.SC_Login_VALUE, builder);
                    return;
                }
            }

            GameServerTcpChannel oldChannel = GlobalData.getInstance().getOnlinePlayerChannel(tmpPlayerId);
            if (oldChannel != null) {
                //存在旧channel先对前一个channel登出
                LogService.getInstance().submit(new LogOutLog(String.valueOf(oldChannel.getPlayerId1())));

                //结算上次在线时长
                LogService.getInstance().submit(new OnLineTimeLog(tmpPlayerId));

                // 顶掉上一个
                String oldAddr = oldChannel.channel.remoteAddress().toString();
                LogUtil.info("KickOut channel, playerId:" + tmpPlayerId + ",oldIp:"
                        + oldAddr + ",newIp:" + addr);
                oldChannel.setPlayerId1(0);
                oldChannel.setPlayerId("");
                SC_KickOut.Builder kickBuilder = SC_KickOut.newBuilder();
                RetCodeEnum retCodeEnum = oldAddr.equals(addr) ? RetCodeEnum.RCE_KickOut_RepeatLoginWithSameIp : RetCodeEnum.RCE_KickOut_RepeatLogin;
                kickBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
                oldChannel.send(MsgIdEnum.SC_KickOut_VALUE, kickBuilder);
                oldChannel.close();
            }


            boolean isNewPlayer = false;
            if (player == null) {
                player = ObjUtil.createPlayer(userId, req.getClientData().getLanguage());
                if (player == null) {
                    builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                    channel.send(MsgIdEnum.SC_Login_VALUE, builder);
                    return;
                }
                player.initNewbiePlayer();
                //玩家初始化宠物
                initNewBee(player.getIdx());
                isNewPlayer = true;
            }

            //判断玩家是否可以登录
            RetCodeEnum retCodeEnum = player.canLogIn();
            if (retCodeEnum != RetCodeEnum.RCE_Success) {
                //发送提示消息
                Common.SC_Tips.Builder tips = Common.SC_Tips.newBuilder();
                tips.setMsg(PlatformManager.getInstance().getBanMsg(player.getBanMsgId(GameConst.Ban.ROLE), player.getLanguage()));
                channel.send(MsgIdEnum.SC_Tips_VALUE, tips);

                builder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
                channel.send(MsgIdEnum.SC_Login_VALUE, builder);
                return;
            }
            channel.setPlayerId1(Long.parseLong(player.getIdx()));
            channel.setPlayerId(userId);
            GlobalData.getInstance().addOnlinePlayer(player.getIdx(), channel);

            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            builder.setServerTime(GlobalTick.getInstance().getCurrentTime());
            builder.setServerTimeZone(TimeUtil.defaultTimeZone);
            channel.send(MsgIdEnum.SC_Login_VALUE, builder);

            Event event = Event.valueOf(EventType.ET_Login, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
            event.pushParam(player.getIdx());
            event.pushParam(req.getIsResume());
            event.pushParam(req.getClientData());
            event.pushParam(isNewPlayer);
            EventManager.getInstance().dispatchEvent(event);

            player.setIp(addr);

            //登陆日志
            LogService.getInstance().submit(new LogInLog(player, req.getClientData(), addr));
            if (isNewPlayer) {
                //统计：创角
                LogService.getInstance().submit(new CreateRoleLog(player.getIdx(), req.getClientData(), addr));
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return;
        }
    }

//    protected boolean checkVersion(String userId, ClientData clientData) {
//        if (ServerConfig.getInstance().isGM() && userId.startsWith("robot")) {
//            return true;
//        }
//        if (StringHelper.isNull(clientData.getClientVersion())) {
//            return false;
//        }
//        ServerVersionObject cfgObj = ServerVersion.getByDevicesystem(clientData.getClientVersionType());
//        if (cfgObj == null || StringHelper.isNull(cfgObj.getVersionstr())) {
//            return false;
//        }
//        return clientData.getClientVersion().equals(cfgObj.getVersionstr());
//    }

    protected RetCodeEnum authenticateUser(CS_Login loginData) {
        if (loginData == null
                || StringHelper.isNull(loginData.getUserId())
                || StringHelper.isNull(loginData.getToken())) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        if (ServerConfig.getInstance().isGM() && loginData.getUserId().startsWith("robot")) {
            return RetCodeEnum.RCE_Success;
        }
        return HttpRequestUtil.login(loginData);
    }

    /**
     * 新手引导宠物
     *
     * @param playerIdx
     */
    private void initNewBee(String playerIdx) {
        if (playerIdx == null) {
            return;
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_NewBee);

        //新手奖励
        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(GameConfig.getById(GameConst.CONFIG_ID).getNewbeerewards());
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, false);

        //新手玩家欢迎邮件
        EventUtil.triggerAddMailEvent(playerIdx, MailTemplateUsed.getById(GameConst.CONFIG_ID).getNewbeewelcome(), null, reason);

        //新手宠物
        Map<Integer, Integer> petMap = Arrays.stream(GameConfig.getById(GameConst.CONFIG_ID).getNewbeepet())
                .boxed().collect(Collectors.toMap(key -> key, value -> 1));
        petCache.getInstance().playerObtainPets(playerIdx, petMap, reason);


        teamEntity team = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        petEntity petByPlayer = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (team == null || petByPlayer == null) {
            return;
        }

        List<Pet> petList = sortByCfgOrder(petByPlayer.getAllPet());
        if (CollectionUtils.isEmpty(petList)) {
            return;
        }

        List<String> successSet = new ArrayList<>();
        SyncExecuteFunction.executeConsumer(team, e -> {
            Team dbTeam = team.getDBTeam(TeamNumEnum.TNE_Team_1);
            if (dbTeam == null) {
                return;
            }
            dbTeam.clearLinkPet();

            for (int i = 0; i < Math.min(petList.size(), team.getDB_Builder().getUnlockPosition()); i++) {
                dbTeam.putLinkPet(i + 1, petList.get(i).getId());
                successSet.add(petList.get(i).getId());
            }
        });
        petCache.getInstance().statisticTeamUpdate(playerIdx, Collections.emptyList(), team.getTeamPetIdxList(TeamNumEnum.TNE_Team_1));
        EventUtil.updatePetTeamState(playerIdx, successSet, true, true);
    }

    /**
     * 将宠物按照配置的顺序重排序
     *
     * @param pets
     * @return
     */
    private List<Pet> sortByCfgOrder(List<Pet> pets) {
        if (CollectionUtils.isEmpty(pets)) {
            return null;
        }

        List<Pet> needSortList = new ArrayList<>(pets);
        List<Pet> result = new ArrayList<>();
        int[] cfgOrder = GameConfig.getById(GameConst.CONFIG_ID).getNewbeepet();
        for (int i = 0; i < cfgOrder.length; i++) {
            for (int j = 0; j < needSortList.size(); j++) {
                if (needSortList.get(j).getPetBookId() == cfgOrder[i]) {
                    Pet remove = needSortList.remove(j);
                    result.add(remove);
                    break;
                }
            }
        }

        return result;
    }
}
