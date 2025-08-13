package cfg;
import model.base.baseConfigObject;
public class  MistGhostConfigObject implements baseConfigObject{



private int id;

private int ghosttouchscore;

private int[] ghosttouchreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setGhosttouchscore(int ghosttouchscore) {

this.ghosttouchscore = ghosttouchscore;

}

public int getGhosttouchscore() {

return this.ghosttouchscore;

}


public void setGhosttouchreward(int[] ghosttouchreward) {

this.ghosttouchreward = ghosttouchreward;

}

public int[] getGhosttouchreward() {

return this.ghosttouchreward;

}




}
