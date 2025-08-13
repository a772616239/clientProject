package cfg;
import model.base.baseConfigObject;
public class  BossTowerBossBuffCountConfigObject implements baseConfigObject{



private int difficult;

private int[][] buffcount;




public void setDifficult(int difficult) {

this.difficult = difficult;

}

public int getDifficult() {

return this.difficult;

}


public void setBuffcount(int[][] buffcount) {

this.buffcount = buffcount;

}

public int[][] getBuffcount() {

return this.buffcount;

}




}
