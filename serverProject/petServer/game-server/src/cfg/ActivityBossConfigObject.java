package cfg;
import model.base.baseConfigObject;
public class  ActivityBossConfigObject implements baseConfigObject{



private int id;

private int cfgid;

private int buffid;

private int fightmakeid;

private int opencecyle;

private int opendays;

private int displayaheadtime;

private int displaylagtime;

private String begibtime;

private String endtime;

private int times;

private int limitbuytimes;

private int[] buyprice;

private int help;

private int unlocklevel;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setCfgid(int cfgid) {

this.cfgid = cfgid;

}

public int getCfgid() {

return this.cfgid;

}


public void setBuffid(int buffid) {

this.buffid = buffid;

}

public int getBuffid() {

return this.buffid;

}


public void setFightmakeid(int fightmakeid) {

this.fightmakeid = fightmakeid;

}

public int getFightmakeid() {

return this.fightmakeid;

}


public void setOpencecyle(int opencecyle) {

this.opencecyle = opencecyle;

}

public int getOpencecyle() {

return this.opencecyle;

}


public void setOpendays(int opendays) {

this.opendays = opendays;

}

public int getOpendays() {

return this.opendays;

}


public void setDisplayaheadtime(int displayaheadtime) {

this.displayaheadtime = displayaheadtime;

}

public int getDisplayaheadtime() {

return this.displayaheadtime;

}


public void setDisplaylagtime(int displaylagtime) {

this.displaylagtime = displaylagtime;

}

public int getDisplaylagtime() {

return this.displaylagtime;

}


public void setBegibtime(String begibtime) {

this.begibtime = begibtime;

}

public String getBegibtime() {

return this.begibtime;

}


public void setEndtime(String endtime) {

this.endtime = endtime;

}

public String getEndtime() {

return this.endtime;

}


public void setTimes(int times) {

this.times = times;

}

public int getTimes() {

return this.times;

}


public void setLimitbuytimes(int limitbuytimes) {

this.limitbuytimes = limitbuytimes;

}

public int getLimitbuytimes() {

return this.limitbuytimes;

}


public void setBuyprice(int[] buyprice) {

this.buyprice = buyprice;

}

public int[] getBuyprice() {

return this.buyprice;

}


public void setHelp(int help) {

this.help = help;

}

public int getHelp() {

return this.help;

}


public void setUnlocklevel(int unlocklevel) {

this.unlocklevel = unlocklevel;

}

public int getUnlocklevel() {

return this.unlocklevel;

}




}
