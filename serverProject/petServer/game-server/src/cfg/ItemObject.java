package cfg;
import model.base.baseConfigObject;
public class  ItemObject implements baseConfigObject{



private int id;

private String name_tips;

private int quality;

private boolean usable;

private int[][] mustreward;

private String paramname;

private int[][] paramstr;

private int randomtimes;

private int[][] randomrewards;

private int randomtimes2;

private boolean salable;

private int[][] gainaftersell;

private int specialtype;

private int maxownedcount;

private boolean autouse;

private int useneedlv;

private int[][] usecostitem;

private int usetimeslimit;

private int dailyusetimeslimit;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setName_tips(String name_tips) {

this.name_tips = name_tips;

}

public String getName_tips() {

return this.name_tips;

}


public void setQuality(int quality) {

this.quality = quality;

}

public int getQuality() {

return this.quality;

}


public void setUsable(boolean usable) {

this.usable = usable;

}

public boolean getUsable() {

return this.usable;

}


public void setMustreward(int[][] mustreward) {

this.mustreward = mustreward;

}

public int[][] getMustreward() {

return this.mustreward;

}


public void setParamname(String paramname) {

this.paramname = paramname;

}

public String getParamname() {

return this.paramname;

}


public void setParamstr(int[][] paramstr) {

this.paramstr = paramstr;

}

public int[][] getParamstr() {

return this.paramstr;

}


public void setRandomtimes(int randomtimes) {

this.randomtimes = randomtimes;

}

public int getRandomtimes() {

return this.randomtimes;

}


public void setRandomrewards(int[][] randomrewards) {

this.randomrewards = randomrewards;

}

public int[][] getRandomrewards() {

return this.randomrewards;

}


public void setRandomtimes2(int randomtimes2) {

this.randomtimes2 = randomtimes2;

}

public int getRandomtimes2() {

return this.randomtimes2;

}


public void setSalable(boolean salable) {

this.salable = salable;

}

public boolean getSalable() {

return this.salable;

}


public void setGainaftersell(int[][] gainaftersell) {

this.gainaftersell = gainaftersell;

}

public int[][] getGainaftersell() {

return this.gainaftersell;

}


public void setSpecialtype(int specialtype) {

this.specialtype = specialtype;

}

public int getSpecialtype() {

return this.specialtype;

}


public void setMaxownedcount(int maxownedcount) {

this.maxownedcount = maxownedcount;

}

public int getMaxownedcount() {

return this.maxownedcount;

}


public void setAutouse(boolean autouse) {

this.autouse = autouse;

}

public boolean getAutouse() {

return this.autouse;

}


public void setUseneedlv(int useneedlv) {

this.useneedlv = useneedlv;

}

public int getUseneedlv() {

return this.useneedlv;

}


public void setUsecostitem(int[][] usecostitem) {

this.usecostitem = usecostitem;

}

public int[][] getUsecostitem() {

return this.usecostitem;

}


public void setUsetimeslimit(int usetimeslimit) {

this.usetimeslimit = usetimeslimit;

}

public int getUsetimeslimit() {

return this.usetimeslimit;

}


public void setDailyusetimeslimit(int dailyusetimeslimit) {

this.dailyusetimeslimit = dailyusetimeslimit;

}

public int getDailyusetimeslimit() {

return this.dailyusetimeslimit;

}




}
