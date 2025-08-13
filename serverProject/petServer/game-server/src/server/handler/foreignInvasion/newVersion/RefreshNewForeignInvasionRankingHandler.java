package server.handler.foreignInvasion.newVersion;

import common.AbstractBaseHandler;
import common.GameConst.RankingName;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.ranking.EnumRankingSenderType;
import model.ranking.RankingManager;
import protocol.Activity.EnumRankingType;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.NewForeignInvasion.CS_RefreshNewForeignInvasionRanking;
import protocol.NewForeignInvasion.SC_RefreshNewForeignInvasionRanking;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.11.13
 */
@MsgId(msgId = MsgIdEnum.CS_RefreshNewForeignInvasionRanking_VALUE)
public class RefreshNewForeignInvasionRankingHandler extends AbstractBaseHandler<CS_RefreshNewForeignInvasionRanking> {
    @Override
    protected CS_RefreshNewForeignInvasionRanking parse(byte[] bytes) throws Exception {
        return CS_RefreshNewForeignInvasionRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RefreshNewForeignInvasionRanking req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        RankingManager.getInstance().sendRankingInfoToPlayer(EnumRankingSenderType.ERST_NewForeignInvasion,
                EnumRankingType.ERT_NewForeignInvasion, RankingName.RN_New_ForInv_Score, playerIdx);

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NewForeignInvasion;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_RefreshNewForeignInvasionRanking_VALUE, SC_RefreshNewForeignInvasionRanking.newBuilder().setRetCode(retCode));
    }
}
