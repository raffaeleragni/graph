package com.qopru.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This interface represents a graph.
 *
 * A graph is a collection of elements, called vertices, but sometimes also nodes.
 * Each node can be inserted into the collection, and the base Collection interface will handle such nodes or vertices.
 *
 * Nodes of this collection must be: Serializable, Comparable, and implementing proper equals and hashCode methods.
 *
 * Each node can be connected to others through edges. Connection is done with the connect method, the opposite with disconnect.
 * The node object is needed in both cases, plus extra information can be passed in the connect method, properties and values
 * that are related to the connection between nodes. They can be later retrieved again with the
 *
 * @param <T> The type of a node.
 * @param <E> The type of the object associated with the Edge.
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public interface Graph<T, E> extends Collection<T>
{
    /**
     * Returns the in and out degree of a node, summed.
     *
     * @param node the node to calculate
     *
     * @return the degree of the node
     */
    int getDegree(T node);

    /**
     * Returns a degree sequence of the graph.
     *
     * A degree sequence is a list of the nodes ordered based on their degree.
     *
     * @return the list of nodes ordered by degree.
     */
    List<T> toDegreSequence();

    /**
     * Connects two nodes with an edge.
     *
     * @param from The start node of the connection
     * @param to The end node of the connection
     * @param edgeInfo the object containing the properties to be associated to the edge being created.
     */
    void connect(T from, T to, E edgeInfo);

    /**
     * Disconnects two nodes.
     *
     * The order of the two nodes given to this command matters, since edges are directional.
     *
     * @param from The from node of the edge to delete
     * @param to The end node of the edge to delete
     */
    void disconnect(T from, T to);

    /**
     * Returns the neighborhood nodes that are outgoing from this node.
     * @param node the node to which the list is going to. All the nodes going FROM this node out, will be returned.
     * @return the list of adjacent nodes going out.
     */
    List<T> getOutNeighborhoods(T node);

    /**
     * Returns the neighborhood nodes that are ingoing from this node.
     * @param node the node from which the list is got from. All the nodes going TO this node will be returned.
     * @return the list of adjacent nodes going in.
     */
    List<T> getInNeighborhoods(T node);

    /**
     * Returns the neighborhood nodes.
     * @param node the node from which the list is obtained.
     * @return the list of adjacent nodes, both in and out.
     */
    default List<T> getAllNeighborhoods(T node)
    {
        List<T> result = new ArrayList<>();
        result.addAll(getOutNeighborhoods(node));
        result.addAll(getInNeighborhoods(node));
        return result;
    };

    /**
     * Returns the information saved for a specific edge.
     *
     * Edges are stored as pairs of from/to nodes, and so are retrieved.
     *
     * @param from the node at the from side of the edge
     * @param to the node at the to side of the edge
     *
     * @return the edge information (optional, it is possible it is not found)
     */
    Optional<E> getEdgeInfo(T from, T to);

    /**
     * This function will try to find the best path between the nodes start and end.
     *
     * A path is not guaranteed to be found. If so, the optional returned will be empty.
     * Based on the state of configuration of this Graph class, different algorithms will be chosen to complete this task.
     *
     * This is obtained with setting a couple of functions.
     * setEdgeLengthResolver will set a function that given the type of edge info (E) it will return the length of the edge as double.
     * setNodeDistanceResolver will set a function that calculates the distance between two nodes (T).
     *
     * If no function is configures, the algorithm used is BFS.
     * If only setEdgeLengthResolver is configured, Dijkstra is used.
     * if both setEdgeLengthResolver and setNodeDistanceResolver are configured, A* is used.
     *
     * The more functions can be set, the best this method will be efficient.
     *
     * @param start the node where to start the path.
     * @param end the node where the path must end.
     *
     * @return The list of nodes to walk through, including start and end. It is an optional, so it may be empty if no path is found.
     */
    Optional<List<T>> findPath(T start, T end);

    /**
     * Configures this Graph to use a function to resolve the length of an edge.
     *
     * If this function is correctly applied, then the graph will be able to use Dijkstra in the findPath() method.
     * (required to apply Dijkstra and A*)
     *
     * @param fn a function that given the edge info E, will return its 'length' as double.
     */
    void setEdgeLengthResolver(Function<E, Double> fn);

    /**
     * Configures this Graph to use a function to calculate the distance between two nodes.
     *
     * If this function is set correctly, the graph will be able to use A* in the findPath() method.
     * For that to work also the setEdgeLengthResolver() must be used.
     *
     * @param fn a function that given two nodes (type T) will return the distance between them.
     */
    void setNodeScoreResolver(BiFunction<T, T, Double> fn);
}
