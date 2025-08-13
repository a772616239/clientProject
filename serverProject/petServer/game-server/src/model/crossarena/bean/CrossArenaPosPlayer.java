package model.crossarena.bean;

/**
 * 玩家视野位置
 */
public class CrossArenaPosPlayer {

    private String playerIdx;
    private int sceneId = 0;
    private int viewPos = 0;
    private int currPos = 0;

    public CrossArenaPosPlayer(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public int getViewPos() {
        return viewPos;
    }

    public void setViewPos(int viewPos) {
        this.viewPos = viewPos;
    }

    public int getCurrPos() {
        return currPos;
    }

    public void setCurrPos(int currPos) {
        this.currPos = currPos;
    }

    public boolean canRef(int sid) {
        if (Math.abs(sid - viewPos) < 2) {
            return true;
        }
        return false;
    }

}
