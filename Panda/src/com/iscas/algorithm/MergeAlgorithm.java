package com.iscas.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import com.iscas.model.Edge;
import com.iscas.model.Graph;
import com.iscas.model.Node;
import com.iscas.model.OuterEdge;

/**
 * 本类实现merge算法，把一个子图用一个merged节点代替，修改原始G图， 并且把subgraph与merged node对应关系存于本类中
 * 分为两种，foriso，一个节点对应一个节点 forsimulation，一个节点对应多个节点 输入是不同的，但是方法类似
 */

public class MergeAlgorithm {

    // 对应关系, mergedNode -> mergedGraph
    private HashMap<String, Graph> mergeHashtable;

    // 对应关系， mergedNode ->outerEdges(from_Id, To_Id)
    private HashMap<String, ArrayList<OuterEdge>> outerEdges;

    // 记录是sim还是iso
    private boolean isISO = true;

    public MergeAlgorithm() {
        this.mergeHashtable = new HashMap();
        this.outerEdges = new HashMap<String, ArrayList<OuterEdge>>();
    }

    // 对于多个Pi的matching
    public void mergeForIso(Graph orgraph,
                            ArrayList<ArrayList<HashMap<String, String>>> mappers) {
        isISO = true;
        // 判断match的非空在每个iso完，由主程序check
        for (int i = 0; i < mappers.size(); i++) {
            if (mappers.get(i) != null && mappers.get(i).size() != 0) {
                // 一个hashtable，变成一个merged node
                for (int j = 0; j < mappers.get(i).size(); j++) {
                    HashMap<String, String> mapHashtable = mappers.get(i)
                            .get(j);
                    // initial merged node's info
                    Node mergedNode = new Node("mn_" + i + "," + j);
                    mergedNode.addMids(i);
                    mergedNode.setMerged(true);
                    // 需要增加到类的hashtable中
                    Graph mergedGraph = new Graph();
                    ArrayList<OuterEdge> outerEdges = new ArrayList();
                    // 合并
                    for (Iterator<String> iterator = mapHashtable.keySet()
                            .iterator(); iterator.hasNext(); ) {
                        // 遍历原图节点
                        String vIdString = mapHashtable.get(iterator.next());
                        Node node = orgraph.getNodes().get(vIdString);
                        if (node != null) {
                            // 存在这个节点
                            mergedGraph.addNode(vIdString, node.getLabels());

                            // 设置hub
                            if (node.isHub()) {
                                mergedGraph.getNodes().get(vIdString)
                                        .setHub(true);
                                mergedNode.setHub(true);
                            }

                            // 遍历每一条边
                            ArrayList<String> deleteArrayList = new ArrayList<String>();

                            for (Iterator<String> nedgeIterator = node
                                    .getEdgesArrayList().keySet().iterator(); nedgeIterator
                                         .hasNext(); ) {
                                String toIdString = nedgeIterator.next();
                                Edge edge = node.getEdgesArrayList().get(
                                        toIdString);
                                Node toNode = edge.getToNode();

                                if (mapHashtable.values().contains(toIdString)
                                        || toIdString
                                        .equals(mergedNode.getId())) {
                                    // 说明该边为内边,在mergeTable中增加该边,因为内边所以双向,下次处理tonode时不再增加了
                                    if (toIdString.equals(mergedNode.getId())) {
                                        // 在outedges中，找反向vId
                                        OuterEdge deEdge = null;
                                        for (OuterEdge outerEdge : outerEdges) {
                                            if (outerEdge.getToId().equals(
                                                    vIdString)) {
                                                mergedGraph
                                                        .addEdge(
                                                                vIdString,
                                                                outerEdge
                                                                        .getFromId(),
                                                                edge.getWeight(),
                                                                false);
                                                deEdge = outerEdge;
                                                break;
                                            }
                                        }
                                        // 删除outedges
                                        if (deEdge != null) {
                                            outerEdges.remove(deEdge);
                                        }
                                        // g图中删除多余边
                                        if (toNode.getEdgesArrayList()
                                                .containsKey(vIdString)) {
                                            if (toNode.equals(node)) {
                                                deleteArrayList.add(vIdString);
                                            } else {
                                                toNode.getEdgesArrayList()
                                                        .remove(vIdString);
                                            }

                                        }

                                    } else {
                                        // 一般地增加内部节点

                                        mergedGraph.addEdge(vIdString,
                                                toIdString, edge.getWeight(),
                                                false);
                                        mergedGraph.getNodes().get(toIdString)
                                                .addLabels(toNode.getLabels());
                                        // 设置hub

                                        if (toNode.isHub()) {
                                            mergedGraph.getNodes()
                                                    .get(toIdString)
                                                    .setHub(true);
                                            mergedNode.setHub(true);
                                        }
                                        // 删除反向边
                                        if (toNode.getEdgesArrayList()
                                                .containsKey(vIdString)) {
                                            if (toNode.equals(node)) {
                                                deleteArrayList.add(vIdString);
                                            } else {
                                                toNode.getEdgesArrayList()
                                                        .remove(vIdString);
                                            }
                                        }

                                        orgraph.setEdgeNum(orgraph.getEdgeNum() - 1);
                                    }

                                } else {
                                    // 外边
                                    // 记录该外边
                                    // 如果toIdString是一个mergedNode，更改为内部点
                                    if (toNode.isMerged()) {
                                        ArrayList<OuterEdge> toOuterEdges = this.outerEdges
                                                .get(toIdString);
                                        String realToNodeId = null;
                                        for (OuterEdge outerEdge1 : toOuterEdges) {
                                            if (outerEdge1.getToId().equals(
                                                    vIdString)) {
                                                realToNodeId = outerEdge1
                                                        .getFromId();
                                                break;
                                            }
                                        }
                                        outerEdges
                                                .add(new OuterEdge(vIdString,
                                                        realToNodeId, edge
                                                        .getWeight()));

                                    } else {
                                        outerEdges.add(new OuterEdge(vIdString,
                                                toIdString, edge.getWeight()));

                                    }
                                    // 更改orgraph
                                    mergedNode.addEdge(new Edge(toNode, edge
                                            .getWeight()));
                                    // 更改反向边
                                    if (toNode.getEdgesArrayList().containsKey(
                                            vIdString)) {

                                        toNode.addEdge(new Edge(mergedNode,
                                                toNode.getEdgesArrayList()
                                                        .get(vIdString)
                                                        .getWeight()));
                                        // toNode.deleteEdge(vIdString);
                                        if (toNode.equals(node)) {
                                            deleteArrayList.add(vIdString);
                                        } else {
                                            toNode.getEdgesArrayList().remove(
                                                    vIdString);
                                        }

                                    }
                                }
                            }
                            for (int k = 0; k < deleteArrayList.size(); k++) {
                                node.getEdgesArrayList().remove(
                                        deleteArrayList.get(k));
                            }

                            // 删除原起始节点node在orG中
                            orgraph.getNodes().remove(vIdString);
                        } else {
                            // 已经被合并过了
                            // 查vIdString在哪个已经merged图当中
                            for (Iterator<String> mergedIterator = mergeHashtable
                                    .keySet().iterator(); mergedIterator
                                         .hasNext(); ) {
                                String hasMergedNodeId = mergedIterator.next();
                                Graph hasMergedGraph = mergeHashtable
                                        .get(hasMergedNodeId);
                                if (hasMergedGraph.getNodes().containsKey(
                                        vIdString)) {
                                    Node hasMergedNode = orgraph.getNodes()
                                            .get(hasMergedNodeId);
                                    // mergedNode 所有的mid，添加mid
                                    for (int k = 0; k < mergedNode.getMids().size(); k++) {
                                        int midm = mergedNode.getMids().get(k);
                                        if (!hasMergedNode.getMids().contains(midm)) {
                                            hasMergedNode.addMids(midm);
                                        }
                                    }

                                    // 更新hub
                                    if (mergedNode.isHub()
                                            && !hasMergedNode.isHub()) {
                                        hasMergedNode.setHub(true);
                                    }

                                    // 删除hasmergednode到mergednode上的边
                                    hasMergedNode.getEdgesArrayList().remove(
                                            mergedNode.getId());

                                    // copy mergednode边到hasmergednode
                                    for (Iterator<String> mIterator = mergedNode
                                            .getEdgesArrayList().keySet()
                                            .iterator(); mIterator.hasNext(); ) {
                                        String key = mIterator.next();
                                        Edge edge = mergedNode
                                                .getEdgesArrayList().get(key);
                                        if (!key.equals(hasMergedNodeId)) {
                                            hasMergedNode.addEdge(new Edge(edge
                                                    .getToNode(), edge
                                                    .getWeight()));
                                            // 更改反向边，从指向mergedNode改为指向hasMergedNode
                                            edge.getToNode().deleteEdge(
                                                    mergedNode.getId());
                                            edge.getToNode().addEdge(
                                                    new Edge(hasMergedNode,
                                                            edge.getWeight()));
                                        } else {
                                            hasMergedNode.deleteEdge(mergedNode
                                                    .getId());
                                        }
                                    }

                                    // 合并外边与子图
                                    ArrayList<OuterEdge> oldOuterEdges = this.outerEdges
                                            .get(hasMergedNodeId);
                                    ArrayList<OuterEdge> deleteOuterEdges = new ArrayList();
                                    for (OuterEdge outerEdge : oldOuterEdges) {
                                        if (outerEdge.getFromId().equals(
                                                vIdString)) {

                                            if (mergedGraph
                                                    .getNodes()
                                                    .keySet()
                                                    .contains(
                                                            outerEdge.getToId())) {
                                                // 删除
                                                deleteOuterEdges.add(outerEdge);
                                                // 增加内部边
                                                hasMergedGraph
                                                        .getNodes()
                                                        .get(vIdString)
                                                        .addEdge(
                                                                new Edge(
                                                                        mergedGraph
                                                                                .getNodes()
                                                                                .get(outerEdge
                                                                                        .getToId()),
                                                                        outerEdge
                                                                                .getWeight()));
                                                // 增加反向边
                                                mergedGraph
                                                        .getNodes()
                                                        .get(outerEdge
                                                                .getToId())
                                                        .addEdge(
                                                                new Edge(
                                                                        hasMergedGraph
                                                                                .getNodes()
                                                                                .get(vIdString),
                                                                        outerEdge
                                                                                .getWeight()));
                                            }

                                        }
                                    }

                                    // 删除外部转内部边
                                    for (OuterEdge outerEdge2 : deleteOuterEdges) {
                                        oldOuterEdges.remove(outerEdge2);
                                    }

                                    // copy outeredge to oldouteredge
                                    for (int e = 0; e < outerEdges.size(); e++) {
                                        OuterEdge outerEdge = outerEdges.get(e);
                                        // 判断外边节点是否在hasmergedgraph内部
                                        Node inhasMergedNode = hasMergedGraph
                                                .getNodes().get(
                                                        outerEdge.getToId());
                                        if (inhasMergedNode != null) {
                                            // 把外部边改成内部边
                                            // 增加双向内部边

                                            Node inMergedNode = mergedGraph
                                                    .getNodes()
                                                    .get(outerEdge.getFromId());
                                            inhasMergedNode.addEdge(new Edge(
                                                    inMergedNode, outerEdge
                                                    .getWeight()));
                                            inMergedNode.addEdge(new Edge(
                                                    inhasMergedNode, outerEdge
                                                    .getWeight()));

                                            // 删除外部边oldouteredge
                                            for (int in = 0; in < oldOuterEdges
                                                    .size(); in++) {
                                                if (oldOuterEdges
                                                        .get(in)
                                                        .getToId()
                                                        .equals(inMergedNode
                                                                .getId())) {
                                                    oldOuterEdges.remove(in);
                                                    break;
                                                }
                                            }
                                        } else {
                                            // copy
                                            if (!oldOuterEdges
                                                    .contains(outerEdge)) {
                                                oldOuterEdges.add(outerEdge);
                                            }
                                        }
                                    }

                                    // 合并子图节点
                                    for (Iterator<String> nodeIterator = mergedGraph
                                            .getNodes().keySet().iterator(); nodeIterator
                                                 .hasNext(); ) {
                                        Node nodeInMergeGraph = mergedGraph
                                                .getNodes().get(
                                                        nodeIterator.next());
                                        hasMergedGraph
                                                .addNode(nodeInMergeGraph);

                                    }

                                    // 用hasmergednode代替mergednode,改变指针
                                    if (orgraph.getNodes().get(
                                            mergedNode.getId()) != null) {
                                        orgraph.getNodes().remove(
                                                mergedNode.getId());
                                        this.mergeHashtable.remove(mergedNode
                                                .getId());
                                        this.outerEdges.remove(mergedNode
                                                .getId());

                                    }
                                    mergedNode = hasMergedNode;
                                    mergedGraph = hasMergedGraph;
                                    outerEdges = oldOuterEdges;

                                    break;
                                }

                            }
                        }

                    }
                    // 存储, 这个子图
                    this.mergeHashtable.put(mergedNode.getId(), mergedGraph);
//					System.out.println(mergedNode.getId());
                    this.outerEdges.put(mergedNode.getId(), outerEdges);
                    // add merged node to V
                    orgraph.addNode(mergedNode);
                    orgraph.setNodeNum();
                }
            }
        }
    }

    // 针对SIM图进行子图合并
    public void mergeForSim(Graph orgraph,
                            ArrayList<HashMap<String, ArrayList<String>>> mapper)
            throws Exception {
        this.isISO = false;
        // 直接遍历一遍，合并节点
        for (int i = 0; i < mapper.size(); i++) {
            // 得到一个Q.Pi的sim结果
            HashMap<String, ArrayList<String>> match = mapper.get(i);
            // 得到一个sim集合最小的集合，做遍历，查询connected
            ArrayList<String> sim_min = null;
            int num = Integer.MAX_VALUE;
            ArrayList<String> allMatchedNodes = new ArrayList();
            for (Iterator<String> iterator = match.keySet().iterator(); iterator
                    .hasNext(); ) {
                String idString = iterator.next();
                if (match.get(idString).size() < num) {
                    num = match.get(idString).size();
                    sim_min = match.get(idString);
                }
                allMatchedNodes.addAll(match.get(idString));
            }
            ArrayList<String> skipNodeIdArrayList = new ArrayList();
            // 遍历每个候选匹配的点
            for (int j = 0; j < num; j++) {
                // 如果没有重合，那么一个sourceNode，遍历结果将产生一个mergedNode
                String sourceNodeIdString = sim_min.get(j);
                if (skipNodeIdArrayList.contains(sourceNodeIdString)) {
                    continue;
                } else {
                    Node sourceNode = orgraph.getNodes()
                            .get(sourceNodeIdString);
                    // 遍历BFS
                    ArrayList<String> bfsStrings = new ArrayList();
                    // 需要增加到类的hashtable中
                    Graph mergedGraph = null;
                    ArrayList<OuterEdge> outerEdges = null;
                    Node mergedNode = null;
                    // 判断重合
                    if (sourceNode != null) {
                        mergedGraph = new Graph();
                        mergedNode = new Node("mn_" + i + "," + j);
                        mergedNode.addMids(i);
                        mergedNode.setMerged(true);
                        mergedGraph.addNode(sourceNodeIdString,
                                sourceNode.getLabels());
                        outerEdges = new ArrayList();
                        // 设置hub
                        mergedNode.setBetweeness(sourceNode.getBetweeness());
                        if (sourceNode.isHub()) {
                            mergedNode.setHub(true);
                            mergedGraph.getNodes().get(sourceNodeIdString)
                                    .setHub(true);
                        }
                        // 遍历BFS
                        bfsStrings.add(sourceNodeIdString);
                    } else {
                        // 与其他Pi已合并节点重合
                        for (Iterator<String> iterator = this.mergeHashtable
                                .keySet().iterator(); iterator.hasNext(); ) {
                            String mergeNodeIdString = iterator.next();
                            Graph graph = mergeHashtable.get(mergeNodeIdString);
                            if (graph.getNodes()
                                    .containsKey(sourceNodeIdString)) {
                                mergedGraph = graph;
                                mergedNode = orgraph.getNodes().get(
                                        mergeNodeIdString);
                                mergedNode.addMids(i);
                                outerEdges = this.outerEdges
                                        .get(mergeNodeIdString);
                                bfsStrings.add(mergeNodeIdString);
                                break;
                            }
                        }
                    }
                    allMatchedNodes.remove(sourceNodeIdString);
                    while (bfsStrings.size() != 0) {
                        String currentId = bfsStrings.remove(0);
                        // visit 并在原图上删除
                        Node currentNode = orgraph.getNodes().remove(currentId);
                        if (currentNode == null) {
                            continue;
                        }
                        if (currentNode.isMerged()) {
                            // 删除subgraph信息
                            this.mergeHashtable.remove(currentId);
                            this.outerEdges.remove(currentId);
                        }
                        ArrayList<Edge> remainDedgeArrayList = new ArrayList();
                        // explore 入栈
                        for (Iterator<String> cIterator = currentNode
                                .getEdgesArrayList().keySet().iterator(); cIterator
                                     .hasNext(); ) {
                            String toNodeId = cIterator.next();
                            Edge edge = currentNode.getEdgesArrayList().get(
                                    toNodeId);
                            Node toNode = edge.getToNode();

                            // 查看是否访问过
                            if (!bfsStrings.contains(toNodeId)
                                    && !mergedGraph.getNodes().keySet()
                                    .contains(toNodeId)) {
                                // 查看是否是matched节点
                                if (allMatchedNodes.contains(toNodeId)) {
                                    // 内部点,增加到BFS中且增加到mergeGraph里
                                    bfsStrings.add(toNodeId);
                                    mergedGraph.addNode(toNodeId,
                                            toNode.getLabels());

                                    // 设置hub
                                    if (toNode.isHub()) {
                                        mergedNode.setHub(true);
                                        mergedGraph.getNodes().get(toNodeId)
                                                .setHub(true);
                                    }
                                    if (sim_min.contains(toNodeId)) {
                                        skipNodeIdArrayList.add(toNodeId);
                                    }
                                    // 删除toNode指向current的边
                                    if (toNode != currentNode) {
                                        toNode.getEdgesArrayList().remove(
                                                currentId);
                                    }

                                    if (currentNode.isMerged()) {
                                        // 删除从merged节点到内部点得边,外边
                                        remainDedgeArrayList.add(edge);
                                        // 寻找真正的内部边的出边点
                                        ArrayList<OuterEdge> deletO = new ArrayList<OuterEdge>();
                                        for (OuterEdge outerEdge : outerEdges) {
                                            if (outerEdge.getToId().equals(
                                                    toNodeId)) {
                                                // 增加内边
                                                mergedGraph
                                                        .addEdge(
                                                                outerEdge
                                                                        .getFromId(),
                                                                toNodeId,
                                                                edge.getWeight(),
                                                                false);
                                                // 删除这个outedge
                                                deletO.add(outerEdge);
                                            }
                                        }
                                        for (OuterEdge e : deletO) {
                                            outerEdges.remove(e);
                                        }

                                        // startFromHasMerged = false;
                                    } else {
                                        mergedGraph.addEdge(currentId,
                                                toNodeId, edge.getWeight(),
                                                false);
                                    }
                                    allMatchedNodes.remove(toNodeId);

                                } else {
                                    // 外部点
                                    if (toNode.isMerged()) {
                                        // 判断是否需要合并，查看merged图中是否有节点在matches节点中
                                        Graph hasMergedGraph = this.mergeHashtable
                                                .get(toNodeId);
                                        ArrayList<OuterEdge> hasOuterEdges = this.outerEdges
                                                .get(toNodeId);
                                        boolean needM = false;
                                        if (hasMergedGraph != null
                                                && hasOuterEdges != null) {
                                            for (OuterEdge outerEdge : hasOuterEdges) {
                                                if (outerEdge.getToId().equals(
                                                        currentId)) {
                                                    String fromIdString = outerEdge
                                                            .getFromId();
                                                    if (allMatchedNodes
                                                            .contains(fromIdString)) {
                                                        // 需要合并
                                                        needM = true;
                                                        // 合并内部节点，复制内部节点到hasmerged
                                                        for (Iterator<String> iterator = mergedGraph
                                                                .getNodes()
                                                                .keySet()
                                                                .iterator(); iterator
                                                                     .hasNext(); ) {
                                                            Node node = mergedGraph
                                                                    .getNodes()
                                                                    .get(iterator
                                                                            .next());
                                                            hasMergedGraph
                                                                    .addNode(node);
                                                        }
                                                        hasMergedGraph
                                                                .addEdge(
                                                                        fromIdString,
                                                                        currentId,
                                                                        outerEdge
                                                                                .getWeight(),
                                                                        false);
                                                        allMatchedNodes
                                                                .remove(fromIdString);
                                                    }
                                                }
                                            }
                                        } else {
                                            continue;
                                        }
                                        // 合并内部边
                                        if (needM) {
                                            // 增加该节点到BFS
                                            bfsStrings.add(toNodeId);

                                            ArrayList<OuterEdge> deleteOuterEdges = new ArrayList();
                                            for (OuterEdge outerEdge : hasOuterEdges) {
                                                String idString = outerEdge
                                                        .getToId();
                                                if (mergedGraph.getNodes()
                                                        .keySet()
                                                        .contains(idString)) {
                                                    // 增加内边
                                                    hasMergedGraph
                                                            .addEdge(
                                                                    idString,
                                                                    outerEdge
                                                                            .getFromId(),
                                                                    outerEdge
                                                                            .getWeight(),
                                                                    false);

                                                    // 删除该外边
                                                    deleteOuterEdges
                                                            .add(outerEdge);
                                                    // 删除外边
                                                    if (toNode != currentNode) {
                                                        toNode.getEdgesArrayList()
                                                                .remove(idString);
                                                    }
                                                }
                                            }
                                            // 删除
                                            for (OuterEdge outerEdge : deleteOuterEdges) {
                                                hasOuterEdges.remove(outerEdge);
                                            }

                                            // 处理orgraph上的外边

                                            toNode.getEdgesArrayList().remove(
                                                    currentId);
                                            toNode.getEdgesArrayList().remove(
                                                    mergedNode.getId());

                                            // 把mergedGraph的外边增加到hasMergedGraph上
                                            for (Iterator<String> mIterator = mergedNode
                                                    .getEdgesArrayList()
                                                    .keySet().iterator(); mIterator
                                                         .hasNext(); ) {
                                                String toIdString = mIterator
                                                        .next();
                                                Edge edge2 = mergedNode
                                                        .getEdgesArrayList()
                                                        .get(toIdString);
                                                if (!toIdString.equals(toNode
                                                        .getId())) {
                                                    toNode.addEdge(edge2);
                                                    // 更改反向边

                                                    if (!edge2.getToNode()
                                                            .getId()
                                                            .equals(currentId)) {
                                                        Edge reEdge = edge2
                                                                .getToNode()
                                                                .getEdgesArrayList()
                                                                .remove(mergedNode
                                                                        .getId());
                                                        if (reEdge != null) {
                                                            edge2.getToNode()
                                                                    .addEdge(
                                                                            new Edge(
                                                                                    toNode,
                                                                                    reEdge.getWeight()));
                                                        }
                                                    }

                                                }
                                            }

                                            for (OuterEdge outerEdge : outerEdges) {
                                                String innerToString = outerEdge
                                                        .getToId();
                                                if (!hasMergedGraph
                                                        .getNodes()
                                                        .contains(innerToString)) {
                                                    hasOuterEdges
                                                            .add(outerEdge);
                                                }
                                            }
                                            // 设置hub
                                            if (mergedNode.isHub()) {
                                                toNode.setHub(true);
                                            }

                                            for (Integer mid : mergedNode
                                                    .getMids()) {
                                                toNode.addMids(mid);
                                            }

                                            // 删除mergedNode
                                            if (orgraph.getNodes().get(
                                                    mergedNode.getId()) != null) {
                                                orgraph.getNodes().remove(
                                                        mergedNode.getId());
                                                this.mergeHashtable
                                                        .remove(mergedNode
                                                                .getId());
                                                this.outerEdges
                                                        .remove(mergedNode
                                                                .getId());
                                            }

                                            mergedGraph = hasMergedGraph;
                                            mergedNode = toNode;
                                            outerEdges = hasOuterEdges;
                                            this.mergeHashtable
                                                    .remove(toNodeId);
                                            this.outerEdges.remove(toNodeId);

                                        } else {
                                            // 不需要合并,增加mergedNode外边
                                            mergedNode.addEdge(new Edge(toNode,
                                                    edge.getWeight()));
                                            // 更改反向边
                                            if (toNode != currentNode) {
                                                Edge dEdge = toNode
                                                        .getEdgesArrayList()
                                                        .remove(currentId);
                                                if (dEdge != null) {
                                                    float weight = dEdge
                                                            .getWeight();
                                                    toNode.addEdge(new Edge(
                                                            mergedNode, weight));
                                                }
                                            }

                                            // 更改outerEdges
                                            for (OuterEdge outerEdge : this.outerEdges
                                                    .get(toNodeId)) {
                                                if (outerEdge.getToId().equals(
                                                        currentId)) {
                                                    outerEdges
                                                            .add(new OuterEdge(
                                                                    currentId,
                                                                    outerEdge
                                                                            .getFromId(),
                                                                    edge.getWeight()));
                                                    break;
                                                }
                                            }
                                        }

                                    } else {
                                        // 单纯外部节点
                                        // 增加外部边
                                        if (!currentNode.isMerged()) {
                                            outerEdges.add(new OuterEdge(
                                                    currentId, toNodeId, edge
                                                    .getWeight()));
                                            mergedNode.addEdge(new Edge(toNode,
                                                    edge.getWeight()));
                                            // 更改反向边
                                            if (toNode != currentNode) {
                                                Edge dEdge = toNode
                                                        .getEdgesArrayList()
                                                        .remove(currentId);
                                                if (dEdge != null) {
                                                    float weight = dEdge
                                                            .getWeight();
                                                    toNode.addEdge(new Edge(
                                                            mergedNode, weight));
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // 已经把内部点包含了
                                // 判断当前mergedgraph是否有outedges指向toNode
                                if (currentNode.isMerged()) {
                                    ArrayList<OuterEdge> removeOuterEdges = new ArrayList();
                                    for (int k = 0; k < outerEdges.size(); k++) {
                                        if (outerEdges.get(k).getToId()
                                                .equals(toNodeId)) {
                                            // 说明合并后有外边需要删除
                                            // 增加内边
                                            mergedGraph.addEdge(
                                                    outerEdges.get(k)
                                                            .getFromId(),
                                                    toNodeId, edge.getWeight(),
                                                    false);
                                            removeOuterEdges.add(outerEdges
                                                    .get(k));
                                        }
                                    }
                                    remainDedgeArrayList.add(edge);
                                    for (OuterEdge edge2 : removeOuterEdges) {
                                        outerEdges.remove(edge2);
                                    }
                                } else {
                                    if (!toNode.isMerged()) {
                                        mergedGraph.addEdge(currentId, toNodeId,
                                                edge.getWeight(), false);
                                    }
                                }

                            }

                        }
                        // 删除边
                        for (int k = 0; k < remainDedgeArrayList.size(); k++) {
                            currentNode.getEdgesArrayList().remove(
                                    remainDedgeArrayList.get(k).getToNode()
                                            .getId());
                        }

                    }
                    // 增加合并节点到orgraph中
                    orgraph.addNode(mergedNode);
                    // 更改table
                    this.outerEdges.put(mergedNode.getId(), outerEdges);
                    this.mergeHashtable.put(mergedNode.getId(), mergedGraph);
                }
            }
        }

    }

    // 不用
    // 把simluation的结果转换成连图子图
    public ArrayList<ArrayList<Hashtable<String, Node>>> getconnected(
            Graph orgraph,
            ArrayList<Hashtable<String, ArrayList<String>>> mapper) {
        ArrayList<ArrayList<Hashtable<String, Node>>> connectArrayLists = new ArrayList();
        for (int i = 0; i < mapper.size(); i++) {
            ArrayList<Hashtable<String, Node>> connectArrayList = new ArrayList();
            // 得到一个Q.Pi的sim结果
            Hashtable<String, ArrayList<String>> match = mapper.get(i);
            // 得到一个sim集合最小的集合，做遍历，查询connected
            ArrayList<String> sim_min = null;
            int num = Integer.MAX_VALUE;
            ArrayList<String> allMatchedNodes = new ArrayList();
            for (Iterator<String> iterator = match.keySet().iterator(); iterator
                    .hasNext(); ) {
                String idString = iterator.next();
                if (match.get(idString).size() < num) {
                    num = match.get(idString).size();
                    sim_min = match.get(idString);
                }
                allMatchedNodes.addAll(match.get(idString));
            }
            ArrayList<String> skipNodeIdArrayList = new ArrayList();
            // 遍历每个候选匹配的点
            for (int j = 0; j < num; j++) {
                Hashtable<String, Node> connect = new Hashtable();
                String sourceNodeIdString = sim_min.get(j);
                if (skipNodeIdArrayList.contains(sourceNodeIdString)) {
                    continue;
                } else {
                    // 遍历BFS
                    ArrayList<String> bfsStrings = new ArrayList();
                    bfsStrings.add(sourceNodeIdString);
                    while (bfsStrings.size() != 0) {
                        String currentId = bfsStrings.remove(0);
                        // visit
                        Node currentNode = orgraph.getNodes().get(currentId);
                        connect.put(currentId, currentNode);
                        // explore 入栈
                        for (Iterator<String> iterator = currentNode
                                .getEdgesArrayList().keySet().iterator(); iterator
                                     .hasNext(); ) {
                            String newIdString = iterator.next();
                            // 查看是否访问过
                            if (!bfsStrings.contains(newIdString)
                                    && !connect.keySet().contains(newIdString)) {
                                // 查看是否是matched节点
                                if (allMatchedNodes.contains(newIdString)) {
                                    // 应该在connect图中
                                    bfsStrings.add(newIdString);
                                    // 查看是否为重合节点
                                    if (sim_min.contains(newIdString)) {
                                        skipNodeIdArrayList.add(newIdString);
                                    }
                                }
                            }
                        }
                    }

                }
                connectArrayList.add(connect);
            }
            connectArrayLists.add(connectArrayList);
        }

        return connectArrayLists;
    }

    /**
     * mergedNode -> mergedGraph
     */
    public HashMap<String, Graph> getMergeHashtable() {
        return mergeHashtable;
    }

    /**
     * mergedNode ->outerEdges(from_Id, To_Id)
     */
    public HashMap<String, ArrayList<OuterEdge>> getOuterEdges() {
        return outerEdges;
    }

    public boolean isISO() {
        return isISO;
    }

}
