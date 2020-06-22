package com.iscas.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.iscas.algorithm.Isomorphism;
import com.iscas.model.Edge;
import com.iscas.model.Graph;
import com.iscas.model.Node;
import com.iscas.model.QueryGraph;
import com.iscas.model.SearchElement;
import com.iscas.model.Tree;

public class ScoreManager {

	private double Precision;
	private double MAP;
	private double NDCG;

	private QueryGraph queries;
	private ArrayList<Tree> trees;
	private int k;
	private boolean isMerged;

	// 计算每个返回结果子图的匹配结果，数组内的内容为cost，cost=0，为不匹配，数组的index就是返回结果的rank
	private ArrayList<Float> matchedResults;

	public ScoreManager(QueryGraph queries, ArrayList<Tree> trees, int k,
			boolean isMerged) {
		this.queries = queries;
		this.trees = trees;
		this.k = k;
		this.isMerged = isMerged;
		this.matchedResults = new ArrayList<>();
		precision();
		mapAndNdcg();
	}

	// 返回结果树可能不是valid
	private void precision() {
		int validN = 0;
		int index = 0;
		if (!(trees == null || trees.size() == 0)) {
			for (int i = 0; i < trees.size() && validN < k; i++) {
				if (!isMerged) {
					int matchs = validate(trees.get(i), index, k - validN);
					if (matchs != 0) {
						validN += matchs;
					}
					while (this.matchedResults.size() < index) {
						float score = -1;
						this.matchedResults.add(score);
					}
					int rank = trees.get(i).getMatchNum();
					index = index + rank;
				} else {
					for (int j = 0; j < trees.get(i).getMatchNum()
							&& validN < k; j++) {
						if (trees.get(i).getCost() < (queries.getQueryNum() - 1)) {
							matchedResults.add(new Float(queries.getQueryNum() - 1));
						} else {
							matchedResults.add(trees.get(i).getCost());
						}
						validN++;
					}
				}
			}
			this.Precision = (double) validN / (double) k;
		} else {
			this.Precision = 0;
		}

	}

	private void mapAndNdcg() {
		double map = 0;
		int preN = 0;
		double r = 0;
		double ndcg = 0;
		double indcg = 0;
		int i = 0;
		for (; i < matchedResults.size(); i++) {
			if (matchedResults.get(i) != 0 && matchedResults.get(i) != -1) {
				// map
				preN++;
				map += (double) preN / (double) (i + 1);
				// ndcg
				r = (double) 1 / (double) matchedResults.get(i);
				double d1 = Math.pow(2, r) - 1;
				double d2 = Math.log(i + 2) / Math.log(2);
				double cndcg = d1 / d2;
				ndcg += cndcg;
				indcg += (Math.pow(2,
						(double) 1 / (double) (queries.getQueryNum() - 1)) - 1)
						/ (Math.log(i + 2) / Math.log(2));
			} else if (matchedResults.get(i) == 0) {
				preN++;
				map += (double) preN / (double) (i + 1);
				// ndcg
				double value = (Math.pow(2,
						(double) 1 / (double) (queries.getQueryNum() - 1)) - 1)
						/ (Math.log(i + 2) / Math.log(2));
				indcg += value;
				ndcg += value;

			}

		}
		if (i < k) {
			for (int j = 0; j < k - i; j++) {
				indcg += (Math.pow(2,
						(double) 1 / (double) (queries.getQueryNum() - 1)) - 1)
						/ (Math.log(i + 2) / Math.log(2));
			}
		}
		this.MAP = map / k;
		this.NDCG = ndcg / indcg;
	}

	/**
	 * 验证并计算Cost
	 * */
	private int validate(Tree tree, int startIndex, int findN) {
		Graph graph = tree.getGraph();
		int matchNum = 0;
		ArrayList<ArrayList<HashMap<String, String>>> maps = new ArrayList<>();
		// 得到子图iso结果,重新计算子图iso，应该开销不大，因为图小
		Isomorphism isomorphism = new Isomorphism();
		for (int i = 0; i < queries.getQueryNum(); i++) {
			ArrayList<HashMap<String, String>> rArrayList = isomorphism
					.ullmann(graph, queries.getGraphs().get(i));
			if (rArrayList == null || rArrayList.size() == 0) {
				// 对于一个query，如果没有匹配，直接返回0
				return 0;
			}
			maps.add(rArrayList);
		}
		System.out.println("finish iso");
		// 判断重合度
		// 利用BFS构建
		ArrayList<ArrayList<Integer>> bfsSet = new ArrayList<>();
		// 增加初始元素
		for (int i = 0; i < maps.get(0).size(); i++) {
			ArrayList<Integer> init = new ArrayList<>();
			init.add(i);
			bfsSet.add(init);
		}
		while (bfsSet.size() != 0) {
			ArrayList<Integer> current = bfsSet.remove(0);
			// 遍历
			if (current.size() == queries.getQueryNum()) {
				// 得到了一个匹配
				matchNum++;
				// 求cost
				ArrayList<ArrayList<String>> innerNodesArrayLists = new ArrayList<>();
				for (int i = 0; i < current.size(); i++) {
					innerNodesArrayLists.add(new ArrayList<>(maps.get(i)
							.get(current.get(i)).values()));
				}
				float costT = calculateCost(graph, innerNodesArrayLists);
				this.matchedResults.add(costT);
				if (matchNum >= findN) {
					return matchNum;
				}
				continue;
			}
			// 查看下一个匹配元素
			for (int j = 0; j < maps.get(current.size()).size(); j++) {
				// 判断重合
				ArrayList<String> s1 = new ArrayList<String>(maps
						.get(current.size()).get(j).values());
				boolean overlap = false;
				for (int z = 0; z < current.size(); z++) {
					ArrayList<String> s2 = new ArrayList<String>(maps.get(z)
							.get(current.get(z)).values());
					s2.retainAll(s1);
					if (s2.size() != 0) {
						overlap = true;
						break;
					}
				}
				if (!overlap) {
					// 增加到BFS中
					ArrayList<Integer> nextState = new ArrayList<>();
					nextState.addAll(current);
					nextState.add(j);
					bfsSet.add(nextState);
				}
			}
		}
		return matchNum;
	}

	private float calculateCost(Graph graph,
			ArrayList<ArrayList<String>> innerNodesArrayList) {
		// TODO Auto-generated method stub
		float cost = 0;
		PriorityQueue<SearchElement> bfsQueue = new PriorityQueue<>();
		HashSet<String> closeSet = new HashSet<>();
		HashMap<String, Float> openHashMap = new HashMap<>();
		// initial bfs from the first graph
		for (int i = 0; i < innerNodesArrayList.get(0).size(); i++) {
			float distance = 0;
			SearchElement searchElement = new SearchElement(graph.getNodes()
					.get(innerNodesArrayList.get(0).get(i)), distance);
			bfsQueue.offer(searchElement);
			openHashMap.put(innerNodesArrayList.get(0).get(i), distance);
		}
		innerNodesArrayList.remove(0);
		while (!bfsQueue.isEmpty() && innerNodesArrayList.size() != 0) {
			SearchElement curElement = bfsQueue.poll();
			Node currentNode = curElement.getNode();
			openHashMap.remove(currentNode.getId());
			closeSet.add(currentNode.getId());

			ArrayList<String> removeA = null;
			for (ArrayList<String> setArrayList : innerNodesArrayList) {
				if (setArrayList.contains(currentNode.getId())) {
					// calculate cost
					cost += curElement.getDistance();
					// add all elements to bfs and remove innerA
					for (String nodeId : setArrayList) {
						float distance = 0;
						SearchElement element = new SearchElement(graph
								.getNodes().get(nodeId), distance);
						if (openHashMap.containsKey(nodeId)) {
							// 删除原值
							bfsQueue.remove(element);
						}
						bfsQueue.offer(element);
						openHashMap.put(nodeId, distance);
					}
					removeA = setArrayList;
					break;
				}
			}
			if (removeA != null) {
				innerNodesArrayList.remove(removeA);
				continue;
			}
			// 遍历
			for (Iterator<String> iterator = currentNode.getEdgesArrayList()
					.keySet().iterator(); iterator.hasNext();) {
				String toNodeId = iterator.next();
				if (!closeSet.contains(toNodeId)) {
					Edge edge = currentNode.getEdgesArrayList().get(toNodeId);
					float newDistance = curElement.getDistance()
							+ edge.getWeight();
					// 加入open中
					SearchElement element = new SearchElement(edge.getToNode(),
							newDistance);
					if (openHashMap.containsKey(toNodeId)) {
						// 在open里，比distance
						if (newDistance < openHashMap.get(toNodeId)) {
							// 替换
							bfsQueue.remove(element);
							bfsQueue.offer(element);
							openHashMap.put(toNodeId, newDistance);
						}
					} else {
						// 直接加
						bfsQueue.offer(element);
						openHashMap.put(toNodeId, newDistance);
					}
				}
			}
		}

		return cost;
	}

	public double getMAP() {
		return MAP;
	}

	public double getNDCG() {
		return NDCG;
	}

	public double getPrecision() {
		return Precision;
	}

}
