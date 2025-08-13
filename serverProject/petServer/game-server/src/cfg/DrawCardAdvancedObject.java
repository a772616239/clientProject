package cfg;
import model.base.baseConfigObject;
public class  DrawCardAdvancedObject implements baseConfigObject{



private int id;

private int[][] group;

private int weight;

private int[][] innermustpool;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setGroup(int[][] group) {

this.group = group;

}

public int[][] getGroup() {

return this.group;

}


public void setWeight(int weight) {

this.weight = weight;

}

public int getWeight() {

return this.weight;

}


public void setInnermustpool(int[][] innermustpool) {

this.innermustpool = innermustpool;

}

public int[][] getInnermustpool() {

return this.innermustpool;

}




}
