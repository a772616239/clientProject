package cfg;
import model.base.baseConfigObject;

public class  MistMapConfigObject implements baseConfigObject{



private int mapid;

private int[][] mapblock;

private int[][] mapblock1;

private int[][] mapblock2;




public void setMapid(int mapid) {

this.mapid = mapid;

}

public int getMapid() {

return this.mapid;

}


public void setMapblock(int[][] mapblock) {

this.mapblock = mapblock;

}

public int[][] getMapblock() {

return this.mapblock;

}


public void setMapblock1(int[][] mapblock1) {

this.mapblock1 = mapblock1;

}

public int[][] getMapblock1() {

return this.mapblock1;

}


public void setMapblock2(int[][] mapblock2) {

this.mapblock2 = mapblock2;

}

public int[][] getMapblock2() {

return this.mapblock2;

}




}
