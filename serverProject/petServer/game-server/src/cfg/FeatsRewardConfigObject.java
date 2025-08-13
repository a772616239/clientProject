package cfg;
import model.base.baseConfigObject;
public class  FeatsRewardConfigObject implements baseConfigObject{



private int id;

private int featsneed;

private int type;

private int basicreward;

private int advancedreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setFeatsneed(int featsneed) {

this.featsneed = featsneed;

}

public int getFeatsneed() {

return this.featsneed;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setBasicreward(int basicreward) {

this.basicreward = basicreward;

}

public int getBasicreward() {

return this.basicreward;

}


public void setAdvancedreward(int advancedreward) {

this.advancedreward = advancedreward;

}

public int getAdvancedreward() {

return this.advancedreward;

}




}
