package cfg;
import model.base.baseConfigObject;
public class  PetUpConsumeObject implements baseConfigObject{



private int id;

private int uptype;

private int needexp;

private int needpetlv;

private int uplvl;

private int petcfgid;

private int[][] upconsume;

private int upsucrate;

private int[][] extraconsume;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setUptype(int uptype) {

this.uptype = uptype;

}

public int getUptype() {

return this.uptype;

}


public void setNeedexp(int needexp) {

this.needexp = needexp;

}

public int getNeedexp() {

return this.needexp;

}


public void setNeedpetlv(int needpetlv) {

this.needpetlv = needpetlv;

}

public int getNeedpetlv() {

return this.needpetlv;

}


public void setUplvl(int uplvl) {

this.uplvl = uplvl;

}

public int getUplvl() {

return this.uplvl;

}


public void setPetcfgid(int petcfgid) {

this.petcfgid = petcfgid;

}

public int getPetcfgid() {

return this.petcfgid;

}


public void setUpconsume(int[][] upconsume) {

this.upconsume = upconsume;

}

public int[][] getUpconsume() {

return this.upconsume;

}


public void setUpsucrate(int upsucrate) {

this.upsucrate = upsucrate;

}

public int getUpsucrate() {

return this.upsucrate;

}


public void setExtraconsume(int[][] extraconsume) {

this.extraconsume = extraconsume;

}

public int[][] getExtraconsume() {

return this.extraconsume;

}




}
