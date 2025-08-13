package cfg;
import model.base.baseConfigObject;
public class  BanShuConfigObject implements baseConfigObject{



private int id;

private int drawcardlimit;

private int altarlimit;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDrawcardlimit(int drawcardlimit) {

this.drawcardlimit = drawcardlimit;

}

public int getDrawcardlimit() {

return this.drawcardlimit;

}


public void setAltarlimit(int altarlimit) {

this.altarlimit = altarlimit;

}

public int getAltarlimit() {

return this.altarlimit;

}




}
