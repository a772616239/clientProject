package model.arena.entity;

import lombok.Getter;
import lombok.Setter;
import protocol.ArenaDB.DB_ArenaDefinedTeamsInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;

/**
 * 竞技场的所有信息
 * @author huhan
 * @date 2020.08.12
 */
@Setter
@Getter
public class ArenaTotalInfo {
    private DB_ArenaPlayerInfo arenaPlayerInfo;
    private DB_ArenaDefinedTeamsInfo definedTeams;

    public boolean isEmpty() {
        return arenaPlayerInfo == null && definedTeams == null;
    }
}
