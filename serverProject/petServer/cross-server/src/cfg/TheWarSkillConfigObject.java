package cfg;
import model.base.baseConfigObject;
public class  TheWarSkillConfigObject implements baseConfigObject{



private int id;

private int skillid;

private int skilllevel;

private int buffid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setSkillid(int skillid) {

this.skillid = skillid;

}

public int getSkillid() {

return this.skillid;

}


public void setSkilllevel(int skilllevel) {

this.skilllevel = skilllevel;

}

public int getSkilllevel() {

return this.skilllevel;

}


public void setBuffid(int buffid) {

this.buffid = buffid;

}

public int getBuffid() {

return this.buffid;

}




}
