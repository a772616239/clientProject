package cfg;
import model.base.baseConfigObject;
public class  HeadBorderObject implements baseConfigObject{



private int id;

private boolean isdefault;

private int expiretime;

private int name;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setIsdefault(boolean isdefault) {

this.isdefault = isdefault;

}

public boolean getIsdefault() {

return this.isdefault;

}


public void setExpiretime(int expiretime) {

this.expiretime = expiretime;

}

public int getExpiretime() {

return this.expiretime;

}


public void setName(int name) {

this.name = name;

}

public int getName() {

return this.name;

}




}
