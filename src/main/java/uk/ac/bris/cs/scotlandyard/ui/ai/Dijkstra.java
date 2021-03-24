package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")

public class Dijkstra {
    int[] distTo = new int[200];
    public final ImmutableValueGraph<Integer, ImmutableSet<Transport>> graph;
    ArrayList<Piece> pieces;
    ArrayList<Integer> detectiveLocations;

    Dijkstra(Board board){
        Arrays.fill(distTo, 1000);

        this.graph = board.getSetup().graph;

        pieces = new ArrayList<>();
        this.pieces.addAll(board.getPlayers());
        detectiveLocations = new ArrayList<>();

        for (Piece piece : pieces){
            if(piece.isDetective()){
            Optional<Integer> location = board.getDetectiveLocation((Piece.Detective) piece);
                location.ifPresent(integer -> detectiveLocations.add(integer));
        }}
    }
}
