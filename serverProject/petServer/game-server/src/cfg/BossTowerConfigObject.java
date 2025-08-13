package cfg;
import model.base.baseConfigObject;
public class  BossTowerConfigObject implements baseConfigObject{



private int id;

private int unlevel;

private int fightmakeid;

private int difficultfightmakeid;

private int unbeatablefirstreward;

private int unbeatablefightmakeid;

private int passlimit;

private int[][] commonrandom;

private int[][] difficultrandom;

private int[][] unbeatablerandom;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setUnlevel(int unlevel) {

this.unlevel = unlevel;

}

public int getUnlevel() {

return this.unlevel;

}


public void setFightmakeid(int fightmakeid) {

this.fightmakeid = fightmakeid;

}

public int getFightmakeid() {

return this.fightmakeid;

}


public void setDifficultfightmakeid(int difficultfightmakeid) {

this.difficultfightmakeid = difficultfightmakeid;

}

public int getDifficultfightmakeid() {

return this.difficultfightmakeid;

}


public void setUnbeatablefirstreward(int unbeatablefirstreward) {

this.unbeatablefirstreward = unbeatablefirstreward;

}

public int getUnbeatablefirstreward() {

return this.unbeatablefirstreward;

}


public void setUnbeatablefightmakeid(int unbeatablefightmakeid) {

this.unbeatablefightmakeid = unbeatablefightmakeid;

}

public int getUnbeatablefightmakeid() {

return this.unbeatablefightmakeid;

}


public void setPasslimit(int passlimit) {

this.passlimit = passlimit;

}

public int getPasslimit() {

return this.passlimit;

}


public void setCommonrandom(int[][] commonrandom) {

this.commonrandom = commonrandom;

}

public int[][] getCommonrandom() {

return this.commonrandom;

}


public void setDifficultrandom(int[][] difficultrandom) {

this.difficultrandom = difficultrandom;

}

public int[][] getDifficultrandom() {

return this.difficultrandom;

}


public void setUnbeatablerandom(int[][] unbeatablerandom) {

this.unbeatablerandom = unbeatablerandom;

}

public int[][] getUnbeatablerandom() {

return this.unbeatablerandom;

}




}
