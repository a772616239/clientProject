package model.warpServer.battleServer.handler;

import common.GameConst.EventType;
import common.GlobalData;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import model.mistforest.MistConst.MistBattleSide;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetBuffData;
import protocol.Battle.PlayerExtDataDict;
import protocol.Battle.PlayerExtDataEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_CS_ReplyPvpBattle;
import protocol.ServerTransfer.CS_GS_EnterMistPvpBattle;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import protocol.ServerTransfer.ReplyPvpBattleData;
import server.event.Event;
import server.event.EventManager;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.BS_CS_ReplyPvpBattle_VALUE)
public class ApplyBattleResultHandler extends AbstractHandler<BS_CS_ReplyPvpBattle> {
    @Override
    protected BS_CS_ReplyPvpBattle parse(byte[] bytes) throws Exception {
        return BS_CS_ReplyPvpBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_CS_ReplyPvpBattle req, int i) {
        if (req.getReplyPvpBattleData().getPlayerListCount() <= 0) {
            return;
        }
        String addr = gsChn.channel.remoteAddress().toString().substring(1);
        int serverIndex = BattleServerManager.getInstance().getServerIndexByAddr(addr);
        if (req.getReplyPvpBattleData().getSubBattleType() == BattleSubTypeEnum.BSTE_MistForest) {
            handleMistForestApplyResult(req.getReplyPvpBattleData(), serverIndex);
        } else if (req.getReplyPvpBattleData().getSubBattleType() == BattleSubTypeEnum.BSTE_MineFight) {
            handleMineFightApplyResult(req.getReplyPvpBattleData(), serverIndex);
        }
    }

    private void handleMistForestApplyResult(ReplyPvpBattleData req, int battleSvrIndex) {
        if (!req.getResult()) {
            // 失败清除被碰撞标记
            for (PvpBattlePlayerInfo plyData : req.getPlayerListList()) {
                MistPlayer player = MistPlayerCache.getInstance().queryObject(plyData.getPlayerInfo().getPlayerId());
                if (player == null || player.getMistRoom() == null) {
                    continue;
                }
                Event event = Event.valueOf(EventType.ET_EnterMistPvpBattle, player, player.getMistRoom());
                event.pushParam(false);
                event.pushParam(0);
                event.pushParam(0);
                event.pushParam(0);
            }
            return;
        }
        CS_GS_EnterMistPvpBattle.Builder builder = CS_GS_EnterMistPvpBattle.newBuilder();
        builder.setBattleId(req.getBattleId());
        builder.setRandSeed(req.getRandSeed());
        builder.setFightMakeId(req.getFightMakeId());
        builder.setBattleSvrIndex(battleSvrIndex);
        List<MistPlayer> playerList = new ArrayList<>();
        long camp1FighterId = 0;
        long camp2FighterId = 0;
        for (PvpBattlePlayerInfo plyData : req.getPlayerListList()) {
            MistPlayer player = MistPlayerCache.getInstance().queryObject(plyData.getPlayerInfo().getPlayerId());
            if (player == null || player.getMistRoom() == null) {
                continue;
            }
            playerList.add(player);
            BattlePlayerInfo.Builder battlePlyInfo = BattlePlayerInfo.newBuilder();
            battlePlyInfo.setPlayerInfo(player.builderBaseInfo());
            battlePlyInfo.setCamp(plyData.getCamp());
            battlePlyInfo.addAllPetList(player.getPetDataList());
            battlePlyInfo.addAllPlayerSkillIdList(player.getSkillList());

            PlayerExtDataDict.Builder playerExtData = PlayerExtDataDict.newBuilder();
            playerExtData.addKeys(PlayerExtDataEnum.PEDE_ServerIndex);
            playerExtData.addValues(player.getServerIndex());

            battlePlyInfo.setPlayerExtData(playerExtData);
            builder.addPlayerInfo(battlePlyInfo);

            List<PetBuffData> extBuff = player.getExtendBuffList();
            if (extBuff != null) {
                ExtendProperty.Builder builder1 = ExtendProperty.newBuilder();
                builder1.setCamp(plyData.getCamp());
                builder1.addAllBuffData(extBuff);
                builder.addExtendProp(builder1);
            }

            if (plyData.getCamp() == 1) {
                camp1FighterId = player.getFighterId();
            } else if (plyData.getCamp() == 2) {
                camp2FighterId = player.getFighterId();
            }
        }

        if (camp1FighterId == 0 || camp2FighterId == 0) {
            LogUtil.error("Apply mist battle camp error");
            return;
        }
        for (MistPlayer player : playerList) {
            for (PvpBattlePlayerInfo plyData : req.getPlayerListList()) {
                if (player.getIdx().equals(plyData.getPlayerInfo().getPlayerId())) {
                    builder.setPlayerIdx(player.getIdx());
                    builder.setCamp(plyData.getCamp());
                    break;
                }
            }
            GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_EnterMistPvpBattle_VALUE, builder);
            if (player.getMistRoom() == null) {
                continue;
            }
            Event event = Event.valueOf(EventType.ET_EnterMistPvpBattle, player, player.getMistRoom());
            event.pushParam(true);
            if (builder.getCamp() == 1) {
                event.pushParam(camp2FighterId);
                event.pushParam(camp1FighterId);
                event.pushParam(MistBattleSide.leftSide);
            } else if (builder.getCamp() == 2) {
                event.pushParam(camp1FighterId);
                event.pushParam(camp2FighterId);
                event.pushParam(MistBattleSide.rightSide);
            }
            EventManager.getInstance().dispatchEvent(event);
        }
    }

    private void handleMineFightApplyResult(ReplyPvpBattleData req, int battleSvrIndex) {
//        if (req.getParamsCount() <= 0) {
//            return;
//        }
//        MineObj mine = MineObjCache.getByIdx(req.getParams(0));
//        if (mine == null) {
//            return;
//        }
//        long curTime = GlobalTick.getInstance().getCurrentTime();
//        if (req.getResult()) {
//            CS_GS_EnterMinePvpFight.Builder builder = CS_GS_EnterMinePvpFight.newBuilder();
//            builder.setMineId(mine.getIdx());
//            builder.setBattleId(req.getBattleId());
//            builder.setRandSeed(req.getRandSeed());
//            builder.setFightMakeId(req.getFightMakeId());
//            builder.setBattleIp(battleIp);
//            for (PvpBattlePlayerInfo plyData : req.getPlayerListList()) {
//                BattlePlayerInfo.Builder battlePlyInfo = BattlePlayerInfo.newBuilder();
//                battlePlyInfo.setPlayerInfo(plyData.getPlayerInfo());
//                battlePlyInfo.setCamp(plyData.getCamp());
//                if ((plyData.getCamp() == 1 && mine.getAttackerPlayerId().equals(plyData.getPlayerInfo().getPlayerId()))
//                        || (plyData.getCamp() == 2 && mine.getOwnerPlayerId().equals(plyData.getPlayerInfo().getPlayerId()))) {
//                    battlePlyInfo.addAllPetList(plyData.getPetListList());
//                    battlePlyInfo.addAllPlayerSkillIdList(plyData.getPlayerSkillIdListList());
//                    battlePlyInfo.addAllFriendHelpPets(plyData.getFriendPetsList());
//                }
//                builder.addPlayerInfo(battlePlyInfo);
//            }
//            for (PvpBattlePlayerInfo plyData : req.getPlayerListList()) {
//                builder.setPlayerIdx(plyData.getPlayerInfo().getPlayerId());
//                builder.setCamp(plyData.getCamp());
//                GlobalData.getInstance().sendMsgToServer(plyData.getFromSvrIp(), MsgIdEnum.CS_GS_EnterMinePvpFight_VALUE, builder);
//            }
//            SyncExecuteFunction.executeConsumer(mine, mineObj -> {
//                mineObj.changeMineState(curTime, EnumMineState.EMS_UnderAttack_VALUE);
//                mineObj.setPvpflag(true);
//            });
//        } else {
//            SyncExecuteFunction.executeConsumer(mine, mineObj -> mineObj.enterPveBattle(curTime));
//        }
    }
}
