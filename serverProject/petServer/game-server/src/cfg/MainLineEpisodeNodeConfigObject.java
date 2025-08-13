package cfg;
import model.base.baseConfigObject;
public class  MainLineEpisodeNodeConfigObject implements baseConfigObject{



private int id;

private int type;

private int[][] showreward;

private int[] beforeplot;

private int fightmakeid;

private int[] laterplot;

private int[] helppetpool;

private int helppetnum;

private int[][] playerskill;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setShowreward(int[][] showreward) {

this.showreward = showreward;

}

public int[][] getShowreward() {

return this.showreward;

}


public void setBeforeplot(int[] beforeplot) {

this.beforeplot = beforeplot;

}

public int[] getBeforeplot() {

return this.beforeplot;

}


public void setFightmakeid(int fightmakeid) {

this.fightmakeid = fightmakeid;

}

public int getFightmakeid() {

return this.fightmakeid;

}


public void setLaterplot(int[] laterplot) {

this.laterplot = laterplot;

}

public int[] getLaterplot() {

return this.laterplot;

}


public void setHelppetpool(int[] helppetpool) {

this.helppetpool = helppetpool;

}

public int[] getHelppetpool() {

return this.helppetpool;

}


public void setHelppetnum(int helppetnum) {

this.helppetnum = helppetnum;

}

public int getHelppetnum() {

return this.helppetnum;

}


public void setPlayerskill(int[][] playerskill) {

this.playerskill = playerskill;

}

public int[][] getPlayerskill() {

return this.playerskill;

}




}
