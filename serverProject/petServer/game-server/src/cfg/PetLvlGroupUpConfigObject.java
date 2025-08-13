package cfg;
import model.base.baseConfigObject;
public class  PetLvlGroupUpConfigObject implements baseConfigObject{



private int id;

private int lvl;

private int propertymodel;

private int[][] factor;

private int[][] monsterfactor;

private int[][] otherfactors;

private int[][] monsterotherfactors;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLvl(int lvl) {

this.lvl = lvl;

}

public int getLvl() {

return this.lvl;

}


public void setPropertymodel(int propertymodel) {

this.propertymodel = propertymodel;

}

public int getPropertymodel() {

return this.propertymodel;

}


public void setFactor(int[][] factor) {

this.factor = factor;

}

public int[][] getFactor() {

return this.factor;

}


public void setMonsterfactor(int[][] monsterfactor) {

this.monsterfactor = monsterfactor;

}

public int[][] getMonsterfactor() {

return this.monsterfactor;

}


public void setOtherfactors(int[][] otherfactors) {

this.otherfactors = otherfactors;

}

public int[][] getOtherfactors() {

return this.otherfactors;

}


public void setMonsterotherfactors(int[][] monsterotherfactors) {

this.monsterotherfactors = monsterotherfactors;

}

public int[][] getMonsterotherfactors() {

return this.monsterotherfactors;

}




}
