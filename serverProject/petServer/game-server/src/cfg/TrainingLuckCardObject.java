package cfg;
import model.base.baseConfigObject;
public class  TrainingLuckCardObject implements baseConfigObject{



private int id;

private int freecd;

private int luck;

private int opentime;

private int normal;

private int better;

private int[] cost1;

private int[] cost3;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setFreecd(int freecd) {

this.freecd = freecd;

}

public int getFreecd() {

return this.freecd;

}


public void setLuck(int luck) {

this.luck = luck;

}

public int getLuck() {

return this.luck;

}


public void setOpentime(int opentime) {

this.opentime = opentime;

}

public int getOpentime() {

return this.opentime;

}


public void setNormal(int normal) {

this.normal = normal;

}

public int getNormal() {

return this.normal;

}


public void setBetter(int better) {

this.better = better;

}

public int getBetter() {

return this.better;

}


public void setCost1(int[] cost1) {

this.cost1 = cost1;

}

public int[] getCost1() {

return this.cost1;

}


public void setCost3(int[] cost3) {

this.cost3 = cost3;

}

public int[] getCost3() {

return this.cost3;

}




}
