package cfg;
import model.base.baseConfigObject;
public class  TheWarFightMakeConfigObject implements baseConfigObject{



private int id;

private int[] fightmakelist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setFightmakelist(int[] fightmakelist) {

this.fightmakelist = fightmakelist;

}

public int[] getFightmakelist() {

return this.fightmakelist;

}




}
