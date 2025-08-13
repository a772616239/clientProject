package model.training.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrainPoint {

	private int pid;
	private int branchPoint;
	private List<Integer> father = new ArrayList<>();
	private List<Integer> son = new ArrayList<>();
	private Set<Integer> allfather = new HashSet<Integer>();
	private List<Integer> canReset = new ArrayList<>();
	private int nextBoss;
	public TrainPoint(int pid) {
		this.pid = pid;
	}

	public int getPid() {
		return pid;
	}

	public List<Integer> getFather() {
		return father;
	}

	public List<Integer> getSon() {
		return son;
	}

	public Set<Integer> getAllfather() {
		return allfather;
	}

	public List<Integer> getCanReset() {
		return canReset;
	}

	public int getBranchPoint() {
		return branchPoint;
	}

	public void setBranchPoint(int branchPoint) {
		this.branchPoint = branchPoint;
	}

	public int getNextBoss() {
		return nextBoss;
	}

	public void setNextBoss(int nextBoss) {
		this.nextBoss = nextBoss;
	}
	
}
