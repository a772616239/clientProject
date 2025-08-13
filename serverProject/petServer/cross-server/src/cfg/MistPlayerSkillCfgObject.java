package cfg;
import model.base.baseConfigObject;
public class  MistPlayerSkillCfgObject implements baseConfigObject{



private int id;

private int type;

private int maxstack;

private int cooldown;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setMaxstack(int maxstack) {

this.maxstack = maxstack;

}

public int getMaxstack() {

return this.maxstack;

}


public void setCooldown(int cooldown) {

this.cooldown = cooldown;

}

public int getCooldown() {

return this.cooldown;

}




}
