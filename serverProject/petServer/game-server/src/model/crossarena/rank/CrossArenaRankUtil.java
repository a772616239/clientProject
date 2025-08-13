package model.crossarena.rank;

public class CrossArenaRankUtil {
    public static double getRankScore(int primary, int secondary) {
        return primary + secondary / 100000000.0;
    }
}
