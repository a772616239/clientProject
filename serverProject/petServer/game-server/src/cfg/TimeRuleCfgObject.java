package cfg;
import model.base.baseConfigObject;
public class  TimeRuleCfgObject implements baseConfigObject{



private int id;

private int type;

private int father_id;

private String begin_time;

private String end_time;

private String open_time;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setFather_id(int father_id) {

this.father_id = father_id;

}

public int getFather_id() {

return this.father_id;

}


public void setBegin_time(String begin_time) {

this.begin_time = begin_time;

}

public String getBegin_time() {

return this.begin_time;

}


public void setEnd_time(String end_time) {

this.end_time = end_time;

}

public String getEnd_time() {

return this.end_time;

}


public void setOpen_time(String open_time) {

this.open_time = open_time;

}

public String getOpen_time() {

return this.open_time;

}




}
