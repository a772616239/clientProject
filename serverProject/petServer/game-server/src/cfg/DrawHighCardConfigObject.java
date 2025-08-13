package cfg;

import lombok.ToString;
import model.base.baseConfigObject;
import model.drawCard.OddsRandom;

@ToString
public class  DrawHighCardConfigObject implements baseConfigObject, OddsRandom {



private int id;

private int odds;

private int quality;

private int[] rewards;

private int selectedodds;

private int rouletterate;

private boolean iscorepet;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setOdds(int odds) {

this.odds = odds;

}

public int getOdds() {

return this.odds;

}


public void setQuality(int quality) {

this.quality = quality;

}

public int getQuality() {

return this.quality;

}


public void setRewards(int[] rewards) {

this.rewards = rewards;

}

public int[] getRewards() {

return this.rewards;

}


public void setSelectedodds(int selectedodds) {

this.selectedodds = selectedodds;

}

public int getSelectedodds() {

return this.selectedodds;

}


public void setRouletterate(int rouletterate) {

this.rouletterate = rouletterate;

}

public int getRouletterate() {

return this.rouletterate;

}


public void setIscorepet(boolean iscorepet) {

this.iscorepet = iscorepet;

}

public boolean getIscorepet() {

return this.iscorepet;

}




}
