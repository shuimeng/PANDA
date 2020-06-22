package com.iscas.model;

public class Tree implements Comparable<Tree> {

	private float cost;
	private int matchNum;
	private Graph graph;

	public Tree(Graph graph, float cost, int matchNum) {
		this.graph = graph;
		this.cost = cost;
		this.matchNum = matchNum;
	}

	public void setMatchNum(int num) {
		this.matchNum = num;
	}

	public int getMatchNum() {
		return matchNum;
	}

	public float getCost() {
		return cost;
	}

	public void setCost(float cost) {
		this.cost = cost;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	@Override
	public int compareTo(Tree tree2) {
		// TODO Auto-generated method stub
		if (this.cost < tree2.cost) {
			return -1;
		} else if (this.cost > tree2.cost) {
			return 1;
		} else {
			return 0;
		}

	}

}
