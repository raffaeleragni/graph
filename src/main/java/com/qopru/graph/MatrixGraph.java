package com.qopru.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This implementation is using an adjacency matrix for edges.
 * @param <T> The type of a node.
 * @param <E> The type of the object associated with the Edge.
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class MatrixGraph<T, E> extends AbstractGraph<T, E>
{
    // Matrix array. Here we intend the first index to be thw row, the second the column.
    // Needs to be an Object matrix because native arrays don't support generics.
    // But since it is internally controlled only by this class, it is OK to cast.
    private Object[][] matrix;
    private final Map<T, Integer> nodeIndexes;

    public MatrixGraph()
    {
        nodeIndexes = new HashMap<>();
    }

    // Must be synchronized and 'blocking' because while copying to the new matrix edgex and indexes could change in the same time.
    // Also nodes will get new generated indexes, most surely different than the previous ones.
    private synchronized void newMatrix()
    {
        Object[][] oldmatrix = matrix;
        matrix = new Object[size()][size()];
        // The list returned is in a potantial random order. But we don't care, because as long as the newMatrix() methos is called
        // only when the size of the collection changes. (or rather, when a node is connected to the other and the size is not matching anymore)
        // So as long as the index generated here stays the same until the new matrix is generated, all is fine.
        // This object is the actual collection of nodes.
        int indexCounter = 0;
        for (T t: this)
        {
            // Keep reference of the old index
            Integer oldIndex = nodeIndexes.get(t);
            // Assign the new index, we already have the reference to the old one so it is safe to do now.
            int newIndex = indexCounter;
            nodeIndexes.put(t, newIndex);
            indexCounter++;
            // Nodes that were not present before, get a new index but they are not copied from the old matrix.
            // Also, since there is nothing to copy, there is also no edge to get from the matrix, so just got a new index and nothing else.
            if (oldIndex == null)
                continue;
            // Also nothing to copy when the old matrix was not even there to begin with.
            if (oldmatrix ==  null)
                continue;

            // Get the old edge, it may be null as well.
            Object edge = null;
            // Always check for boundaries, to be sure
            if (oldIndex < oldmatrix.length)
                if (oldIndex < oldmatrix[oldIndex].length)
                    edge = matrix[oldIndex][oldIndex];

            // Save the edge in the new position
            if (newIndex < matrix.length)
                if (newIndex < matrix[oldIndex].length)
                    matrix[newIndex][newIndex] = edge;
        }

        // This pass ensures the map will not contain anymore indexes that are not present in our collection.
        nodeIndexes.keySet()
            .stream()
            .filter(t -> !this.contains(t))
            .forEach(nodeIndexes::remove);
    }

    @Override
    public int getDegree(T node)
    {
        Integer idx = nodeIndexes.get(node);
        // A not found node has no degree, it is not even in the graph!
        if (idx == null)
            return -1;
        // No connections in this graph for this case.
        // Matrix could be null because we keep a lazy initialization on connect()
        // Since nothing would be connected before a matrix initialization, then any node has 0 degree, or, disconnected to anything.
        if (matrix == null || matrix.length == 0)
            return 0;
        int result = 0;

        // Matrix is always square (we create it, so we know)
        for (int i = 0; i < matrix.length; i++)
        {
            // Out degree
            if (matrix[idx][i] != null)
                result++;
            // In degree
            if (matrix[i][idx] != null)
                result++;
        }

        return result;
    }

    @Override
    public void connect(T from, T to, E edgeInfo)
    {
        // Generate the matrix only in the moment of a node connection.
        // There is no other reason to initialize one, since if connect() is never called, no information can be possibly
        // contained by the matrix. Plus, in this way, we are not constantly recreating a matrix to each add() of nodes but only
        // when actual edges are connected. If the user first loads nodes and only after edges, this way can be more performant.
        if (matrix == null || matrix.length < size())
            newMatrix();

        // Row is the from node, that is how the matrix is read.
        Integer row = nodeIndexes.get(from);
        Integer col = nodeIndexes.get(to);
        // Nothing to connect here
        if (row == null || col == null)
            return;

        if (row < matrix.length)
            if (col < matrix[row].length)
                matrix[row][col] = (Object) edgeInfo;
    }

    @Override
    public void disconnect(T from, T to)
    {
        // Row is the from node, that is how the matrix is read.
        Integer row = nodeIndexes.get(from);
        Integer col = nodeIndexes.get(to);
        // Nothing to disconnect here
        if (row == null || col == null)
            return;

        if (row < matrix.length)
            if (col < matrix[row].length)
                matrix[row][col] = null;
    }

    @Override
    public List<T> getOutNeighborhoods(T node)
    {
        Integer idx = nodeIndexes.get(node);
        if (idx == null || matrix == null || idx >= matrix.length)
            return Collections.emptyList();

        List<T> result = new LinkedList<>();
        for (T cur: this)
        {
            Integer i = nodeIndexes.get(cur);
            if (matrix[idx][i] != null)
                result.add(cur);
        }

        return result;
    }

    @Override
    public List<T> getInNeighborhoods(T node)
    {
        Integer idx = nodeIndexes.get(node);
        if (idx == null || matrix == null || idx >= matrix.length)
            return Collections.emptyList();

        List<T> result = new LinkedList<>();
        for (T cur: this)
        {
            Integer i = nodeIndexes.get(cur);
            if (matrix[i][idx] != null)
                result.add(cur);
        }

        return result;
    }

    @Override
    public Optional<E> getEdgeInfo(T from, T to)
    {
        Integer row = nodeIndexes.get(from);
        Integer col = nodeIndexes.get(to);
        // Nothing to disconnect here
        if (row == null || col == null || matrix == null)
            return Optional.empty();

        if (row < matrix.length)
            if (col < matrix[row].length)
                return Optional.ofNullable((E) matrix[row][col]);

        return Optional.empty();
    }
}
