package cfg;
import model.base.baseConfigObject;
public class  ShopConfigObject implements baseConfigObject{



private int id;

private int autorefresh;

private int refreshtime;

private int manualrefresh;

private int refhcountevyday;

private int randommincount;

private int randommaxcount;

private int[][] randomgroupcfg;

private int[] notjoinmanual;

private int initcycle;

private int generalcycle ;

private int[][] goodscycle;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setAutorefresh(int autorefresh) {

this.autorefresh = autorefresh;

}

public int getAutorefresh() {

return this.autorefresh;

}


public void setRefreshtime(int refreshtime) {

this.refreshtime = refreshtime;

}

public int getRefreshtime() {

return this.refreshtime;

}


public void setManualrefresh(int manualrefresh) {

this.manualrefresh = manualrefresh;

}

public int getManualrefresh() {

return this.manualrefresh;

}


public void setRefhcountevyday(int refhcountevyday) {

this.refhcountevyday = refhcountevyday;

}

public int getRefhcountevyday() {

return this.refhcountevyday;

}


public void setRandommincount(int randommincount) {

this.randommincount = randommincount;

}

public int getRandommincount() {

return this.randommincount;

}


public void setRandommaxcount(int randommaxcount) {

this.randommaxcount = randommaxcount;

}

public int getRandommaxcount() {

return this.randommaxcount;

}


public void setRandomgroupcfg(int[][] randomgroupcfg) {

this.randomgroupcfg = randomgroupcfg;

}

public int[][] getRandomgroupcfg() {

return this.randomgroupcfg;

}


public void setNotjoinmanual(int[] notjoinmanual) {

this.notjoinmanual = notjoinmanual;

}

public int[] getNotjoinmanual() {

return this.notjoinmanual;

}


public void setInitcycle(int initcycle) {

this.initcycle = initcycle;

}

public int getInitcycle() {

return this.initcycle;

}


public void setGeneralcycle (int generalcycle ) {

this.generalcycle  = generalcycle ;

}

public int getGeneralcycle () {

return this.generalcycle ;

}


public void setGoodscycle(int[][] goodscycle) {

this.goodscycle = goodscycle;

}

public int[][] getGoodscycle() {

return this.goodscycle;

}




}
