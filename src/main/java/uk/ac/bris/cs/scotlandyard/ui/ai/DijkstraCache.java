package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DijkstraCache {
    Map<ArrayList<Integer>, Integer> transpositionTable;

    DijkstraCache() {
        transpositionTable = new HashMap<>();
    }

    public int getDistance(Board.GameState state, ArrayList<Integer> detectiveLocations, int mrXLocation){
        ArrayList<Integer> playerLocations = new ArrayList<>(detectiveLocations);
        playerLocations.add(mrXLocation);
        int distance;

        if (!transpositionTable.containsKey(playerLocations)) {

            distance = new Dijkstra(state.getSetup().graph, detectiveLocations, mrXLocation).getDistTo();
            transpositionTable.put(playerLocations, distance);
        }
        else
            distance = transpositionTable.get(playerLocations);

        return distance;
    }

    public int getSize(){
        return transpositionTable.size();
    }
}
