package cfg;
import model.base.baseConfigObject;
public class  PointCopyOpenTimeObject implements baseConfigObject{



private int id;

private int[] starttime;

private int[] endtime;

private int[] fightlist;

private int[] pointlist;

private int defaultunlockfightid;

private int[] dropticket;

private int pointcopyticketdropodds;

private int pointcopyticketdropinterval;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStarttime(int[] starttime) {

this.starttime = starttime;

}

public int[] getStarttime() {

return this.starttime;

}


public void setEndtime(int[] endtime) {

this.endtime = endtime;

}

public int[] getEndtime() {

return this.endtime;

}


public void setFightlist(int[] fightlist) {

this.fightlist = fightlist;

}

public int[] getFightlist() {

return this.fightlist;

}


public void setPointlist(int[] pointlist) {

this.pointlist = pointlist;

}

public int[] getPointlist() {

return this.pointlist;

}


public void setDefaultunlockfightid(int defaultunlockfightid) {

this.defaultunlockfightid = defaultunlockfightid;

}

public int getDefaultunlockfightid() {

return this.defaultunlockfightid;

}


public void setDropticket(int[] dropticket) {

this.dropticket = dropticket;

}

public int[] getDropticket() {

return this.dropticket;

}


public void setPointcopyticketdropodds(int pointcopyticketdropodds) {

this.pointcopyticketdropodds = pointcopyticketdropodds;

}

public int getPointcopyticketdropodds() {

return this.pointcopyticketdropodds;

}


public void setPointcopyticketdropinterval(int pointcopyticketdropinterval) {

this.pointcopyticketdropinterval = pointcopyticketdropinterval;

}

public int getPointcopyticketdropinterval() {

return this.pointcopyticketdropinterval;

}




}
