package cfg;
import model.base.baseConfigObject;
public class  LocalActivityOpenTimeObject implements baseConfigObject{



private int id;

private int title;

private int desc;

private int detail;

private String icon;

private String startdistime;

private String begintime;

private String endtime;

private String overdistime;

private int[] missionlist;

private int tabtype;

private int reddottype;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

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


public void setIcon(String icon) {

this.icon = icon;

}

public String getIcon() {

return this.icon;

}


public void setStartdistime(String startdistime) {

this.startdistime = startdistime;

}

public String getStartdistime() {

return this.startdistime;

}


public void setBegintime(String begintime) {

this.begintime = begintime;

}

public String getBegintime() {

return this.begintime;

}


public void setEndtime(String endtime) {

this.endtime = endtime;

}

public String getEndtime() {

return this.endtime;

}


public void setOverdistime(String overdistime) {

this.overdistime = overdistime;

}

public String getOverdistime() {

return this.overdistime;

}


public void setMissionlist(int[] missionlist) {

this.missionlist = missionlist;

}

public int[] getMissionlist() {

return this.missionlist;

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
