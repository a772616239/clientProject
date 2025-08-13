package cfg;
import model.base.baseConfigObject;
public class  StoneRiftRobotObject implements baseConfigObject{



private int id;

private int[] stoneriftlevel;

private int[][] factorycfg;

private int[] efficiency;

private int[] storemax;

private int[] durable;

private int[][] defendpet;

private int[] storeprogress;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStoneriftlevel(int[] stoneriftlevel) {

this.stoneriftlevel = stoneriftlevel;

}

public int[] getStoneriftlevel() {

return this.stoneriftlevel;

}


public void setFactorycfg(int[][] factorycfg) {

this.factorycfg = factorycfg;

}

public int[][] getFactorycfg() {

return this.factorycfg;

}


public void setEfficiency(int[] efficiency) {

this.efficiency = efficiency;

}

public int[] getEfficiency() {

return this.efficiency;

}


public void setStoremax(int[] storemax) {

this.storemax = storemax;

}

public int[] getStoremax() {

return this.storemax;

}


public void setDurable(int[] durable) {

this.durable = durable;

}

public int[] getDurable() {

return this.durable;

}


public void setDefendpet(int[][] defendpet) {

this.defendpet = defendpet;

}

public int[][] getDefendpet() {

return this.defendpet;

}


public void setStoreprogress(int[] storeprogress) {

this.storeprogress = storeprogress;

}

public int[] getStoreprogress() {

return this.storeprogress;

}




}
