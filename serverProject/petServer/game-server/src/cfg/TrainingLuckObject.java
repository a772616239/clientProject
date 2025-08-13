package cfg;
import model.base.baseConfigObject;
public class  TrainingLuckObject implements baseConfigObject{



private int id;

private int type;

private int[][] configformat;

private int buff;

private int weight;

private int level;

private int buffflag;

private int grade;

private int[][] price;

private int[][] discount;




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


public void setConfigformat(int[][] configformat) {

this.configformat = configformat;

}

public int[][] getConfigformat() {

return this.configformat;

}


public void setBuff(int buff) {

this.buff = buff;

}

public int getBuff() {

return this.buff;

}


public void setWeight(int weight) {

this.weight = weight;

}

public int getWeight() {

return this.weight;

}


public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setBuffflag(int buffflag) {

this.buffflag = buffflag;

}

public int getBuffflag() {

return this.buffflag;

}


public void setGrade(int grade) {

this.grade = grade;

}

public int getGrade() {

return this.grade;

}


public void setPrice(int[][] price) {

this.price = price;

}

public int[][] getPrice() {

return this.price;

}


public void setDiscount(int[][] discount) {

this.discount = discount;

}

public int[][] getDiscount() {

return this.discount;

}




}
