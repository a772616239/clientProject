package model.ranking.ranking.arena;

public class ArenaDanAllServerRanking extends ArenaDanRanking {
    @Override
    public void init() {
        setCrossRanking(true);
        super.init();
    }
}
