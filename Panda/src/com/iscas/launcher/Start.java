package com.iscas.launcher;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Iterator;

import com.iscas.controller.ProperitiesController;
import com.iscas.experiments.E1Thread;
import com.iscas.model.Edge;
import com.iscas.model.Graph;
import com.iscas.model.Node;

public class Start {

    public static String dataSetName = "";
    public static String labelFileName = "";
    public static String queryFileName = "";

    public static long beginTime;
    public static long endTime;
    public static BufferedWriter writer;
    public static BufferedWriter timeWriter;

    private static void readProperties() {
        // read property
        ProperitiesController properitiesController = ProperitiesController
                .getProperitiesController();
        properitiesController.loadProperitiesToEnvironment();
        properitiesController.close();
    }


    public static void main(String[] args) {
        Start.readProperties();

        try {
            run(200, "SEN_PANDA");
            // Input the parameters
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public static void bfs(Graph graph1, boolean toFile) throws Exception {
        Node node = graph1.getNodes().get(
                graph1.getNodes().keySet().iterator().next());
        ArrayList<Node> setArrayList = new ArrayList<>();
        setArrayList.add(node);
        ArrayList<String> hasArrayList = new ArrayList<>();
        while (setArrayList.size() != 0) {
            Node pnode = setArrayList.remove(0);
            hasArrayList.add(pnode.getId());
            if (pnode.getEdgeNum() == 0)
                if (toFile) {
                    writer.write(pnode.getId());
                    writer.newLine();
                } else {
                    System.out.println(pnode.getId());
                }
            for (Iterator<String> iterator = pnode.getEdgesArrayList().keySet()
                    .iterator(); iterator.hasNext(); ) {
                String toIdString = iterator.next();
                Edge edge = pnode.getEdgesArrayList().get(toIdString);
                if (toFile) {
                    writer.write(pnode.getId() + "->" + toIdString);
                    writer.newLine();
                    writer.write(pnode.getLabels() + "->"
                            + edge.getToNode().getLabels());
                    writer.newLine();
                } else {
                    System.out.println(pnode.getId() + "->" + toIdString);
                    System.out.println(pnode.getLabels() + "->"
                            + edge.getToNode().getLabels());
                }
                if (!hasArrayList.contains(toIdString)
                        && !setArrayList.contains(edge.getToNode()))
                    setArrayList.add(edge.getToNode());
            }
        }
        if (toFile) {
            writer.flush();
        }
    }

    public static void run(int queryN, String alg) throws InterruptedException {
        E1Thread E1 = new E1Thread(0);
        E1.run(queryN, alg);
    }

}
