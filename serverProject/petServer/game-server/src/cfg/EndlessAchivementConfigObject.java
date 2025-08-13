package cfg;
import model.base.baseConfigObject;
public class  EndlessAchivementConfigObject implements baseConfigObject{



private int stepid;

private int startlayer;

private int layergap;

private int[] rewardlist;

private int artifactid;




public void setStepid(int stepid) {

this.stepid = stepid;

}

public int getStepid() {

return this.stepid;

}


public void setStartlayer(int startlayer) {

this.startlayer = startlayer;

}

public int getStartlayer() {

return this.startlayer;

}


public void setLayergap(int layergap) {

this.layergap = layergap;

}

public int getLayergap() {

return this.layergap;

}


public void setRewardlist(int[] rewardlist) {

this.rewardlist = rewardlist;

}

public int[] getRewardlist() {

return this.rewardlist;

}


public void setArtifactid(int artifactid) {

this.artifactid = artifactid;

}

public int getArtifactid() {

return this.artifactid;

}




}
