package model.ranking.sender;

import com.alibaba.fastjson.JSONObject;
import common.GlobalData;
import common.entity.RankingQuerySingleResult;
import helper.StringUtils;
import model.arena.ArenaManager;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import protocol.Activity.EnumRankingType;
import protocol.Activity.PetRankingInfo;
import protocol.Activity.SC_ClaimRanking;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/12/11
 */
public class PetAbilityRankingMsgSender extends AbstractRankingMsgSender<PetRankingInfo> {
    @Override
    public PetRankingInfo buildRankingDisInfo(RankingQuerySingleResult singleResult) {
        if (singleResult == null) {
            return null;
        }
        String petId = singleResult.getPrimaryKey();


        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(singleResult.getExtInfo());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
        if (!jsonObject.containsKey("playerId")) {
            return null;
        }
        String playerIdx = jsonObject.getString("playerId");
        if (!jsonObject.containsKey("petBookId")) {
            return null;
        }
        int petBookId = jsonObject.getIntValue("petBookId");

        playerEntity player = playerCache.getByIdx(playerIdx);
        PetMessage.Pet pet = petCache.getInstance().getPetById(playerIdx, petId);
        if (player == null || pet == null) {
            return null;
        }

        PetRankingInfo.Builder builder = PetRankingInfo.newBuilder();
        builder.setPetId(petId);
        builder.setPetBookId(petBookId);
        builder.setTitleId(player.getTitleId());
        builder.setAvatarBorder(player.getAvatar());
        builder.setPlayerId(playerIdx);
        if (builder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            builder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(player.getIdx()));
        }
        builder.setRanking(singleResult.getRanking());
        builder.setRankingScore(singleResult.getPrimaryScore());
        builder.setPlayerName(player.getName());

        return builder.build();
    }


    @Override
    public void sendRankingInfo(String playerIdx, RankingQuerySingleResult playerRankinInfo, EnumRankingType rankingType) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }

        SC_ClaimRanking.Builder msgBuilder = SC_ClaimRanking.newBuilder();
        msgBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        msgBuilder.addAllPetRankingInfo(getDisInfoList());
        if (playerRankinInfo != null) {
            msgBuilder.setPlayerRanking(playerRankinInfo.getRanking());
            msgBuilder.setPlayerScore(playerRankinInfo.getPrimaryScore());
        } else {
            msgBuilder.setPlayerRanking(-1);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimRanking_VALUE, msgBuilder);
    }
}
