package cfg;
import model.base.baseConfigObject;
public class  MarqueeTemplateObject implements baseConfigObject{



private int id;

private int content;

private int rolltimes;

private int[] scene;

private int priority;

private int duration;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setContent(int content) {

this.content = content;

}

public int getContent() {

return this.content;

}


public void setRolltimes(int rolltimes) {

this.rolltimes = rolltimes;

}

public int getRolltimes() {

return this.rolltimes;

}


public void setScene(int[] scene) {

this.scene = scene;

}

public int[] getScene() {

return this.scene;

}


public void setPriority(int priority) {

this.priority = priority;

}

public int getPriority() {

return this.priority;

}


public void setDuration(int duration) {

this.duration = duration;

}

public int getDuration() {

return this.duration;

}




}
