package model.drawCard;

/**
 * @author huhan
 * @date 2021/2/4
 */
public class AdvanceRandom implements OddsRandom {

    private final int[] rewards;
    private final int odds;
    private final int id;
    private final boolean isCorePet;

    public AdvanceRandom(int id, int[] rewards, int odds, boolean isCorePet) {
        this.id = id;
        this.rewards = rewards;
        this.odds = odds;
        this.isCorePet = isCorePet;
    }

    @Override
    public int getQuality() {
        return 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getOdds() {
        return odds;
    }

    @Override
    public int[] getRewards() {
        return rewards;
    }

    @Override
    public boolean getIscorepet() {
        return isCorePet;
    }
}
