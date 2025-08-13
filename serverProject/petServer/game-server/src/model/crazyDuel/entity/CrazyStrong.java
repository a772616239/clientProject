package model.crazyDuel.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CrazyStrong {
    private int petLv;
    CrazyTeamsDb templateTeam;
    List<CrazyTeamsDb> allTeams = new ArrayList<>();
}
