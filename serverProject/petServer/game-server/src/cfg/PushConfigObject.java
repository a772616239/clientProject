package cfg;
import model.base.baseConfigObject;
public class  PushConfigObject implements baseConfigObject{



private int id;

private int leadtime;

private int cycle;

private int title;

private int content;

private String[] pushtime;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLeadtime(int leadtime) {

this.leadtime = leadtime;

}

public int getLeadtime() {

return this.leadtime;

}


public void setCycle(int cycle) {

this.cycle = cycle;

}

public int getCycle() {

return this.cycle;

}


public void setTitle(int title) {

this.title = title;

}

public int getTitle() {

return this.title;

}


public void setContent(int content) {

this.content = content;

}

public int getContent() {

return this.content;

}


public void setPushtime(String[] pushtime) {

this.pushtime = pushtime;

}

public String[] getPushtime() {

return this.pushtime;

}




}
