package com.iscas.algorithm;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;
import com.iscas.launcher.Start;
import com.iscas.model.Edge;
import com.iscas.model.Graph;
import com.iscas.model.Node;
import com.iscas.model.ParentElement;
import com.iscas.model.QueryGraph;
import com.iscas.model.SearchElement;
import com.iscas.model.Tree;

public class GerateMinCostTree {

	// 原图
	private Graph graph;
	// query
	private QueryGraph queryGraph;
	// k，input匹配的个数
	private int k;
	// 已匹配的个数
	private int pk;
	// 结果树
	private ArrayList<Tree> treeGraphs;
	private MergeAlgorithm mergeAlgorithm;

	public GerateMinCostTree(Graph graph, QueryGraph queryGraph,
			MergeAlgorithm mergeAlgorithm, int k) {
		this.graph = graph;
		this.queryGraph = queryGraph;
		this.k = k;
		this.pk = 0;
		this.treeGraphs = new ArrayList();
		this.mergeAlgorithm = mergeAlgorithm;
	}

	// combination based, do not solve "or" relation
	public void NaiveCombinationSearching() {
		// 遍历每个mergedNode
		// 拥有多个mid的mergenode集合
		int fromNum = 0;
		ArrayList<Integer> hasmids = new ArrayList();
		ArrayList<Node> mMergedNodes = new ArrayList();
		for (Iterator<String> iterator = mergeAlgorithm.getMergeHashtable()
				.keySet().iterator(); iterator.hasNext();) {
			Node mergedNode = graph.getNodes().get(iterator.next());
			if (mergedNode.getMids().size() == queryGraph.getQueryNum()) {
				// 检测是否是最优解,先从个数上查看
				// System.out.println("best node: " + mergedNode.getId());
				int num = ValidateForAnd(mergeAlgorithm.getMergeHashtable()
						.get(mergedNode.getId()), mergedNode.getMids());
				if (num > 0) {
					Graph treeGraph = new Graph();
					treeGraph.addNode(mergedNode.getId());
					Tree tree = new Tree(treeGraph,
							queryGraph.getQueryNum() - 1, num);
					this.treeGraphs.add(tree);
					this.pk += num;
					fromNum++;
					if (pk >= k) {
						// System.out.println("k:" + k + ",pk:" + pk);
						return;
					}
				}
				mMergedNodes.add(mergedNode);
			} else if (mergedNode.getMids().size() > 1) {
				
				// 有多个mids
				Graph mgraph = mergeAlgorithm.getMergeHashtable().get(
						mergedNode.getId());
				int re = ValidateForAnd(mgraph, mergedNode.getMids());
				if (re == 0) {
					// 不能共存
					if (!mergeAlgorithm.isISO()) {
						ArrayList<Integer> deArrayList = new ArrayList<Integer>();
						for (int i = 0; i < mergedNode.getMids().size(); i++) {
							int re2 = Validate(mgraph, mergedNode.getMids()
									.get(i));
							if (re2 == 0) {
								deArrayList.add(mergedNode.getMids().get(i));
							}
						}
						for (int i = 0; i < deArrayList.size(); i++) {
							mergedNode.removeMid(i);
						}
						if (mergedNode.getMids().size() >= 2) {
							mMergedNodes.add(mergedNode);
						}
					} else {
						mMergedNodes.add(mergedNode);
					}
				}
			} else {
				// 只有一个
				int mid = mergedNode.getMids().get(0);
				if (!hasmids.contains(mid)) {
					hasmids.add(mid);
				}
			}
		}
		// 需要判断一个节点能不能同时拥有多个mids

		// 还需要k-pk个
		if (mMergedNodes.size() != 0) {
			// 获得补集, hasmids就是剩下需要增加的
			for (int i = 0; i < queryGraph.getQueryNum(); i++) {
				if (hasmids.contains(i)) {
					hasmids.remove((Integer) i);
				} else {
					hasmids.add(i);
				}
			}
			// 需要遍历有多个mids的节点的各种可能的mids组合,用hasmids进行判断，选取的mids必须覆盖hasmids
			// BFS
			ArrayList<ArrayList<Integer>> bfsSet = new ArrayList();
			// 增加初始元素
			for (int i = 0; i < mMergedNodes.get(0).getMids().size(); i++) {
				ArrayList<Integer> init = new ArrayList();
				init.add(mMergedNodes.get(0).getMids().get(i));
				bfsSet.add(init);
			}

			while (bfsSet.size() != 0) {
				ArrayList<Integer> currentState = bfsSet.remove(0);
				if (currentState.size() == mMergedNodes.size()) {
					// 判断是否符合mid条件
					// 判断是否mids能够全部覆盖hasmids
					boolean meet = true;
					for (int i = 0; i < hasmids.size(); i++) {
						if (!currentState.contains(hasmids.get(i))) {
							meet = false;
						}
					}
					if (meet) {
						// 设置每个多mids的mergeNode的mid，启动搜索
						// 保证每个mergedNode都只有一个mid
						for (Iterator<String> iterator = graph.getNodes()
								.keySet().iterator(); iterator.hasNext();) {
							String key = iterator.next();
							Node node = graph.getNodes().get(key);
							if (node.isMerged()) {
								// merged节点只留第一个
								if (node.getMids().size() > 1) {
									int mid = node.getMids().get(0);
									node.removeAllMids();
									node.addMids(mid);
								}

							} else {
								node.removeAllMids();
							}
						}

						for (int i = 0; i < mMergedNodes.size(); i++) {
							mMergedNodes.get(i).setMids(currentState.get(i));
						}
						// System.out.println("call NBS!!");
						NaiveBackwardSearching(fromNum);
					}
					continue;
				}
				for (int i = 0; i < mMergedNodes.get(currentState.size())
						.getMids().size(); i++) {
					ArrayList<Integer> nextState = new ArrayList();
					nextState.addAll(currentState);
					nextState.add(mMergedNodes.get(currentState.size())
							.getMids().get(i));
					bfsSet.add(nextState);

				}
			}
		} else {
			// 每个节点都只有一个mid，直接做即可
			NaiveBackwardSearching(0);
		}
	}

	// 判断子图graph是否匹配query图，返回匹配个数
	private int Validate(Graph graph, int queryIndex) {
		// System.out.println("Validate");
		Isomorphism isomorphism = new Isomorphism();
		ArrayList<HashMap<String, String>> rArrayList = isomorphism.ullmann(
				graph, queryGraph.getGraphs().get(queryIndex));
		if (rArrayList != null) {
			return rArrayList.size();
		}
		return 0;
	}

	private int ValidateForAnd2(Graph graph, ArrayList<Integer> queries) {
		// System.out.println("ValidateAnd");
		// System.out.println("graph size:" + graph.getNodes().size());
		try {
			Start.bfs(graph, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int matchNum = 0;
		ArrayList<ArrayList<HashMap<String, String>>> maps = new ArrayList();
		HashMap<Integer, Integer> midsHashMap = new HashMap();
		// 得到子图iso结果,重新计算子图iso，应该开销不大，因为图小
		Isomorphism isomorphism = new Isomorphism();
		for (int i = 0; i < queries.size(); i++) {
			ArrayList<HashMap<String, String>> rArrayList = isomorphism
					.ullmann(graph, queryGraph.getGraphs().get(queries.get(i)));
			if (rArrayList == null || rArrayList.size() == 0) {
				// 对于一个query，如果没有匹配，直接返回0
				return 0;
			}
			maps.add(rArrayList);
		}
		// System.out.println("finish iso");
		// 应该更改为复制graph，不能更改原graph, hard copy
		MergeAlgorithm smallMergeAlgorithm = new MergeAlgorithm();
		smallMergeAlgorithm.mergeForIso(graph, maps);

		for (Iterator<String> iterator = smallMergeAlgorithm
				.getMergeHashtable().keySet().iterator(); iterator.hasNext();) {
			String mergedId = iterator.next();
			ArrayList<Integer> mids = graph.getNodes().get(mergedId).getMids();
			if (mids.size() == 1) {
				if (midsHashMap.containsKey(mids.get(0))) {
					int currentN = midsHashMap.get(mids.get(0));
					midsHashMap.put(mids.get(0), currentN + 1);
				} else {
					midsHashMap.put(mids.get(0), 1);
				}

			} else {
				// approximate
				int mergedGSize = smallMergeAlgorithm.getMergeHashtable()
						.get(mergedId).getNodes().size();
				int totalQSize = 0;
				for (int i = 0; i < mids.size(); i++) {
					totalQSize += queryGraph.getGraphs()
							.get(queries.get(mids.get(i))).getNodes().size();
				}
				if (mergedGSize > totalQSize) {
					for (int i = 0; i < mids.size(); i++) {
						if (!midsHashMap.containsKey(i)) {
							midsHashMap.put(mids.get(0), 1);
						}
					}
				}
			}
		}
		if (midsHashMap.size() == queries.size()) {
			matchNum = 1;
			for (Iterator<Integer> iterator = midsHashMap.keySet().iterator(); iterator
					.hasNext();) {
				matchNum *= midsHashMap.get(iterator.next());
			}
		}
		return matchNum;
	}

	// 判断子图graph中，是否存在多个可共存的match（不重合），可能costly
	private int ValidateForAnd(Graph graph, ArrayList<Integer> queries) {
		// System.out.println("ValidateAnd");
		// System.out.println("graph size:" + graph.getNodes().size());
		int matchNum = 0;
		ArrayList<ArrayList<HashMap<String, String>>> maps = new ArrayList();
		// 得到子图iso结果,重新计算子图iso，应该开销不大，因为图小
		Isomorphism isomorphism = new Isomorphism();
		for (int i = 0; i < queries.size(); i++) {
			ArrayList<HashMap<String, String>> rArrayList = isomorphism
					.ullmann(graph, queryGraph.getGraphs().get(queries.get(i)));
			if (rArrayList == null || rArrayList.size() == 0) {
				// 对于一个query，如果没有匹配，直接返回0
				return 0;
			}
			maps.add(rArrayList);
		}
		// System.out.println("finish iso");
		// 判断重合度
		// 利用BFS构建
		ArrayList<ArrayList<Integer>> bfsSet = new ArrayList();
		// 增加初始元素

		for (int i = 0; i < maps.get(0).size(); i++) {
			ArrayList<Integer> init = new ArrayList();
			init.add(i);
			bfsSet.add(init);
		}
		while (bfsSet.size() != 0) {
			ArrayList<Integer> current = bfsSet.remove(0);
			// 遍历
			if (current.size() == queries.size()) {
				// 得到了一个匹配
				matchNum++;
				break;
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
					ArrayList<Integer> nextState = new ArrayList();
					nextState.addAll(current);
					nextState.add(j);
					bfsSet.add(nextState);
				}
			}
		}
		return matchNum;
	}

	// 被NaiveCombinationSearching调用,已有多少个匹配了，从0开始
	private void NaiveBackwardSearching(int fromk) {

		// 初始化BFSs
		ArrayList<PriorityQueue<SearchElement>> BFSs = new ArrayList();
		ArrayList<HashMap<String, Float>> distanceMaps = new ArrayList();

		for (int i = 0; i < queryGraph.getQueryNum(); i++) {
			// 初始化空BFS
			BFSs.add(new PriorityQueue<SearchElement>());
			distanceMaps.add(new HashMap<String, Float>());
		}
		// 把每个MergedNode加入BFS中
		for (Iterator<String> iterator = mergeAlgorithm.getMergeHashtable()
				.keySet().iterator(); iterator.hasNext();) {
			String mergeNodeIdString = iterator.next();
			Node mergedNode = graph.getNodes().get(mergeNodeIdString);

			// 设置距离为0
			for (int i = 0; i < mergedNode.getMids().size(); i++) {
				float distance = 0;
	//			System.out.println("out:"+mergedNode.getId()+' '+mergedNode.getMids().get(i));
				BFSs.get(mergedNode.getMids().get(i)).offer(
						new SearchElement(mergedNode, distance));
				distanceMaps.get(mergedNode.getMids().get(i)).put(
						mergeNodeIdString, distance);
			}
		}
		// 构建查找数据结构
		// parentId
		Hashtable<String, Hashtable<Integer, String>> parentHashtable = new Hashtable();

		// 开始查找
		while (!isBFSEmpty(BFSs)) {
			// 依次遍历每个bfs
			for (int mid = 0; mid < BFSs.size(); mid++) {
				PriorityQueue<SearchElement> bfs = BFSs.get(mid);
				HashMap<String, Float> distanceHashMap = distanceMaps.get(mid);
				if (bfs.size() != 0) {
					SearchElement element = bfs.poll();
					Node vNode = element.getNode();
//					System.out.println("start :" + vNode.getId());
					float vDistance = element.getDistance();
					distanceHashMap.remove(vNode.getId());
					// 传播mid
					vNode.addMids(mid);
//					if(mid==1) System.out.println("\t"+vNode.getId());
//					if(vNode.getId().equals("mn_0,32")) System.out.println(vNode.getMids().toString()+mid);
					// 判断终止条件
					if (vNode.getMids().size() == queryGraph.getQueryNum()) {
						// 找到了一个树
//						 System.out.println("find one :" + vNode.getId());
						Graph tree = new Graph();
						float cost = 0;
						// 从V开始遍历构建tree, 追随每个mid
						int matchNum = 1;
						Node firstNode = new Node(vNode.getId());
						firstNode.addLabels(vNode.getLabels());
						tree.addNode(firstNode);
						Hashtable<Node, ArrayList<Integer>> targetHashtable = new Hashtable();
						for (int i = 0; i < queryGraph.getQueryNum(); i++) {
							Node currentNode = vNode;
							while (!(currentNode.isMerged() && (parentHashtable
									.get(currentNode.getId()) == null || parentHashtable
									.get(currentNode.getId()).get(i) == null))) {
								// 一直从currentNode pass下去
								String parentIdString = parentHashtable.get(
										currentNode.getId()).get(i);
//								 System.out.println("currentId: " +
//								 currentNode.getId());
//								 System.out.println("parent :"
//								 + parentIdString);
//								 System.out.println("mid: " + i);
								float edgeWeight = currentNode
										.getEdgesArrayList()
										.get(parentIdString).getWeight();

								tree.addEdge(currentNode.getId(),
										parentIdString, edgeWeight, false);
								if (tree.getNodes().get(currentNode.getId())
										.getLabels().size() == 0) {
									tree.getNodes().get(currentNode.getId())
											.addLabels(currentNode.getLabels());
								}
								cost += edgeWeight;
								currentNode = graph.getNodes().get(
										parentIdString);
							}
							if (targetHashtable.containsKey(currentNode)) {
								targetHashtable.get(currentNode).add(i);
							} else {
								ArrayList<Integer> midsArrayList = new ArrayList();
								midsArrayList.add(i);
								targetHashtable.put(currentNode, midsArrayList);
							}
						}
						for (Iterator<Node> iterator = targetHashtable.keySet()
								.iterator(); iterator.hasNext();) {
							// 判断iso
							Node node = iterator.next();
							if (targetHashtable.get(node).size() > 1) {
								matchNum = matchNum
										* ValidateForAnd(
												mergeAlgorithm
														.getMergeHashtable()
														.get(node.getId()),
												targetHashtable.get(node));
							} else {
								matchNum = matchNum
										* Validate(
												mergeAlgorithm
														.getMergeHashtable()
														.get(node.getId()),
												targetHashtable.get(node)
														.get(0));
								if (matchNum == 0) {
									// iso失败，一般为sim的问题
									node.removeMid(targetHashtable.get(node)
											.get(0));
									break;
								}
							}

						}

						if (matchNum == 0) {
							continue;
						}
						// 判断一下是否加入
						int pos = isOverlap(tree, fromk, matchNum, cost);
						if (pos == -2) {
							// 重合
							continue;
						}
						Tree newTree = new Tree(tree, cost, matchNum);
						boolean conS = true;
						if (pk < this.k) {
							if (pos != -1 && pos < treeGraphs.size()) {
								// 插入
								treeGraphs.add(pos, newTree);
							} else {
								// 增加
								treeGraphs.add(newTree);
							}

							this.pk += matchNum;
							// if (pk >= k) {
							// conS = false;
							// }

						} else {
							if (pos != -1 && pos < treeGraphs.size()) {
								int deletNum = 0;
								if (pk
										+ matchNum
										- treeGraphs.get(treeGraphs.size() - 1)
												.getMatchNum() >= this.k) {
									// 删除最后一个tree
									deletNum = treeGraphs.get(
											treeGraphs.size() - 1)
											.getMatchNum();
									treeGraphs.remove(treeGraphs.size() - 1);
								}
								if (pos == treeGraphs.size()) {
									treeGraphs.add(newTree);
								} else {
									treeGraphs.add(pos, newTree);
								}
								pk = pk + matchNum - deletNum;
							} else {
								conS = false;
							}
						}
						// 是否继续
						if (conS) {
							continue;
						} else {
							return;
						}

					}
					// 遍历每一条边，寻找下一个节点
					for (Iterator<String> iterator = vNode.getEdgesArrayList()
							.keySet().iterator(); iterator.hasNext();) {

						Edge edge = (Edge) vNode.getEdgesArrayList().get(
								iterator.next());
						Node uNode = edge.getToNode();
//						if(vNode.getId().equals("mn_1,22")&&uNode.getId().equals("mn_0,32"))
//						System.out.println(vNode.getId()+'\t'+uNode.getId());
						float distance = edge.getWeight();
						// 判断新节点是否包含本次的mid的parent信息
						if (!uNode.getMids().contains(mid)) {
							// 判断是否待遍历
//							if(vNode.getId().equals("mn_1,22")&&uNode.getId().equals("mn_0,32"))
//								System.out.println("1");
							boolean in = false;
							if (distanceHashMap.containsKey(uNode.getId())) {
//								if(vNode.getId().equals("mn_1,22")&&uNode.getId().equals("mn_0,32"))
//									System.out.println("2");
								// System.out.println("exist");
								// 存在
								in = true;
								// 获取之前的distance
								float ordistance = distanceHashMap.get(uNode
										.getId());
								float newDistance = vDistance + distance;
								if (ordistance > newDistance) {
									// 更新
									SearchElement searchElement = new SearchElement(
											uNode, newDistance);
									distanceHashMap.put(uNode.getId(),
											newDistance);
									// 重新排序
									bfs.remove(searchElement);
									bfs.offer(searchElement);
									parentHashtable.get(uNode.getId()).put(mid,
											vNode.getId());
								}
							}

							if (!in) {
								
								// 新节点
								if (parentHashtable.get(uNode.getId()) == null) {
									Hashtable<Integer, String> newHashtable = new Hashtable<Integer, String>();
									newHashtable.put(mid, vNode.getId());
									parentHashtable.put(uNode.getId(),
											newHashtable);
								} else {
									parentHashtable.get(uNode.getId()).put(mid,
											vNode.getId());
								}

								// 增加到bfs中
								float newDistance = vDistance + distance;
								SearchElement newElement = new SearchElement(
										uNode, newDistance);
								bfs.offer(newElement);
								distanceHashMap.put(uNode.getId(), newDistance);
//								if(vNode.getId().equals("mn_1,22")&&uNode.getId().equals("mn_0,32"))
//									System.out.println(newDistance);
							}
						}
					}
				}
			}
		}
	}

	private boolean isBFSEmpty(ArrayList<PriorityQueue<SearchElement>> BFSs) {
		boolean empty = true;
		for (int i = 0; i < BFSs.size(); i++) {
			if (BFSs.get(i).size() != 0) {
				empty = false;
				return empty;
			}
		}
		return empty;
	}

	// 查询结果集合中与新生产的tree，是否有重合
	private int isOverlap(Graph newTree, int fromk, int matchNum, float cost) {
		int pos = -1;
		for (int i = fromk; i < treeGraphs.size(); i++) {
			if (treeGraphs.get(i).getCost() > cost) {
				pos = i;
				break;
			} else if (treeGraphs.get(i).getCost() == cost
					&& treeGraphs.get(i).getMatchNum() == matchNum) {
				// 判断去重
				boolean isEnd = true;
				Graph ortreeGraph = treeGraphs.get(i).getGraph();
				for (Iterator<String> iter = ortreeGraph.getNodes().keySet()
						.iterator(); iter.hasNext();) {
					if (newTree.getNodes().get(iter.next()) == null) {
						isEnd = false;
						break;
					}
				}
				if (isEnd) {
					// 重合的
					pos = -2;
					break;
				}
			}
		}
		return pos;
	}

	// 被Backward Searching与hub算法调用
	// 判断是否为simulation，然后进行verification在搜索过程中(version2)
	private void explore(
			boolean sim,
			Node vNode,
			float vDistance,
			int mid,
			PriorityQueue<SearchElement> bfs,
			HashMap<String, Float> distanceHashMap,
			Hashtable<String, Hashtable<String, ParentElement>> parentHashtable,
			Hashtable<String, ArrayList<Integer>> shareTargetMids,
			HashMap<String, Integer> targetValidHashMap,
			Hashtable<Integer, ArrayList<String>> hubsmids) {
		// 进行遍历
		// 获取vnode对mid的targetId
		String targetId = null;
		for (Iterator<String> iterator = parentHashtable.get(vNode.getId())
				.keySet().iterator(); iterator.hasNext();) {
			targetId = iterator.next();
			if (parentHashtable.get(vNode.getId()).get(targetId).getMids()
					.contains(mid)) {
				break;
			}
		}
		for (Iterator<String> edgeIterator = vNode.getEdgesArrayList().keySet()
				.iterator(); edgeIterator.hasNext();) {
			Edge edge = vNode.getEdgesArrayList().get(edgeIterator.next());
			Node uNode = edge.getToNode();
			float distance = edge.getWeight();
			float newDistance = vDistance + distance;
			boolean betterDistance = false;
			// 判断新节点是否包含本次的mid的parent信息
			if (!uNode.getMids().contains(mid)) {
				// 不包含mid
				boolean inBfs = false;
				// 查看是否在bfs中
				Float originDistanceFloat = distanceHashMap.get(uNode.getId());
				if (originDistanceFloat != null) {
					if (originDistanceFloat > newDistance) {
						betterDistance = true;
					}
					inBfs = true;
				}

				if (inBfs) {
					// 在bfs中
					// 获取uNode的targetId
					String uTargetIdString = null;
					boolean uTargetIdBad = false;
					Hashtable<String, ParentElement> utHash = parentHashtable
							.get(uNode.getId());
					// 查找uTargetIdString
					for (Iterator<String> iterator = utHash.keySet().iterator(); iterator
							.hasNext();) {
						uTargetIdString = iterator.next();
						ArrayList<Integer> arrayList = utHash.get(
								uTargetIdString).getMids();
						if (arrayList.contains(mid)) {
							if (arrayList.size() > 1) {
								uTargetIdBad = true;
							}
							break;
						}
					}

					if (targetId.equals(uTargetIdString)) {
						// targetId相同的，判断distance
						if (betterDistance) {
							// 换
							// 修改parent信息
							utHash.get(targetId).setParentId(vNode.getId());
							// 改bfs
							distanceHashMap.put(uNode.getId(), newDistance);
							SearchElement uElement = new SearchElement(uNode,
									newDistance);
							bfs.remove(uElement);
							bfs.offer(uElement);
						}

					} else {
						// 判断v的targetId是否重合于u
						if (!utHash.containsKey(targetId)) {
							// 不重合
							if (betterDistance || uTargetIdBad) {
								// 改parents与shared信息
								if (uTargetIdBad) {
									// 删除信息
									ParentElement utElement = utHash
											.get(uTargetIdString);
									utElement.removeMid(mid);
									if (utElement.getMids().size() == 1) {
										shareTargetMids.get(uNode.getId())
												.remove(utElement.getMids()
														.get(0));
									}
									shareTargetMids.get(uNode.getId()).remove(
											(Integer) mid);
									// 增加信息
									ParentElement newParentElement = new ParentElement(
											vNode.getId());
									newParentElement.addMid(mid);
									utHash.put(targetId, newParentElement);
								} else {
									// 直接删除utarget的信息
									utHash.remove(uTargetIdString);
									// 增加信息
									ParentElement newParentElement = new ParentElement(
											vNode.getId());
									newParentElement.addMid(mid);
									utHash.put(targetId, newParentElement);
								}

								// 改bfs
								distanceHashMap.put(uNode.getId(), newDistance);
								SearchElement uElement = new SearchElement(
										uNode, newDistance);
								bfs.remove(uElement);
								bfs.offer(uElement);

							} else {
								if (sim) {
									// 验证
									Integer validNumInteger = targetValidHashMap
											.get(uTargetIdString);
									int score = 0;
									if (validNumInteger != null) {
										score = validNumInteger;
									} else {
										score = Validate(
												mergeAlgorithm
														.getMergeHashtable()
														.get(uTargetIdString),
												mid);
										targetValidHashMap.put(uTargetIdString,
												score);
										if (score == 0) {
											deleteNodeMid(uNode, mid,
													uTargetIdString,
													parentHashtable,
													shareTargetMids, hubsmids,
													bfs);
										}
									}
									if (score == 0) {
										// update and add bfs
										distanceHashMap.put(uNode.getId(),
												newDistance);
										SearchElement uElement = new SearchElement(
												uNode, newDistance);
										bfs.remove(uElement);
										bfs.offer(uElement);
										// 删除targetId
										utHash.remove(uTargetIdString);
										// 增加信息
										ParentElement newParentElement = new ParentElement(
												vNode.getId());
										newParentElement.addMid(mid);
										utHash.put(targetId, newParentElement);
									}
								}
							}
						}
					}
				} else {
					// 不在BFS中
					// 增加parent信息与shareMids信息
					Hashtable<String, ParentElement> hashtable = parentHashtable
							.get(uNode.getId());
					if (hashtable == null) {
						// mid一定不在该节点的结构中，所以直接增加即可
						hashtable = new Hashtable();
						ParentElement parentElement = new ParentElement(
								vNode.getId());
						parentElement.addMid(mid);
						hashtable.put(targetId, parentElement);
						parentHashtable.put(uNode.getId(), hashtable);
					} else {
						// 判断targetId是否重复
						if (hashtable.containsKey(targetId)) {
							if (hashtable.get(targetId).getMids().size() > 1) {
								shareTargetMids.get(uNode.getId()).add(mid);
							} else {
								ArrayList<Integer> aList = shareTargetMids
										.get(uNode.getId());

								if (aList == null) {
									aList = new ArrayList();
								}
								aList.add(mid);
								aList.add(hashtable.get(targetId).getMids()
										.get(0));
								shareTargetMids.put(uNode.getId(), aList);
							}
							hashtable.get(targetId).addMid(mid);
						} else {
							ParentElement parentElement = new ParentElement(
									vNode.getId());
							parentElement.addMid(mid);
							hashtable.put(targetId, parentElement);
						}
					}
					// 直接增加到bfs
					distanceHashMap.put(uNode.getId(), newDistance);
					SearchElement uElement = new SearchElement(uNode,
							newDistance);
					bfs.offer(uElement);
				}

			} else {
				// u包含mid,需要查看是否是个重复的target
				if (shareTargetMids.get(uNode.getId()) != null
						&& shareTargetMids.get(uNode.getId()).contains(mid)) {
					// 重复
					// 查看v的target是否和u的target重合
					Hashtable<String, ParentElement> utHash = parentHashtable
							.get(uNode.getId());
					if (!utHash.containsKey(targetId)) {
						// 改u的parent信息与shared信息
						for (Iterator<String> iterator = utHash.keySet()
								.iterator(); iterator.hasNext();) {
							String utargetId = iterator.next();
							if (utHash.get(utargetId).getMids().contains(mid)) {
								// 修改shared信息
								utHash.get(utargetId).removeMid(mid);
								if (utHash.get(utargetId).getMids().size() == 1) {
									shareTargetMids.get(uNode.getId()).remove(
											(Integer) utHash.get(utargetId)
													.getMids().get(0));
								}
								shareTargetMids.get(uNode.getId()).remove(
										(Integer) mid);
								break;
							}
						}
						// 增加parent信息
						ParentElement parentElement = new ParentElement(
								vNode.getId());
						parentElement.addMid(mid);
						utHash.put(targetId, parentElement);
						uNode.removeMid(mid);
						if (hubsmids != null && uNode.isHub()) {
							ArrayList<String> hubsArrayList = hubsmids.get(mid);
							if (hubsArrayList != null
									&& hubsArrayList.size() == 1) {
								hubsmids.remove(mid);
							} else if (hubsArrayList.size() > 1) {
								hubsArrayList.remove(uNode.getId());
							}
						}
						distanceHashMap.put(uNode.getId(), newDistance);
						SearchElement uElement = new SearchElement(uNode,
								newDistance);
						bfs.offer(uElement);
					}
				} else {
					// 有label但不在sharedlabels中，需要verification
					if (sim) {
						// 仅包含一个，查看该label是否为无效的
						String uTargetIdString = null;
						Hashtable<String, ParentElement> utHash = parentHashtable
								.get(uNode.getId());
						for (Iterator<String> iterator = utHash.keySet()
								.iterator(); iterator.hasNext();) {
							uTargetIdString = iterator.next();
							ArrayList<Integer> arrayList = utHash.get(
									uTargetIdString).getMids();
							if (arrayList.contains(mid)) {
								break;
							}
						}
						if (targetId != uTargetIdString) {
							// 验证原来的
							Integer validNumInteger = targetValidHashMap
									.get(uTargetIdString);
							int score = 0;
							if (validNumInteger != null) {
								score = validNumInteger;
							} else {
								score = Validate(
										mergeAlgorithm.getMergeHashtable().get(
												uTargetIdString), mid);
								targetValidHashMap.put(uTargetIdString, score);
								if (score == 0) {
									deleteNodeMid(uNode, mid, uTargetIdString,
											parentHashtable, shareTargetMids,
											hubsmids, bfs);
								}
							}
							if (score == 0) {
								// 删除targetId
								utHash.remove(uTargetIdString);
								// 增加信息
								ParentElement newParentElement = new ParentElement(
										vNode.getId());
								newParentElement.addMid(mid);
								utHash.put(targetId, newParentElement);
								// add bfs
								distanceHashMap.put(uNode.getId(), newDistance);
								SearchElement uElement = new SearchElement(
										uNode, newDistance);
								bfs.offer(uElement);
							}

						}
					}
				}
			}
		}

	}

	// Backward Searching, solve "or" relation
	public void BackWardSearching(boolean sim) {

		// 初始化数据结构
		// parent结构，<节点id,<targetId, parentElement>>
		// parentElement: mids,parentId
		Hashtable<String, Hashtable<String, ParentElement>> parentHashtable = new Hashtable();
		// shareTargetMids
		Hashtable<String, ArrayList<Integer>> shareTargetMids = new Hashtable();

		// 初始化BFSs
		ArrayList<PriorityQueue<SearchElement>> BFSs = new ArrayList();
		ArrayList<HashMap<String, Float>> distanceMaps = new ArrayList();

		// targetId的验证状态 (mid,targetId, matchNum), only save one to one
		ArrayList<HashMap<String, Integer>> targetValidHashMaps = new ArrayList();

		for (int i = 0; i < queryGraph.getQueryNum(); i++) {
			// 初始化空BFS
			BFSs.add(new PriorityQueue<SearchElement>());
			distanceMaps.add(new HashMap<String, Float>());
			targetValidHashMaps.add(new HashMap<String, Integer>());
		}

		int fromNum = 0;
		// 把每个MergedNode加入BFS中,如果有多个mid，就加入多个bfs中
		// System.out.println("merged NodeN:"
		// + mergeAlgorithm.getMergeHashtable().size());
		for (Iterator<String> iterator = mergeAlgorithm.getMergeHashtable()
				.keySet().iterator(); iterator.hasNext();) {
			String mergeNodeIdString = iterator.next();
			// System.out.println(mergeNodeIdString);
			Node mergedNode = graph.getNodes().get(mergeNodeIdString);
			// System.out.println(mergedNode.getMids());
			// 判断是否有一个node已经拥有所有的mids了
			if (mergedNode.getMids().size() == queryGraph.getQueryNum()) {
				// 检测是否是最优解,先从个数上查看
				// System.out.println("best node: " + mergedNode.getId());
				int num = ValidateForAnd(mergeAlgorithm.getMergeHashtable()
						.get(mergedNode.getId()), mergedNode.getMids());
				if (num > 0) {
					Graph treeGraph = new Graph();
					treeGraph.addNode(mergedNode.getId());
					Tree tree = new Tree(treeGraph, 0, num);
					this.treeGraphs.add(tree);
					this.pk += num;
					fromNum++;
					if (pk >= k) {
						return;
					}
				}
			}

			// 设置距离为0,入BFS
			for (int i = 0; i < mergedNode.getMids().size(); i++) {
				int mid = mergedNode.getMids().get(i);
				float distance = 0;
				BFSs.get(mid).offer(new SearchElement(mergedNode, distance));
				distanceMaps.get(mid).put(mergedNode.getId(), distance);
			}
			// 存储parent信息
			if (mergedNode.getMids().size() > 1) {
				shareTargetMids.put(mergeNodeIdString, new ArrayList(
						mergedNode.getMids()));
			}
			Hashtable<String, ParentElement> hashtable = new Hashtable();
			ParentElement parentElement = new ParentElement(mergeNodeIdString);
			parentElement.setMids(new ArrayList(mergedNode.getMids()));
			hashtable.put(mergeNodeIdString, parentElement);
			parentHashtable.put(mergeNodeIdString, hashtable);
		}

		while (!isBFSEmpty(BFSs)) {
			// 依次遍历每个bfs
			for (int mid = 0; mid < BFSs.size(); mid++) {
				PriorityQueue<SearchElement> bfs = BFSs.get(mid);
				HashMap<String, Float> distanceHashMap = distanceMaps.get(mid);
				if (bfs.size() != 0) {
					// 弹出一个元素，遍历
					SearchElement element = bfs.poll();
					Node vNode = element.getNode();
					// 同步弹出bfs中的查找缓存
					distanceHashMap.remove(vNode.getId());
					float vDistance = element.getDistance();

					// 判断是否是来自于一个已非法的targetId的传播
					String targetId = null;
					for (Iterator<String> iterator = parentHashtable
							.get(vNode.getId()).keySet().iterator(); iterator
							.hasNext();) {
						targetId = iterator.next();
						if (parentHashtable.get(vNode.getId()).get(targetId)
								.getMids().contains(mid)) {
							break;
						}
					}
					Integer validNum = targetValidHashMaps.get(mid).get(
							targetId);
					// 如果为非法，那么退出，继续搜其他的
					if (validNum != null && validNum == 0) {
						ArrayList<Integer> invalidMids = parentHashtable
								.get(vNode.getId()).get(targetId).getMids();
						if (invalidMids.size() == 1) {
							parentHashtable.get(vNode.getId()).remove(targetId);
						} else {
							invalidMids.remove((Integer) mid);
							shareTargetMids.get(vNode.getId()).remove(
									(Integer) mid);
							if (invalidMids.size() == 1) {
								shareTargetMids.get(vNode.getId()).remove(
										(Integer) invalidMids.get(0));
							}
						}
						continue;
					}

					// 传播mid
					vNode.addMids(mid);
					// 判断终止条件
					// 判断v是否有所有的mids了
					if (vNode.getMids().size() == queryGraph.getQueryNum()) {
						boolean nContinue = true;
						Hashtable<String, ParentElement> vparents = parentHashtable
								.get(vNode.getId());
						if (shareTargetMids.get(vNode.getId()) == null
								|| shareTargetMids.get(vNode.getId()).size() == 0) {
							// 真实的1对1结果
							// 验证树是否对
							int matchNum = 1;
							for (Iterator<String> iterator = vparents.keySet()
									.iterator(); iterator.hasNext();) {
								String ptargetId = iterator.next();
								Graph tgraph = mergeAlgorithm
										.getMergeHashtable().get(ptargetId);
								int pmid = vparents.get(ptargetId).getMids()
										.get(0);
								Integer pvalidNum = targetValidHashMaps.get(
										pmid).get(ptargetId);
								if (pvalidNum != null) {
									// test before
									if (pvalidNum != 0) {
										// 成功匹配
										matchNum *= pvalidNum;
										
									} else {
										// 没有匹配的
										matchNum = 0;
										vNode.removeMid(pmid);
										vparents.remove(ptargetId);
										if (mid == pmid) {
											// 这轮正是错targetId遍历，所以无需继续
											nContinue = false;
										}
										break;
									}
								} else {
									// test
									int num = Validate(tgraph, pmid);
									matchNum *= num;
									// 存valid信息
									targetValidHashMaps.get(pmid).put(
											ptargetId, num);
									if (num == 0) {
										// 匹配失败
										// 删除节点信息
										vNode.removeMid(pmid);
										vparents.remove(ptargetId);

										// 删除ptargetId节点信息
										if (!vNode.getId().equals(ptargetId)) {
											Node ptargetNode = graph.getNodes()
													.get(ptargetId);
											ptargetNode.removeMid(pmid);
											if (parentHashtable.get(ptargetId) != null
													&& parentHashtable.get(
															ptargetId).get(
															ptargetId) != null) {
												if (parentHashtable
														.get(ptargetId)
														.get(ptargetId)
														.getMids().size() == 1) {
													parentHashtable.get(
															ptargetId).remove(
															ptargetId);
												} else {
													parentHashtable
															.get(ptargetId)
															.get(ptargetId)
															.getMids()
															.remove((Integer) pmid);
													shareTargetMids.get(
															ptargetId).remove(
															(Integer) pmid);
													if (parentHashtable
															.get(ptargetId)
															.get(ptargetId)
															.getMids().size() == 1) {
														shareTargetMids
																.get(ptargetId)
																.remove((Integer) parentHashtable
																		.get(ptargetId)
																		.get(ptargetId)
																		.getMids()
																		.get(0));
													}
												}
											}
										}
										if (mid == pmid) {
											// 这轮正是错targetId遍历，所以无需继续
											nContinue = false;
										}
										break;
									}
								}
							}
							if (matchNum != 0) {
								
								// 从vNode开始产生树，存储
								// System.out.println("find a tree from: " +
								// vNode.getId());
								Graph rootTree = new Graph();
								rootTree.addNode(vNode.getId(),
										vNode.getLabels());
								float cost = 0;
								for (Iterator<String> iterator = vparents
										.keySet().iterator(); iterator
										.hasNext();) {
									String ptargetId = iterator.next();
									// 从vNode开始找ptargetId节点
									String currentId = vNode.getId();
									while (!currentId.equals(ptargetId)) {

										String nextId = parentHashtable
												.get(currentId).get(ptargetId)
												.getParentId();
										Node nextNode = graph.getNodes().get(
												nextId);
										float weight = 0;

										weight = nextNode.getEdgesArrayList()
												.get(currentId).getWeight();
										cost += weight;
										rootTree.addNode(nextId,
												nextNode.getLabels());
										rootTree.addEdge(currentId, nextId,
												weight, false);
										currentId = nextId;
									}
								}
								// 查重，并加入结果集合中,tree不是按cost顺序产生的！！！
								int pos = isOverlap(rootTree, fromNum,
										matchNum, cost);
								if (pos != -2) {
									// 不重合,加到结果集，如何够了，就退出
									if (pos == -1) {
										this.treeGraphs.add(new Tree(rootTree,
												cost, matchNum));
									} else {
										this.treeGraphs.add(pos, new Tree(
												rootTree, cost, matchNum));
									}
									pk += matchNum;
									if (pk >= k) {
										return;
									}
								}
								// System.out.println(cost);
								nContinue = false;
							}

						} else {
							// 存在1对多
							// 判断是否为多余根，或者单纯一个节点 (parentId是不是全一样)
							boolean useless = true;
							String pI = null;
							for (Iterator<String> iterator = vparents.keySet()
									.iterator(); iterator.hasNext();) {
								ParentElement parentElement = vparents
										.get(iterator.next());
								if (pI == null) {
									pI = parentElement.getParentId();
								} else {
									if (!pI.equals(parentElement.getParentId())) {
										useless = false;
										break;
									}
								}
							}
							if (!useless) {
								// 验证树
								int matchNum = 1;
								for (Iterator<String> iterator = vparents
										.keySet().iterator(); iterator
										.hasNext();) {
									String ptargetId = iterator.next();
									ArrayList<Integer> midsArrayList = vparents
											.get(ptargetId).getMids();
									Graph tgraph = mergeAlgorithm
											.getMergeHashtable().get(ptargetId);
									if (midsArrayList.size() == 1) {
										Integer pvalidNum = targetValidHashMaps
												.get(midsArrayList.get(0)).get(
														ptargetId);
										if (pvalidNum != null) {
											if (pvalidNum == 0) {
												matchNum = 0;
												vNode.removeMid(midsArrayList
														.get(0));
												vparents.remove(ptargetId);
												if (mid == midsArrayList.get(0)) {
													// 这轮正是错误的targetId遍历，所以无需继续
													nContinue = false;
												}
												break;
											} else {
												matchNum *= pvalidNum;
											}
										} else {
											int num = Validate(tgraph,
													midsArrayList.get(0));
											matchNum *= num;
											targetValidHashMaps.get(
													midsArrayList.get(0)).put(
													ptargetId, num);
											if (num == 0) {
												int pmid = midsArrayList.get(0);
												// 匹配失败
												// 删除节点信息
												vNode.removeMid((Integer) pmid);
												vparents.remove(ptargetId);

												// 删除ptarget节点信息
												if (!vNode.getId().equals(
														ptargetId)) {
													Node ptargetNode = graph
															.getNodes().get(
																	ptargetId);
													ptargetNode.removeMid(pmid);
													if (parentHashtable
															.get(ptargetId)
															.get(ptargetId)
															.getMids().size() == 1) {
														parentHashtable
																.get(ptargetId)
																.remove(ptargetId);
													} else {
														parentHashtable
																.get(ptargetId)
																.get(ptargetId)
																.getMids()
																.remove((Integer) pmid);
														shareTargetMids
																.get(ptargetId)
																.remove((Integer) pmid);
														if (parentHashtable
																.get(ptargetId)
																.get(ptargetId)
																.getMids()
																.size() == 1) {
															shareTargetMids
																	.get(ptargetId)
																	.remove((Integer) parentHashtable
																			.get(ptargetId)
																			.get(ptargetId)
																			.getMids()
																			.get(0));
														}
													}
												}
												if (mid == pmid) {
													// 这轮正式错targetId遍历，所以无需继续
													nContinue = false;
												}
												break;
											}
										}

									} else {
										// ptargetId多个mids
										int num = ValidateForAnd(tgraph,
												midsArrayList);
										matchNum *= num;

										if (matchNum == 0) {
											// 不匹配，且关系无法删除
											break;
										}
									}
								}
								if (matchNum != 0) {
									// System.out.println("find a duotree from: "
									// + vNode.getId());
									// 生成树
									// 从vNode开始产生树，存储
									Graph rootTree = new Graph();
									rootTree.addNode(vNode.getId(),
											vNode.getLabels());
									float cost = 0;
									for (Iterator<String> iterator = vparents
											.keySet().iterator(); iterator
											.hasNext();) {
										String ptargetId = iterator.next();
										// 从vNode开始找ptargetId节点
										String currentId = vNode.getId();
										while (!currentId.equals(ptargetId)) {
											String nextId = parentHashtable
													.get(currentId)
													.get(ptargetId)
													.getParentId();
											Node nextNode = graph.getNodes()
													.get(nextId);
											float weight = 0;
											weight = nextNode
													.getEdgesArrayList()
													.get(currentId).getWeight();
											cost += weight;
											rootTree.addNode(nextId,
													nextNode.getLabels());
											rootTree.addEdge(currentId, nextId,
													weight, false);
											currentId = nextId;
										}
									}
									// System.out.println(cost);
									// 查重，并加入结果集合中,tree不是按cost顺序产生的
									nContinue = false;
									int pos = isOverlap(rootTree, fromNum,
											matchNum, cost);
									if (pos != -2) {
										// 不重合,加到结果集，如何够了，就退出
										if (pos == -1) {
											this.treeGraphs.add(new Tree(
													rootTree, cost, matchNum));
										} else {
											this.treeGraphs.add(pos, new Tree(
													rootTree, cost, matchNum));
										}
										pk += matchNum;
										if (pk >= k) {
											return;
										}
									}
								}
							}
						}
						if (!nContinue) {
							continue;
						}
					}
					// 进行遍历
					explore(sim, vNode, vDistance, mid, bfs, distanceHashMap,
							parentHashtable, shareTargetMids,
							targetValidHashMaps.get(mid), null);
				}
			}
		}

	}


	private void deleteNodeMid(
			Node node,
			int mid,
			String targetId,
			Hashtable<String, Hashtable<String, ParentElement>> parentHashtable,
			Hashtable<String, ArrayList<Integer>> shareTargetMids,
			Hashtable<Integer, ArrayList<String>> hubsmids,
			PriorityQueue<SearchElement> pbfs) {
		// 删除本node的信息
		node.removeMid(mid);

		ParentElement parentElement = parentHashtable.get(node.getId()).get(
				targetId);
		if (parentElement.getMids().size() == 1) {
			parentHashtable.get(node.getId()).remove(targetId);
		} else {
			parentElement.removeMid(mid);
			ArrayList<Integer> sharedMidsArrayList = shareTargetMids.get(node
					.getId());
			sharedMidsArrayList.remove((Integer) mid);
			if (parentElement.getMids().size() == 1) {
				sharedMidsArrayList.remove(parentElement.getMids().get(0));
			}
		}

		if (node.isHub() && hubsmids.containsKey(mid)) {
			ArrayList<String> hubsArrayList = hubsmids.get(mid);
			if (hubsArrayList != null && hubsArrayList.size() == 1) {
				hubsmids.remove(mid);
			} else if (hubsArrayList.size() > 1) {
				hubsArrayList.remove(node.getId());
			}
		}
		// 删除targetNode信息
		if (!node.getId().equals(targetId)) {
			Node ptargetNode = graph.getNodes().get(targetId);
			ptargetNode.removeMid(mid);
			if (ptargetNode.isHub()) {
				ArrayList<String> hubsArrayList = hubsmids.get(mid);
				if (hubsArrayList != null && hubsArrayList.size() == 1) {
					hubsmids.remove(mid);
				} else if (hubsArrayList.size() > 1) {
					hubsArrayList.remove(targetId);
				}
			}
			if (parentHashtable.get(targetId) != null
					&& parentHashtable.get(targetId).get(targetId) != null) {
				if (parentHashtable.get(targetId).get(targetId).getMids()
						.size() == 1) {
					parentHashtable.get(targetId).remove(targetId);
				} else {
					parentHashtable.get(targetId).get(targetId).getMids()
							.remove((Integer) mid);
					shareTargetMids.get(targetId).remove((Integer) mid);
					if (parentHashtable.get(targetId).get(targetId).getMids()
							.size() == 1) {
						shareTargetMids.get(targetId).remove(
								(Integer) parentHashtable.get(targetId)
										.get(targetId).getMids().get(0));
					}
				}
			}
		}
	}

	// 被hub算法调用，把数组按照与Node节点的距离进行排序，从近到远，并处理不可达的
	private void sortBasedOnDistance(ArrayList<String> hubs, Node aNode) {
		for (int i = 0; i < hubs.size() - 1; i++) {
			float dd1 = aNode.gethubDistance(hubs.get(i));
			for (int j = i + 1; j < hubs.size(); j++) {
				float dd2 = aNode.gethubDistance(hubs.get(j));
				if (dd2 < dd1) {
					String c = hubs.get(i);
					hubs.set(i, hubs.get(j));
					hubs.set(j, c);
					dd1 = dd2;
				}
			}
		}
		int pos = -1;
		for (int i = 0; i < hubs.size(); i++) {
			if (aNode.gethubDistance(hubs.get(i)) == -1) {
				pos = i;
			} else {
				break;
			}
		}
		if (pos != -1) {
			for (int i = 0; i <= pos; i++) {
				hubs.remove(0);
			}
		}
	}

	public Graph getGraph() {
		return graph;
	}

	public QueryGraph getQueryGraph() {
		return queryGraph;
	}

	public int getPK() {
		return pk;
	}

	public ArrayList<Tree> getTreeGraphs() {
		return treeGraphs;
	}

	public int TreeFoundNum() {
		return this.treeGraphs.size();
	}


}
