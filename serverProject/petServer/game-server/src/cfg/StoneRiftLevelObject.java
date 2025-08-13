package cfg;
import model.base.baseConfigObject;
public class  StoneRiftLevelObject implements baseConfigObject{



private int level;

private int upexp;

private int currencyamax;

private int[] unlockscience;

private int outputup;

private int[] recoveryconsume;




public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setUpexp(int upexp) {

this.upexp = upexp;

}

public int getUpexp() {

return this.upexp;

}


public void setCurrencyamax(int currencyamax) {

this.currencyamax = currencyamax;

}

public int getCurrencyamax() {

return this.currencyamax;

}


public void setUnlockscience(int[] unlockscience) {

this.unlockscience = unlockscience;

}

public int[] getUnlockscience() {

return this.unlockscience;

}


public void setOutputup(int outputup) {

this.outputup = outputup;

}

public int getOutputup() {

return this.outputup;

}


public void setRecoveryconsume(int[] recoveryconsume) {

this.recoveryconsume = recoveryconsume;

}

public int[] getRecoveryconsume() {

return this.recoveryconsume;

}




}
