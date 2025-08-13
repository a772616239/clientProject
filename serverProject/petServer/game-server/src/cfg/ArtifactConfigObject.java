package cfg;
import model.base.baseConfigObject;
public class  ArtifactConfigObject implements baseConfigObject{



private int key;

private int playerskillid;

private int[] enhancepointid;

private int[] starconfgid;

private String tip_name;

private int[][] talent;




public void setKey(int key) {

this.key = key;

}

public int getKey() {

return this.key;

}


public void setPlayerskillid(int playerskillid) {

this.playerskillid = playerskillid;

}

public int getPlayerskillid() {

return this.playerskillid;

}


public void setEnhancepointid(int[] enhancepointid) {

this.enhancepointid = enhancepointid;

}

public int[] getEnhancepointid() {

return this.enhancepointid;

}


public void setStarconfgid(int[] starconfgid) {

this.starconfgid = starconfgid;

}

public int[] getStarconfgid() {

return this.starconfgid;

}


public void setTip_name(String tip_name) {

this.tip_name = tip_name;

}

public String getTip_name() {

return this.tip_name;

}


public void setTalent(int[][] talent) {

this.talent = talent;

}

public int[][] getTalent() {

return this.talent;

}




}
