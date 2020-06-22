package com.iscas.model;

import java.util.ArrayList;

public class QueryGraph {

	private ArrayList<Graph> graphs;

	public QueryGraph() {
		graphs = new ArrayList<>();
	}

	public ArrayList<Graph> getGraphs() {
		if (graphs == null)
			graphs = new ArrayList<>();
		return graphs;
	}

	public void addQuery(Graph graph) {
		if (graphs == null) {
			graphs = new ArrayList<>();
		}
		graphs.add(graph);
	}

	// 获取有多少个子图
	public int getQueryNum() {
		if (graphs != null) {
			return graphs.size();
		} else {
			return 0;
		}
	}

}
