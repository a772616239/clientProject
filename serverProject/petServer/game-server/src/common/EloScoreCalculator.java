package common;

import lombok.ToString;
import util.LogUtil;

/**
 * @author huhan
 * @date 2021/05/18
 * <p>
 * Ra：A玩家当前的积分
 * Rb：B玩家当前的积分
 * Sa：实际胜负值，胜=1，平=0.5，负=0
 * Ea：预期A选手的胜负值，Ea=1/(1+10^[(Rb-Ra)/400])
 * Eb：预期B选手的胜负值，Eb=1/(1+10^[(Ra-Rb)/400])
 * 因为E值也为预估，则Ea+ Eb=1
 * <p>
 * R’a=Ra+K（Sa-Ea）
 * R’a：A玩家进行了一场比赛之后的积分
 * @link https://blog.csdn.net/qq100440110/article/details/70240824
 */
@ToString
public class EloScoreCalculator {
    /**
     * 胜利积分修正
     */
    private double saWin;
    /**
     * 失败积分修正
     */
    private double saFailed;
    /**
     * 平局积分修正
     */
    private double saDraw;
    /**
     * 常数， 公式中的400
     */
    private int constNum;
    /**
     * 积分常数值
     */
    private int k;

    public EloScoreCalculator(double saWin, double saFailed, double saDraw, int constNum, int k) {
        this.saWin = saWin;
        this.saFailed = saFailed;
        this.saDraw = saDraw;
        this.constNum = constNum;
        this.k = k;
    }

    /**
     * @param playerScore
     * @param opponentScore
     * @param battleResult  -1平局， 1 胜利， 2失败
     * @return
     */
    public int calculateScore(int playerScore, int opponentScore, int battleResult) {
        double sa = battleResult == 1 ? saWin : battleResult == 2 ? saFailed : saDraw;
        double ea = 1 / (1 + Math.pow(10, ((opponentScore - playerScore) * 1.0) / constNum));
        int change = (int) (k * (sa - ea));
        if (battleResult == 1 && change < 0) {
            change = 0;
        }
        LogUtil.info("common.EloScoreCalculator.calculateScore, param:" + this + ", playerScore:" + playerScore
                + ", opponentScore:" + opponentScore + ", battleResult:" + battleResult + ", change:" + change);
        return change;
    }
}


