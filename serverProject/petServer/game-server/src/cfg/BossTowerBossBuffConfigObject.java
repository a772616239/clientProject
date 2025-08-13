package cfg;
import model.base.baseConfigObject;
public class  BossTowerBossBuffConfigObject implements baseConfigObject{



private int id;

private int prefixtype;

private int linkbuffdisid;

private int weight;

private int effectcamp;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setPrefixtype(int prefixtype) {

this.prefixtype = prefixtype;

}

public int getPrefixtype() {

return this.prefixtype;

}


public void setLinkbuffdisid(int linkbuffdisid) {

this.linkbuffdisid = linkbuffdisid;

}

public int getLinkbuffdisid() {

return this.linkbuffdisid;

}


public void setWeight(int weight) {

this.weight = weight;

}

public int getWeight() {

return this.weight;

}


public void setEffectcamp(int effectcamp) {

this.effectcamp = effectcamp;

}

public int getEffectcamp() {

return this.effectcamp;

}




}
