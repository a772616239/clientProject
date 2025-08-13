package cfg;
import model.base.baseConfigObject;
public class  TimeLimitActivityObject implements baseConfigObject{



private int id;

private String picture;

private int title;

private int desc;

private int detail;

private int enddistime;

private int openlv;

private int showlv;

private int[] tasklist;

private int tabtype;

private int reddottype;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setPicture(String picture) {

this.picture = picture;

}

public String getPicture() {

return this.picture;

}


public void setTitle(int title) {

this.title = title;

}

public int getTitle() {

return this.title;

}


public void setDesc(int desc) {

this.desc = desc;

}

public int getDesc() {

return this.desc;

}


public void setDetail(int detail) {

this.detail = detail;

}

public int getDetail() {

return this.detail;

}


public void setEnddistime(int enddistime) {

this.enddistime = enddistime;

}

public int getEnddistime() {

return this.enddistime;

}


public void setOpenlv(int openlv) {

this.openlv = openlv;

}

public int getOpenlv() {

return this.openlv;

}


public void setShowlv(int showlv) {

this.showlv = showlv;

}

public int getShowlv() {

return this.showlv;

}


public void setTasklist(int[] tasklist) {

this.tasklist = tasklist;

}

public int[] getTasklist() {

return this.tasklist;

}


public void setTabtype(int tabtype) {

this.tabtype = tabtype;

}

public int getTabtype() {

return this.tabtype;

}


public void setReddottype(int reddottype) {

this.reddottype = reddottype;

}

public int getReddottype() {

return this.reddottype;

}




}
