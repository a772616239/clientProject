package cfg;
import model.base.baseConfigObject;
public class  PetGemConfigLeveObject implements baseConfigObject{



private int lv;

private int[][] uplvconsume;

private int[][] gemsale;




public void setLv(int lv) {

this.lv = lv;

}

public int getLv() {

return this.lv;

}


public void setUplvconsume(int[][] uplvconsume) {

this.uplvconsume = uplvconsume;

}

public int[][] getUplvconsume() {

return this.uplvconsume;

}


public void setGemsale(int[][] gemsale) {

this.gemsale = gemsale;

}

public int[][] getGemsale() {

return this.gemsale;

}




}
