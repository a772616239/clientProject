package cfg;
import model.base.baseConfigObject;
public class  MineDoubleRewardConfigObject implements baseConfigObject{



private int id;

private int startime;

private int endtime;

private int extrarewardrate;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStartime(int startime) {

this.startime = startime;

}

public int getStartime() {

return this.startime;

}


public void setEndtime(int endtime) {

this.endtime = endtime;

}

public int getEndtime() {

return this.endtime;

}


public void setExtrarewardrate(int extrarewardrate) {

this.extrarewardrate = extrarewardrate;

}

public int getExtrarewardrate() {

return this.extrarewardrate;

}




}
