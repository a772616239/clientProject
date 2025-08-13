package cfg;
import model.base.baseConfigObject;
public class  MistActivityBossConfigObject implements baseConfigObject{



private int id;

private int bossunittype;

private int changestagehprate;

private int[][] dropboxlist;

private int[][] activtiybossindividualprops;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setBossunittype(int bossunittype) {

this.bossunittype = bossunittype;

}

public int getBossunittype() {

return this.bossunittype;

}


public void setChangestagehprate(int changestagehprate) {

this.changestagehprate = changestagehprate;

}

public int getChangestagehprate() {

return this.changestagehprate;

}


public void setDropboxlist(int[][] dropboxlist) {

this.dropboxlist = dropboxlist;

}

public int[][] getDropboxlist() {

return this.dropboxlist;

}


public void setActivtiybossindividualprops(int[][] activtiybossindividualprops) {

this.activtiybossindividualprops = activtiybossindividualprops;

}

public int[][] getActivtiybossindividualprops() {

return this.activtiybossindividualprops;

}




}
