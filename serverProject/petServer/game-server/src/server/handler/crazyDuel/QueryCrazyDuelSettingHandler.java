package server.handler.crazyDuel;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crazyDuel.CrazyDuelManager;
import protocol.Common;
import protocol.CrayzeDuel.CS_QueryCrazyDuelTeamSetting;
import protocol.CrayzeDuel.SC_QueryCrazyDuelTeamSetting;
import protocol.CrazyDuelDB.CrazyDuelSettingDB;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_QueryCrazyDuelTeamSetting_VALUE)
public class QueryCrazyDuelSettingHandler extends AbstractBaseHandler<CS_QueryCrazyDuelTeamSetting> {
    @Override
    protected CS_QueryCrazyDuelTeamSetting parse(byte[] bytes) throws Exception {
        return CS_QueryCrazyDuelTeamSetting.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryCrazyDuelTeamSetting req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        SC_QueryCrazyDuelTeamSetting.Builder msg = SC_QueryCrazyDuelTeamSetting.newBuilder();
        CrazyDuelSettingDB playerSetting = CrazyDuelManager.getInstance().findPlayerSetting(playerIdx);
        if (playerSetting!=null) {
            msg.addAllSettings(playerSetting.getBuffSettingMap().values());
            msg.addAllMaps(playerSetting.getPetPosList());
        }
        gsChn.send(MessageId.MsgIdEnum.SC_QueryCrazyDuelTeamSetting_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_QueryCrazyDuelTeamSetting_VALUE, SC_QueryCrazyDuelTeamSetting.newBuilder());
    }
}
