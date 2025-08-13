package cfg;
import model.base.baseConfigObject;
public class  ResourceCopyConfigObject implements baseConfigObject{



private int id;

private int challengetimes;

private int[] buytimesconsume;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setChallengetimes(int challengetimes) {

this.challengetimes = challengetimes;

}

public int getChallengetimes() {

return this.challengetimes;

}


public void setBuytimesconsume(int[] buytimesconsume) {

this.buytimesconsume = buytimesconsume;

}

public int[] getBuytimesconsume() {

return this.buytimesconsume;

}




}
