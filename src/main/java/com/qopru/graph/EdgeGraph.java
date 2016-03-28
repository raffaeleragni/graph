package com.qopru.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is the more common implementation, for a graph having an adjacency list.
 * @param <T> The type of a node.
 * @param <E> The type of the object associated with the Edge.
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class EdgeGraph<T, E> extends AbstractGraph<T, E>
{
    private final Map<T, List<T>> inEdges;
    private final Map<T, List<T>> outEdges;
    private final Map<NodePair<T>, E> edgeInfos;

    public EdgeGraph()
    {
        inEdges = new HashMap<>();
        outEdges = new HashMap<>();
        edgeInfos = new HashMap<>();
    }

    @Override
    public List<T> getInNeighborhoods(T node)
    {
        return new ArrayList<>(safeGet(inEdges, node));
    }

    @Override
    public List<T> getOutNeighborhoods(T node)
    {
        return new ArrayList<>(safeGet(outEdges, node));
    }

    @Override
    public Optional<E> getEdgeInfo(T from, T to)
    {
        NodePair np = new NodePair(from, to);
        return Optional.ofNullable(edgeInfos.get(np));
    }

    @Override
    public void connect(T from, T to, E edgeInfo)
    {
        List<T> outfrom = safeGet(outEdges, from);
        List<T> into = safeGet(inEdges, to);

        NodePair e = new NodePair(from, to);
        edgeInfos.put(e, edgeInfo);

        outfrom.add(to);
        into.add(from);
    }

    @Override
    public void disconnect(T from, T to)
    {
        List<T> outfrom = safeGet(outEdges, from);
        List<T> into = safeGet(inEdges, to);

        outfrom.remove(to);
        into.remove(from);
    }

    @Override
    public int getDegree(T node)
    {
        List<T> in = safeGet(inEdges, node);
        List<T> out = safeGet(outEdges, node);
        int count = 0;
        count += in.size();
        count += out.size();
        return count;
    }

    // Avoid repeating the same long instruction for safely getting the list.
    // Additional initializations may also be added later.
    private List<T> safeGet(Map<T, List<T>> map, T key)
    {
        return map.computeIfAbsent(key, k -> new LinkedList<>());
    }
}
