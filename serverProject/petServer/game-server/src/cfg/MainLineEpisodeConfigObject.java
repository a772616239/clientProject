package cfg;
import model.base.baseConfigObject;
public class  MainLineEpisodeConfigObject implements baseConfigObject{



private int id;

private int[] episondenodeid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setEpisondenodeid(int[] episondenodeid) {

this.episondenodeid = episondenodeid;

}

public int[] getEpisondenodeid() {

return this.episondenodeid;

}




}
