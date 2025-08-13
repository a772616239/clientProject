package cfg;
import model.base.baseConfigObject;
public class  MistMagicGuardConfigObject implements baseConfigObject{



private int id;

private int rewardconfigid;

private int lifetime;

private int[][] extbufflist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRewardconfigid(int rewardconfigid) {

this.rewardconfigid = rewardconfigid;

}

public int getRewardconfigid() {

return this.rewardconfigid;

}


public void setLifetime(int lifetime) {

this.lifetime = lifetime;

}

public int getLifetime() {

return this.lifetime;

}


public void setExtbufflist(int[][] extbufflist) {

this.extbufflist = extbufflist;

}

public int[][] getExtbufflist() {

return this.extbufflist;

}




}
