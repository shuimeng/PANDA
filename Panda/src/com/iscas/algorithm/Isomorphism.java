package com.iscas.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.iscas.model.Edge;
import com.iscas.model.Graph;
import com.iscas.model.Node;

public class Isomorphism {

    /**
     * 本类实现isomorphism的算法
     */
    public Isomorphism() {

    }

    // 返回映射，一个节点可能有多个映射节点与其对应， key:query节点Id，Value：graph中节点Id
    // 该算法很慢

    public ArrayList<HashMap<String, String>> matchResArrayList;

    public ArrayList<HashMap<String, String>> ullmann(Graph graph, Graph query) {
        HashMap<String, ArrayList<String>> mapperHashtable = new HashMap<String, ArrayList<String>>();
        // filter process
        // System.out.println("Candidate Section:");


        for (Iterator<String> iterator = query.getNodes().keySet().iterator(); iterator
                .hasNext(); ) {
            Node qnode = query.getNodes().get(iterator.next());
            // query中node只有一个label
            String labelString = qnode.getLabels().get(0);
            ArrayList<String> gnodeIdsArrayList = new ArrayList();
            // 参看label一致的，且大于等于querynode的度的节点，加入candidate map中
            for (Iterator<String> iterator2 = graph.getNodes().keySet()
                    .iterator(); iterator2.hasNext(); ) {
                Node gnode = graph.getNodes().get(iterator2.next());
                if (gnode.getEdgeNum() >= qnode.getEdgeNum()) {
                    for (int i = 0; i < gnode.getLabels().size(); i++) {
                        if (gnode.getLabels().get(i).equals(labelString)) {
                            // label相等且度满足要求
                            // System.out.println("qnode:" + qnode.getId()
                            // + "gnode:" + gnode.getId());
                            gnodeIdsArrayList.add(gnode.getId());
                            break;
                        }
                    }
                }
            }
            if (gnodeIdsArrayList.size() != 0) {
                mapperHashtable.put(qnode.getId(), gnodeIdsArrayList);
            } else {
                // C(u)=0
                // System.out.println("C(u)=0");
                return null;
            }
        }
        // subgraphSearch
        matchResArrayList = new ArrayList();
        HashMap<String, String> matchTable = new HashMap();
        subgraphSearch_ullmann(graph, query, matchTable, mapperHashtable);
        return matchResArrayList;
    }

    public void subgraphSearch_ullmann(Graph graph, Graph querGraph,
                                       HashMap<String, String> matchTable,
                                       HashMap<String, ArrayList<String>> mapperHashtable) {
        // 判断是否匹配完成
        if (matchTable.size() == querGraph.getNodeNum()) {
            // System.out.println("match one!");
            // copy
            HashMap<String, String> copyHashtable = new HashMap<String, String>();
            for (Iterator<String> it = matchTable.keySet().iterator(); it
                    .hasNext(); ) {
                String keyString = it.next();
                copyHashtable.put(keyString, matchTable.get(keyString));
            }
            matchResArrayList.add(copyHashtable);
        } else {
            // nextQueryVertex,u
            String nextQueryId = null;
            for (Iterator<String> iterator = querGraph.getNodes().keySet()
                    .iterator(); iterator.hasNext(); ) {
                nextQueryId = iterator.next();
                // 查看是否没有match
                if (matchTable.get(nextQueryId) == null) {
                    break;
                }
            }
//			System.out.println(nextQueryId );
            // 遍历没有matched候选点集合，match后从mapper重删除，restore时再加入
            HashMap<String, Edge> uprimeArrayList = querGraph.getNodes()
                    .get(nextQueryId).getEdgesArrayList();
            for (int i = 0; i < mapperHashtable.get(nextQueryId).size(); i++) {
                // 判断是否joinable
                // 得到一个候选节点,v
                String vId = mapperHashtable.get(nextQueryId).get(i);
                boolean match = true;
                // 查看是否对于每个u'都有一条边在g中
                for (Iterator<String> uprimeIterator = uprimeArrayList.keySet()
                        .iterator(); uprimeIterator.hasNext(); ) {
                    Node uprimeNode = uprimeArrayList
                            .get(uprimeIterator.next()).getToNode();
                    String vprimeId = matchTable.get(uprimeNode.getId());
                    if (vprimeId != null) {

                        // 查看(v,v') \in E(g)?
                        if (!graph.getNodes().get(vId).getEdgesArrayList()
                                .containsKey(vprimeId)) {
                            match = false;
                            //		System.out.println(vId+' '+vprimeId);
                        }
                    }
                }
                if (match) {
                    // update
                    matchTable.put(nextQueryId, vId);
                    // System.out.println(nextQueryId + "," + vId);
                    // 删除匹配候选节点
                    mapperHashtable.get(nextQueryId).remove(vId);
                    subgraphSearch_ullmann(graph, querGraph, matchTable,
                            mapperHashtable);
                    // restore
                    // 还原匹配候选节点
                    mapperHashtable.get(nextQueryId).add(i, vId);
                    matchTable.remove(nextQueryId);
                }

            }

        }
    }
}
