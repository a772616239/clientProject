package server.handler.ranking;

import java.util.ArrayList;
import lombok.Data;
import protocol.Activity;

import java.util.List;

@Data
public class RankingEntranceDto {
    private Activity.EnumRankingType rankType;
    private String playerName;                   //最强玩家名字
    private int playerAvatar;                   //玩家头像
    private int avatarBorder;
    private int avatarBorderRank;
    private long rankingScore;                    //排名分数
    private int petBookId;                      //最强宠物bookId
    private List<Integer> canClaimRankTargetIds = new ArrayList<>();                     //是否可领奖励
    private int titleId ;                   //竞技场称号id
}
