package cfg;
import model.base.baseConfigObject;
public class  NewForeignInvasionWaveConfigObject implements baseConfigObject{



private int id;

private int linkbuildingid;

private int wave;

private int lvaddition;

private int expropertyaddition;

private int battlerewards;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLinkbuildingid(int linkbuildingid) {

this.linkbuildingid = linkbuildingid;

}

public int getLinkbuildingid() {

return this.linkbuildingid;

}


public void setWave(int wave) {

this.wave = wave;

}

public int getWave() {

return this.wave;

}


public void setLvaddition(int lvaddition) {

this.lvaddition = lvaddition;

}

public int getLvaddition() {

return this.lvaddition;

}


public void setExpropertyaddition(int expropertyaddition) {

this.expropertyaddition = expropertyaddition;

}

public int getExpropertyaddition() {

return this.expropertyaddition;

}


public void setBattlerewards(int battlerewards) {

this.battlerewards = battlerewards;

}

public int getBattlerewards() {

return this.battlerewards;

}




}
