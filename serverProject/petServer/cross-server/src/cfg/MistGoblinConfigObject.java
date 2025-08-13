package cfg;
import model.base.baseConfigObject;
public class  MistGoblinConfigObject implements baseConfigObject{



private int id;

private int[][] generaterewardidlist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setGeneraterewardidlist(int[][] generaterewardidlist) {

this.generaterewardidlist = generaterewardidlist;

}

public int[][] getGeneraterewardidlist() {

return this.generaterewardidlist;

}




}
