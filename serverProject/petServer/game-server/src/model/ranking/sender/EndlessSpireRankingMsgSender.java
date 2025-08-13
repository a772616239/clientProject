package model.ranking.sender;

import common.GlobalData;
import common.entity.RankingQuerySingleResult;
import helper.StringUtils;
import model.arena.ArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import protocol.Activity.EnumRankingType;
import protocol.EndlessSpire.EndlessSpireRanking;
import protocol.EndlessSpire.SC_ClaimEndlessSpireRanking;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/12/11
 */
public class EndlessSpireRankingMsgSender extends AbstractRankingMsgSender<EndlessSpireRanking> {
    @Override
    public EndlessSpireRanking buildRankingDisInfo(RankingQuerySingleResult singleResult) {
        playerEntity player = playerCache.getByIdx(singleResult.getPrimaryKey());
        if (player == null) {
            return null;
        }
        EndlessSpireRanking.Builder builder = EndlessSpireRanking.newBuilder();
        builder.setPlayerIdx(player.getIdx());
        builder.setPlayerName(player.getName());
        builder.setPlayerAvatar(player.getAvatar());
        builder.setPlayerLv(player.getLevel());
        builder.setPlayerSpireLv(singleResult.getIntPrimaryScore());
        builder.setRankingIndex(singleResult.getRanking());
        builder.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
        if (builder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            builder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(singleResult.getPrimaryKey()));
        }
        builder.setTitleId(player.getTitleId());
        builder.setNewTitleId(player.getCurEquipNewTitleId());
        return builder.build();
    }

    @Override
    public void sendRankingInfo(String playerIdx, RankingQuerySingleResult playerRankinInfo, EnumRankingType rankingType) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        SC_ClaimEndlessSpireRanking.Builder builder = SC_ClaimEndlessSpireRanking.newBuilder();
        builder.addAllRankingInfo(getDisInfoList());
        if (playerRankinInfo != null) {
            builder.setPlayerRanking(playerRankinInfo.getRanking());
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimEndlessSpireRanking_VALUE, builder);
    }
}
