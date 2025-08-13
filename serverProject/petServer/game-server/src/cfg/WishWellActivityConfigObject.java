package cfg;
import model.base.baseConfigObject;
public class  WishWellActivityConfigObject implements baseConfigObject{



private int id;

private String picture;

private int title;

private int desc;

private String startdistime;

private String overdistime;

private int duration;

private int help;




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


public void setStartdistime(String startdistime) {

this.startdistime = startdistime;

}

public String getStartdistime() {

return this.startdistime;

}


public void setOverdistime(String overdistime) {

this.overdistime = overdistime;

}

public String getOverdistime() {

return this.overdistime;

}


public void setDuration(int duration) {

this.duration = duration;

}

public int getDuration() {

return this.duration;

}


public void setHelp(int help) {

this.help = help;

}

public int getHelp() {

return this.help;

}




}
