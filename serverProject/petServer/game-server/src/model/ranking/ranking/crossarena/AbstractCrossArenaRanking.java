package model.ranking.ranking.crossarena;

import common.entity.RankingQuerySingleResult;
import common.entity.RankingUpdateRequest;
import model.crossarena.CrossArenaRankManager;
import model.ranking.ranking.AbstractRanking;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Activity;
import protocol.CrossArena;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbstractCrossArenaRanking extends AbstractRanking {

    @Override
    public List<Integer> getSortRules() {
        return RankingUpdateRequest.SORT_RULES_DES_DES;
    }


    @Override
    public void updatePlayerRankingScore(String playerIdx, long primaryScore, long subScore) {
        super.updatePlayerRankingScore(playerIdx, primaryScore, subScore);
        CrossArenaRankManager.getInstance().uploadRankShowData(playerIdx);
    }

    private int pageSize = 50;

    List<CrossArena.CrossArenaRankItem> showRankData = Collections.emptyList();

    public List<CrossArena.CrossArenaRankItem> getShowRankData() {
        return showRankData;
    }

    @Override
    public void updateRanking() {
        List<RankingQuerySingleResult> rankInfos = queryPlatformRanking();

        if (CollectionUtils.isEmpty(rankInfos)) {
            return;
        }

        List<CrossArena.CrossArenaRankItem> rankData = new ArrayList<>(pageSize);

        rankInfos.sort(Comparator.comparingInt(RankingQuerySingleResult::getRanking));

        List<String> playerIds = rankInfos.stream().map(RankingQuerySingleResult::getPrimaryKey).collect(Collectors.toList());
        Map<String, CrossArena.CrossArenaRankItem> showMap = CrossArenaRankManager.getInstance().findPlayerShowData(playerIds);

        CrossArena.CrossArenaRankItem rankItem;
        int rank = 0;
        for (RankingQuerySingleResult rankInfo : rankInfos) {
            rank++;
            rankItem = showMap.get(rankInfo.getPrimaryKey());
            if (rankItem != null) {
                if (this.getRankingType() == Activity.EnumRankingType.ERT_Lt_SerialWin) {
                    CrossArena.CrossArenaRankExData.Builder serialWinNum = CrossArena.CrossArenaRankExData.newBuilder().setKey(CrossArena.CrossArenaRankExKey.CARED_SerialWin_MaxWinNum).setValue((int) rankInfo.getSubScore());
                    rankItem = rankItem.toBuilder().addExData(serialWinNum).setRank(rank).setRankScore(rankInfo.getPrimaryScore()).build();
                } else {
                    rankItem = rankItem.toBuilder().setRank(rank).setRankScore(rankInfo.getPrimaryScore()).build();
                }
                rankData.add(rankItem);
            } else {
                LogUtil.error("cross arena rank:{} player:{} data is miss", rankInfo.getRanking(), rankInfo.getPrimaryKey());
            }
        }
        showRankData = rankData;
    }

    @Override
    public long getLocalScore(String playerIdx) {
        return 0L;
    }

}
