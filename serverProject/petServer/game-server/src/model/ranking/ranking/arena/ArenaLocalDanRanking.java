package model.ranking.ranking.arena;

import lombok.Setter;
import model.arena.dbCache.arenaCache;
import model.ranking.RankingUtils;
import model.ranking.sender.AbstractRankingMsgSender;
import model.ranking.sender.ArenaDanMsgSender;

@Setter
public class ArenaLocalDanRanking extends ArenaDanRanking {

    int dan;

    @Override
    public void setRankingName(String rankingName) {
        super.setRankingName(rankingName);
        this.dan = RankingUtils.getArenaScoreLocalDanRankDan(getRankingName());
    }

    @Override
    public long getLocalScore(String playerIdx) {
        return arenaCache.getInstance().getPlayerDan(playerIdx) == dan ? dan : 0;
    }

    @Override
    protected void initSender(AbstractRankingMsgSender<?> msgSender) {
        super.initSender(msgSender);
        if (msgSender instanceof ArenaDanMsgSender) {
            ((ArenaDanMsgSender) msgSender).setDan(dan);
        }
    }

}
