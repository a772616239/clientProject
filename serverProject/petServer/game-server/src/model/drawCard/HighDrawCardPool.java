package model.drawCard;

/**
 * 高级抽卡奖次只会出紫卡
 * @author huhan
 * @date 2020.11.18
 */
public class HighDrawCardPool extends DrawCardPool{
    @Override
    public int randomQuality(String playerIdx) {
        return DrawCardManager.HIGHEST_QUALITY;
    }
}
