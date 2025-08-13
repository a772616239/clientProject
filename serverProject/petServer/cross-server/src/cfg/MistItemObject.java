package cfg;
import model.base.baseConfigObject;
public class  MistItemObject implements baseConfigObject{



private int itemid;

private int[] itemprice;

private boolean isshopitem;




public void setItemid(int itemid) {

this.itemid = itemid;

}

public int getItemid() {

return this.itemid;

}


public void setItemprice(int[] itemprice) {

this.itemprice = itemprice;

}

public int[] getItemprice() {

return this.itemprice;

}


public void setIsshopitem(boolean isshopitem) {

this.isshopitem = isshopitem;

}

public boolean getIsshopitem() {

return this.isshopitem;

}




}
