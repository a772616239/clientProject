package model.warpServer.crossServer.handler.thewar;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TheWarRoomInfo;
import protocol.TheWar.SC_TheWarTotalInfo;

@MsgId(msgId = MsgIdEnum.CS_GS_TheWarRoomInfo_VALUE)
public class TheRoomInfoHandler extends AbstractHandler<CS_GS_TheWarRoomInfo> {
    @Override
    protected CS_GS_TheWarRoomInfo parse(byte[] bytes) throws Exception {
        return CS_GS_TheWarRoomInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_TheWarRoomInfo ret, int i) {
        String playerIdx = ret.getPlayerData().getPlayerIdx();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_TheWarTotalInfo.Builder builder = SC_TheWarTotalInfo.newBuilder();
        builder.setRoomIdx(ret.getRoomIdx());
        builder.setMapName(ret.getMapName());
        builder.addAllRoomMembers(ret.getRoomMembersList());
        builder.setPlayerData(ret.getPlayerData());
        builder.addAllOwnedGrids(ret.getOwnedGridsList());
        builder.addAllCollectionPos(ret.getCollectionPosList());
        builder.addAllCampInfo(ret.getCampInfoList());
        builder.setIsFirstEnter(ret.getIsFirstTimeEnter());
        builder.setBuyBackTimes(player.getDb_data().getTheWarData().getDailyBuyBackCount());
        builder.setBuyStamiaTimes(player.getDb_data().getTheWarData().getDailyBuyStaminaCount());
        builder.setCurMission(ret.getCurMission());

        //远征任务
//        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
//        if (entity != null) {
//            SyncExecuteFunction.executeConsumer(entity, e -> {
//                TargetMission mission = entity.getTheWarCurMission();
//                if (mission != null) {
//                    builder.setCurMission(mission);
//                }
//            });
//        }

        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_TheWarTotalInfo_VALUE, builder);
    }
}
