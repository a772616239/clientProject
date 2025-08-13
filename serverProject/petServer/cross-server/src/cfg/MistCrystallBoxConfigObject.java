package cfg;
import model.base.baseConfigObject;
public class  MistCrystallBoxConfigObject implements baseConfigObject{



private int id;

private int[] optionallist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setOptionallist(int[] optionallist) {

this.optionallist = optionallist;

}

public int[] getOptionallist() {

return this.optionallist;

}




}
