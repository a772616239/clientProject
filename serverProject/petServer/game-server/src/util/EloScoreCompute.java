package util;

import cfg.ArenaConfig;
import cfg.ArenaConfigObject;
import common.GameConst;
import common.entity.ScoreChange;

public class EloScoreCompute {

    /**
     * 结算积分算法和规则
     * 默认值
     * 玩家a积分：	        Ra
     * 玩家b积分：	        Rb
     * 积分差实力期望	：	    E	    500
     * 比赛中胜利分数修正系数：	Win	    1
     * 比赛中失败分数修正系数：	Lose	0.1
     * 积分增加平均期望：	    K	    40
     * 玩家a获胜期望值	：	    Ea=(1-Ea)
     * 玩家b获胜期望值	：	    Eb=1/(1+10^((Ra	-Rb)/E))
     * 玩家a获胜后实际得分	：	R‘a=K*(win	-Ea)
     * 玩家a失败后实际得分	：	R‘a=K*(lose	-Ea)
     * 玩家b获胜后实际得分	：	R‘b=K*(win	-Eb)
     * 玩家b失败后实际得分	：	R‘b=K*(lose	-Eb)
     *
     * @param playerWin
     * @param playerScore
     * @param opponentScore
     * @return
     */
    public static ScoreChange scoreChange(boolean playerWin, float playerScore, float opponentScore) {
        ArenaConfigObject arenaCfg = ArenaConfig.getById(GameConst.CONFIG_ID);
        float expectStrength = arenaCfg.getExpectstrength();
        float victoryCorrection = (arenaCfg.getVictorycorrection() * 1F) / 100;
        float failureCorrection = (arenaCfg.getFailurecorrection() * 1F) / 100;
        float averageExpected = arenaCfg.getAverageexpected();

        double opponentExpected = 1D / (1 + Math.pow(10, (playerScore - opponentScore) / expectStrength));
        double playerExpected = 1D - opponentExpected;

        ScoreChange scoreChange = new ScoreChange();
        if (playerWin) {
            scoreChange.setPlayerScoreChange((int) (averageExpected * (victoryCorrection - playerExpected)));
            scoreChange.setOpponentScoreChange((int) (averageExpected * (failureCorrection - opponentExpected)));
        } else {
            scoreChange.setPlayerScoreChange((int) (averageExpected * (failureCorrection - playerExpected)));
            scoreChange.setOpponentScoreChange((int) (averageExpected * (victoryCorrection - opponentExpected)));
        }

        //玩家失败后不再加分
        if (!playerWin && scoreChange.getPlayerScoreChange() > 0) {
            scoreChange.setPlayerScoreChange(0);
        }
        return scoreChange;
    }


}
