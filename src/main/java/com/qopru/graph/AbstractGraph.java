package com.qopru.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;

/**
 * Common implementation parts of the Graph.
 * This common class will contain nodes only. The different implementation will have different ways to handle edges.
 * @param <T> The type of a node.
 * @param <E> The type of the object associated with the Edge.
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public abstract class AbstractGraph<T, E> implements Graph<T, E>
{
    // All graphs have in common to contain nodes.
    // Edges may differ in implementation, but nodes is always a collection.
    // Use a set so that it is fast to detect is a node is in the collection.
    @Delegate // Delegate the composition with lombok generated methods.
    private final Set<T> nodes;

    private Function<E, Double> edgeLengthResolver;
    private BiFunction<T, T, Double> nodeScoreResolver;

    public AbstractGraph()
    {
        nodes = new HashSet<>();
    }

    @Override
    public List<T> toDegreSequence()
    {
        List<T> result = new ArrayList<>(nodes);
        Collections.sort(result, Comparator.comparing(t -> getDegree(t)));
        return result;
    }

    @Override
    public Optional<List<T>> findPath(T start, T end)
    {
        if (edgeLengthResolver != null && nodeScoreResolver != null)
            return astar(start, end);
        else if (edgeLengthResolver != null)
            return dijkstra(start, end);
        else
            return bfs(start, end);
    }

    private List<T> buildReturnPath(T start, T end, Map<T, T> parents)
    {
        List<T> result = new LinkedList<>();
        for (T node = end; node != null && !node.equals(start); node = parents.get(node))
            result.add(0, node);
        result.add(0, start);
        return result;
    }

    private Optional<List<T>> bfs(T start, T end)
    {
        // parent hashmap used to reconstruct path
        Map<T, T> parents = new HashMap<>();
        // keep track of which nodes were visited
        Set<T> visited = new HashSet<>();
        // use a queue for navigating nodes at their level and not by depth
        Queue<T> queue = new LinkedList<>();
        // First node in queue
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty())
        {
            // Go through nodes
            T node = queue.remove();
            if (node.equals(end))
                return Optional.of(buildReturnPath(start, end, parents));
            for (T child: getOutNeighborhoods(node))
            {
                if (visited.contains(child))
                    continue;
                visited.add(child);

                parents.put(child, node);
                queue.add(child);
            }
        }

        return Optional.empty();
    }

    private Optional<List<T>> dijkstra(T start, T end)
    {
        // Distance is an additional property for each node.
        Map<T, Double> distance = new HashMap<>();
        // parent hashmap used to reconstruct path
        Map<T, T> parents = new HashMap<>();
        // keep track of which nodes were visited
        Set<T> visited = new HashSet<>();
        // use a queue for navigating nodes at their level and not by depth
        // priority queue takes Comparable into consideration for its ordering and priority for removal
        PriorityQueue<EnqueuedNode<T>> queue = new PriorityQueue<>();

        // Must initialize all nodes to infinity first
        this.stream().forEach(n -> distance.put(n, Double.MAX_VALUE));
        // First node starts as 0
        distance.put(start, 0d);
        // And automatically added to queue
        queue.add(new EnqueuedNode<>(start, 0));
        visited.add(start);

        while (!queue.isEmpty())
        {
            // Go through nodes
            EnqueuedNode<T> eq = queue.remove();
            T node = eq.getNode();

            if (node.equals(end))
                return Optional.of(buildReturnPath(start, end, parents));

            for (T child: getOutNeighborhoods(node))
            {
                if (visited.contains(child))
                    continue;

                visited.add(child);

                Optional<E> edge = getEdgeInfo(node, child);

                // Nodes not connected? go ahead.
                if (!edge.isPresent())
                    continue;

                // Distance is useful for finding the shortest path, depending in how the user has defined
                // the function that will tell us how much long an edge is (edgeLengthResolver).
                double curDis = distance.computeIfAbsent(node, n -> Double.MAX_VALUE)
                    + edgeLengthResolver.apply(edge.get());

                if (curDis < distance.computeIfAbsent(child, n -> Double.MAX_VALUE))
                {
                    distance.put(child, curDis);
                    parents.put(child, node);
                    queue.add(new EnqueuedNode<>(child, curDis));
                }
            }
        }

        return Optional.empty();
    }

    private Optional<List<T>> astar(T start, T end)
    {
        // Score is distance + heuristic function
        Map<T, Double> scores = new HashMap<>();
        // parent hashmap used to reconstruct path
        Map<T, T> parents = new HashMap<>();
        // keep track of which nodes were visited
        Set<T> visited = new HashSet<>();
        // use a queue for navigating nodes at their level and not by depth
        // priority queue takes Comparable into consideration for its ordering and priority for removal
        PriorityQueue<EnqueuedNode<T>> queue = new PriorityQueue<>();

        // Must initialize all nodes to infinity first
        this.stream().forEach(n -> scores.put(n, Double.MAX_VALUE));
        // First node starts as 0
        scores.put(start, 0d);
        // And automatically added to queue
        queue.add(new EnqueuedNode<>(start, 0));

        while (!queue.isEmpty())
        {
            // Go through nodes
            EnqueuedNode<T> eq = queue.remove();
            T node = eq.getNode();
            if (visited.contains(node))
                continue;

            visited.add(node);

            if (node.equals(end))
                return Optional.of(buildReturnPath(start, end, parents));

            for (T child: getOutNeighborhoods(node))
            {
                Optional<E> edge = getEdgeInfo(node, child);

                // Nodes not connected? go ahead.
                if (!edge.isPresent())
                    continue;

                double nodeScore = nodeScoreResolver.apply(node, end);

                // Distance is useful for finding the shortest path, depending in how the user has defined
                // the function that will tell us how much long an edge is (edgeLengthResolver).
                double curScore = scores.computeIfAbsent(node, n -> Double.MAX_VALUE)
                    + edgeLengthResolver.apply(edge.get())
                    + nodeScore;

                if (curScore < scores.computeIfAbsent(child, n -> Double.MAX_VALUE))
                {
                    scores.put(child, curScore);
                    parents.put(child, node);
                    queue.add(new EnqueuedNode<>(child, curScore));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void setEdgeLengthResolver(Function<E, Double> fn)
    {
        this.edgeLengthResolver = fn;
    }

    @Override
    public void setNodeScoreResolver(BiFunction<T, T, Double> fn)
    {
        this.nodeScoreResolver = fn;
    }

    // This node pair class is used to idenfity and index an edge.
    // It is only an internal use implementation, not accessible to API.
    // Very important to have equals and hash to match the pair from and to.
    @Data
    @EqualsAndHashCode(of = {"from", "to"})
    class NodePair<N>
    {
        private N from;
        private N to;
        NodePair(N from, N to)
        {
            this.from = from;
            this.to = to;
        }
    }

    // This is used as a container for the PriorityQueues
    @Data
    private class EnqueuedNode<N> implements Comparable<EnqueuedNode<N>>
    {
        private N node;
        private double priority;
        EnqueuedNode(N node, double priority)
        {
            this.node = node;
            this.priority = priority;
        }

        @Override
        public int compareTo(EnqueuedNode<N> o)
        {
            if (o == null)
                return 1;
            return Double.compare(priority, o.priority);
        }
    }
}
