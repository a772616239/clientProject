package cfg;
import model.base.baseConfigObject;
public class  MistMoveEffectConfigObject implements baseConfigObject{



private int id;

private int expiretime;

private int[] extendbufflist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setExpiretime(int expiretime) {

this.expiretime = expiretime;

}

public int getExpiretime() {

return this.expiretime;

}


public void setExtendbufflist(int[] extendbufflist) {

this.extendbufflist = extendbufflist;

}

public int[] getExtendbufflist() {

return this.extendbufflist;

}




}
