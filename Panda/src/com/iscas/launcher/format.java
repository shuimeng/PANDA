package com.iscas.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

import com.iscas.controller.ExperimentEnv;
import com.iscas.controller.ProperitiesController;

public class format {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ProperitiesController properitiesController = ProperitiesController
					.getProperitiesController();
			properitiesController.loadProperitiesToEnvironment();
			properitiesController.close();

			File outFile = new File(ExperimentEnv.outputDirectory + "chart.txt");
			outFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

			File file = new File(ExperimentEnv.inputDirectory + "chart.txt");

			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line_s = null;
			while ((line_s = reader.readLine()) != null) {
				// 去掉注释
				if (!line_s.startsWith("#")) {
					break;
				}
			}

			while (line_s != null) {
				String[] nodesStrings = line_s.split(",");
				if (nodesStrings.length > 0) {
					for (int i = 0; i < nodesStrings.length - 1; i++) {
						writer.write(nodesStrings[i] + "\t");
					}
					writer.write(nodesStrings[nodesStrings.length - 1]);
					writer.newLine();
				}
				line_s = reader.readLine();
			}

			// 创建label
			// int index = 0;
			// int num_bigclus = 0;
			// int num_class = 0;
			// int vnumm = 0;
			// while (line_s != null) {
			// String[] nodesStrings = line_s.split("\t");
			// if (nodesStrings.length > 500) {
			// num_bigclus++;
			// } else {
			// for (int i = 0; i < nodesStrings.length; i++) {
			// writer.write(nodesStrings[i] + "\t" + index);
			// writer.newLine();
			// }
			// if(nodesStrings.length>vnumm){
			// vnumm = nodesStrings.length;
			// }
			// index++;
			// }
			// num_class++;
			// line_s = reader.readLine();
			// }
			//
			// System.out.println("Total_class:" + num_class);
			// System.out.println("hasClass:" + index);
			// System.out.println("bigclass:" + num_bigclus);
			// System.out.println("hasbigClass:" + vnumm);
			// int edges=0;
			// // 从头一直读到尾
			// writer.write("65608366"+"\t"+"1806067135");
			// writer.newLine();
			// writer.flush();
			// while (line_s != null) {
			// String []nodesStrings = line_s.split("\t");
			// // Random random = new Random();
			// // if(random.nextInt()>=7){
			// // edges++;
			// // writer.write(line_s);
			// // writer.newLine();
			// // }else{
			// // System.out.println(line_s);
			// // }
			// if(!nodesStrings[0].equals(nodesStrings[1])){
			// edges++;
			// writer.write(line_s);
			// writer.newLine();
			// }else{
			// System.out.println(line_s);
			// }
			// line_s = reader.readLine();
			// }
			// System.out.println("edges:" + edges);
			writer.flush();
			writer.close();
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
