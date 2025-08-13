package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import model.cp.entity.CpTeamMember;
import protocol.Battle;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_QueryCpPlayerDetail;
import protocol.CpFunction.SC_QueryCpPlayerDetail;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

/**
 * 查询组队玩家详情
 */
@MsgId(msgId = MsgIdEnum.CS_QueryCpPlayerDetail_VALUE)
public class QueryCpPlayerDetailHandler extends AbstractBaseHandler<CS_QueryCpPlayerDetail> {
    @Override
    protected CS_QueryCpPlayerDetail parse(byte[] bytes) throws Exception {
        return CS_QueryCpPlayerDetail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryCpPlayerDetail req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_QueryCpPlayerDetail.Builder msg = SC_QueryCpPlayerDetail.newBuilder();
        CpTeamMember playerInfo = CpTeamManger.getInstance().findPlayerInfo(req.getPlayerIdx());
        if (playerInfo == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_QueryPlayerNotExist));
            gsChn.send(MsgIdEnum.SC_QueryCpPlayerDetail_VALUE, msg);
            return;
        }
        setPlayerInfo(playerIdx, msg, playerInfo);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_QueryCpPlayerDetail_VALUE, msg);
    }

    public static void setPlayerInfo(String playerIdx, SC_QueryCpPlayerDetail.Builder msg, CpTeamMember playerInfo) {
        if (playerInfo == null) {
            return;
        }
        msg.setPlayerIdx(playerInfo.getPlayerIdx());
        msg.setPlayerName(playerInfo.getPlayerName());
        msg.setAvatarId(playerInfo.getHeader());
        msg.setTotalAbility(playerInfo.getAbility());
        msg.setPlayerLv(playerInfo.getPlayerLv());
        msg.setVipLv(playerInfo.getVipLv());

        for (Battle.BattlePetData petDatum : playerInfo.getPetData()) {
            msg.addPets(CpFunctionUtil.toPetVo(petDatum));
        }
        msg.setVipLv(playerInfo.getVipLv());
        msg.setAvatarBorder(playerInfo.getAvatarBorder());
        msg.setAvatarBorderRank(playerInfo.getAvatarBorderRank());
        msg.setTitleId(playerInfo.getTitleId());
        msg.setCurEquipNewTitleId(playerInfo.getCurEquipNewTitleId());
        msg.setShortId(playerInfo.getShortId());
        msg.setServerIndex(playerInfo.getServerIndex());
        msg.setSex(playerInfo.getSex());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_QueryCpPlayerDetail_VALUE, SC_QueryCpPlayerDetail.newBuilder().setRetCode(retCode));
    }
}
