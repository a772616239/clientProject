package server.handler.newbee;

import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Newbee.CS_NewBeeName;
import protocol.Newbee.SC_NewBeeName;
import protocol.Newbee.SC_NewBeeName.Builder;
import protocol.PlayerDB.DB_NewBee;
import protocol.PlayerDB.DB_PlayerData;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_NewBeeName_VALUE)
public class NewBeeNameHandler extends AbstractBaseHandler<CS_NewBeeName> {
    @Override
    protected CS_NewBeeName parse(byte[] bytes) throws Exception {
        return CS_NewBeeName.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_NewBeeName req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        Builder resultBuilder = SC_NewBeeName.newBuilder();
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
            return;
        }

        String name = req.getName();
        RetCodeEnum retCodeEnum = PlayerUtil.checkName(name);
        if (retCodeEnum != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
            gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
            return;
        }

        if (req.getSex() != GameConst.PlayerSex.Male.getCode()
                && req.getSex() != GameConst.PlayerSex.Female.getCode()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            DB_PlayerData.Builder db_data = player.getDb_data();
            player.setSex(req.getSex());
            if (db_data == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
                return;
            }

            DB_NewBee.Builder newBeeInfoBuilder = db_data.getNewBeeInfoBuilder();
            if (newBeeInfoBuilder.getNewBeeName()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_NewBee_RepeatedName));
                gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
                return;
            }

            player.setName(name);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
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
