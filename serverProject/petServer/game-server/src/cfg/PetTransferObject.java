package cfg;
import model.base.baseConfigObject;
public class  PetTransferObject implements baseConfigObject{



private int id;

private int targetpetid;

private int odds;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setTargetpetid(int targetpetid) {

this.targetpetid = targetpetid;

}

public int getTargetpetid() {

return this.targetpetid;

}


public void setOdds(int odds) {

this.odds = odds;

}

public int getOdds() {

return this.odds;

}




}
