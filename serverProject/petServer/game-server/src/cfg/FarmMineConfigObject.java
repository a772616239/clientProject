package cfg;
import model.base.baseConfigObject;
public class  FarmMineConfigObject implements baseConfigObject{



private int id;

private String parm;

private int mainmax;

private int[] offerpriceconsume;

private int[] offerpricitem;

private int addpricepre;

private int pricenum;

private int[][] extawardtime;

private int bestealsnum;

private int stealstimecan;

private int stealsnum;

private int stealsvue;

private int[][] speedadd;

private int speedaddvue;

private int harvestinstime;

private int harvesttimemax;

private int[] title;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setParm(String parm) {

this.parm = parm;

}

public String getParm() {

return this.parm;

}


public void setMainmax(int mainmax) {

this.mainmax = mainmax;

}

public int getMainmax() {

return this.mainmax;

}


public void setOfferpriceconsume(int[] offerpriceconsume) {

this.offerpriceconsume = offerpriceconsume;

}

public int[] getOfferpriceconsume() {

return this.offerpriceconsume;

}


public void setOfferpricitem(int[] offerpricitem) {

this.offerpricitem = offerpricitem;

}

public int[] getOfferpricitem() {

return this.offerpricitem;

}


public void setAddpricepre(int addpricepre) {

this.addpricepre = addpricepre;

}

public int getAddpricepre() {

return this.addpricepre;

}


public void setPricenum(int pricenum) {

this.pricenum = pricenum;

}

public int getPricenum() {

return this.pricenum;

}


public void setExtawardtime(int[][] extawardtime) {

this.extawardtime = extawardtime;

}

public int[][] getExtawardtime() {

return this.extawardtime;

}


public void setBestealsnum(int bestealsnum) {

this.bestealsnum = bestealsnum;

}

public int getBestealsnum() {

return this.bestealsnum;

}


public void setStealstimecan(int stealstimecan) {

this.stealstimecan = stealstimecan;

}

public int getStealstimecan() {

return this.stealstimecan;

}


public void setStealsnum(int stealsnum) {

this.stealsnum = stealsnum;

}

public int getStealsnum() {

return this.stealsnum;

}


public void setStealsvue(int stealsvue) {

this.stealsvue = stealsvue;

}

public int getStealsvue() {

return this.stealsvue;

}


public void setSpeedadd(int[][] speedadd) {

this.speedadd = speedadd;

}

public int[][] getSpeedadd() {

return this.speedadd;

}


public void setSpeedaddvue(int speedaddvue) {

this.speedaddvue = speedaddvue;

}

public int getSpeedaddvue() {

return this.speedaddvue;

}


public void setHarvestinstime(int harvestinstime) {

this.harvestinstime = harvestinstime;

}

public int getHarvestinstime() {

return this.harvestinstime;

}


public void setHarvesttimemax(int harvesttimemax) {

this.harvesttimemax = harvesttimemax;

}

public int getHarvesttimemax() {

return this.harvesttimemax;

}


public void setTitle(int[] title) {

this.title = title;

}

public int[] getTitle() {

return this.title;

}




}
