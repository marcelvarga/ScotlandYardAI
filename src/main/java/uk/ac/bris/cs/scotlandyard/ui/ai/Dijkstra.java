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

    private ArrayList<Integer> distTo;
    private ImmutableValueGraph<Integer, ImmutableSet<Transport>> graph;
    private ArrayList<Piece> pieces;
    private ArrayList<Integer> detectiveLocations;
    private int mrXLocation;
    private PriorityQueue<Node> pQueue;
    private Board board;

    Dijkstra(Board board, Integer mrXLocation){
        distTo = new ArrayList<>(Collections.nCopies(200, 1000));
        this.board = board;
        this.graph = board.getSetup().graph;
        pieces = new ArrayList<>();
        this.pieces.addAll(board.getPlayers());
        this.mrXLocation = mrXLocation;
        detectiveLocations = getDetectiveLocations();

        //Generate a priority queue that stores detective locations, and their distance "travelled"
        this.pQueue = new PriorityQueue<>();
        for(int location : detectiveLocations){
            distTo.set(location, 0);
            pQueue.add(new Node(location, 0));
        }

        while(!pQueue.isEmpty()) {
            Node current = pQueue.poll();
            int loc = current.getLocation();
            int dist = current.getDistance();

            if(distTo.get(loc) == dist)
            for(Integer next : graph.adjacentNodes(loc)) {
                if(distTo.get(next) > distTo.get(loc) + 1) {
                    distTo.set(next, distTo.get(loc) + 1);
                    pQueue.add(new Node(next, distTo.get(next)));
                }
            }
        }
    }

    public ArrayList<Integer> getDistTo() { return distTo; }
    public Integer getDistToMrX() { return distTo.get(mrXLocation); }

    private ArrayList<Integer> getDetectiveLocations() {
        detectiveLocations = new ArrayList<>();

        for (Piece piece : pieces)
            if(piece.isDetective()){
                Optional<Integer> location = board.getDetectiveLocation((Piece.Detective) piece);
                location.ifPresent(integer -> detectiveLocations.add(integer));
            }
        return detectiveLocations;
    }
}
