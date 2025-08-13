package server.handler.player;

import protocol.Common.EnumFunction;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_FinishedWatchCG;
import common.AbstractBaseHandler;

/**
 * @author huhan
 * @date 2020/04/14
 */
@MsgId(msgId = MsgIdEnum.CS_FinishedWatchCG_VALUE)
public class FinishedWatchCGHandler extends AbstractBaseHandler<CS_FinishedWatchCG> {
    @Override
    protected CS_FinishedWatchCG parse(byte[] bytes) throws Exception {
        return CS_FinishedWatchCG.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_FinishedWatchCG req, int i) {
        playerEntity player = playerCache.getByIdx(String.valueOf(gsChn.getPlayerId1()));
        if (null != player) {
            SyncExecuteFunction.executeConsumer(player, p -> p.getDb_data().setFinishedWatchCG(true));
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
