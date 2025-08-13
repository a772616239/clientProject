package cfg;
import model.base.baseConfigObject;
public class  MarqueeObject implements baseConfigObject{



private int id;

private String starttime;

private String endtime;

private int marqueetemplateid;

private int interval;

private int cycletype;

private int[] validday;

private int[][] timescope;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStarttime(String starttime) {

this.starttime = starttime;

}

public String getStarttime() {

return this.starttime;

}


public void setEndtime(String endtime) {

this.endtime = endtime;

}

public String getEndtime() {

return this.endtime;

}


public void setMarqueetemplateid(int marqueetemplateid) {

this.marqueetemplateid = marqueetemplateid;

}

public int getMarqueetemplateid() {

return this.marqueetemplateid;

}


public void setInterval(int interval) {

this.interval = interval;

}

public int getInterval() {

return this.interval;

}


public void setCycletype(int cycletype) {

this.cycletype = cycletype;

}

public int getCycletype() {

return this.cycletype;

}


public void setValidday(int[] validday) {

this.validday = validday;

}

public int[] getValidday() {

return this.validday;

}


public void setTimescope(int[][] timescope) {

this.timescope = timescope;

}

public int[][] getTimescope() {

return this.timescope;

}




}
