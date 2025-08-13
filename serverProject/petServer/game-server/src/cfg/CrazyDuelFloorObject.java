package cfg;
import model.base.baseConfigObject;
public class  CrazyDuelFloorObject implements baseConfigObject{



private int floor;

private int[] fixbuffpool;

private int fixbuffnum;

private int[][] randombuff;

private int randumbuffnum;

private int lastbuffposrarity;

private int[] exbuffposappeare;

private int[][] fightreward;




public void setFloor(int floor) {

this.floor = floor;

}

public int getFloor() {

return this.floor;

}


public void setFixbuffpool(int[] fixbuffpool) {

this.fixbuffpool = fixbuffpool;

}

public int[] getFixbuffpool() {

return this.fixbuffpool;

}


public void setFixbuffnum(int fixbuffnum) {

this.fixbuffnum = fixbuffnum;

}

public int getFixbuffnum() {

return this.fixbuffnum;

}


public void setRandombuff(int[][] randombuff) {

this.randombuff = randombuff;

}

public int[][] getRandombuff() {

return this.randombuff;

}


public void setRandumbuffnum(int randumbuffnum) {

this.randumbuffnum = randumbuffnum;

}

public int getRandumbuffnum() {

return this.randumbuffnum;

}


public void setLastbuffposrarity(int lastbuffposrarity) {

this.lastbuffposrarity = lastbuffposrarity;

}

public int getLastbuffposrarity() {

return this.lastbuffposrarity;

}


public void setExbuffposappeare(int[] exbuffposappeare) {

this.exbuffposappeare = exbuffposappeare;

}

public int[] getExbuffposappeare() {

return this.exbuffposappeare;

}


public void setFightreward(int[][] fightreward) {

this.fightreward = fightreward;

}

public int[][] getFightreward() {

return this.fightreward;

}




}
