package cfg;
import model.base.baseConfigObject;
public class  MistGuardMonsterConfigObject implements baseConfigObject{



private int id;

private int[] boxinitpos;

private int[][] guardmonsterposlist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setBoxinitpos(int[] boxinitpos) {

this.boxinitpos = boxinitpos;

}

public int[] getBoxinitpos() {

return this.boxinitpos;

}


public void setGuardmonsterposlist(int[][] guardmonsterposlist) {

this.guardmonsterposlist = guardmonsterposlist;

}

public int[][] getGuardmonsterposlist() {

return this.guardmonsterposlist;

}




}
