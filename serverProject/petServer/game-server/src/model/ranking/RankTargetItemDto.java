package model.ranking;

import lombok.Data;

@Data
public class RankTargetItemDto {
    private String playerId;                 //玩家id
    private String playerName;              //玩家名字
    private int playerAvatar;             //玩家头像
    private int avatarBorder;             //头像框
    private int avatarBorderRank;
    private int titleId;                   //称号
    private boolean canClaimReward;       //领奖状态
    private int targetRewardId;
}
