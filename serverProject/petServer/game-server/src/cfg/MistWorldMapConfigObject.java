package cfg;
import model.base.baseConfigObject;
public class  MistWorldMapConfigObject implements baseConfigObject{



private int mapid;

private int maprule;

private int level;

private int maxplayercount;

private int[] mapsize;

private int[] aoiarea;

private int[] saferegion;

private int[][] teleporterlist;




public void setMapid(int mapid) {

this.mapid = mapid;

}

public int getMapid() {

return this.mapid;

}


public void setMaprule(int maprule) {

this.maprule = maprule;

}

public int getMaprule() {

return this.maprule;

}


public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setMaxplayercount(int maxplayercount) {

this.maxplayercount = maxplayercount;

}

public int getMaxplayercount() {

return this.maxplayercount;

}


public void setMapsize(int[] mapsize) {

this.mapsize = mapsize;

}

public int[] getMapsize() {

return this.mapsize;

}


public void setAoiarea(int[] aoiarea) {

this.aoiarea = aoiarea;

}

public int[] getAoiarea() {

return this.aoiarea;

}


public void setSaferegion(int[] saferegion) {

this.saferegion = saferegion;

}

public int[] getSaferegion() {

return this.saferegion;

}


public void setTeleporterlist(int[][] teleporterlist) {

this.teleporterlist = teleporterlist;

}

public int[][] getTeleporterlist() {

return this.teleporterlist;

}




}
