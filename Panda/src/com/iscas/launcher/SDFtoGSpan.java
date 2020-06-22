package com.iscas.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.iscas.controller.ExperimentEnv;
import com.iscas.controller.ProperitiesController;

public class SDFtoGSpan {

	private static final String[] dbLabels = { "C", "O", "Cu", "N", "S", "P",
			"Cl", "Zn", "B", "Br", "Co", "Mn", "As", "Al", "Ni", "Se", "Si",
			"V", "Sn", "I", "F", "Li", "Sb", "Fe", "Pd", "Hg", "Bi", "Na",
			"Ca", "Ti", "Ho", "Ge", "Pt", "Ru", "Rh", "Cr", "Ga", "K", "Ag",
			"Au", "Tb", "Ir", "Te", "Mg", "Pb", "W", "Cs", "Mo", "Re", "Cd",
			"Os", "Pr", "Nd", "Sm", "Gd", "Yb", "Er", "U", "Tl", "Ac", "H",
			"Sr", "Ba", "Nb", "Rb", "Hf", "In", "Ce", "Zr", "Eu", "Tm", "Dy",
			"Y", "La", "Lu", "Ta", "Be", "Th", "Sc" };

	private BufferedWriter writer;
	private BufferedReader reader;
	private int threshold;
	private int node_total_num;
	private int edge_total_num;

	public SDFtoGSpan(int max_graph_num) {
		this.threshold = max_graph_num;
	}

	public void Read() {
		try {
			ProperitiesController properitiesController = ProperitiesController
					.getProperitiesController();
			properitiesController.loadProperitiesToEnvironment();
			properitiesController.close();

			File outFile = new File(ExperimentEnv.outputDirectory
					+ "gSpanInput.txt");
			outFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(outFile));

			// versionBB.sdf
			File file = new File(ExperimentEnv.inputDirectory + "versionBB.sdf");
			reader = new BufferedReader(new FileReader(file));

			String line_s = null;
			int graph_N = 0;
			while ((line_s = reader.readLine()) != null) {
				// 去掉注释
				if (line_s.startsWith("  EMOLECUL")) {
					System.out.println(line_s);
					// 空行
					line_s = reader.readLine();
					line_s = reader.readLine();
					String[] firstlines = line_s.split(" ");
					// 读取节点数与边数
					int edgeN = -1;
					int nodeN = -1;
					for (int i = 0; i < firstlines.length; i++) {
						if (!firstlines[i].equals("")
								&& !firstlines[i].equals(" ")) {
							if (firstlines[i].length() >= 5) {
								nodeN = Integer.parseInt(firstlines[i]
										.substring(0,
												firstlines[i].length() - 3));
								edgeN = Integer.parseInt(firstlines[i]
										.substring(firstlines[i].length() - 3,
												firstlines[i].length()));
								System.out.println(nodeN + "," + edgeN);
								break;
							}
							for (int j = i + 1; j < firstlines.length; j++) {
								if (!firstlines[j].equals("")
										&& !firstlines[j].equals(" ")) {
									nodeN = Integer.parseInt(firstlines[i]);
									edgeN = Integer.parseInt(firstlines[j]);
									break;
								}
							}
							break;
						}
					}
					if (edgeN > 0 && nodeN > 0) {
						if (read_graph(nodeN, edgeN, graph_N)) {
							graph_N++;
							node_total_num += nodeN;
							edge_total_num += edgeN;
						}
						System.out.println(graph_N);
					}
				}
				if (graph_N >= threshold) {
					break;
				}
			}

			System.out.println("Finished");
			System.out.println("Graph Count:" + graph_N);
			System.out.println("Avg_nodes:" + node_total_num / graph_N);
			System.out.println("Avg_edges:" + edge_total_num / graph_N);
			writer.close();
			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean read_graph(int nodeN, int edgeN, int index)
			throws IOException {
		if (edgeN < nodeN - 1) {
			System.out.println("node:" + nodeN);
			System.out.println("edge:" + edgeN);
			System.out.println("false");
			return false;
		}

		String line_s = "";
		StringBuffer outBuffer = new StringBuffer();
		outBuffer.append("t # " + (index) + " " + nodeN + "\n");
		int count = 0;
		while ((line_s = reader.readLine()) != null) {
			// 获取label
			String label = null;
			label = line_s.substring(31, 33).trim();
			// write node
			int labelIndex = -1;
			for (int i = 0; i < dbLabels.length; i++) {
				if (dbLabels[i].equals(label)) {
					labelIndex = i;
					break;
				}
			}
			if (label.startsWith("*")) {
				return false;
			}
			if (labelIndex == -1) {
				System.out.println("No match label:");
				System.out.println(line_s);
				System.out.println(label);
				System.exit(0);
			}
			outBuffer.append("v " + count + " " + labelIndex + "\n");

			count++;
			if (count == nodeN) {
				break;
			}
		}

		// edge module
		count = 0;
		while (edgeN > 0 && (line_s = reader.readLine()) != null) {
			String[] edgeStrings = line_s.split(" ");
			for (int i = 0; i < edgeStrings.length; i++) {
				if (!edgeStrings[i].equals("") && !edgeStrings[i].equals(" ")) {
					if (edgeStrings[i].length() >= 4) {
						outBuffer.append("e "
								+ (Integer.parseInt(edgeStrings[i].substring(0,
										edgeStrings[i].length() - 3)) - 1)
								+ " "
								+ (Integer.parseInt(edgeStrings[i].substring(
										edgeStrings[i].length() - 3,
										edgeStrings[i].length())) - 1) + " 0"
								+ "\n");
						// writer.write("e "
						// + (Integer.parseInt(edgeStrings[i].substring(0,
						// edgeStrings[i].length() - 3)) - 1)
						// + " "
						// + (Integer.parseInt(edgeStrings[i].substring(
						// edgeStrings[i].length() - 3,
						// edgeStrings[i].length())) - 1) + " 0");
						// writer.newLine();
						break;
					}

					for (int j = i + 1; j < edgeStrings.length; j++) {
						if (!edgeStrings[j].equals("")
								&& !edgeStrings[j].equals(" ")) {
							outBuffer.append("e "
									+ (Integer.parseInt(edgeStrings[i]) - 1)
									+ " "
									+ (Integer.parseInt(edgeStrings[j]) - 1)
									+ " 0" + "\n");
							break;
						}
					}
					break;
				}
			}
			count++;
			if (count == edgeN) {
				break;
			}
		}
		outBuffer.append("\n");
		writer.write(outBuffer.toString());
		return true;
	}

	public void changeGSpanToGraphGrepSX(String gspanFileName) {
		try {
			ProperitiesController properitiesController = ProperitiesController
					.getProperitiesController();
			properitiesController.loadProperitiesToEnvironment();
			properitiesController.close();

			File outFile = new File(ExperimentEnv.outputDirectory
					+ "GrepSXDataSets.txt");
			outFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(outFile));

			File file = new File(ExperimentEnv.inputDirectory + gspanFileName);
			reader = new BufferedReader(new FileReader(file));

			String line_s = null;
			int graph_N = 0;
			int max_nodes = 0;
			int max_edges = 0;
			int average_nodes = 0;
			int average_edges = 0;
			while ((line_s = reader.readLine()) != null) {
				if (line_s.startsWith("t")) {
					// new graph
					graph_N++;
					String[] content = line_s.split(" ");
					int index = Integer.parseInt(content[2]);
					int node_n = Integer.parseInt(content[3]);
					// System.out.println(node_n);
					if (node_n > max_nodes) {
						max_nodes = node_n;
					}
					average_nodes += node_n;
					writer.write("#" + index);
					writer.newLine();
					writer.write("" + node_n);
					writer.newLine();
					// read nodes
					for (int i = 0; i < node_n; i++) {
						line_s = reader.readLine();
						content = line_s.split(" ");
						String label = content[2];
						// System.out.println("label " + label);
						writer.write(label);
						writer.newLine();
					}
					// read edges
					ArrayList<String> froms = new ArrayList<>();
					ArrayList<String> tos = new ArrayList<>();
					int edges = 0;
					while (!(line_s = reader.readLine()).equals("")) {
						// System.out.println(line_s);
						if (line_s.startsWith("e")) {
							edges++;
							content = line_s.split(" ");
							froms.add(content[1]);
							tos.add(content[2]);
							// System.out
							// .println(content[1] + " to " + content[2]);
						}
					}
					// calculate max_edges
					writer.write("" + edges);
					writer.newLine();
					for (int i = 0; i < edges; i++) {
						writer.write(froms.get(i) + " " + tos.get(i));
						writer.newLine();
					}
					if (edges > max_edges) {
						max_edges = edges;
					}
					average_edges += edges;
				}
			}
			// print max,averge
			System.out.println("total graphs: " + graph_N);
			System.out.println("max_nodes: " + max_nodes);
			System.out.println("max_edges: " + max_edges);
			System.out.println("average_nodes: " + average_nodes / graph_N);
			System.out.println("average_edges: " + average_edges / graph_N);
			writer.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// SDFtoGSpan sdFtoGSpan = new SDFtoGSpan(1300000);
		// sdFtoGSpan.Read();
		SDFtoGSpan sdFtoGSpan = new SDFtoGSpan(0);
		sdFtoGSpan.changeGSpanToGraphGrepSX("MD960.txt");
	}

}
