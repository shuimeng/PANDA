package com.iscas.model;

public class Edge {

	private Node toNode;
	// 原始图中边的权重
	private float weight;

	public Edge(Node toNode, float weight) {
		this.toNode = toNode;
		this.weight = weight;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		// return super.equals(obj);
		if (((Edge) obj).getToNode().getId().equals(this.toNode.getId())) {
			return true;
		} else {
			return false;
		}
	}

	public Node getToNode() {
		return toNode;
	}

	public void setToNode(Node toNode) {
		this.toNode = toNode;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

}
