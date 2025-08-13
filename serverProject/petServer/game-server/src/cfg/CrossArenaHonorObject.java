package cfg;

import model.base.baseConfigObject;

public class CrossArenaHonorObject implements baseConfigObject {

	private int id;

	private int bigtype;

	private int parm;

	private int[][] award;

	private int missiontype;

	public void setId(int id) {

		this.id = id;

	}

	public int getId() {

		return this.id;

	}

	public void setBigtype(int bigtype) {

		this.bigtype = bigtype;

	}

	public int getBigtype() {

		return this.bigtype;

	}

	public void setParm(int parm) {

		this.parm = parm;

	}

	public int getParm() {

		return this.parm;

	}

	public void setAward(int[][] award) {

		this.award = award;

	}

	public int[][] getAward() {

		return this.award;

	}

	public void setMissiontype(int missiontype) {

		this.missiontype = missiontype;

	}

	public int getMissiontype() {

		return this.missiontype;

	}

}
