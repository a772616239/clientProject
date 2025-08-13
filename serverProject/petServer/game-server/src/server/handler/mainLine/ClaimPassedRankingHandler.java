package server.handler.mainLine;

import common.AbstractBaseHandler;
import common.GameConst.RankingName;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.ranking.EnumRankingSenderType;
import model.ranking.RankingManager;
import protocol.Activity.EnumRankingType;
import protocol.Common.EnumFunction;
import protocol.MainLine.CS_ClaimPassedRanking;
import protocol.MainLine.SC_ClaimPassedRanking;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimPassedRanking_VALUE)
public class ClaimPassedRankingHandler extends AbstractBaseHandler<CS_ClaimPassedRanking> {
    @Override
    protected CS_ClaimPassedRanking parse(byte[] bytes) throws Exception {
        return CS_ClaimPassedRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimPassedRanking req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
//        RankingManager.getInstance().sendRankingInfoToPlayer(playerIdx, EnumRankingType.ERT_MainLine, RankingName.RN_MainLinePassed);
        RankingManager.getInstance().sendRankingInfoToPlayer(EnumRankingSenderType.ERST_MainLine, EnumRankingType.ERT_MainLine,
                RankingName.RN_MainLinePassed, playerIdx);
//        Class<AbstractRankingMsgSender> mainLineRankingMsgSenderClass = MainLineRankingMsgSender.class;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimPassedRanking_VALUE, SC_ClaimPassedRanking.newBuilder().setRetCode(retCode));
    }
}
