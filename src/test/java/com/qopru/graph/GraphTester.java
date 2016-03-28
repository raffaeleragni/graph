package com.qopru.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class GraphTester
{
    @Test
    public void testCollectionAdd()
    {
        Graph<Integer, Integer> g = new EdgeGraph<>();
        g.add(1);
        g.add(2);
        g.add(3);
        g.add(4);
        // Nulls can be inserted but there is no use for them really
        g.add(null);
        // These values are accepted because any object could be the generic
        g.add(-1);

        assert g.contains(1);
        assert g.contains(2);
        assert g.contains(3);
        assert g.contains(4);
        assert g.contains(null);
        assert g.contains(-1);
        assert g.size() == 6;
    }

    @Test
    public void testCollectionRemove()
    {
        Graph<Integer, Integer> g = new EdgeGraph<>();
        g.add(1);
        g.add(2);
        g.add(3);
        g.add(4);

        g.remove(2);
        g.remove(3);
        g.remove(-1);
        g.remove(null);

        assert g.contains(1);
        assert !g.contains(2);
        assert !g.contains(3);
        assert g.contains(4);
        assert g.size() == 2;
    }

    @Test
    public void testEdge()
    {
        Graph<Integer, Integer> g = new EdgeGraph<>();
        g.add(1);
        g.add(2);
        g.add(3);
        g.add(4);

        g.connect(1, 2, 1);
        g.connect(3, 4, 2);

        assert g.getEdgeInfo(1, 2).get() == 1;
        assert g.getEdgeInfo(3, 4).get() == 2;
    }

    @Test
    public void simpleTest()
    {
        Graph<Integer, Integer> ge = new EdgeGraph<>();
        Graph<Integer, Integer> gm = new MatrixGraph<>();

        /*
        1 ---- 2 ---- 3 ---- 7 ---- 9 ----\ : distance 2 on these lines
        |      |             |            | : distance 1 on vertical lines
        8 ---- 4 ---- 5 ---- 6 --- 10 -- 11 : distance 1 on these lines
        */
        // Test both implementations.
        // Should give the same result, even if with different performance.
        for (Graph<Integer, Integer> g: new Graph[]{ge, gm})
        {
            g.add(1);
            g.add(2);
            g.add(3);
            g.add(4);
            g.add(5);
            g.add(6);
            g.add(7);
            g.add(8);
            g.add(9);
            g.add(10);
            g.add(11);

            g.connect(1, 2, 2);
            g.connect(1, 8, 1);
            g.connect(8, 4, 1);
            g.connect(2, 4, 1);
            g.connect(2, 3, 2);
            g.connect(4, 5, 1);
            g.connect(3, 7, 2);
            g.connect(5, 6, 1);
            g.connect(7, 6, 1);
            g.connect(7, 9, 2);
            g.connect(6, 10, 1);
            g.connect(10, 11, 1);
            g.connect(9, 11, 2);

            assert g.getEdgeInfo(1, 2).isPresent();
            assert g.getEdgeInfo(1, 2).get() == 2;

            List<Integer> sol1 = Arrays.asList(1, 2, 3, 7, 9, 11);
            assert sol1.equals(g.findPath(1, 11).orElse(Collections.emptyList()));

            List<Integer> sol2 = Arrays.asList(1, 8, 4, 5, 6, 10, 11);
            // Setting this function enabled dijkstra
            g.setEdgeLengthResolver(e -> (double) e);
            assert sol2.equals(g.findPath(1, 11).orElse(Collections.emptyList()));

            List<Integer> sol3 = Arrays.asList(1, 2, 3, 7, 9, 11);
            // Setting this function enabled astar
            g.setNodeScoreResolver((n1, n2) -> (double) Math.abs(n1 + n2));
            assert sol3.equals(g.findPath(1, 11).orElse(Collections.emptyList()));
        }
    }
}
