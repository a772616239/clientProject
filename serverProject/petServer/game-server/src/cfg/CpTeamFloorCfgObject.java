package cfg;
import model.base.baseConfigObject;
public class  CpTeamFloorCfgObject implements baseConfigObject{



private int id;

private int[][] monstercfg;

private int[][] eventhappen;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMonstercfg(int[][] monstercfg) {

this.monstercfg = monstercfg;

}

public int[][] getMonstercfg() {

return this.monstercfg;

}


public void setEventhappen(int[][] eventhappen) {

this.eventhappen = eventhappen;

}

public int[][] getEventhappen() {

return this.eventhappen;

}




}
