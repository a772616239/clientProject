package server.handler.thewar;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import platform.logs.LogService;
import platform.logs.entity.GamePlayLog;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.CS_EnterTheWar;
import protocol.TheWar.SC_EnterTheWar;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_EnterTheWar_VALUE)
public class EnterTheWarHandler extends AbstractBaseHandler<CS_EnterTheWar> {
    @Override
    protected CS_EnterTheWar parse(byte[] bytes) throws Exception {
        return CS_EnterTheWar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_EnterTheWar req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        TheWarRetCode retCode = TheWarManager.getInstance().enterWarRoom(player, req.getIsResume());
        if (retCode != TheWarRetCode.TWRC_Success) {
            SC_EnterTheWar.Builder retBuilder = SC_EnterTheWar.newBuilder();
            retBuilder.setRetCode(retCode);
            gsChn.send(MsgIdEnum.SC_EnterTheWar_VALUE, retBuilder);
        }

        LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.TheWar));
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_EnterTheWar_VALUE,
                SC_EnterTheWar.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
