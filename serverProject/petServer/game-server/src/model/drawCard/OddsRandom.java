package model.drawCard;

/**
 * 概率随机接口
 */
public interface OddsRandom {
    int getQuality();
    int getId();
    int getOdds();
    int[] getRewards();
    boolean getIscorepet();
}
