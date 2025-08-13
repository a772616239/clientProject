package cfg;
import model.base.baseConfigObject;
public class  MonthlyCardConfigObject implements baseConfigObject{



private int id;

private int[][] instantrewards;

private int[][] everydayrewards;

private int sumrewards;

private int[] price;

private int rechargeproductid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setInstantrewards(int[][] instantrewards) {

this.instantrewards = instantrewards;

}

public int[][] getInstantrewards() {

return this.instantrewards;

}


public void setEverydayrewards(int[][] everydayrewards) {

this.everydayrewards = everydayrewards;

}

public int[][] getEverydayrewards() {

return this.everydayrewards;

}


public void setSumrewards(int sumrewards) {

this.sumrewards = sumrewards;

}

public int getSumrewards() {

return this.sumrewards;

}


public void setPrice(int[] price) {

this.price = price;

}

public int[] getPrice() {

return this.price;

}


public void setRechargeproductid(int rechargeproductid) {

this.rechargeproductid = rechargeproductid;

}

public int getRechargeproductid() {

return this.rechargeproductid;

}




}
