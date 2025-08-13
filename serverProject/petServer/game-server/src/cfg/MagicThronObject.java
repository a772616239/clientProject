package cfg;
import model.base.baseConfigObject;
public class  MagicThronObject implements baseConfigObject{



private int id;

private int boss;

private int[] fightpoint;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setBoss(int boss) {

this.boss = boss;

}

public int getBoss() {

return this.boss;

}


public void setFightpoint(int[] fightpoint) {

this.fightpoint = fightpoint;

}

public int[] getFightpoint() {

return this.fightpoint;

}




}
