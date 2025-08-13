package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_RequestMistBaseInfo;
import protocol.MistForest.SC_UpdateMistBaseInfo;
import protocol.PlayerDB.DB_MistForestData;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_RequestMistBaseInfo_VALUE)
public class RequestMistBaseInfoHandler extends AbstractBaseHandler<CS_RequestMistBaseInfo> {
    @Override
    protected CS_RequestMistBaseInfo parse(byte[] bytes) throws Exception {
        return CS_RequestMistBaseInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RequestMistBaseInfo req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        DB_MistForestData mistData = player.getDb_data().getMistForestData();
        if (mistData == null) {
            return;
        }
        player.sendMistBaseData();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_UpdateMistBaseInfo_VALUE, SC_UpdateMistBaseInfo.newBuilder());
    }
}
