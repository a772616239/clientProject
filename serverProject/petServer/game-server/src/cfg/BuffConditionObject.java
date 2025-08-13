package cfg;
import model.base.baseConfigObject;
public class  BuffConditionObject implements baseConfigObject{



private int id;

private int pettype;

private int petclass;

private int onbattleindex;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setPettype(int pettype) {

this.pettype = pettype;

}

public int getPettype() {

return this.pettype;

}


public void setPetclass(int petclass) {

this.petclass = petclass;

}

public int getPetclass() {

return this.petclass;

}


public void setOnbattleindex(int onbattleindex) {

this.onbattleindex = onbattleindex;

}

public int getOnbattleindex() {

return this.onbattleindex;

}




}
