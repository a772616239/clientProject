package server.handler.player;

import protocol.Common.EnumFunction;

import hyzNet.GameServerTcpChannel;
import common.AbstractBaseHandler;
import hyzNet.message.MsgId;
import protocol.LoginProto.CS_CreateRole;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_CreateRole_VALUE)
public class CreateRoleHandler extends AbstractBaseHandler<CS_CreateRole> {
    @Override
    protected CS_CreateRole parse(byte[] msgByte) throws Exception {
        return CS_CreateRole.parseFrom(msgByte);
    }

    @Override
    protected void execute(GameServerTcpChannel gsTcpChannel, CS_CreateRole req, int i) {
//        LogUtil.info("recv createRole msg from " + gsTcpChannel.channel.remoteAddress());
//        if (StringHelper.isNull(gsTcpChannel.getPlayerId())) {
//            return;
//        }
//        String newName = req.getName();
//        SC_CreateRole.Builder builder = SC_CreateRole.newBuilder();
//        if (playerCache.getInstance().getIdxByName(newName) != null) {
//            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CreateRole_DuplicateName));
//            gsTcpChannel.send(MsgIdEnum.SC_CreateRole_VALUE, builder);
//            return;
//        }
//        String id = IdGenerator.getInstance().generateId();
//        playerEntity newPlayer = ObjUtil.createPlayer(id);
//        if (newPlayer == null) {
//            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CreateRole_ErrorOrder));
//            gsTcpChannel.send(MsgIdEnum.SC_CreateRole_VALUE, builder);
//            return;
//        }
//        newPlayer.setName(newName);
//        newPlayer.setUserid(gsTcpChannel.getPlayerId());
//        newPlayer.setAvatar(req.getAvatar());
//        newPlayer.initNewbiePlayer();
//        LogUtil.info("create player id =" + id + ",userId=" + gsTcpChannel.getPlayerId());
//        playerCache.getInstance().put(newPlayer);
//        GlobalData.getInstance().addOnlinePlayer(newPlayer.getIdx(), gsTcpChannel);
//        //==================================huhan============================
//        BagCapacityManager.getInstance().createNewCapacity(newPlayer.getIdx(),true);
//        ItemBagManager.getInstance().createNewItemBagEntity(newPlayer.getIdx());
//        MailBoxManager.getInstance().createNewMailBox(newPlayer.getIdx());
//        MailBoxManager.getInstance().test(newPlayer.getIdx());
//
//        //===============================================================
//
//        Event event = Event.valueOf(EnvetType.ET_Login, GameUtil.getDefaultEventSource(), newPlayer);
//        event.pushParam(false);
//        event.pushParam(req.getClientData());
//        EventManager.getInstance().dispatchEvent(event);
//
//        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
//        gsTcpChannel.send(MsgIdEnum.SC_CreateRole_VALUE, builder);

    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
