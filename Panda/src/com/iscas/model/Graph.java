package com.iscas.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class Graph {
	// 边总数
	private int edgeNum;
	private int nodeNum;
	// nodes hash，随机读取O(1). <id,node>
	private Hashtable<String, Node> nodes;

	public Graph() {
		edgeNum = 0;
		nodeNum = 0;
		nodes = new Hashtable<String, Node>();
	}

	public Graph(int nodeN, int edgeN) {
		edgeNum = edgeN;
		nodeNum = nodeN;
		nodes = new Hashtable<String, Node>();
	}

	// 增加边，构建图的方法
	public void addEdge(String fromId, String toId, float weight,
			boolean singleDirect) {
	//	if (fromId.equals("9907233")) System.out.println(toId);
		if (singleDirect) {
			// 查找fromId的节点
			Node fromNode = nodes.get(fromId);
			if (fromNode == null) {
				fromNode = new Node(fromId);
				nodes.put(fromId, fromNode);
			}
			Node toNode = nodes.get(toId);
			if (toNode == null) {
				// 增加toNode
				toNode = new Node(toId);
				nodes.put(toId, toNode);
			}
			fromNode.addEdge(new Edge(toNode, weight));

		} else {
			// 双向
			addEdge(fromId, toId, weight, true);
			addEdge(toId, fromId, weight, true);
		}
	}

	/*
	 * 验证图的信息是否全，节点个数和边数是否与具体信息一样
	 */
	public boolean validate() {
		int realNodeN = this.nodes.size();
		if (realNodeN == nodeNum) {
			int realEdgeN = 0;
			for (Iterator<String> iterator = nodes.keySet().iterator(); iterator
					.hasNext();) {
				realEdgeN += nodes.get(iterator.next()).getEdgeNum();
			}
			if ((realEdgeN / 2) != edgeNum) {
				return false;
			} else {
				return true;
			}
		} else {
			// 信息不全
			return false;
		}
	}

	//仅添加G中出现的节点label，对于没出现的直接忽略，以防有孤立节点
	public void addLabelForNode(String node_id, String label) {
		Node node = nodes.get(node_id);
		if (node != null) {
			node.addLabel(label);
		} 
	}

	public int getNodeNum() {
		return nodeNum;
	}

	public int getEdgeNum() {
		return edgeNum;
	}

	public Hashtable<String, Node> getNodes() {
		return nodes;
	}

	// 增加一个空节点
	public void addNode(String id) {
		// 如果不存在增加，否则啥都不做
		if (this.nodes.get(id) == null) {
			Node node = new Node(id);
			nodes.put(id, node);
		}
	}

	// 增加一个空节点
	public void addNode(String id, ArrayList<String> labels) {
		// 如果不存在增加，否则啥都不做
		if (this.nodes.get(id) == null) {
			Node node = new Node(id);
			node.addLabels(labels);
			nodes.put(id, node);
		}
	}
	
	public void addNode(Node e) {
		// 如果不存在增加，否则啥都不做
		if (nodes.get(e.getId()) == null) {
			nodes.put(e.getId(), e);
		}
	}

	public void setNodeNum() {
		this.nodeNum = this.nodes.size();
	}

	public void setEdgeNum(int num) {
		this.edgeNum = num;
	}
}
