package cfg;
import model.base.baseConfigObject;
public class  RollCardObject implements baseConfigObject{



private int id;

private int type;

private int[][] grade;

private int[][] pool1;

private int[][] pool2;

private int[] cost1;

private int[] cost10;

private int[] othercost1;

private int[] othercost10;

private int[] luckpool;




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


public void setGrade(int[][] grade) {

this.grade = grade;

}

public int[][] getGrade() {

return this.grade;

}


public void setPool1(int[][] pool1) {

this.pool1 = pool1;

}

public int[][] getPool1() {

return this.pool1;

}


public void setPool2(int[][] pool2) {

this.pool2 = pool2;

}

public int[][] getPool2() {

return this.pool2;

}


public void setCost1(int[] cost1) {

this.cost1 = cost1;

}

public int[] getCost1() {

return this.cost1;

}


public void setCost10(int[] cost10) {

this.cost10 = cost10;

}

public int[] getCost10() {

return this.cost10;

}


public void setOthercost1(int[] othercost1) {

this.othercost1 = othercost1;

}

public int[] getOthercost1() {

return this.othercost1;

}


public void setOthercost10(int[] othercost10) {

this.othercost10 = othercost10;

}

public int[] getOthercost10() {

return this.othercost10;

}


public void setLuckpool(int[] luckpool) {

this.luckpool = luckpool;

}

public int[] getLuckpool() {

return this.luckpool;

}




}
