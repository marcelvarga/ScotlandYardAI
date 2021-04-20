package uk.ac.bris.cs.scotlandyard.ui.ai;

// Creates possible locations of Moriarty
// Used by Moriarty to optimise number of possible locations
// Used by Sherlock to aid in capture

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.LogEntry;

import java.util.ArrayList;
import java.util.Optional;

public class PossibleLocations {
    ArrayList<Integer> possibleLocations;
    ImmutableList<LogEntry> log;

    PossibleLocations(ImmutableList<LogEntry> log) {
        this.log = log;
        this.possibleLocations = getPossibleLocations();
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
