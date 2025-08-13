package cfg;
import model.base.baseConfigObject;
public class  TheWarJobTileConfigObject implements baseConfigObject{



private int id;

private int maxpetcount;

private int teammaxpetcount;

private int maxoccupygirdcount;

private int maxtechlevel;

private int[] achievecondition;

private int[][] jobtilereward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMaxpetcount(int maxpetcount) {

this.maxpetcount = maxpetcount;

}

public int getMaxpetcount() {

return this.maxpetcount;

}


public void setTeammaxpetcount(int teammaxpetcount) {

this.teammaxpetcount = teammaxpetcount;

}

public int getTeammaxpetcount() {

return this.teammaxpetcount;

}


public void setMaxoccupygirdcount(int maxoccupygirdcount) {

this.maxoccupygirdcount = maxoccupygirdcount;

}

public int getMaxoccupygirdcount() {

return this.maxoccupygirdcount;

}


public void setMaxtechlevel(int maxtechlevel) {

this.maxtechlevel = maxtechlevel;

}

public int getMaxtechlevel() {

return this.maxtechlevel;

}


public void setAchievecondition(int[] achievecondition) {

this.achievecondition = achievecondition;

}

public int[] getAchievecondition() {

return this.achievecondition;

}


public void setJobtilereward(int[][] jobtilereward) {

this.jobtilereward = jobtilereward;

}

public int[][] getJobtilereward() {

return this.jobtilereward;

}




}
