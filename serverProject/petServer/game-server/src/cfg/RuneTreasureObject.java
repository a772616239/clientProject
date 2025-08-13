package cfg;
import model.base.baseConfigObject;
public class  RuneTreasureObject implements baseConfigObject{



private int id;

private int[] keyprice;

private int[] dailymission;

private int[] drawprice;

private int stagerewardsmaxsize;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setKeyprice(int[] keyprice) {

this.keyprice = keyprice;

}

public int[] getKeyprice() {

return this.keyprice;

}


public void setDailymission(int[] dailymission) {

this.dailymission = dailymission;

}

public int[] getDailymission() {

return this.dailymission;

}


public void setDrawprice(int[] drawprice) {

this.drawprice = drawprice;

}

public int[] getDrawprice() {

return this.drawprice;

}


public void setStagerewardsmaxsize(int stagerewardsmaxsize) {

this.stagerewardsmaxsize = stagerewardsmaxsize;

}

public int getStagerewardsmaxsize() {

return this.stagerewardsmaxsize;

}




}
