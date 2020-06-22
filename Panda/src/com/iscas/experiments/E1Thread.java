package com.iscas.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.iscas.algorithm.GerateMinCostTree;
import com.iscas.algorithm.Isomorphism;
import com.iscas.algorithm.MatchPrune;
import com.iscas.algorithm.MergeAlgorithm;
import com.iscas.controller.ExperimentEnv;
import com.iscas.controller.InputFromFile;
import com.iscas.launcher.Start;
import com.iscas.model.Edge;
import com.iscas.model.Graph;
import com.iscas.model.Node;
import com.iscas.model.OuterEdge;
import com.iscas.model.QueryGraph;
import com.iscas.model.Tree;
import com.iscas.score.ScoreManager;


public class E1Thread {

    private int queryFileIndex;
    private QueryGraph queryGraph;
    public Graph graph;

    public long beginTime;
    public long endTime;


    private BufferedWriter timeWriter;
    private BufferedWriter treeWriter;

    public E1Thread(int queryFileIndex) {
        this.queryFileIndex = queryFileIndex;

    }

    public void run(int k, String alg) {
        // TODO Auto-generated method stub
        try {
            String queryFileName1 = Start.queryFileName + queryFileIndex;
            InputFromFile inputFromFile = new InputFromFile(Start.dataSetName,
                    Start.labelFileName, queryFileName1);
            inputFromFile.readQueries();
            queryGraph = inputFromFile.getQueryGraph();
            File timeFile = new File(ExperimentEnv.outputDirectory
                    + queryFileName1 + "time.txt");
            timeFile.createNewFile();
            timeWriter = new BufferedWriter(new FileWriter(timeFile));

            File treeFile = new File(ExperimentEnv.outputDirectory
                    + queryFileName1 + "tree.txt");
            treeFile.createNewFile();
            treeWriter = new BufferedWriter(new FileWriter(treeFile));

            System.out.println("Running for finding Top " + k + " matches at most!");
            timeWriter.newLine();
            treeWriter.write("k=" + k);
            treeWriter.newLine();
            if (alg.equals("SEN_PANDA")) {
                timeWriter.write("SEN_PANDA");
                timeWriter.newLine();
                treeWriter.write("SEN_PANDA");
                treeWriter.newLine();
                readGraph(inputFromFile);
                isoCombine(k);
                treeWriter.newLine();
                timeWriter.write("Running Time: " + (endTime - beginTime)
                        + " ms");
                timeWriter.newLine();
                timeWriter.flush();
            } else if (alg.equals("SIMPO_PANDA")) {
                timeWriter.write("SIMPO_PANDA");
                timeWriter.newLine();
                treeWriter.write("SIMPO_PANDA");
                treeWriter.newLine();
                readGraph(inputFromFile);
                simImproveBackward(k);
                treeWriter.newLine();
                timeWriter.write("Running Time: " + (endTime - beginTime)
                        + " ms");
                timeWriter.newLine();
                timeWriter.flush();
            } else if (alg.equals("PO_PANDA")) {
                timeWriter.write("PO_PANDA");
                timeWriter.newLine();
                treeWriter.write("PO_PANDA");
                treeWriter.newLine();
                readGraph(inputFromFile);
                isoImprovedBackward(k);
                treeWriter.newLine();
                timeWriter.write("Running Time: " + (endTime - beginTime)
                        + " ms");
                timeWriter.newLine();
                timeWriter.flush();
            }


            treeWriter.flush();
            treeWriter.close();
            timeWriter.flush();
            timeWriter.close();
            System.out.println("Thread for No " + queryFileIndex + " query finished successfully!");
            System.out.println("Pls find matching results of query " + queryFileIndex + " in files: "
                    + queryFileName1 + "time.txt and " + queryFileName1 + "tree.txt！");

        } catch (Exception exception) {
            System.out.println("Thread for No " + queryFileIndex + " query has errors!");
            exception.printStackTrace();
        }

    }

    private void readGraph(InputFromFile inputFromFile) {
        inputFromFile.read();
        inputFromFile.readLabels();
        this.graph = inputFromFile.getGraph();
    }

    private void bfs(Graph graph1) throws Exception {
        Node node = graph1.getNodes().get(
                graph1.getNodes().keySet().iterator().next());
        ArrayList<Node> setArrayList = new ArrayList();
        setArrayList.add(node);
        ArrayList<String> hasArrayList = new ArrayList();
        while (setArrayList.size() != 0) {
            Node pnode = setArrayList.remove(0);
            hasArrayList.add(pnode.getId());
            if (pnode.getEdgeNum() == 0) {
                treeWriter.write(pnode.getId());
                treeWriter.newLine();
            }
            for (Iterator<String> iterator = pnode.getEdgesArrayList().keySet()
                    .iterator(); iterator.hasNext(); ) {
                String toIdString = iterator.next();
                Edge edge = pnode.getEdgesArrayList().get(toIdString);
                treeWriter.write(pnode.getId() + "->" + toIdString);
                treeWriter.newLine();
                treeWriter.write(pnode.getLabels() + "->"
                        + edge.getToNode().getLabels());
                treeWriter.newLine();
                if (!hasArrayList.contains(toIdString)
                        && !setArrayList.contains(edge.getToNode()))
                    setArrayList.add(edge.getToNode());
            }
        }
        treeWriter.flush();
    }


    private void isoCombine(int k) throws Exception {
        beginTime = System.currentTimeMillis();
        ArrayList<ArrayList<HashMap<String, String>>> mappers = new ArrayList();
        Isomorphism isomorphism = new Isomorphism();
        for (int j = 0; j < queryGraph.getGraphs().size(); j++) {
            ArrayList<HashMap<String, String>> matchesArrayList = isomorphism
                    .ullmann(graph, queryGraph.getGraphs().get(j));
            // check results
            if (matchesArrayList == null || matchesArrayList.size() == 0) {
                treeWriter.write("no match for " + j);
                treeWriter.newLine();
                return;
            }
            treeWriter
                    .write("matches for No " + j + " of " + matchesArrayList.size());
            treeWriter.newLine();
            treeWriter.flush();
            mappers.add(matchesArrayList);
        }

        long time = System.currentTimeMillis() - beginTime;
        treeWriter.write("isomorphism computation time: " + time + " ms");
        treeWriter.newLine();
        treeWriter.flush();

        MergeAlgorithm mergeAlgorithm = new MergeAlgorithm();
        mergeAlgorithm.mergeForIso(graph, mappers);

//		printMerged(mergeAlgorithm);

        time = System.currentTimeMillis() - beginTime;
        treeWriter.write("isomorphism graph merging time: " + time + " ms");
        treeWriter.newLine();
        treeWriter.flush();

        GerateMinCostTree gerateMinCostTree = new GerateMinCostTree(graph,
                queryGraph, mergeAlgorithm, k);
        gerateMinCostTree.NaiveCombinationSearching();
        endTime = System.currentTimeMillis();
        printTrees(k, gerateMinCostTree, mergeAlgorithm);

        ScoreManager scoreManager = new ScoreManager(queryGraph,
                gerateMinCostTree.getTreeGraphs(), k, true);
        timeWriter.write("P: " + scoreManager.getPrecision());
        timeWriter.newLine();
        timeWriter.write("MAP: " + scoreManager.getMAP());
        timeWriter.newLine();
        timeWriter.write("NDCG: " + scoreManager.getNDCG());
        timeWriter.newLine();
    }


    private void isoImprovedBackward(int k) throws Exception {
        beginTime = System.currentTimeMillis();
        ArrayList<ArrayList<HashMap<String, String>>> mappers = new ArrayList();
        Isomorphism isomorphism = new Isomorphism();
        for (int j = 0; j < queryGraph.getGraphs().size(); j++) {
            ArrayList<HashMap<String, String>> matchesArrayList = isomorphism
                    .ullmann(graph, queryGraph.getGraphs().get(j));
            // check results
            if (matchesArrayList == null || matchesArrayList.size() == 0) {
                treeWriter.write("no match");
                treeWriter.newLine();
                return;
            }

            mappers.add(matchesArrayList);
        }
        MergeAlgorithm mergeAlgorithm = new MergeAlgorithm();
        mergeAlgorithm.mergeForIso(graph, mappers);
        GerateMinCostTree gerateMinCostTree = new GerateMinCostTree(graph,
                queryGraph, mergeAlgorithm, k);
        gerateMinCostTree.BackWardSearching(false);
        endTime = System.currentTimeMillis();
        printTrees(k, gerateMinCostTree, mergeAlgorithm);

        ScoreManager scoreManager = new ScoreManager(queryGraph,
                gerateMinCostTree.getTreeGraphs(), k, true);
        timeWriter.write("P: " + scoreManager.getPrecision());
        timeWriter.newLine();
        timeWriter.write("MAP: " + scoreManager.getMAP());
        timeWriter.newLine();
        timeWriter.write("NDCG: " + scoreManager.getNDCG());
        timeWriter.newLine();
    }


    private void simImproveBackward(int k) throws Exception {
        beginTime = System.currentTimeMillis();
        ArrayList<HashMap<String, ArrayList<String>>> simsArrayList = new ArrayList();
        MatchPrune matchPrune = new MatchPrune();
        for (int i = 0; i < queryGraph.getQueryNum(); i++) {
            HashMap<String, ArrayList<String>> sim = matchPrune
                    .SimulationPrune(graph, queryGraph.getGraphs().get(i));

            if (sim == null || sim.size() == 0) {
                treeWriter.write("no match");
                treeWriter.newLine();
                treeWriter.flush();
                return;
            }
            simsArrayList.add(sim);
        }
        long time = System.currentTimeMillis() - beginTime;
        treeWriter.write("simST sim time:" + time);
        treeWriter.newLine();
        treeWriter.flush();
        MergeAlgorithm mergeAlgorithm = new MergeAlgorithm();
        mergeAlgorithm.mergeForSim(graph, simsArrayList);

        float mergetime = System.currentTimeMillis() - beginTime;
        treeWriter.write("simST merge time: " + mergetime);
        treeWriter.newLine();
        treeWriter.flush();

        GerateMinCostTree gerateMinCostTree = new GerateMinCostTree(graph,
                queryGraph, mergeAlgorithm, k);
        gerateMinCostTree.BackWardSearching(true);
        endTime = System.currentTimeMillis();
        printTrees(k, gerateMinCostTree, mergeAlgorithm);

        ScoreManager scoreManager = new ScoreManager(queryGraph,
                gerateMinCostTree.getTreeGraphs(), k, true);
        timeWriter.write("P: " + scoreManager.getPrecision());
        timeWriter.newLine();
        timeWriter.write("MAP: " + scoreManager.getMAP());
        timeWriter.newLine();
        timeWriter.write("NDCG: " + scoreManager.getNDCG());
        timeWriter.newLine();
    }


    private void printTrees(Integer k, GerateMinCostTree gerateMinCostTree,
                            MergeAlgorithm mergeAlgorithm) throws Exception {
        int pk = 0;
        float totalCost = 0;
        treeWriter.write("find " + gerateMinCostTree.getPK() + " trees");
        treeWriter.newLine();

        for (Tree tree : gerateMinCostTree.getTreeGraphs()) {
            int count = tree.getMatchNum();
            if (pk < k) {
                int c = Math.min(k - pk, count);
                pk += c;
                totalCost += tree.getCost() * c;
            }
            treeWriter.write("Tree Cost: " + tree.getCost());
            treeWriter.newLine();
            treeWriter.write("Tree MatchNum: " + count);
            treeWriter.newLine();
            bfs(tree.getGraph());
        }
        treeWriter.newLine();
        treeWriter.write("total Cost: " + totalCost);
        treeWriter.newLine();
        treeWriter.flush();

        treeWriter.write("Merged Nodes information:");
        treeWriter.newLine();
        for (Iterator<String> mergeIterator = mergeAlgorithm
                .getMergeHashtable().keySet().iterator(); mergeIterator
                     .hasNext(); ) {
            String mergeNodeIdString = mergeIterator.next();
            ArrayList<Integer> midsArrayList = graph.getNodes()
                    .get(mergeNodeIdString).getMids();
            treeWriter.write("merged node id, mids:");
            treeWriter.write(midsArrayList.toString());
            treeWriter.newLine();
            Graph mergedGraph = mergeAlgorithm.getMergeHashtable().get(
                    mergeNodeIdString);
            treeWriter.write("matched graph for merged Node " + mergeNodeIdString);
            ArrayList<OuterEdge> outerEdges = mergeAlgorithm.getOuterEdges().get(
                    mergeNodeIdString);
            treeWriter.newLine();
//			for (OuterEdge e:outerEdges)
//			treeWriter.write(e.getFromId()+' '+e.getToId()+'\t');
            treeWriter.flush();
            bfs(mergedGraph);
        }
    }

    private void printMerged(MergeAlgorithm mergeAlgorithm) throws Exception {
        treeWriter.write("Merged Nodes information:");
        treeWriter.newLine();
        for (Iterator<String> mergeIterator = mergeAlgorithm
                .getMergeHashtable().keySet().iterator(); mergeIterator
                     .hasNext(); ) {
            String mergeNodeIdString = mergeIterator.next();
            ArrayList<Integer> midsArrayList = graph.getNodes()
                    .get(mergeNodeIdString).getMids();
            treeWriter.write("merged node id, mids:");
            treeWriter.write(midsArrayList.toString());
            treeWriter.newLine();
            Graph mergedGraph = mergeAlgorithm.getMergeHashtable().get(
                    mergeNodeIdString);
            treeWriter.write("matched graph for merged node " + mergeNodeIdString);
            ArrayList<OuterEdge> outerEdges = mergeAlgorithm.getOuterEdges().get(
                    mergeNodeIdString);
            treeWriter.newLine();
//		for (OuterEdge e:outerEdges)
//		treeWriter.write(e.getFromId()+' '+e.getToId()+'\t');
            treeWriter.flush();
            bfs(mergedGraph);
        }
    }

}
