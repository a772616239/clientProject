package cfg;

import lombok.ToString;
import model.base.baseConfigObject;
import model.drawCard.OddsRandom;


@ToString
public class DrawFriendShipCardConfigObject implements baseConfigObject, OddsRandom {


    private int id;

    private int odds;

    private int quality;

    private int[] rewards;

    private int selectedodds;


    public void setId(int id) {

        this.id = id;

    }

    @Override
    public int getId() {

        return this.id;

    }


    public void setOdds(int odds) {

        this.odds = odds;

    }

    @Override
    public int getOdds() {

        return this.odds;

    }


    public void setQuality(int quality) {

        this.quality = quality;

    }

    @Override
    public int getQuality() {

        return this.quality;

    }


    public void setRewards(int[] rewards) {

        this.rewards = rewards;

    }

    @Override
    public int[] getRewards() {

        return this.rewards;

    }

    @Override
    public boolean getIscorepet() {
        return false;
    }


    public void setSelectedodds(int selectedodds) {

        this.selectedodds = selectedodds;

    }

    public int getSelectedodds() {

        return this.selectedodds;

    }

}

