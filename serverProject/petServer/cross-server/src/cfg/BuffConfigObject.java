package cfg;
import model.base.baseConfigObject;

public class  BuffConfigObject implements baseConfigObject{



private int id;

private int fixability1;

private int fixability2;

private int abilityaddtion;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setFixability1(int fixability1) {

this.fixability1 = fixability1;

}

public int getFixability1() {

return this.fixability1;

}


public void setFixability2(int fixability2) {

this.fixability2 = fixability2;

}

public int getFixability2() {

return this.fixability2;

}


public void setAbilityaddtion(int abilityaddtion) {

this.abilityaddtion = abilityaddtion;

}

public int getAbilityaddtion() {

return this.abilityaddtion;

}




}
