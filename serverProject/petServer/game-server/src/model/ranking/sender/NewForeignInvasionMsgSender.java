package model.ranking.sender;

import common.GlobalData;
import common.entity.RankingQuerySingleResult;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.lang.StringUtils;
import protocol.Activity.EnumRankingType;
import protocol.MessageId.MsgIdEnum;
import protocol.NewForeignInvasion.NewForeignInvasionPlayerRankingInfo;
import protocol.NewForeignInvasion.SC_RefreshNewForeignInvasionRanking;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/12/16
 */
public class NewForeignInvasionMsgSender extends AbstractRankingMsgSender<NewForeignInvasionPlayerRankingInfo> {
    @Override
    public NewForeignInvasionPlayerRankingInfo buildRankingDisInfo(RankingQuerySingleResult singleResult) {
        if (singleResult == null) {
            return null;
        }
        NewForeignInvasionPlayerRankingInfo.Builder resultBuilder = NewForeignInvasionPlayerRankingInfo.newBuilder();
        resultBuilder.setRanking(singleResult.getRanking());
        resultBuilder.setScore(singleResult.getIntPrimaryScore());
        playerEntity player = playerCache.getByIdx(singleResult.getPrimaryKey());
        if (player != null) {
            resultBuilder.setName(player.getName());
            resultBuilder.setTitleId(player.getTitleId());
        }
        return resultBuilder.build();
    }

    @Override
    public void sendRankingInfo(String playerIdx, RankingQuerySingleResult playerRankinInfo, EnumRankingType rankingType) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        SC_RefreshNewForeignInvasionRanking.Builder msgBuilder = SC_RefreshNewForeignInvasionRanking.newBuilder();
        msgBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        msgBuilder.addAllRankingInfo(getDisInfoList());
        if (playerRankinInfo != null) {
            msgBuilder.setPlayerRanking(playerRankinInfo.getRanking());
            msgBuilder.setPlayerScore(playerRankinInfo.getIntPrimaryScore());
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefreshNewForeignInvasionRanking_VALUE, msgBuilder);
    }
}
