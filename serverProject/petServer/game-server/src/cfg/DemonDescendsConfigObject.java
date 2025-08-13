package cfg;
import model.base.baseConfigObject;
public class  DemonDescendsConfigObject implements baseConfigObject{



private int id;

private int[] drawuseitem;

private int rechargerewardneedcoupon;

private int eachrechargerewards;

private int buyupperlimit;

private int[] price;

private int[] dailymission;

private int eachdrawscore;

private int rankingneedminscore;

private int scorerankingtemplate;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDrawuseitem(int[] drawuseitem) {

this.drawuseitem = drawuseitem;

}

public int[] getDrawuseitem() {

return this.drawuseitem;

}


public void setRechargerewardneedcoupon(int rechargerewardneedcoupon) {

this.rechargerewardneedcoupon = rechargerewardneedcoupon;

}

public int getRechargerewardneedcoupon() {

return this.rechargerewardneedcoupon;

}


public void setEachrechargerewards(int eachrechargerewards) {

this.eachrechargerewards = eachrechargerewards;

}

public int getEachrechargerewards() {

return this.eachrechargerewards;

}


public void setBuyupperlimit(int buyupperlimit) {

this.buyupperlimit = buyupperlimit;

}

public int getBuyupperlimit() {

return this.buyupperlimit;

}


public void setPrice(int[] price) {

this.price = price;

}

public int[] getPrice() {

return this.price;

}


public void setDailymission(int[] dailymission) {

this.dailymission = dailymission;

}

public int[] getDailymission() {

return this.dailymission;

}


public void setEachdrawscore(int eachdrawscore) {

this.eachdrawscore = eachdrawscore;

}

public int getEachdrawscore() {

return this.eachdrawscore;

}


public void setRankingneedminscore(int rankingneedminscore) {

this.rankingneedminscore = rankingneedminscore;

}

public int getRankingneedminscore() {

return this.rankingneedminscore;

}


public void setScorerankingtemplate(int scorerankingtemplate) {

this.scorerankingtemplate = scorerankingtemplate;

}

public int getScorerankingtemplate() {

return this.scorerankingtemplate;

}




}
