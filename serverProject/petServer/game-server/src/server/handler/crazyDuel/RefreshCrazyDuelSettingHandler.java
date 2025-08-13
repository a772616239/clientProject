package server.handler.crazyDuel;

import cfg.CrazyDuelCfg;
import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import common.AbstractBaseHandler;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.entity.CrazyDuelPlayerDB;
import model.crossarena.CrossArenaManager;
import protocol.Common;
import protocol.CrayzeDuel.CS_RefreshCrazyDuelTeamSetting;
import protocol.CrayzeDuel.SC_RefreshCrazyDuelTeamSetting;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_RefreshCrazyDuelTeamSetting_VALUE)
public class RefreshCrazyDuelSettingHandler extends AbstractBaseHandler<CS_RefreshCrazyDuelTeamSetting> {
    @Override
    protected CS_RefreshCrazyDuelTeamSetting parse(byte[] bytes) throws Exception {
        return CS_RefreshCrazyDuelTeamSetting.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RefreshCrazyDuelTeamSetting req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        SC_RefreshCrazyDuelTeamSetting.Builder msg = SC_RefreshCrazyDuelTeamSetting.newBuilder();

        RetCodeId.RetCodeEnum codeEnum = checkCanRefresh(playerIdx);
        if (RetCodeId.RetCodeEnum.RCE_Success==codeEnum) {
            msg.addAllSettings(CrazyDuelManager.getInstance().refreshPlayerSetting(playerIdx));
            CrazyDuelManager.getInstance().incrPlayerRefreshTime(playerIdx);
        }
        msg.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MessageId.MsgIdEnum.SC_RefreshCrazyDuelTeamSetting_VALUE, msg);
    }

    private RetCodeId.RetCodeEnum checkCanRefresh(String playerIdx) {
        CrazyDuelPlayerDB playerDb = CrazyDuelManager.getInstance().findPlayerDb(playerIdx);
        if (playerDb==null){
            return RetCodeId.RetCodeEnum.RCE_UnknownError;
        }
        int gradeLv = CrossArenaManager.getInstance().findPlayerGradeLv(playerIdx);
        if (gradeLv< CrazyDuelCfg.getById(GameConst.CONFIG_ID).getRefreshunlockgradelv()){
            return RetCodeId.RetCodeEnum.RCE_FunctionIsLock;
        }
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(gradeLv);
        if (cfg==null){
            return RetCodeId.RetCodeEnum.RSE_ConfigNotExist;
        }
       if (CrazyDuelManager.getInstance().findPlayerRefreshTime(playerIdx)>=cfg.getCrazyduelcanrefresh()){
           return RetCodeId.RetCodeEnum.RCE_CrazyDuel_RefreshTimeUseOut;
       }
        return RetCodeId.RetCodeEnum.RCE_Success;
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_RefreshCrazyDuelTeamSetting_VALUE, SC_RefreshCrazyDuelTeamSetting.newBuilder());
    }
}
