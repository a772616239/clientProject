package cfg;
import model.base.baseConfigObject;
public class  GiftConfigObject implements baseConfigObject{



private int id;

private String icon;

private int desc;

private int begintime;

private int endtime;

private int displayendtime;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setIcon(String icon) {

this.icon = icon;

}

public String getIcon() {

return this.icon;

}


public void setDesc(int desc) {

this.desc = desc;

}

public int getDesc() {

return this.desc;

}


public void setBegintime(int begintime) {

this.begintime = begintime;

}

public int getBegintime() {

return this.begintime;

}


public void setEndtime(int endtime) {

this.endtime = endtime;

}

public int getEndtime() {

return this.endtime;

}


public void setDisplayendtime(int displayendtime) {

this.displayendtime = displayendtime;

}

public int getDisplayendtime() {

return this.displayendtime;

}




}
