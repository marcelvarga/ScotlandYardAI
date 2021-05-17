package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")

// Helper class that stores previously seen mrX - detective arrangements and maps them to distances
public class DijkstraCache {
    Map<ArrayList<Integer>, Integer> transpositionTable;

    DijkstraCache() {
        transpositionTable = new HashMap<>();
    }

    public int getDistance(Board.GameState state, ArrayList<Integer> detectiveLocations, int mrXLocation){
        ArrayList<Integer> playerLocations = new ArrayList<>(detectiveLocations);
        playerLocations.add(mrXLocation);
        int distance;

        // If the configuration was not seen before make a new Dijkstra call
        if (!transpositionTable.containsKey(playerLocations)) {

            distance = new Dijkstra(state.getSetup().graph, detectiveLocations, mrXLocation, false).getDistToDestination();
            transpositionTable.put(playerLocations, distance);
        }
        else // Fetch the distance from the table
            distance = transpositionTable.get(playerLocations);

        return distance;
    }
}
