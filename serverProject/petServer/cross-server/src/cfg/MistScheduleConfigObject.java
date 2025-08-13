package cfg;
import model.base.baseConfigObject;
public class  MistScheduleConfigObject implements baseConfigObject{



private int id;

private int mistlevel;

private int scheduletype;

private int dailystarttime;

private int duration;

private int interval;

private int[][] closedsection;

private boolean removeclosedsectionobj;

private int[] initobjdata;

private int[] refreshobjdata;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMistlevel(int mistlevel) {

this.mistlevel = mistlevel;

}

public int getMistlevel() {

return this.mistlevel;

}


public void setScheduletype(int scheduletype) {

this.scheduletype = scheduletype;

}

public int getScheduletype() {

return this.scheduletype;

}


public void setDailystarttime(int dailystarttime) {

this.dailystarttime = dailystarttime;

}

public int getDailystarttime() {

return this.dailystarttime;

}


public void setDuration(int duration) {

this.duration = duration;

}

public int getDuration() {

return this.duration;

}


public void setInterval(int interval) {

this.interval = interval;

}

public int getInterval() {

return this.interval;

}


public void setClosedsection(int[][] closedsection) {

this.closedsection = closedsection;

}

public int[][] getClosedsection() {

return this.closedsection;

}


public void setRemoveclosedsectionobj(boolean removeclosedsectionobj) {

this.removeclosedsectionobj = removeclosedsectionobj;

}

public boolean getRemoveclosedsectionobj() {

return this.removeclosedsectionobj;

}


public void setInitobjdata(int[] initobjdata) {

this.initobjdata = initobjdata;

}

public int[] getInitobjdata() {

return this.initobjdata;

}


public void setRefreshobjdata(int[] refreshobjdata) {

this.refreshobjdata = refreshobjdata;

}

public int[] getRefreshobjdata() {

return this.refreshobjdata;

}




}
