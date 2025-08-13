package cfg;
import model.base.baseConfigObject;
public class  MatchArenaRobotTeamObject implements baseConfigObject{



private int id;

private int[] team;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setTeam(int[] team) {

this.team = team;

}

public int[] getTeam() {

return this.team;

}




}
