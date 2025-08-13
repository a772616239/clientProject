package server.handler.collection;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Collection.CS_CollectionLvUp;
import protocol.Collection.SC_CollectionLvUp;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 处理客户端请求图鉴收集进度
 *
 * @author xiao_FL
 * @date 2019/9/6
 */
@MsgId(msgId = MsgIdEnum.CS_CollectionLvUp_VALUE)
public class CollectionLvUpHandler extends AbstractBaseHandler<CS_CollectionLvUp> {

    @Override
    protected CS_CollectionLvUp parse(byte[] bytes) throws Exception {
        return CS_CollectionLvUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CollectionLvUp req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_CollectionLvUp.Builder resultBuilder = SC_CollectionLvUp.newBuilder();
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_CollectionLvUp_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, e -> {
            player.settleCollectionExp();
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_CollectionLvUp_VALUE, resultBuilder);
            player.sendPetCollectionUpdate();
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetCollect;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CollectionLvUp_VALUE, SC_CollectionLvUp.newBuilder().setResult(retCode));
    }


}
