package model.training.bean;

public class TrainRankSortInfo {

	private String playerId;
	private int star;
	private int jifen;
	private long refTime;
	public TrainRankSortInfo(String playerId) {
		this.playerId = playerId;
	}
	public TrainRankSortInfo(String playerId, int star, int jifen, long time) {
		this.playerId = playerId;
		this.star = star;
		this.jifen = jifen;
		this.refTime = time;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public int getStar() {
		return star;
	}
	public void setStar(int star) {
		this.star = star;
	}
	public int getJifen() {
		return jifen;
	}
	public void setJifen(int jifen) {
		this.jifen = jifen;
	}
	public long getRefTime() {
		return refTime;
	}
	public void setRefTime(long refTime) {
		this.refTime = refTime;
	}
	
}
