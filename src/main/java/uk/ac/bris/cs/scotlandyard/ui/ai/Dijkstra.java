package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")

public class Dijkstra {

    private class Node implements Comparable<Node> {
        private final int location;
        private final int distance;

        Node(int location, int distance){
            this.location = location;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node o) {
            return this.distance - o.distance;
        }

        public int getDistance() {
            return distance;
        }

        public int getLocation() {
            return location;
        }
    }

    private final ArrayList<Integer> distTo;
    private final int towards;

    Dijkstra(ImmutableValueGraph<Integer, ImmutableSet<Transport>> graph, ArrayList<Integer> from, Integer towards){
        distTo = new ArrayList<>(Collections.nCopies(200, 1000));
        this.towards = towards;

        //Generate a priority queue that stores detective locations, and their distance "travelled"
        PriorityQueue<Node> pQueue = new PriorityQueue<>();
        for(int location : from){
            distTo.set(location, 0);
            pQueue.add(new Node(location, 0));
        }

        while(!pQueue.isEmpty()) {
            Node current = pQueue.poll();
            int loc = current.getLocation();
            int dist = current.getDistance();
            if (loc == towards) return;

            if(distTo.get(loc) == dist)
            for(Integer next : graph.adjacentNodes(loc)) {
                if(distTo.get(next) > distTo.get(loc) + 1) {
                    distTo.set(next, distTo.get(loc) + 1);
                    pQueue.add(new Node(next, distTo.get(next)));
                }
            }
        }
    }

    public Integer getDistTo() { return distTo.get(towards); }
}
