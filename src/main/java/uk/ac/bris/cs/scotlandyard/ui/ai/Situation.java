package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.esotericsoftware.minlog.Log;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.SECRET;

@SuppressWarnings("UnstableApiUsage")

// Creates possible locations of Moriarty
// Used by Moriarty to optimise number of possible locations
// Used by Sherlock to aid in capture

public class Situation {
    Board.GameState state;
    ArrayList<Integer> possibleLocations;

    // Used when initialising the Situation for the first time
    public Situation(Board.GameState state) {
        this.state = state;
        this.possibleLocations = new ArrayList<>(Arrays.asList(35, 45, 51, 71, 78, 104, 106, 127, 132, 166, 170, 172));
    }

    // Used when advancing a situation
    public Situation(Board.GameState state, ArrayList<Integer> possibleLocations) {
        this.state = state;
        this.possibleLocations = possibleLocations;
    }

    public ArrayList<Integer> possibleLocations() {
        return possibleLocations;
    }

    public int numPossibleLocations() {
        return possibleLocations.size();
    }

    /*public ArrayList<Integer> computePossibleLocations() {
        ImmutableList<LogEntry> log = state.getMrXTravelLog();
        Collections.reverse(log);
        // Work backwards until it's a reveal turn
        for(LogEntry e : state.getMrXTravelLog()) {
            if (e.location().isPresent()) {
                // Go forwards computing possible locations

            }
        }
    }*/

    private ArrayList<Integer> updatePossibleLocations(Move move) {
        ArrayList<Integer> input = this.possibleLocations;
        ArrayList<Integer> output = new ArrayList<>();

        if(move.commencedBy() == MRX) {
            for (Integer location : input) {
                output.addAll(getSingleMovesWithTicket(location, Iterables.get(move.tickets(), 0)));
            }
            output.addAll(input);
            return output;
        } else {
           input.remove(move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)));
           return input;

        }
    }

    public ImmutableSet<Piece> getWinner(){
        return state.getWinner();
    }

    private ArrayList<Integer> getSingleMovesWithTicket(int source, ScotlandYard.Ticket ticket) {
        ArrayList<Integer> output = new ArrayList<>();
        GameSetup setup = state.getSetup();
        ImmutableSet<Piece> detectiveLocations = state.getPlayers();

        for (int destination : setup.graph.adjacentNodes(source)) {

            // You cannot move onto a detective
            //if (detectiveLocations.stream().anyMatch(d -> d.isDetective()
            //          && (Optional.get(state.getDetectiveLocation((Piece.Detective) d)) == destination))) continue;

            // Secret tickets can move you anywhere
            if (ticket == SECRET) {
                output.add(destination);
                continue;
            }

            // You must have the given ticket
            for (ScotlandYard.Transport transport : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
                if (transport.requiredTicket() == ticket)  {
                    output.add(destination);
                }
            }
        }
        return output;
    }

    public ImmutableSet<Move> getAvailableMoves() {
        return state.getAvailableMoves();
    }

    public GameSetup getSetup() {
        return state.getSetup();
    }

    public Optional<Integer> getDetectiveLocation(Piece.Detective d) {
        return state.getDetectiveLocation(d);
    }

    public ImmutableSet<Piece> getPlayers() {
        return state.getPlayers();
    }

    public Board.GameState getState() {
        return state;
    }

    // Use progress instead of advance to update possible locations
    public Situation advance(Move move) {
        return new Situation(state.advance(move), updatePossibleLocations(move));
    }
}
