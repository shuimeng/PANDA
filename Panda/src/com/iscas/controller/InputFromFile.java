package com.iscas.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.iscas.model.Graph;
import com.iscas.model.Node;
import com.iscas.model.QueryGraph;

public class InputFromFile {

	// topo 文件
	private String fileNameString;
	// topo 文件路径
	private String filePath;

	// 节点label 文件
	private String labelFileNameString;

	// queries 文件名
	private String queryFileNameString;

	// 读入的邻接链表
	private Graph graph;
	private QueryGraph queryGraph;

	public InputFromFile(String fileNameString, String labelFileString,
			String queryFileNameString) {
		this.fileNameString = fileNameString;
		this.labelFileNameString = labelFileString;
		this.queryFileNameString = queryFileNameString;
		this.filePath = ExperimentEnv.inputDirectory;
	}

	public InputFromFile(String fileNameString) {
		this.fileNameString = fileNameString;
		this.filePath = ExperimentEnv.inputDirectory;
	}

	/*
	 * 从文件中读取拓扑结构
	 */
	public void read() {
		File file = new File(filePath + this.fileNameString + ".txt");
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String line_s = null;
			while ((line_s = reader.readLine()) != null) {
				// 去掉注释
				if (!line_s.startsWith("#")) {
					break;
				}
			}
			// 节点数与边数
			String[] valueStrings = line_s.split("\t");
			int node_n = Integer.parseInt(valueStrings[0]);
			int edge_n = Integer.parseInt(valueStrings[1]);
			graph = new Graph(node_n, edge_n);

			// 读入节点对
			while ((line_s = reader.readLine()) != null) {
				// 分析对
				valueStrings = line_s.split("\t");
				String toid = valueStrings[1];
				if (valueStrings.length == 2) {
					// 无权重,权重自动赋值为1,双向边
					graph.addEdge(valueStrings[0], toid, 1, false);

				} else if (valueStrings.length == 3) {
					// 有权重信息,双向边
					graph.addEdge(valueStrings[0], toid,
							Float.parseFloat(valueStrings[2]), false);
				}
			}
			// 验证图信息是否全
			// if (!graph.validate()) {
			// System.out.println("Information Error in Topology file!");
			// }
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Info read error in Topology file!");
			e.printStackTrace();
		}
	}

	public void readSubgraph(int vNum) {
		File file = new File(filePath + this.fileNameString + ".txt");
		BufferedReader reader = null;

		int selectedN = 0;
		int targetVN = vNum / 4;
		int cycle = 1;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line_s = null;
			while ((line_s = reader.readLine()) != null) {
				// 去掉注释
				if (!line_s.startsWith("#")) {
					break;
				}
			}
			// 节点数与边数
			String[] valueStrings;
			graph = new Graph();

			// 读入节点对
			while ((line_s = reader.readLine()) != null
					&& graph.getNodes().size() < vNum) {
				// 分析对
				valueStrings = line_s.split("\t");
				String toid = valueStrings[1];
				if (selectedN == 0
						|| graph.getNodes().containsKey(valueStrings[0])
						|| graph.getNodes().containsKey(valueStrings[1])) {
					selectedN++;
					graph.addEdge(valueStrings[0], toid, 1, false);
					// 判断终止条件
					if (graph.getNodes().size() >= targetVN) {
						// 输出
						// 存graph
						System.out.println("Save Grapn, Nodes:"
								+ graph.getNodes().size() + ";" + "Edges:"
								+ selectedN);
						File outFile = new File(ExperimentEnv.outputDirectory
								+ fileNameString + cycle);
						outFile.createNewFile();
						BufferedWriter graphWriter = new BufferedWriter(
								new FileWriter(outFile));

						// 记录节点与边总数
						graphWriter.write(graph.getNodes().size() + "\t"
								+ selectedN);
						graphWriter.newLine();

						for (Iterator<String> nodeIterator = graph.getNodes()
								.keySet().iterator(); nodeIterator.hasNext();) {
							String nodeId = nodeIterator.next();
							Node node = graph.getNodes().get(nodeId);
							for (Iterator<String> edgeIterator = node
									.getEdgesArrayList().keySet().iterator(); edgeIterator
									.hasNext();) {
								String toId = edgeIterator.next();
								graphWriter.write(nodeId + "\t" + toId);
								graphWriter.newLine();
							}
						}
						graphWriter.flush();
						graphWriter.close();
						targetVN += vNum / 4;
						cycle++;
					}
				}
			}
			System.out.println("END!");
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Info read error in Topology file!");
			e.printStackTrace();
		}
	}

	/*
	 * 随机产生每个节点的label
	 */
	public void randomLabels(int label_num) {
		// 遍历图中每个节点，随机加上label
		for (Iterator<String> itr = graph.getNodes().keySet().iterator(); itr
				.hasNext();) {
			String key = (String) itr.next();
			Node node = (Node) graph.getNodes().get(key);
			Random random = new Random();
			int s = random.nextInt(label_num) % (label_num) + 1;
			node.addLabel("" + s);
		}
	}

	/*
	 * 读betweeness文件，每行为： vertexId \t betweeness
	 */
	// public void readbetweeness(){
	// if(this.betweenessFileNameString!=null&&!this.betweenessFileNameString.equals("")){
	// File file = new File(filePath + this.betweenessFileNameString + ".txt");
	// BufferedReader reader = null;
	//
	// try {
	// reader = new BufferedReader(new FileReader(file));
	// String line_s = null;
	// while ((line_s = reader.readLine()) != null) {
	// // 去掉注释
	// if (!line_s.startsWith("#")) {
	// break;
	// }
	// }
	// //读入并设定节点值
	// while (line_s != null) {
	// String[] valueStrings = line_s.split("\t");
	// //找不到点，抛异常
	// graph.getNodes().get(valueStrings[0])
	// .setBetweeness(Float.parseFloat(valueStrings[1]));
	// // System.out.println(valueStrings[0] + "," + valueStrings[1]);
	// line_s = reader.readLine();
	// }
	//
	// reader.close();
	// }catch(Exception e){
	// e.printStackTrace();
	// }
	//
	// }else{
	// System.err.println("betweeness file null");
	// }
	// }

	/*
	 * 读取节点的label信息文件 第一行节点的个数 一行一个节点id \t label \t betweeness 其中包括betweenness
	 */
	public void readLabels() {
		File file = new File(filePath + this.labelFileNameString + ".txt");
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String line_s = null;
			while ((line_s = reader.readLine()) != null) {
				// 去掉注释
				if (!line_s.startsWith("#")) {
					break;
				}
			}
			// 从头一直读到尾
			while (line_s != null) {
				String[] valueStrings = line_s.split("\t");
				if (valueStrings.length == 3) {
					// 判断有无betweeness
					graph.getNodes().get(valueStrings[0])
							.setBetweeness(Float.parseFloat(valueStrings[2]));
				}
				graph.addLabelForNode(valueStrings[0], valueStrings[1]);
				line_s = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Info read error in labels file!");
			e.printStackTrace();
		}

	}

	public void readQueries() {
		File file = new File(filePath + this.queryFileNameString + ".txt");
		BufferedReader reader = null;
		this.queryGraph = new QueryGraph();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line_s = null;
			while ((line_s = reader.readLine()) != null) {
				// 去掉注释
				if (!line_s.startsWith("#")) {
					break;
				}
			}
			// 读取query子图个数
			int query_num = Integer.parseInt(line_s);
			for (int i = 0; i < query_num; i++) {
				Graph qgraph = new Graph();
				// 读取label子块
				while (!(line_s = reader.readLine()).equals("#")) {
					String[] valueStrings = line_s.split("\t");
					Node node = new Node(valueStrings[0]);
					node.addLabel(valueStrings[1]);
					qgraph.addNode(node);
				}
				// 读取topology结构
				int edgeNum = 0;
				while (!(line_s = reader.readLine()).equals("#")) {
					String[] valueStrings = line_s.split("\t");
					qgraph.addEdge(valueStrings[0], valueStrings[1], 0, false);
					edgeNum++;
				}
				qgraph.setNodeNum();
				qgraph.setEdgeNum(edgeNum);
				this.queryGraph.addQuery(qgraph);
			}
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Info read error in Query file!");
			e.printStackTrace();
		}
	}

	public HashMap<String, HashMap<String, Double>> readNeMaIndex(
			String fileName) {
		try {
			HashMap<String, HashMap<String, Double>> neighborHashMap = new HashMap<>();
			File file = new File(ExperimentEnv.inputDirectory + fileName
					+ ".txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line_s = null;
			while ((line_s = reader.readLine()) != null) {
				String[] indexInfo = line_s.split("\t");
				String nodeId = indexInfo[0];
				HashMap<String, Double> indexHashMap = new HashMap<>();
				for (int i = 1; i < indexInfo.length; i++) {
					String[] neighborString = indexInfo[i].split(",");
					indexHashMap.put(neighborString[0],
							Double.parseDouble(neighborString[1]));
				}
				neighborHashMap.put(nodeId, indexHashMap);
			}
			reader.close();
			return neighborHashMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setFileNameString(String fileNameString) {
		this.fileNameString = fileNameString;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public QueryGraph getQueryGraph() {
		return queryGraph;
	}

}
