package server.handler.player;

import cfg.HeadBorder;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.arena.ArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.AvatarBorderInfo;
import protocol.PlayerInfo.CS_ChangeAvatarBorder;
import protocol.PlayerInfo.SC_ChangeAvatarBorder;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ChangeAvatarBorder_VALUE)
public class ChangeAvatarBorderHandler extends AbstractBaseHandler<CS_ChangeAvatarBorder> {
    @Override
    protected CS_ChangeAvatarBorder parse(byte[] bytes) throws Exception {
        return CS_ChangeAvatarBorder.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChangeAvatarBorder req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        SC_ChangeAvatarBorder.Builder builder = SC_ChangeAvatarBorder.newBuilder();

        if(player == null){
            LogUtil.error("ChangeAvatarBorderHandler, playerIdx[" + playerIdx + "] is null");
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ChangeAvatarBorder_VALUE, builder);
            return;
        }
        if (HeadBorder.getById(req.getNewAvatarBorder()) == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MsgIdEnum.SC_ChangeAvatarBorder_VALUE, builder);
            return;
        }

        AvatarBorderInfo.Builder avatarBorderBuilder = null;
        for (AvatarBorderInfo.Builder avatarBorder : player.getDb_data().getAvatarBordersBuilderList()) {
            if (avatarBorder.getAvatarBorderId() == req.getNewAvatarBorder()) {
                avatarBorderBuilder = avatarBorder;
                break;
            }
        }
        if (avatarBorderBuilder == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_AvatarBorderLocked));
            gsChn.send(MsgIdEnum.SC_ChangeAvatarBorder_VALUE, builder);
            return;
        }

        if (avatarBorderBuilder.getExpireTime() != -1
                && GlobalTick.getInstance().getCurrentTime() > avatarBorderBuilder.getExpireTime()) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_AvatarBorderExpire));
            gsChn.send(MsgIdEnum.SC_ChangeAvatarBorder_VALUE, builder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, ply -> ply.getDb_data().setCurAvatarBorder(req.getNewAvatarBorder()));
        builder.setNewAvatarBorder(req.getNewAvatarBorder());
        if (req.getNewAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            builder.setNewAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(playerIdx));
        }
        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ChangeAvatarBorder_VALUE, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
