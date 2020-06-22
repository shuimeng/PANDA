package com.iscas.model;

public class OuterEdge {

	private String fromId;
	private String toId;
	private float weight;

	public OuterEdge(String node1, String node2, float weighted) {
		this.fromId = node1;
		this.toId = node2;
		this.weight = weighted;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

}
