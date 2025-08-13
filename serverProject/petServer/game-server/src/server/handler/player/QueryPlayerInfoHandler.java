package server.handler.player;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.recentpassed.RecentPassedUtil;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_QueryPlayerInfo;
import protocol.PlayerInfo.SC_QueryPlayerInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RecentPassedOuterClass.RecentPassed;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_QueryPlayerInfo_VALUE)
public class QueryPlayerInfoHandler extends AbstractBaseHandler<CS_QueryPlayerInfo> {
    @Override
    protected CS_QueryPlayerInfo parse(byte[] bytes) throws Exception {
        return CS_QueryPlayerInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryPlayerInfo req, int i) {
        String queryPlayerIdx = req.getQueryPlayerIdx();

        RecentPassed recentPassed = RecentPassedUtil.buildRecentPassedInfo(queryPlayerIdx, TeamNumEnum.TNE_Team_1);
        SC_QueryPlayerInfo.Builder resultBuilder = SC_QueryPlayerInfo.newBuilder();
        if (recentPassed == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_QueryPlayerNotExist));
        } else {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setPlayerInfo(recentPassed);
        }
        gsChn.send(MsgIdEnum.SC_QueryPlayerInfo_VALUE, resultBuilder);

//        playerEntity queryPlayer = playerCache.getByIdx(queryPlayerIdx);
//        SC_QueryPlayerInfo.Builder resultBuilder = SC_QueryPlayerInfo.newBuilder();
//        if (queryPlayer == null) {
//            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_QueryPlayerNotExist));
//            gsChn.send(MsgIdEnum.SC_QueryPlayerInfo_VALUE, resultBuilder);
//            return;
//        }

//        SyncExecuteFunction.executeConsumer(queryPlayer, entity -> {
//            DB_PlayerData.Builder db_data = queryPlayer.getDb_data();
//            if (db_data == null) {
//                LogUtil.error("QueryPlayerInfoHandler.builderQueryInfo, playerIdx[" + queryPlayerIdx + " dbData is null");
//                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
//                gsChn.send(MsgIdEnum.SC_QueryPlayerInfo_VALUE, resultBuilder);
//                return;
//            }
//            QueryPlayerInfo.Builder builder = QueryPlayerInfo.newBuilder();
//            builder.setPlayerIdx(queryPlayer.getIdx());
//            builder.setShortId(queryPlayer.getShortid());
//            builder.setPlayerName(queryPlayer.getName());
//            builder.setPlayerLv(queryPlayer.getLevel());
//            builder.setAvatarId(queryPlayer.getAvatar());
//            builder.setAvatarBorder(queryPlayer.getDb_data().getCurAvatarBorder());
//            if (builder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
//                builder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(queryPlayer.getIdx()));
//            }
//            builder.setVipLv(queryPlayer.getVip());
//            Map<Integer, String> displayPetMap = queryPlayer.getDb_data().getDisplayPetMap();
//            if (displayPetMap != null || queryPlayer.getDb_data().getDisplayPetCount() > 0) {
//                for (Entry<Integer, String> entry : displayPetMap.entrySet()) {
//                    Pet petById = model.pet.dbCache.petCache.getInstance().getPetById(queryPlayer.getIdx(), entry.getValue());
//                    if (petById == null) {
//                        continue;
//                    }
//                    builder.addPets(petById);
//                }
//            }
//            builder.setFightingCapacity(petCache.getInstance().totalAbility(queryPlayer.getIdx()));
//            builder.setTitleId(queryPlayer.getTitleId());
//            builder.setNewTitle(queryPlayer.getCurEquipNewTitleId());
//
//            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
//            resultBuilder.setQueryInfo(builder);
//            gsChn.send(MsgIdEnum.SC_QueryPlayerInfo_VALUE, resultBuilder);
//        });
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


}
