package model.warpServer.crossServer.handler.thewar;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TransGMCommand;

@MsgId(msgId = MsgIdEnum.CS_GS_TransGMCommand_VALUE)
public class TheWarGmRetHandler extends AbstractHandler<CS_GS_TransGMCommand> {
    @Override
    protected CS_GS_TransGMCommand parse(byte[] bytes) throws Exception {
        return CS_GS_TransGMCommand.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_TransGMCommand ret, int i) {
//        try {
//            playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
//            if (player == null) {
//                return;
//            }
//            switch (ret.getGmCmdTypeValue()) {
//                case TheWarGmType.TWGT_CreateWarRoom_VALUE: {
//                    if (ret.getResult()) {
//                        CS_GS_CreateWarRoomGmCmd gmCmd = CS_GS_CreateWarRoomGmCmd.parseFrom(ret.getGmParam());
//                        SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().setTheWarRoomIdx(gmCmd.getRoomIdx()));
//                    }
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            LogUtil.printStackTrace(e);
//        }
    }
}
