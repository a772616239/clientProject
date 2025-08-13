package server.handler.player;

import protocol.Common.EnumFunction;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import common.AbstractBaseHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerInfo.CS_ChangeAvatar;
import protocol.PlayerInfo.SC_ChangeAvatar;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;
import static protocol.RetCodeId.RetCodeEnum.RCE_Player_NotOwnedAvatar;

@MsgId(msgId = MsgIdEnum.CS_ChangeAvatar_VALUE)
public class ChangeAvatarHandler extends AbstractBaseHandler<CS_ChangeAvatar> {
    @Override
    protected CS_ChangeAvatar parse(byte[] bytes) throws Exception {
        return CS_ChangeAvatar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChangeAvatar req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int newAvatarId = req.getNewAvatarId();
        playerEntity player = playerCache.getByIdx(playerIdx);
        SC_ChangeAvatar.Builder resultBuilder = SC_ChangeAvatar.newBuilder();
        if(player == null){
            LogUtil.error("ChangeAvatarHandler, playerIdx[" + playerIdx + "] is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ChangeAvatar_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_PlayerData.Builder dbPlayerData = player.getDb_data();
            if(dbPlayerData == null){
                LogUtil.error("ChangeAvatarHandler, playerIdx[" + playerIdx + "] DBData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ChangeAvatar_VALUE, resultBuilder);
                return;
            }

            boolean owned = false;
            List<Integer> avatarListList = dbPlayerData.getAvatarListList();
            if(avatarListList.size() > 0){
                for (Integer avatarCfgId : avatarListList) {
                    if(avatarCfgId.equals(newAvatarId)){
                        owned = true;
                        break;
                    }
                }
            }

            if(!owned){
                LogUtil.info("ChangeAvatarHandler, playerIdx[" + playerIdx + "] , avatar is not owned, avatarCfgId = " + newAvatarId);
                SC_ChangeAvatar.Builder builder = SC_ChangeAvatar.newBuilder();
                builder.setRetCode(GameUtil.buildRetCode(RCE_Player_NotOwnedAvatar));
                gsChn.send(MsgIdEnum.SC_ChangeAvatar_VALUE, builder);
                return;
            }

            player.setAvatar(newAvatarId);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ChangeAvatar_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
