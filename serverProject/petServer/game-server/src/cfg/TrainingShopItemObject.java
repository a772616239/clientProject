package cfg;
import model.base.baseConfigObject;
public class  TrainingShopItemObject implements baseConfigObject{



private int id;

private int buff;

private int[] isfight;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setBuff(int buff) {

this.buff = buff;

}

public int getBuff() {

return this.buff;

}


public void setIsfight(int[] isfight) {

this.isfight = isfight;

}

public int[] getIsfight() {

return this.isfight;

}




}
