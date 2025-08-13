package cfg;
import model.base.baseConfigObject;
public class  ShuraArenaConfigObject implements baseConfigObject{



private int id;

private int[] rankrange;

private int showsize;

private int[][] monsterpool;

private int bossawakelv;

private int[] bosspool;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRankrange(int[] rankrange) {

this.rankrange = rankrange;

}

public int[] getRankrange() {

return this.rankrange;

}


public void setShowsize(int showsize) {

this.showsize = showsize;

}

public int getShowsize() {

return this.showsize;

}


public void setMonsterpool(int[][] monsterpool) {

this.monsterpool = monsterpool;

}

public int[][] getMonsterpool() {

return this.monsterpool;

}


public void setBossawakelv(int bossawakelv) {

this.bossawakelv = bossawakelv;

}

public int getBossawakelv() {

return this.bossawakelv;

}


public void setBosspool(int[] bosspool) {

this.bosspool = bosspool;

}

public int[] getBosspool() {

return this.bosspool;

}




}
