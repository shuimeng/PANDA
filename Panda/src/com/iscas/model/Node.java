package com.iscas.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class Node implements Comparable<Node> {

	// 节点id
	private String id;
	// 节点labels
	private ArrayList<String> labels;
	// 边
	private HashMap<String, Edge> edgesArrayList;
	// betweeness value
	private float betweeness;

	// hub
	private boolean isHub = false;

	// hubs间的distance数组
	private HashMap<String, Float> hubsDistances;

	// ismerged
	private boolean isMerged = false;

	// mids
	private ArrayList<Integer> mids = new ArrayList<>();

	public Node(String id) {
		this.id = id;
		this.labels = new ArrayList<>();
		this.edgesArrayList = new HashMap<String, Edge>();
	}

	public void addLabel(String label) {
		if (labels == null) {
			labels = new ArrayList<>();
		}
		if (!labels.contains(label)) {
			labels.add(label);
		}
	}

	public void addLabels(ArrayList<String> labels) {
		if (this.labels == null) {
			this.labels = new ArrayList<>();
		}
		for (int i = 0; i < labels.size(); i++) {
			if(!this.labels.contains(labels.get(i))){
				this.labels.add(labels.get(i));
			}
		}
	}

	public void addEdge(Edge e) {
		if (edgesArrayList == null) {
			edgesArrayList = new HashMap<String, Edge>();
		}
		// 查看是否存在
		if (edgesArrayList.containsKey(e.getToNode().getId())) {
			if (edgesArrayList.get(e.getToNode().getId()).getWeight() < e
					.getWeight()) {
				edgesArrayList.get(e.getToNode().getId()).setWeight(
						e.getWeight());
			}
		}else{
			edgesArrayList.put(e.getToNode().getId(),e);
		}

	}

	public int getEdgeNum() {
		int num = 0;
		if (this.edgesArrayList != null) {
			num = this.edgesArrayList.size();
		}
		return num;
	}

	public void deleteEdge(String toId) {
		edgesArrayList.remove(toId);
	}

	public ArrayList<String> getLabels() {
		return labels;
	}

	public HashMap<String,Edge> getEdgesArrayList() {
		return edgesArrayList;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/*
	 * 实现度排序降序排列
	 */
	@Override
	public int compareTo(Node o) {
		// TODO Auto-generated method stubs
		if (this.getEdgeNum() > o.getEdgeNum()) {
			return -1;
		} else if (this.getEdgeNum() < o.getEdgeNum()) {
			return 1;
		}
		return 0;
	}

	public boolean isHub() {
		return isHub;
	}

	public void setHub(boolean isHub) {
		this.isHub = isHub;
		if (isHub) {
			// 初始化distance数组
			this.hubsDistances = new HashMap<String, Float>();
		} else {
			this.hubsDistances = null;
		}
	}

	// 查询与其他hubs之间的距离，只有hub节点能用,如果没有可达距离，返回-1
	public float gethubDistance(String destinationId) {
		if (this.isHub) {
			Float distance = this.hubsDistances.get(destinationId);
			if (distance != null) {
				return distance;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	public HashMap<String, Float> getHubsDistances() {
		if (isHub) {
			return hubsDistances;
		} else {
			return null;
		}
	}

	public void setHubsDistances(HashMap<String, Float> hubsDistances) {
		if (isHub) {
			this.hubsDistances = hubsDistances;
		}
	}

	public float getBetweeness() {
		return betweeness;
	}

	public void setBetweeness(float betweeness) {
		this.betweeness = betweeness;
	}

	public boolean isMerged() {
		return isMerged;
	}

	public void setMerged(boolean isMerged) {
		this.isMerged = isMerged;
	}

	public void addMids(int mid) {
		if (!this.mids.contains(mid)) {
			this.mids.add(mid);
		}
	}

	public void setMids(int mid) {
		if (this.mids == null) {
			this.mids = new ArrayList<>();
		} else {
			this.mids.clear();
		}
		this.mids.add(mid);
	}

	public void removeMid(int mid) {
		if (this.mids != null) {
			mids.remove((Integer) mid);
		}
	}

	public void removeAllMids() {
		if (this.mids != null) {
			mids = new ArrayList<>();
		}
	}

	public ArrayList<Integer> getMids() {
		return mids;
	}

}
