package com.iscas.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.iscas.model.Edge;
import com.iscas.model.Graph;
import com.iscas.model.Node;

public class MatchPrune {

	/**
	 * Simulation_Prune算法。 利用neighbors'info提前来定位潜在的matched subgraph 返回一堆子图
	 * */
	public MatchPrune() {

	}

	// undirected graph上的simulation
	public HashMap<String, ArrayList<String>> SimulationPrune(Graph graph,
			Graph query) {
		HashMap<String, ArrayList<String>> mapArrayList = new HashMap<>();
		for (Iterator<String> iterator = query.getNodes().keySet().iterator(); iterator
				.hasNext();) {
			Node qnode = query.getNodes().get(iterator.next());
			// System.out.println(qnode.getId());
			// System.out.println(qnode.getLabels());
			// query中node只有一个label
			String labelString = qnode.getLabels().get(0);
			ArrayList<String> gnodeIdsArrayList = new ArrayList<>();
			// 参看label一致的，且大于等于querynode的度的节点，加入candidate map中
			for (Iterator<String> iterator2 = graph.getNodes().keySet()
					.iterator(); iterator2.hasNext();) {
				Node gnode = graph.getNodes().get(iterator2.next());
				// 查看label的包含关系
				for (int i = 0; i < gnode.getLabels().size(); i++) {
					if (gnode.getLabels().get(i).equals(labelString)) {
						// label相等且度满足要求
						// System.out.println("qnode:" + qnode.getId() +
						// "gnode:"
						// + gnode.getId());
						// 判断topo关系
						boolean has = true;
						if (gnode.getEdgeNum() >= qnode.getEdgeNum()) {
							for (Iterator<String> edgeIterator = qnode
									.getEdgesArrayList().keySet().iterator(); edgeIterator
									.hasNext();) {
								Edge edge = qnode.getEdgesArrayList().get(
										edgeIterator.next());
								String qlabel = edge.getToNode().getLabels()
										.get(0);
								has = false;
								for (Iterator<String> gIterator = gnode
										.getEdgesArrayList().keySet()
										.iterator(); gIterator.hasNext();) {
									Edge gEdge = gnode.getEdgesArrayList().get(
											gIterator.next());
									for (int j = 0; j < gEdge.getToNode()
											.getLabels().size(); j++) {
										if (qlabel.equals(gEdge.getToNode()
												.getLabels().get(j))) {
											// 包含
											has = true;
											break;
										}
									}
									if (has) {
										break;
									}
								}

								if (!has) {
									// 不包含
									break;
								}
							}
						}
						if (has) {
							// 全包
							gnodeIdsArrayList.add(gnode.getId());
						}
						break;
					}
				}

			}
			if (gnodeIdsArrayList.size() != 0) {
				mapArrayList.put(qnode.getId(), gnodeIdsArrayList);
			} else {
				return null;
			}
		}
		boolean hasChange = true;
		while (hasChange) {
			hasChange = false;
			for (Iterator<String> qIterator = query.getNodes().keySet()
					.iterator(); qIterator.hasNext();) {
				Node qNode = query.getNodes().get(qIterator.next());
				ArrayList<String> simv = mapArrayList.get(qNode.getId());
				for (Iterator<String> qeIterator = qNode.getEdgesArrayList()
						.keySet().iterator(); qeIterator.hasNext();) {
					Edge edge = qNode.getEdgesArrayList()
							.get(qeIterator.next());
					ArrayList<String> simu = mapArrayList.get(edge.getToNode()
							.getId());
					for (int i = 0; i < simv.size(); i++) {
						boolean simok = false;
						for (String a : simu) {
							if (graph.getNodes().get(simv.get(i))
									.getEdgesArrayList().containsKey(a)) {
								simok = true;
								break;
							}
						}
						if (!simok) {
							// 删除对应关系
							simv.remove(simv.get(i));
							if (simv.size() == 0) {
								return null;
							}
							hasChange = true;
						}
					}

				}
			}

		}

		return mapArrayList;
	}
}
