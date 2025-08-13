package server.http.entity;

import lombok.Data;
import model.crossarena.CrossArenaManager;
import model.crossarena.entity.playercrossarenaEntity;
import model.itembag.dbCache.itembagCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.CrossArena;
import protocol.CrossArenaDB;

import static common.GameConst.CrossArenaScoreItemId;

@Data
public class PlatformCrossArenaInfo {
    private String userId;
    private String roleName;
    private String roleId;
    //荣誉积分
    private int honorScore;
    //荣誉等级
    private int honorLv;
    //擂台积分
    private long ltCurrency;
    //热度
    private int hot;
    //切磋胜利次数
    private int qcWin;
    //擂台胜利次数
    private int ltWin;
    //最高连胜
    private int ltWinMaxHis;
    //巅峰赛参与次数
   /* private int dfsJoin;*/
    //巅峰赛第一名次数
    private int dfs1stNum;
    //疯狂对决最高楼层
    private int crazyMaxFloor;
    //疯狂对决参与次数
   /* private int crazyJoin;*/
    //组队通关次数
    private int cpPassNum;
    //组队最高星级
    private int cpMaxStar;

    public PlatformCrossArenaInfo(String roleId) {
        playercrossarenaEntity crossarenaEntity = CrossArenaManager.getInstance().getPlayerEntity(roleId);
        if (crossarenaEntity == null) {
            return;
        }
        playerEntity playerEntity = playerCache.getByIdx(roleId);
        if (playerEntity==null){
            return;
        }
        this.roleId = roleId;
        this.roleName = playerEntity.getName();
        this.userId =playerEntity.getUserid();

        CrossArenaDB.CrossArenaPlayerDB.Builder dataMsg = crossarenaEntity.getDataMsg();
        this.honorScore = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.LT_GRADECUR_VALUE, 0);
        this.honorLv = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.LT_GRADELV_VALUE, 0);
        this.ltCurrency = itembagCache.getInstance().getPlayerItemCount(roleId, CrossArenaScoreItemId);
        this.hot = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.LT_ADMIRE_BE_VALUE, 0);
        this.qcWin = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.QC_WINNUM_VALUE, 0);
        this.ltWin = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.LT_WINNUM_VALUE, 0);
        this.ltWinMaxHis = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.LT_WINCOTHIS_VALUE, 0);
        this.dfs1stNum = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.DFS_FIRSTNUM_VALUE, 0);
        this.crazyMaxFloor = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.FKDJ_LAYERMAX_VALUE, 0);
        this.cpPassNum = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.ZD_PASSNUM_VALUE, 0);
        this.cpMaxStar = dataMsg.getDbsOrDefault(CrossArena.CrossArenaDBKey.ZD_STARMAX_VALUE, 0);
    }

}
