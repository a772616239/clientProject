package model.ranking.sender;

import common.GlobalData;
import common.entity.RankingQuerySingleResult;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import protocol.Activity.EnumRankingType;
import protocol.Activity.RankingInfo;
import protocol.Activity.SC_ClaimActivityRanking;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/12/16
 */
public class ActivityRankingMsgSender extends AbstractRankingMsgSender<RankingInfo> {
    @Override
    public RankingInfo buildRankingDisInfo(RankingQuerySingleResult singleResult) {
        if (singleResult == null) {
            return null;
        }
        playerEntity player = playerCache.getByIdx(singleResult.getPrimaryKey());
        if (player == null) {
            return null;
        }

        RankingInfo.Builder resultBuilder = RankingInfo.newBuilder();
        resultBuilder.setPlayerIdx(singleResult.getPrimaryKey());
        resultBuilder.setAvatar(player.getAvatar());
        resultBuilder.setPlayerName(player.getName());
        resultBuilder.setRanking(singleResult.getRanking());
        resultBuilder.setPrimaryScore(singleResult.getPrimaryScore());
        resultBuilder.setTitleId(PlayerUtil.queryPlayerTitleId(singleResult.getPrimaryKey()));
        resultBuilder.setNewTitleId(PlayerUtil.queryPlayerNewTitleId(singleResult.getPrimaryKey()));
        return resultBuilder.build();
    }

    @Override
    public void sendRankingInfo(String playerIdx, RankingQuerySingleResult playerRankinInfo, EnumRankingType rankingType) {
        SC_ClaimActivityRanking.Builder builder = SC_ClaimActivityRanking.newBuilder();
        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        builder.addAllRankingInfos(getDisInfoList());
        if (playerRankinInfo != null) {
            builder.setPlayerRanking(playerRankinInfo.getRanking());
            builder.setPlayerRankingScore(playerRankinInfo.getPrimaryScore());
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimActivityRanking_VALUE, builder);
    }
}
