package cfg;
import model.base.baseConfigObject;
public class  ArenaFreeTicketsObject implements baseConfigObject{



private int id;

private int[] sendtime;

private int lvlimit;

private int mailtemplate;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setSendtime(int[] sendtime) {

this.sendtime = sendtime;

}

public int[] getSendtime() {

return this.sendtime;

}


public void setLvlimit(int lvlimit) {

this.lvlimit = lvlimit;

}

public int getLvlimit() {

return this.lvlimit;

}


public void setMailtemplate(int mailtemplate) {

this.mailtemplate = mailtemplate;

}

public int getMailtemplate() {

return this.mailtemplate;

}




}
