package model.ranking.sender;

import cfg.GameConfig;
import common.GameConst;
import common.GlobalData;
import common.entity.RankingQuerySingleResult;
import helper.StringUtils;
import model.arena.ArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import protocol.Activity.EnumRankingType;
import protocol.MainLine.MainLineRankingInfo;
import protocol.MainLine.SC_ClaimPassedRanking;
import protocol.MainLine.SC_ClaimPassedRanking.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/12/11
 */
public class MainLineRankingMsgSender extends AbstractRankingMsgSender<MainLineRankingInfo> {

    public MainLineRankingMsgSender() {
        setDisSize(GameConfig.getById(GameConst.CONFIG_ID).getMainlinerankingcount());
    }

    @Override
    public MainLineRankingInfo buildRankingDisInfo(RankingQuerySingleResult singleResult) {
        playerEntity player = playerCache.getByIdx(singleResult.getPrimaryKey());
        if (player == null) {
            return null;
        }
        MainLineRankingInfo.Builder builder = MainLineRankingInfo.newBuilder();
        builder.setPlayerIdx(player.getIdx());
        builder.setPlayerName(player.getName());
        builder.setPlayerAvatar(player.getAvatar());
        builder.setPlayerLv(player.getLevel());
        builder.setPassedCount(singleResult.getIntPrimaryScore());
        builder.setRankingNum(singleResult.getRanking());
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

        Builder resultBuilder = SC_ClaimPassedRanking.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.addAllRanking(getDisInfoList());

        if (playerRankinInfo != null) {
            resultBuilder.setPlayerRank(playerRankinInfo.getRanking());
            resultBuilder.setPlayerNode(playerRankinInfo.getIntPrimaryScore());
        } else {
            resultBuilder.setPlayerRank(-1);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimPassedRanking_VALUE, resultBuilder);
    }
}
