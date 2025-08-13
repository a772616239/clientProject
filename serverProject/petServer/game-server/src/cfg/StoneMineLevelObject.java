package cfg;
import model.base.baseConfigObject;
public class  StoneMineLevelObject implements baseConfigObject{



private int level;

private int[][] improveefficiency;

private int[][] upconsume;

private int[][] producetime;




public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setImproveefficiency(int[][] improveefficiency) {

this.improveefficiency = improveefficiency;

}

public int[][] getImproveefficiency() {

return this.improveefficiency;

}


public void setUpconsume(int[][] upconsume) {

this.upconsume = upconsume;

}

public int[][] getUpconsume() {

return this.upconsume;

}


public void setProducetime(int[][] producetime) {

this.producetime = producetime;

}

public int[][] getProducetime() {

return this.producetime;

}




}
