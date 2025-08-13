package server.handler.endlessSpire;

import common.AbstractBaseHandler;
import common.GameConst.RankingName;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.ranking.EnumRankingSenderType;
import model.ranking.RankingManager;
import protocol.Activity.EnumRankingType;
import protocol.Common.EnumFunction;
import protocol.EndlessSpire.CS_ClaimEndlessSpireRanking;
import protocol.EndlessSpire.SC_ClaimEndlessSpireRanking;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_ClaimEndlessSpireRanking_VALUE)
public class ClaimEndlessSpireRankingHandler extends AbstractBaseHandler<CS_ClaimEndlessSpireRanking> {
    @Override
    protected CS_ClaimEndlessSpireRanking parse(byte[] bytes) throws Exception {
        return CS_ClaimEndlessSpireRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimEndlessSpireRanking req, int i) {
//        RankingManager.getInstance().sendRankingInfoToPlayer(String.valueOf(gsChn.getPlayerId1()),
//                EnumRankingType.ERT_Spire, RankingName.RN_EndlessSpire);

        RankingManager.getInstance().sendRankingInfoToPlayer(EnumRankingSenderType.ERST_EndlessSpire,
                EnumRankingType.ERT_Spire, RankingName.RN_EndlessSpire, String.valueOf(gsChn.getPlayerId1()));
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Endless;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ClaimEndlessSpireRanking_VALUE, SC_ClaimEndlessSpireRanking.newBuilder());
    }
}
