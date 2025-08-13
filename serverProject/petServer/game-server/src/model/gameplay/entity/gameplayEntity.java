/**
 *created by tool DAOGenerate
 */
package model.gameplay.entity;
import model.gameplay.dbCache.gameplayCache;
import model.obj.BaseObj;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class gameplayEntity extends BaseObj {
	
	public String getClassType() {
		return "gameplayEntity";
	}

    /**
     * 玩法枚举，在gamePlay中定义
     */
    private String idx;

    /**
     * 
     */
    private byte[] gameplayinfo;


    
        /**
     * 获得玩法枚举，在gamePlay中定义
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置玩法枚举，在gamePlay中定义
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得
     */
    public byte[] getGameplayinfo() {
        return gameplayinfo;
    }

    /**
     * 设置
     */
    public void setGameplayinfo(byte[] gameplayinfo) {
        this.gameplayinfo = gameplayinfo;
    }

	public String getBaseIdx() {
		return idx;
	}

    @Override
    public void putToCache() {
        gameplayCache.put(this);
    }

    @Override
    public void transformDBData() {

    }
}