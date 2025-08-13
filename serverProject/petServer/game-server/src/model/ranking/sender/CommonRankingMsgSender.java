package model.ranking.sender;

import com.alibaba.fastjson.JSONObject;
import common.GlobalData;
import common.entity.RankingQuerySingleResult;
import helper.StringUtils;
import model.arena.ArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import protocol.Activity.EnumRankingType;
import protocol.Activity.PlayerRankingInfo;
import protocol.Activity.SC_ClaimRanking;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/12/11
 */
public class CommonRankingMsgSender extends AbstractRankingMsgSender<PlayerRankingInfo> {
    @Override
    public PlayerRankingInfo buildRankingDisInfo(RankingQuerySingleResult singleResult) {
        if (singleResult == null) {
            return null;
        }
        playerEntity player = playerCache.getByIdx(singleResult.getPrimaryKey());
        PlayerRankingInfo.Builder resultBuilder;
        if (player == null) {
            resultBuilder = buildFromExInfo(singleResult);
        } else {
            resultBuilder = buildByPlayer(player);
        }

        if (resultBuilder == null) {
            return null;
        }

        resultBuilder.setPlayerIdx(singleResult.getPrimaryKey());
        resultBuilder.setRanking(singleResult.getRanking());
        resultBuilder.setRankingScore(getRankingScore(singleResult));
        return resultBuilder.build();
    }

    protected long getRankingScore(RankingQuerySingleResult singleResult) {
        return singleResult.getPrimaryScore();
    }

    protected PlayerRankingInfo.Builder buildByPlayer(playerEntity player) {
        PlayerRankingInfo.Builder resultBuilder = PlayerRankingInfo.newBuilder();
        resultBuilder.setPlayerName(player.getName());
        resultBuilder.setPlayerLv(player.getLevel());
        resultBuilder.setTitleId(player.getTitleId());
        resultBuilder.setPlayerAvatar(player.getAvatar());
        resultBuilder.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
        if (resultBuilder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            resultBuilder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(player.getIdx()));
        }
        resultBuilder.setNewTitleId(player.getCurEquipNewTitleId());
        return resultBuilder;
    }

    protected PlayerRankingInfo.Builder buildFromExInfo(RankingQuerySingleResult singleResult) {
        if (StringUtils.isEmpty(singleResult.getExtInfo())) {
            return null;
        }

        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(singleResult.getExtInfo());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
        PlayerRankingInfo.Builder resultBuilder = PlayerRankingInfo.newBuilder();
        if (jsonObject.containsKey("roleName")) {
            resultBuilder.setPlayerName(jsonObject.getString("roleName"));
        }
        if (jsonObject.containsKey("level")) {
            resultBuilder.setPlayerLv(jsonObject.getIntValue("level"));
        }

        if (jsonObject.containsKey("vipLv")) {
            resultBuilder.setPlayerVipLv(jsonObject.getIntValue("vipLv"));
        }

        if (jsonObject.containsKey("titleId")) {
            resultBuilder.setTitleId(jsonObject.getIntValue("titleId"));
        }

        if (jsonObject.containsKey("avatarId")) {
            resultBuilder.setPlayerAvatar(jsonObject.getIntValue("avatarId"));
        }
        if (jsonObject.containsKey("avatarBorder")) {
            resultBuilder.setAvatarBorder(jsonObject.getIntValue("avatarBorder"));
            if (resultBuilder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
                resultBuilder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(singleResult.getExtInfo()));
            }
        }

        if (jsonObject.containsKey("newTitleId")) {
            resultBuilder.setNewTitleId(jsonObject.getIntValue("newTitleId"));
        }

        subBuildFromExInfo(resultBuilder, jsonObject);

        return resultBuilder;
    }

    @Override
    public void sendRankingInfo(String playerIdx, RankingQuerySingleResult playerRankinInfo, EnumRankingType rankingType) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }

        SC_ClaimRanking.Builder msgBuilder = SC_ClaimRanking.newBuilder();
        msgBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        msgBuilder.addAllRankingInfo(getDisInfoList());
        if (playerRankinInfo != null) {
            msgBuilder.setPlayerRanking(playerRankinInfo.getRanking());
            msgBuilder.setPlayerScore(getRankingScore(playerRankinInfo));
        } else {
            msgBuilder.setPlayerRanking(-1);
        }
        subBuildClaimRanking(msgBuilder);
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimRanking_VALUE, msgBuilder);
    }

    protected void subBuildClaimRanking(SC_ClaimRanking.Builder msgBuilder) {
    }

    protected void subBuildFromExInfo(PlayerRankingInfo.Builder builder, JSONObject jsonObject) {
    }
}
