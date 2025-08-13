package cfg;
import model.base.baseConfigObject;
public class  CrazyDuelDanObject implements baseConfigObject{



private int dan;

private int openfloor;

private int[] buffpool;

private int buffnum;

private int winscore;

private int failscore;

private int coefficient;

private int lowerlimit;

private int upperlimit;

private int[][] reward;

private int[][] coefficientreawrd;




public void setDan(int dan) {

this.dan = dan;

}

public int getDan() {

return this.dan;

}


public void setOpenfloor(int openfloor) {

this.openfloor = openfloor;

}

public int getOpenfloor() {

return this.openfloor;

}


public void setBuffpool(int[] buffpool) {

this.buffpool = buffpool;

}

public int[] getBuffpool() {

return this.buffpool;

}


public void setBuffnum(int buffnum) {

this.buffnum = buffnum;

}

public int getBuffnum() {

return this.buffnum;

}


public void setWinscore(int winscore) {

this.winscore = winscore;

}

public int getWinscore() {

return this.winscore;

}


public void setFailscore(int failscore) {

this.failscore = failscore;

}

public int getFailscore() {

return this.failscore;

}


public void setCoefficient(int coefficient) {

this.coefficient = coefficient;

}

public int getCoefficient() {

return this.coefficient;

}


public void setLowerlimit(int lowerlimit) {

this.lowerlimit = lowerlimit;

}

public int getLowerlimit() {

return this.lowerlimit;

}


public void setUpperlimit(int upperlimit) {

this.upperlimit = upperlimit;

}

public int getUpperlimit() {

return this.upperlimit;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setCoefficientreawrd(int[][] coefficientreawrd) {

this.coefficientreawrd = coefficientreawrd;

}

public int[][] getCoefficientreawrd() {

return this.coefficientreawrd;

}




}
