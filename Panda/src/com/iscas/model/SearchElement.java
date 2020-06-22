package com.iscas.model;

public class SearchElement implements Comparable<SearchElement> {

	private Node node;
	private float distance;

	public SearchElement(Node n, float d) {
		this.node = n;
		this.distance = d;
	}

	public Node getNode() {
		return node;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	@Override
	public int compareTo(SearchElement o) {
		// TODO Auto-generated method stub
		if (this.distance < o.getDistance()) {
			return -1;
		} else if (this.distance > o.getDistance()) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		SearchElement oElement = (SearchElement) obj;
		if (oElement.getNode().getId().equals(this.getNode().getId())) {
			return true;
		} else {
			return false;
		}
	}

}
