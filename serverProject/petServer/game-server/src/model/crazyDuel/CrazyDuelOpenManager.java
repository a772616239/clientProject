package model.crazyDuel;

import lombok.Getter;

public class CrazyDuelOpenManager {


    @Getter
    private static CrazyDuelOpenManager instance = new CrazyDuelOpenManager();


    public boolean init() {

        return true;
    }


    public boolean isOpen() {
        return true;
    }
}
