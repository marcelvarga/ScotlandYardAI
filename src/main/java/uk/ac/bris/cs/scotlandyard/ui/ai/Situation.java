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
    // Used to prevent duplicates
    LinkedHashSet<Integer> possibleLocations;
    int currentRound;
    boolean isRevealTurn;

    // Used when initialising the Situation for the first time
    public Situation(Board.GameState state) {
        this.state = state;
        this.possibleLocations = new LinkedHashSet<>(Arrays.asList(35, 45, 51, 71, 78, 104, 106, 127, 132, 166, 170, 172));
        this.currentRound = state.getMrXTravelLog().size();
        this.isRevealTurn = isRevealTurn();
    }

    // Used when advancing a situation
    public Situation(Board.GameState state, LinkedHashSet<Integer> possibleLocations, Integer currentRound, Boolean doAdvance) {
        this.state = state;
        this.possibleLocations = possibleLocations;
        this.currentRound = currentRound + (doAdvance ? 1 : 0);
        this.isRevealTurn = isRevealTurn();
    }

    public ArrayList<Integer> possibleLocations() {
        return new ArrayList<>(possibleLocations);
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

    // Update possibleLocations based on ticket
    // Input can be either a ticket or a move, depending on which AI is using it
    private LinkedHashSet<Integer> updatePossibleLocations(Object obj) {
        if (obj instanceof Move) {
            return getPossibleLocationsWithMove((Move) obj);
        } else  {
            return getPossibleLocationsWithTicket((ScotlandYard.Ticket) obj);
        }
    }

    private LinkedHashSet<Integer> getPossibleLocationsWithMove(Move move) {
        LinkedHashSet<Integer> input = this.possibleLocations;
        LinkedHashSet<Integer> output = new LinkedHashSet<>();

        if(move.commencedBy() == MRX) {

            if (isRevealTurn) {
                //System.out.println("Resetting possible locations to " + move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)));
                return new LinkedHashSet<>(move.visit((Move.Visitor<Integer>) new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)));
            }

            return getPossibleLocationsWithTicket(Iterables.get(move.tickets(), 0));

        } else {
            input.remove(move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)));
            return input;
        }
    }


    // Only used if MrX uses the ticket
    private LinkedHashSet<Integer> getPossibleLocationsWithTicket(ScotlandYard.Ticket ticket) {
        LinkedHashSet<Integer> output = new LinkedHashSet<>();

        for (Integer location : this.possibleLocations) {
            output.addAll(getSingleMovesWithTicket(location, ticket));
        }

        return output;
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
            //if (detectiveLocations.stream().anyMatch(
            //        d -> d.isDetective() &&
            //                (Optional.get(state.getDetectiveLocation((Piece.Detective) d)) == destination))) continue;

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

    private Boolean isRevealTurn() { return state.getSetup().rounds.get(currentRound); }

    public Boolean getIsRevealTurn() { return isRevealTurn; }

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
    public boolean isRevealTurnNext(){
        return state.getSetup().rounds.get(currentRound+1);
    }
    public Situation advance(Move move) {
        // If the move is a doubleMove, update in parts
        if (move.visit(new Move.FunctionalVisitor<>(m -> false, m -> true))) {
            Iterable<ScotlandYard.Ticket> tickets = move.tickets();
            this.possibleLocations = updatePossibleLocations(Iterables.get(tickets, 0));
            return new Situation(state.advance(move), updatePossibleLocations(Iterables.get(tickets, 1)), currentRound + 1, true);
        }

        // Only advance currentRound if MRX is making a move
        if (move.commencedBy() == MRX) {
            return new Situation(state.advance(move), updatePossibleLocations(move), currentRound, true);
        } else {
            return new Situation(state.advance(move), updatePossibleLocations(move), currentRound, false);
        }
    }
}
