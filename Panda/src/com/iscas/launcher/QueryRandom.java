package com.iscas.launcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import com.iscas.controller.ExperimentEnv;
import com.iscas.controller.InputFromFile;
import com.iscas.controller.ProperitiesController;
import com.iscas.model.Graph;
import com.iscas.model.Node;

public class QueryRandom {

	private Graph graph;
	public String dataSetName = "";
	public String labelFileName = "";

	public QueryRandom(Graph graph) {
		this.graph = graph;
	}

	public QueryRandom() {

	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public Graph randomQuery(int vNumQ, int width, int height, String label,
			int overlap_prob, ArrayList<String> preNodes) {
		Graph subGraph = new Graph();
		int vNum = graph.getNodes().size();
		// 目标数组
		ArrayList<String> selectedNodeIds = new ArrayList<>();
		String startId = "";
		Random random = new Random();

		// randomly make query parts be overlapped
		boolean overlap = false;
		if (label != null && !label.equals("")) {
			int val = random.nextInt(10) % 10;
			if (val <= overlap_prob) {
				overlap = true;
			}
		}
		if (overlap) {
			System.out.println("overlap");
			boolean selected = false;
			for (Iterator<String> iterator = graph.getNodes().keySet()
					.iterator(); iterator.hasNext();) {
				startId = iterator.next();
				Node node = graph.getNodes().get(startId);
				if ((preNodes == null || !preNodes.contains(startId))
						&& node.getLabels().contains(label)) {
					selected = true;
					break;
				}
			}
			if(!selected){
				System.out.println("false overlap");
			}

		} else {
			int vIndex = random.nextInt(vNum) % vNum;
			startId = new ArrayList<>(graph.getNodes().keySet()).get(vIndex);

			while ((preNodes != null && preNodes.contains(startId))
					|| graph.getNodes().get(startId).getLabels().size() == 0) {
				vIndex = random.nextInt(vNum) % vNum;
				startId = new ArrayList<>(graph.getNodes().keySet())
						.get(vIndex);
			}
		}

		selectedNodeIds.add(startId);
		Node startNode = graph.getNodes().get(startId);
		int heightIndex = 0;
		Node seedNode = startNode;
		while (heightIndex < height && selectedNodeIds.size() < vNumQ) {
			int edgeNum = seedNode.getEdgeNum();
			if (edgeNum == 0) {
				break;
			}
			ArrayList<String> thislevelNodesArrayList = new ArrayList<>();
			for (Iterator<String> iterator = seedNode.getEdgesArrayList()
					.keySet().iterator(); iterator.hasNext();) {
				String nextIdString = iterator.next();
				Node nextNode = seedNode.getEdgesArrayList().get(nextIdString)
						.getToNode();
				if (!(nextNode.getLabels().size() == 0 || (preNodes != null && preNodes
						.contains(nextIdString)))) {
					selectedNodeIds.add(nextIdString);
					thislevelNodesArrayList.add(nextIdString);
					if (thislevelNodesArrayList.size() >= width
							|| selectedNodeIds.size() >= vNumQ) {
						break;
					}
				}
			}
			if (selectedNodeIds.size() >= vNumQ
					|| thislevelNodesArrayList.size() == 0) {
				break;
			}

			// select next seed node from thislevelNodes
			int num = thislevelNodesArrayList.size();
			int seedIndex = random.nextInt(num) % num;
			while (seedNode.getEdgesArrayList()
					.get(thislevelNodesArrayList.get(seedIndex)).getToNode()
					.getEdgeNum() == 0) {
				seedIndex = random.nextInt(num) % num;
			}
			seedNode = seedNode.getEdgesArrayList()
					.get(thislevelNodesArrayList.get(seedIndex)).getToNode();
			heightIndex++;

		}
		// 构建图从selectedNodeIds
		for (int i = 0; i < selectedNodeIds.size(); i++) {
			String nodeId = selectedNodeIds.get(i);
			subGraph.addNode(nodeId, graph.getNodes().get(nodeId).getLabels());
		}
		// create edge
		// 30% 抽象概率
		int edgeDelete_prob = 3;
		System.out.println("create edge");
		for (int i = 0; i < selectedNodeIds.size(); i++) {
			String nodeId = selectedNodeIds.get(i);
			Node node = graph.getNodes().get(nodeId);
			for (int j = 0; j < selectedNodeIds.size(); j++) {
				if (node.getEdgesArrayList()
						.containsKey(selectedNodeIds.get(j))) {
					if (subGraph.getNodes().get(nodeId).getEdgeNum() > 0) {
						int value = random.nextInt(10) % 10;
						if (value >= edgeDelete_prob) {
							subGraph.addEdge(nodeId, selectedNodeIds.get(j), 0,
									false);
						} else {
							System.out.println("shrinked edges in Query");
						}
					} else {
						subGraph.addEdge(nodeId, selectedNodeIds.get(j), 0,
								false);
					}
				}
			}
		}
		return subGraph;
	}

	/**
	 * non-overlapped query graph
	 * */
	public ArrayList<Graph> randomQueries(int k, int vNumQ, int width,
			int height) {
		ArrayList<Graph> queryGraphs = new ArrayList<>();
		ArrayList<String> preNodes = new ArrayList<>();
		String label = "";
		for (int i = 0; i < k; i++) {
			System.out.println("create No." + i);
			Graph q = null;
			q = this.randomQuery(vNumQ, width, height, label, 5, preNodes);
			queryGraphs.add(q);
			ArrayList<String> nodes = new ArrayList<>(q.getNodes().keySet());
			preNodes.addAll(nodes);
			label = q.getNodes().get(nodes.get(0)).getLabels().get(0);
		}
		return queryGraphs;
	}

	public void generateToQueryFile(ArrayList<Graph> queries,
			String queryFileName) {
		try {
			File outFile = new File(ExperimentEnv.outputDirectory
					+ queryFileName + ".txt");
			outFile.createNewFile();
			BufferedWriter queryWriter = new BufferedWriter(new FileWriter(
					outFile));
			queryWriter.write(queries.size() + "");
			queryWriter.newLine();
			for (int i = 0; i < queries.size(); i++) {
				Hashtable<String, String> map = new Hashtable<String, String>();
				Graph qGraph = queries.get(i);
				int index = 1;
				for (Iterator<String> iterator = qGraph.getNodes().keySet()
						.iterator(); iterator.hasNext();) {
					String nodeId = iterator.next();
					// first label
					String label = qGraph.getNodes().get(nodeId).getLabels()
							.get(0);
					queryWriter.write(index + "" + "\t" + label);
					queryWriter.newLine();
					map.put(nodeId, index + "");
					index++;
				}
				queryWriter.write("#");
				queryWriter.newLine();
				// structure
				for (Iterator<String> iterator = qGraph.getNodes().keySet()
						.iterator(); iterator.hasNext();) {
					String nodeId = iterator.next();
					Node node = qGraph.getNodes().get(nodeId);
					for (Iterator<String> edgeIterator = node
							.getEdgesArrayList().keySet().iterator(); edgeIterator
							.hasNext();) {
						queryWriter.write(map.get(nodeId) + "\t"
								+ map.get(edgeIterator.next()));
						queryWriter.newLine();
					}
				}
				queryWriter.write("#");
				queryWriter.newLine();
				queryWriter.flush();
			}
			queryWriter.flush();
			queryWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		QueryRandom queryRandom = new QueryRandom();

		ProperitiesController properitiesController = ProperitiesController
				.getProperitiesController();
		properitiesController.loadProperitiesToEnvironmentSimple(queryRandom);
		properitiesController.close();

		String queryFirst = queryRandom.dataSetName.substring(0, 3) + "q";

		InputFromFile inputFromFile = new InputFromFile(
				queryRandom.dataSetName, queryRandom.labelFileName, "");
		inputFromFile.read();
		inputFromFile.readLabels();
		Graph graph = inputFromFile.getGraph();
		queryRandom.setGraph(graph);

		System.out.println("begin");
		long beginTime = System.currentTimeMillis();
		// for (int i = 0; i < 5; i++) {
		// String queryFileName = queryFirst + i;
		// queryRandom.generateToQueryFile(
		// queryRandom.randomQueries(3, 4, 2, 2), queryFileName);
		// }

		ArrayList<Graph> queries = queryRandom.randomQueries(5, 3, 2, 2);

		for (int i = 0; i < 5; i++) {
			String queryFileName = queryFirst + i;
			queryRandom.generateToQueryFile(
					new ArrayList<>(queries.subList(0, i * 2 + 2)),
					queryFileName);
		}

		long endTime = System.currentTimeMillis();
		System.out.println("time:" + (endTime - beginTime));
	}

}
