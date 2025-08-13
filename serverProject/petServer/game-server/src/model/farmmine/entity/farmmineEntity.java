/**
 *created by tool DAOGenerate
 */
package model.farmmine.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import model.farmmine.dbCache.farmmineCache;
import model.obj.BaseObj;
import protocol.FarmMineDB.FarmMineOfferDB;
import protocol.FarmMineDB.FarmMinePDB;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class farmmineEntity extends BaseObj {
	
	public String getClassType() {
		return "farmmineEntity";
	}

        /**
     * 
     */
    private String idx;

    /**
     * 基础产出ID
     */
    private int baseidx;

    /**
     * 概率产出ID
     */
    private String extids;

    /**
     * 加成宠物ID
     */
    private int petid;

    /**
     * 称号ID
     */
    private int titleid;

    /**
     * 占领者ID
     */
    private String occplayerid;

    /**
     * 当前价格
     */
    private int price;

    /**
     * 参与活动时间
     */
    private long jointime;

    /**
     * 参与竞拍时间kaishi
     */
    private long auctionend;

    /**
     * 参与竞拍时间
     */
    private long auctionstart;

    /**
     * 竞价信息
     */
    private byte[] auctioninfo;

    /**
     * 玩家数据
     */
    private byte[] playerdata;

    /**
     * 特殊数据
     */
    private byte[] zerodata;


    
        /**
     * 鑾峰緱
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 璁剧疆
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 鑾峰緱基础产出ID
     */
    public int getBaseidx() {
        return baseidx;
    }

    /**
     * 璁剧疆基础产出ID
     */
    public void setBaseidx(int baseidx) {
        this.baseidx = baseidx;
    }

    /**
     * 鑾峰緱概率产出ID
     */
    public String getExtids() {
        return extids;
    }

    /**
     * 璁剧疆概率产出ID
     */
    public void setExtids(String extids) {
        this.extids = extids;
    }

    /**
     * 鑾峰緱加成宠物ID
     */
    public int getPetid() {
        return petid;
    }

    /**
     * 璁剧疆加成宠物ID
     */
    public void setPetid(int petid) {
        this.petid = petid;
    }

    /**
     * 鑾峰緱称号ID
     */
    public int getTitleid() {
        return titleid;
    }

    /**
     * 璁剧疆称号ID
     */
    public void setTitleid(int titleid) {
        this.titleid = titleid;
    }

    /**
     * 鑾峰緱占领者ID
     */
    public String getOccplayerid() {
        return occplayerid;
    }

    /**
     * 璁剧疆占领者ID
     */
    public void setOccplayerid(String occplayerid) {
        this.occplayerid = occplayerid;
    }

    /**
     * 鑾峰緱当前价格
     */
    public int getPrice() {
        return price;
    }

    /**
     * 璁剧疆当前价格
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * 鑾峰緱参与活动时间
     */
    public long getJointime() {
        return jointime;
    }

    /**
     * 璁剧疆参与活动时间
     */
    public void setJointime(long jointime) {
        this.jointime = jointime;
    }

    /**
     * 鑾峰緱参与竞拍时间kaishi
     */
    public long getAuctionend() {
        return auctionend;
    }

    /**
     * 璁剧疆参与竞拍时间kaishi
     */
    public void setAuctionend(long auctionend) {
        this.auctionend = auctionend;
    }

    /**
     * 鑾峰緱参与竞拍时间
     */
    public long getAuctionstart() {
        return auctionstart;
    }

    /**
     * 璁剧疆参与竞拍时间
     */
    public void setAuctionstart(long auctionstart) {
        this.auctionstart = auctionstart;
    }

    /**
     * 鑾峰緱竞价信息
     */
    public byte[] getAuctioninfo() {
        return auctioninfo;
    }

    /**
     * 璁剧疆竞价信息
     */
    public void setAuctioninfo(byte[] auctioninfo) {
        this.auctioninfo = auctioninfo;
    }

    /**
     * 鑾峰緱玩家数据
     */
    public byte[] getPlayerdata() {
        return playerdata;
    }

    /**
     * 璁剧疆玩家数据
     */
    public void setPlayerdata(byte[] playerdata) {
        this.playerdata = playerdata;
    }

    /**
     * 鑾峰緱特殊数据
     */
    public byte[] getZerodata() {
        return zerodata;
    }

    /**
     * 璁剧疆特殊数据
     */
    public void setZerodata(byte[] zerodata) {
        this.zerodata = zerodata;
    }



    public String getBaseIdx() {
        return idx;
    }

    @Override
    public void putToCache() {
        farmmineCache.put(this);
    }

    @Override
    public void transformDBData() {
        if (null != auctionInfoDB) {
            auctioninfo = auctionInfoDB.build().toByteArray();
        }
        if (null != playerdataDB) {
            playerdata = playerdataDB.build().toByteArray();
        }
    }

    public void toBuilder() throws InvalidProtocolBufferException {
        if (auctioninfo != null) {
            auctionInfoDB = FarmMineOfferDB.parseFrom(auctioninfo).toBuilder();
        } else {
            auctionInfoDB = FarmMineOfferDB.getDefaultInstance().toBuilder();
        }
        if (playerdata != null) {
            playerdataDB = FarmMinePDB.parseFrom(playerdata).toBuilder();
        } else {
            playerdataDB = FarmMinePDB.getDefaultInstance().toBuilder();
        }
    }

    /*************************** 分割 **********************************/

    private FarmMineOfferDB.Builder auctionInfoDB;

    private FarmMinePDB.Builder playerdataDB;

    public FarmMinePDB.Builder getPlayerdataDB() {
        return playerdataDB;
    }

    public void setPlayerdataDB(FarmMinePDB.Builder playerdataDB) {
        this.playerdataDB = playerdataDB;
    }

    public FarmMineOfferDB.Builder getAuctionInfoDB() {
        return auctionInfoDB;
    }

    public void setAuctionInfoDB(FarmMineOfferDB.Builder auctionInfoDB) {
        this.auctionInfoDB = auctionInfoDB;
    }
}