package com.sos.graphviz;

import com.sos.graphviz.enums.RankType;
import com.sos.graphviz.enums.Shape;
import com.sos.graphviz.properties.GraphvizEnumProperty;
import com.sos.graphviz.properties.GraphvizProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The main class to create a graph. With the factory methods new... you can
 * create subsequent elements of the graph. If you want to save the graph as
 * file you can use the class com.sos.graphviz.GraphIO.
 *
 * See class com.sos.graphviz.GraphTest how to create a graph. */
public class Graph extends GraphvizObjectWithId implements IGraphvizObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(Graph.class);
    private static final String SHORTCUT_GRAPH = "G";
    private static final String PLACEHOLDER = "%id%";
    private static final String PROLOG = "digraph " + PLACEHOLDER + " {";
    private static final String PROLOG_SUBGRAPH = "subgraph " + PLACEHOLDER + " {";
    private static final String EPILOG = "}";
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("\\s|\\W");
    private final GraphvizProperty fontsize = new GraphvizEnumProperty("fontsize");
    private final GraphProperties graphProperties = new GraphProperties();
    private final List<Node> nodeList = new ArrayList<Node>();
    private final List<Edge> edgeList = new ArrayList<Edge>();
    private final List<Subgraph> subgraphList = new ArrayList<Subgraph>();
    private GlobalNodeProperties globalNodeProperties = new GlobalNodeProperties(Shape.box);

    protected Graph(String subGraphId) {
        super(subGraphId, PROLOG_SUBGRAPH.replace(PLACEHOLDER, subGraphId), EPILOG);
        init();
    }

    public Graph() {
        super(SHORTCUT_GRAPH, PROLOG.replace(PLACEHOLDER, SHORTCUT_GRAPH), EPILOG);
        init();
    }

    @Override
    public GraphvizObject getProperties() {
        return this.graphProperties;
    }

    public void init() {
        edgeList.clear();
        nodeList.clear();
        subgraphList.clear();
    }

    protected String getQuoted(final String pstrVal) {
        return "\"" + pstrVal.trim() + "\"";
    }

    public GlobalNodeProperties getGlobalNodeProperties() {
        return this.globalNodeProperties;
    }

    protected void setGlobalNodeProperties(GlobalNodeProperties globalNodeProperties) {
        this.globalNodeProperties = globalNodeProperties;
    }

    public Node getNodeOrNull(String id) {
        String quotedId = getQuoted(id);
        if (nodeList.isEmpty()) {
            LOGGER.debug("The graph {} contains no nodes.", this.getId());
        }
        Node result = null;
        for (Node n : nodeList) {
            if (n.getId().equals(quotedId)) {
                result = n;
                break;
            }
        }
        return result;
    }

    public Node getNodeOrNullInAllGraphs(String id) {
        Node result = getNodeOrNull(id);
        if (result == null) {
            for (Subgraph s : subgraphList) {
                result = s.getNodeOrNullInAllGraphs(id);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String getContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(getProlog());
        sb.append(getMainContent());
        return sb.toString();
    }

    protected String getProlog() {
        StringBuilder sb = new StringBuilder();
        sb.append(graphProperties.getSource());
        sb.append(globalNodeProperties.getSource());
        return sb.toString();
    }

    protected String getMainContent() {
        StringBuilder sb = new StringBuilder();
        Iterator<Node> nit = nodeList.iterator();
        Iterator<Subgraph> sit = subgraphList.iterator();
        while (sit.hasNext()) {
            Subgraph s = sit.next();
            sb.append(s.getSource());
        }
        while (nit.hasNext()) {
            Node n = nit.next();
            sb.append(n.getSource());
        }
        Iterator<Edge> eit = edgeList.iterator();
        while (eit.hasNext()) {
            Edge e = eit.next();
            sb.append(e.getSource());
        }
        return sb.toString();
    }

    public Node newNode(String node) {
        String quotedNode = getQuoted(node);
        Node n = new Node(quotedNode);
        nodeList.add(n);
        return n;
    }

    public Edge newEdge(Node nodeFrom, Node nodeTo) {
        Edge e = new Edge(nodeFrom, nodeTo);
        edgeList.add(e);
        return e;
    }

    public Edge newEdge(String from, String to) {
        Node nodeFrom = getNodeOrNull(from);
        Node nodeTo = getNodeOrNull(to);
        if (nodeFrom == null) {
            nodeFrom = newNode(from);
        }
        if (nodeTo == null) {
            nodeTo = newNode(to);
        }
        Edge e = new Edge(nodeFrom, nodeTo);
        edgeList.add(e);
        return e;
    }

    public Subgraph newSubgraph(String subgraphId, RankType rankType) {
        Subgraph s = new Subgraph(subgraphId, rankType, this);
        subgraphList.add(s);
        return s;
    }

    public Subgraph newSubgraph(String subgraphId) {
        return newSubgraph(subgraphId, RankType.same);
    }

    public ClusterSubgraph newClusterSubgraph(String subgraphId) {
        Matcher m = NON_WORD_PATTERN.matcher(subgraphId);
        String replacedSubgraphId = subgraphId;
        if (m.find()) {
            replacedSubgraphId = m.replaceAll("");
            LOGGER.warn("Subgraph label must not contain non word characters - all non word characters replaced.");
        }
        ClusterSubgraph s = new ClusterSubgraph(replacedSubgraphId, this);
        subgraphList.add(s);
        return s;
    }

    public GraphProperties getGraphProperties() {
        return graphProperties;
    }

    public String getFontsize() {
        return (String) fontsize.getValue();
    }

    public void setFontsize(final String pstrFontSize) {
        this.fontsize.setValue(pstrFontSize);
    }

}
