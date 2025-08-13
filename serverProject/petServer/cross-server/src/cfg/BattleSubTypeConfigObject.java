package cfg;
import model.base.baseConfigObject;

public class  BattleSubTypeConfigObject implements baseConfigObject{



private int id;

private boolean hasmonsterbondbuff;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setHasmonsterbondbuff(boolean hasmonsterbondbuff) {

this.hasmonsterbondbuff = hasmonsterbondbuff;

}

public boolean getHasmonsterbondbuff() {

return this.hasmonsterbondbuff;

}




}
