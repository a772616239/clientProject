package cfg;
import model.base.baseConfigObject;
public class  TrainingLuckPoolLittleObject implements baseConfigObject{



private int id;

private int grade;

private int[][] cards;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setGrade(int grade) {

this.grade = grade;

}

public int getGrade() {

return this.grade;

}


public void setCards(int[][] cards) {

this.cards = cards;

}

public int[][] getCards() {

return this.cards;

}




}
