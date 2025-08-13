package server.handler.crazyDuel;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.CrazyDuelOpenManager;
import model.player.util.PlayerUtil;
import org.springframework.util.StringUtils;
import protocol.Common;
import protocol.CrayzeDuel.CS_CrazyDuelInit;
import protocol.CrayzeDuel.SC_CrazyDuelInit;
import protocol.CrazyDuelDB;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_CrazyDuelInit_VALUE)
public class CrazyDuelInitHandler extends AbstractBaseHandler<CS_CrazyDuelInit> {
    @Override
    protected CS_CrazyDuelInit parse(byte[] bytes) throws Exception {
        return CS_CrazyDuelInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrazyDuelInit req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        SC_CrazyDuelInit.Builder msg = SC_CrazyDuelInit.newBuilder();
        if (!CrazyDuelOpenManager.getInstance().isOpen()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_CrazyDuelInit_VALUE, msg);
            return;
        }
        if (PlayerUtil.queryFunctionLock(playerIdx, Common.EnumFunction.LtCrazyDuel)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MessageId.MsgIdEnum.SC_CrazyDuelInit_VALUE, msg);
            return;
        }

        CrazyDuelDB.CrazyDuelSettingDB playerData = CrazyDuelManager.getInstance().findOrInitPlayerData(playerIdx);
        msg.setPublish(playerData.getPublish());
        msg.setRefreshTimes(CrazyDuelManager.getInstance().findPlayerRefreshTime(playerIdx));
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        msg.setScore(CrazyDuelManager.getInstance().findPlayerScore(playerIdx));
        gsChn.send(MessageId.MsgIdEnum.SC_CrazyDuelInit_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.LtCrazyDuel;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_CrazyDuelInit_VALUE, SC_CrazyDuelInit.newBuilder().setRetCode(retCode));
    }
}
