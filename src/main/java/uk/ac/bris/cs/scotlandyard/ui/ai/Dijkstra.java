package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport;

import java.lang.reflect.Array;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")

public class Dijkstra {
    ArrayList<Integer> distTo;
    public ImmutableValueGraph<Integer, ImmutableSet<Transport>> graph;
    ArrayList<Piece> pieces;
    ArrayList<Integer> detectiveLocations;
    PriorityQueue<Node> pQueue;

    Dijkstra(Board board){
        distTo = new ArrayList<>(Collections.nCopies(200, 1000));

        this.graph = board.getSetup().graph;
        pieces = new ArrayList<>();
        this.pieces.addAll(board.getPlayers());

        detectiveLocations = getDetectiveLocations(pieces, board);

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

    public ArrayList<Integer> getDistTo() {
        return distTo;
    }

    public ArrayList<Integer> getDetectiveLocations(ArrayList<Piece> pieces, Board board) {
        detectiveLocations = new ArrayList<>();

        for (Piece piece : pieces)
            if(piece.isDetective()){
                Optional<Integer> location = board.getDetectiveLocation((Piece.Detective) piece);
                location.ifPresent(integer -> detectiveLocations.add(integer));
            }
        return detectiveLocations;
    }
}
