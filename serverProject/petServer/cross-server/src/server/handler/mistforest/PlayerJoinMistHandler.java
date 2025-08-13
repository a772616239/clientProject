package server.handler.mistforest;

import common.GlobalData;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.cache.MistRoomCache;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import model.warpServer.WarpServerConst;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistVipSkillData;
import protocol.ServerTransfer.CS_GS_JoinMistForest;
import protocol.ServerTransfer.GS_CS_JoinMistForest;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_JoinMistForest_VALUE)
public class PlayerJoinMistHandler extends AbstractHandler<GS_CS_JoinMistForest> {
    @Override
    protected GS_CS_JoinMistForest parse(byte[] bytes) throws Exception {
        return GS_CS_JoinMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_JoinMistForest req, int i) {
        try {
            long startTime = System.currentTimeMillis();
            String ip = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));

            String playerIdx = req.getPlayerBaseData().getPlayerId();
            int serverIndex = req.getServerIndex();
            LogUtil.info("recv join CS msg from ip={},serverIndex={},idx={},name=",
                    ip, serverIndex, playerIdx, req.getPlayerBaseData().getPlayerName());
            if (serverIndex <= 0 || StringHelper.isNull(playerIdx)) {
                return;
            }

            GameServerTcpChannel chn = GlobalData.getInstance().getServerChannel(serverIndex);
            if (chn == null) {
                return;
            }
            CS_GS_JoinMistForest.Builder joinBuilder = CS_GS_JoinMistForest.newBuilder();
            joinBuilder.setPlayerId(playerIdx);
            joinBuilder.setMistForestLevel(req.getMistForestLevel());
            joinBuilder.setJoinType(req.getJoinType());
            joinBuilder.setMistRule(req.getMistRule());

            if (req.getMistRule() == EnumMistRuleKind.EMRK_Maze && !MistRoomCache.getInstance().getMazeDataManager().isMazeOpen()) {
                joinBuilder.setRetCode(MistRetCode.MRC_MazeActivityNotOpen);
                chn.send(MsgIdEnum.CS_GS_JoinMistForest_VALUE, joinBuilder);
                return;
            }

            MistPlayer player = MistPlayerCache.getInstance().queryObject(playerIdx);
            MistRoom mistRoom = null;
            if (player != null) {
                mistRoom = player.getMistRoom();
                if (mistRoom != null && mistRoom.getLevel() != req.getMistForestLevel()) {
                    SyncExecuteFunction.executeConsumer(mistRoom, room -> room.forceKickPlayer(playerIdx));
                    SyncExecuteFunction.executeConsumer(player, entity -> entity.onPlayerLogout(false));
                    mistRoom = null;
                }
            } else {
                player = MistPlayerCache.getInstance().createObject(playerIdx);
            }

            player.onPlayerLogin(req, false);
            if (mistRoom == null) {
                mistRoom = MistRoomCache.getInstance().getFitMistRoom(req.getMistRuleValue(), req.getMistForestLevel());
                if (mistRoom == null) {
                    joinBuilder.setRetCode(MistRetCode.MRC_NoFoundMistForest);
                    chn.send(MsgIdEnum.CS_GS_JoinMistForest_VALUE, joinBuilder);
                    return;
                }
                MistRoomCache.getInstance().manageObject(mistRoom);
            }

            MistFighter fighter = SyncExecuteFunction.executeBitFunction(
                    mistRoom, player, (room, ply) -> room.onPlayerJoin(ply, req));
            if (fighter == null) {
                joinBuilder.setRetCode(MistRetCode.MRC_OtherError);
                chn.send(MsgIdEnum.CS_GS_JoinMistForest_VALUE, joinBuilder);
                return;
            }
            player.initByMistFighter(fighter);
            MistPlayerCache.getInstance().manageObject(player);
            SyncExecuteFunction.executeConsumer(mistRoom, room -> room.getWorldMap().objFirstEnter(fighter));
            List<MistVipSkillData> vipSkillList = fighter.getSkillMachine().buildAllVipSkillData();
            if (vipSkillList != null) {
                joinBuilder.addAllVipSkillData(vipSkillList);
            }
            joinBuilder.setRetCode(MistRetCode.MRC_Success);
            chn.send(MsgIdEnum.CS_GS_JoinMistForest_VALUE, joinBuilder);
            LogUtil.info("player join costTime:" + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
