package cfg;
import model.base.baseConfigObject;
public class  CrossArenaPvpObject implements baseConfigObject{



private int id;

private int[][] consume;

private int[] powerlimit;

private int[] levellimit;

private int petnum;

private int blackpetnum;

private int time;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setConsume(int[][] consume) {

this.consume = consume;

}

public int[][] getConsume() {

return this.consume;

}


public void setPowerlimit(int[] powerlimit) {

this.powerlimit = powerlimit;

}

public int[] getPowerlimit() {

return this.powerlimit;

}


public void setLevellimit(int[] levellimit) {

this.levellimit = levellimit;

}

public int[] getLevellimit() {

return this.levellimit;

}


public void setPetnum(int petnum) {

this.petnum = petnum;

}

public int getPetnum() {

return this.petnum;

}


public void setBlackpetnum(int blackpetnum) {

this.blackpetnum = blackpetnum;

}

public int getBlackpetnum() {

return this.blackpetnum;

}


public void setTime(int time) {

this.time = time;

}

public int getTime() {

return this.time;

}




}
