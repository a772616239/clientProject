package cfg;

import model.base.baseConfigObject;
public class  PlayerskillObject implements baseConfigObject{



private int id;

private int name;

private String skillres;

private int unlock;

private int desc;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setName(int name) {

this.name = name;

}

public int getName() {

return this.name;

}


public void setSkillres(String skillres) {

this.skillres = skillres;

}

public String getSkillres() {

return this.skillres;

}


public void setUnlock(int unlock) {

this.unlock = unlock;

}

public int getUnlock() {

return this.unlock;

}


public void setDesc(int desc) {

this.desc = desc;

}

public int getDesc() {

return this.desc;

}




}
