package cfg;
import model.base.baseConfigObject;
public class  NeeBeeGiftActivityCfgObject implements baseConfigObject{



private int id;

private String picture;

private int title;

private int desc;

private int enddistime;




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


public void setEnddistime(int enddistime) {

this.enddistime = enddistime;

}

public int getEnddistime() {

return this.enddistime;

}




}
