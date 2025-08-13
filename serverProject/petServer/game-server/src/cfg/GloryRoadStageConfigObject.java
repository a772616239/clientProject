package cfg;
import model.base.baseConfigObject;
public class  GloryRoadStageConfigObject implements baseConfigObject{



private int id;

private int serverpromotion;

private int serverpromotionfailed;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setServerpromotion(int serverpromotion) {

this.serverpromotion = serverpromotion;

}

public int getServerpromotion() {

return this.serverpromotion;

}


public void setServerpromotionfailed(int serverpromotionfailed) {

this.serverpromotionfailed = serverpromotionfailed;

}

public int getServerpromotionfailed() {

return this.serverpromotionfailed;

}




}
