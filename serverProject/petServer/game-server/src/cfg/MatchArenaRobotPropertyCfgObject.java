package cfg;
import model.base.baseConfigObject;
public class  MatchArenaRobotPropertyCfgObject implements baseConfigObject{



private int id;

private int[] level;

private int rarity;

private int[][] exproperty;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLevel(int[] level) {

this.level = level;

}

public int[] getLevel() {

return this.level;

}


public void setRarity(int rarity) {

this.rarity = rarity;

}

public int getRarity() {

return this.rarity;

}


public void setExproperty(int[][] exproperty) {

this.exproperty = exproperty;

}

public int[][] getExproperty() {

return this.exproperty;

}




}
