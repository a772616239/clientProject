package model.matchArena;

import lombok.Data;

@Data
public class ArenaSuccessMatch {
    private NormalMatchPlayer player;
    private NormalMatchPlayer opponent;
    private volatile boolean finishInit;
}
