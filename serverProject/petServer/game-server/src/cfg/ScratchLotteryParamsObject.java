package cfg;
import model.base.baseConfigObject;
public class  ScratchLotteryParamsObject implements baseConfigObject{



private int id;

private int length;

private int width;

private int needqualitycount;

private int[] consume;

private int[][] progressreward;

private int[][] rewardcountodds;

private int basereward;

private int[][] qualityodds;

private int[][] oddschangequality;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLength(int length) {

this.length = length;

}

public int getLength() {

return this.length;

}


public void setWidth(int width) {

this.width = width;

}

public int getWidth() {

return this.width;

}


public void setNeedqualitycount(int needqualitycount) {

this.needqualitycount = needqualitycount;

}

public int getNeedqualitycount() {

return this.needqualitycount;

}


public void setConsume(int[] consume) {

this.consume = consume;

}

public int[] getConsume() {

return this.consume;

}


public void setProgressreward(int[][] progressreward) {

this.progressreward = progressreward;

}

public int[][] getProgressreward() {

return this.progressreward;

}


public void setRewardcountodds(int[][] rewardcountodds) {

this.rewardcountodds = rewardcountodds;

}

public int[][] getRewardcountodds() {

return this.rewardcountodds;

}


public void setBasereward(int basereward) {

this.basereward = basereward;

}

public int getBasereward() {

return this.basereward;

}


public void setQualityodds(int[][] qualityodds) {

this.qualityodds = qualityodds;

}

public int[][] getQualityodds() {

return this.qualityodds;

}


public void setOddschangequality(int[][] oddschangequality) {

this.oddschangequality = oddschangequality;

}

public int[][] getOddschangequality() {

return this.oddschangequality;

}




}
