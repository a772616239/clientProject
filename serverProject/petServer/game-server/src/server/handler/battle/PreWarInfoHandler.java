package server.handler.battle;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar;





import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_PreWarInfo_VALUE)
public class PreWarInfoHandler extends AbstractBaseHandler<PrepareWar.CS_PreWarInfo> {

    @Override
    protected PrepareWar.CS_PreWarInfo parse(byte[] bytes) throws Exception {
        return PrepareWar.CS_PreWarInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PrepareWar.CS_PreWarInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.debug("PreWarInfoHandler, playerIdx[ " + playerIdx + "Entity is null");
            return;
        }
        PrepareWar.SC_PreWarInfo.Builder result = BattleManager.getInstance().buildPreWarInfo(playerIdx, req);
        gsChn.send(MsgIdEnum.SC_PreWarInfo_VALUE, result);
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
