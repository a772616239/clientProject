package cfg;
import model.base.baseConfigObject;
public class  PetRuneUpConsumeObject implements baseConfigObject{



private int key;

private int runeid;

private int runelvl;

private int[] consume;




public void setKey(int key) {

this.key = key;

}

public int getKey() {

return this.key;

}


public void setRuneid(int runeid) {

this.runeid = runeid;

}

public int getRuneid() {

return this.runeid;

}


public void setRunelvl(int runelvl) {

this.runelvl = runelvl;

}

public int getRunelvl() {

return this.runelvl;

}


public void setConsume(int[] consume) {

this.consume = consume;

}

public int[] getConsume() {

return this.consume;

}




}
