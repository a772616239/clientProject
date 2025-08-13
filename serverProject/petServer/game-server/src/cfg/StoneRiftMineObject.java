package cfg;
import model.base.baseConfigObject;
public class  StoneRiftMineObject implements baseConfigObject{



private int id;

private int type;

private int[][] output;

private int unlockcondition;

private int maxstore;

private int[] unlockconsume;

private int durableconsume;

private int[] exchangeexp;

private int worth;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setOutput(int[][] output) {

this.output = output;

}

public int[][] getOutput() {

return this.output;

}


public void setUnlockcondition(int unlockcondition) {

this.unlockcondition = unlockcondition;

}

public int getUnlockcondition() {

return this.unlockcondition;

}


public void setMaxstore(int maxstore) {

this.maxstore = maxstore;

}

public int getMaxstore() {

return this.maxstore;

}


public void setUnlockconsume(int[] unlockconsume) {

this.unlockconsume = unlockconsume;

}

public int[] getUnlockconsume() {

return this.unlockconsume;

}


public void setDurableconsume(int durableconsume) {

this.durableconsume = durableconsume;

}

public int getDurableconsume() {

return this.durableconsume;

}


public void setExchangeexp(int[] exchangeexp) {

this.exchangeexp = exchangeexp;

}

public int[] getExchangeexp() {

return this.exchangeexp;

}


public void setWorth(int worth) {

this.worth = worth;

}

public int getWorth() {

return this.worth;

}




}
