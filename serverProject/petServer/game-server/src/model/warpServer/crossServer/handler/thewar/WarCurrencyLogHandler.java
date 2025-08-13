package model.warpServer.crossServer.handler.thewar;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import platform.logs.LogService;
import platform.logs.entity.thewar.TheWarCurrencyLog;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TheWarCurrencyLog;
import protocol.ServerTransfer.CS_GS_TheWarCurrencyLog.CurrencyLogData;
import protocol.TheWarDefine.TheWarResourceType;

@MsgId(msgId = MsgIdEnum.CS_GS_TheWarCurrencyLog_VALUE)
public class WarCurrencyLogHandler extends AbstractHandler<CS_GS_TheWarCurrencyLog> {
    @Override
    protected CS_GS_TheWarCurrencyLog parse(byte[] bytes) throws Exception {
        return CS_GS_TheWarCurrencyLog.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_TheWarCurrencyLog ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        for (CurrencyLogData logData : ret.getLogDataList()) {
            String currencyType = null;
            if (logData.getCurrencyType() == TheWarResourceType.TWRT_WarGold) {
                currencyType = "远征币";
            } else if (logData.getCurrencyType() == TheWarResourceType.TWRT_WarDoorPoint) {
                currencyType = "远古水晶";
            }
            if (currencyType != null) {
                LogService.getInstance().submit(new TheWarCurrencyLog(player, TheWarManager.getInstance().getMapName(), logData.getConsume(), currencyType, logData.getBeforeAmount(), logData.getAmount(), logData.getReason()));
            }
        }
    }
}
