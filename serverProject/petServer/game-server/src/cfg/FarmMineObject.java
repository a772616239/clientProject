package cfg;
import model.base.baseConfigObject;
public class  FarmMineObject implements baseConfigObject{



private int id;

private int wight;

private int[] baseaward;

private int extnum;

private int[] extaward;

private int[] petadd;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setWight(int wight) {

this.wight = wight;

}

public int getWight() {

return this.wight;

}


public void setBaseaward(int[] baseaward) {

this.baseaward = baseaward;

}

public int[] getBaseaward() {

return this.baseaward;

}


public void setExtnum(int extnum) {

this.extnum = extnum;

}

public int getExtnum() {

return this.extnum;

}


public void setExtaward(int[] extaward) {

this.extaward = extaward;

}

public int[] getExtaward() {

return this.extaward;

}


public void setPetadd(int[] petadd) {

this.petadd = petadd;

}

public int[] getPetadd() {

return this.petadd;

}




}
