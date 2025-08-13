package cfg;
import model.base.baseConfigObject;
public class  TrainingLuckPoolObject implements baseConfigObject{



private int id;

private int[][] cards;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setCards(int[][] cards) {

this.cards = cards;

}

public int[][] getCards() {

return this.cards;

}




}
