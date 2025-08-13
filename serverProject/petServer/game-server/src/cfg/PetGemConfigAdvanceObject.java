package cfg;
import model.base.baseConfigObject;
public class  PetGemConfigAdvanceObject implements baseConfigObject{



private String id;

private int rarity;

private int gemtype;

private int star;

private int[][] advanceproperties;

private int[][] advancesourceconsume;

private int[][] advancegemconsume;

private int[][] gemsale;

private int[] gemsaleconsume;




public void setId(String id) {

this.id = id;

}

public String getId() {

return this.id;

}


public void setRarity(int rarity) {

this.rarity = rarity;

}

public int getRarity() {

return this.rarity;

}


public void setGemtype(int gemtype) {

this.gemtype = gemtype;

}

public int getGemtype() {

return this.gemtype;

}


public void setStar(int star) {

this.star = star;

}

public int getStar() {

return this.star;

}


public void setAdvanceproperties(int[][] advanceproperties) {

this.advanceproperties = advanceproperties;

}

public int[][] getAdvanceproperties() {

return this.advanceproperties;

}


public void setAdvancesourceconsume(int[][] advancesourceconsume) {

this.advancesourceconsume = advancesourceconsume;

}

public int[][] getAdvancesourceconsume() {

return this.advancesourceconsume;

}


public void setAdvancegemconsume(int[][] advancegemconsume) {

this.advancegemconsume = advancegemconsume;

}

public int[][] getAdvancegemconsume() {

return this.advancegemconsume;

}


public void setGemsale(int[][] gemsale) {

this.gemsale = gemsale;

}

public int[][] getGemsale() {

return this.gemsale;

}


public void setGemsaleconsume(int[] gemsaleconsume) {

this.gemsaleconsume = gemsaleconsume;

}

public int[] getGemsaleconsume() {

return this.gemsaleconsume;

}




}
