package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")

// Creates possible locations of Moriarty
// Used by Moriarty to optimise number of possible locations
// Used by Sherlock to aid in capture

public class PossibleLocations {
    ArrayList<Integer> possibleLocations;
    ImmutableList<LogEntry> log;
    GameSetup setup;

    PossibleLocations(Board.GameState state) {
        this.log = state.getMrXTravelLog();
        this.possibleLocations = getPossibleLocations();
        this.setup = state.getSetup();
    }

    private ImmutableList<LogEntry> getLog() {
        return this.log;
    }

    public ArrayList<Integer> possibleLocations() {
        return possibleLocations;
    }

    public int numPossibleLocations() {
        return possibleLocations.size();
    }

    public ArrayList<Integer> getPossibleLocations() {
        ImmutableList<LogEntry> log = getLog();
        int revealTurn = 0;

        // Search for the latest reveal turn
        for (int i = log.size() - 1; i >= 3; i--) {
            if (log.get(i).location().isPresent()) {
                revealTurn = i;
                break;
            }
            // If the log is less than three moves, the latest reveal turn is 0
            revealTurn = 0;
        }

        // This would be very inaccurate, as detective locations could have closed off certain destinations in the past

        ArrayList<Integer> possibleLocations = new ArrayList<>();
        possibleLocations.add(log.get(revealTurn).location().orElse(0));

        for (int j = 0; j <= revealTurn; j++) {
            for (int location : possibleLocations) {
                return null;
            }
        }
        return possibleLocations;
    }
}
