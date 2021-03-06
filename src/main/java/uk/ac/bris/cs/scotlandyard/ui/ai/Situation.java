package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.SECRET;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.TAXI;

@SuppressWarnings("UnstableApiUsage")

// Wrapper of the GameState class
// Creates possible locations of Moriarty
// Used by Moriarty to optimise number of possible locations
// Used by Sherlock to aid in capture

public class Situation{
    private final Board.GameState state;

    // Used to prevent duplicates
    private LinkedHashSet<Integer> possibleLocations;

    private final int currentRound;
    private final boolean isRevealTurn;

    // Used when initialising the Situation for the first time
    public Situation(Board.GameState state) {
        this.state = state;
        this.currentRound = state.getMrXTravelLog().size();
        this.possibleLocations = computePossibleLocations();
        this.isRevealTurn = isRevealTurn();
    }

    // Used when advancing a situation
    public Situation(Board.GameState state, LinkedHashSet<Integer> possibleLocations) {
        this.state = state;
        this.currentRound = state.getMrXTravelLog().size() - 1;
        this.possibleLocations = possibleLocations;
        this.isRevealTurn = isRevealTurn();
    }

    // Returns a set of possible locations based on current board positions
    // Used when initializing a situation
    private LinkedHashSet<Integer> computePossibleLocations() {

        ImmutableList<Boolean> round = state.getSetup().rounds;
        int lastReveal = round.subList(0, currentRound).lastIndexOf(true);
        LinkedHashSet<Integer> possibleLocations;

        // If no reveal turn has occurred yet
        if (lastReveal == -1) {
            // Set possible locations to the game's default
            possibleLocations = new LinkedHashSet<>(List.of(35, 45, 51, 71, 78, 104, 106, 127, 132, 166, 170, 172));
            lastReveal = 0;
        } else {
            possibleLocations = new LinkedHashSet<>((List.of(state.getMrXTravelLog().get(lastReveal).location().orElse(0))));
        }

        LinkedHashSet<Integer> someLocations = new LinkedHashSet<>();

        // Work forward from the last reveal turn, computing possible locations as it goes using mrX's log
        for (LogEntry l : state.getMrXTravelLog().subList(lastReveal, state.getMrXTravelLog().size())) {
            for (Integer location : possibleLocations)
                someLocations.addAll(getSingleMovesWithTicket(location, l.ticket()));
            possibleLocations.addAll(someLocations);
        }
        return possibleLocations;
    }

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
        LinkedHashSet<Integer> allPossibleLocations = this.possibleLocations;

        if(move.commencedBy() == MRX) {

            if (isRevealTurn) {
                //noinspection RedundantCast
                return new LinkedHashSet<>(List.of(
                        move.visit((Move.Visitor<Integer>) new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2))
                ));
            }

            return getPossibleLocationsWithTicket(Iterables.get(move.tickets(), 0));

        } else {
            allPossibleLocations.remove(move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)));
            return allPossibleLocations;
        }
    }

    // Only used if MrX uses the ticket
    private LinkedHashSet<Integer> getPossibleLocationsWithTicket(ScotlandYard.Ticket ticket) {
        LinkedHashSet<Integer> output = new LinkedHashSet<>();
        if(numPossibleLocations() == 200 && ticket == TAXI)  return this.possibleLocations;

        for (Integer location : this.possibleLocations) {
            output.addAll(getSingleMovesWithTicket(location, ticket));
        }
        return output;
    }
    private ArrayList<Integer> getSingleMovesWithTicket(int source, ScotlandYard.Ticket ticket) {
        ArrayList<Integer> output = new ArrayList<>();
        GameSetup setup = state.getSetup();
        ImmutableSet<Piece> detectiveLocations = state.getPlayers();

        for (int destination : setup.graph.adjacentNodes(source)) {

            if (detectiveLocations.stream().anyMatch(
                    d -> d.isDetective() && (state.getDetectiveLocation((Piece.Detective) d).orElse(0) == destination))) continue;

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

    public int numPossibleLocations() {
        return possibleLocations.size();
    }

    // Used for testing
    public ArrayList<Integer> getPossibleLocations() {
        return new ArrayList<>(possibleLocations);
    }

    // Helper methods //
    private boolean isRevealTurn() {
        if(currentRound == state.getSetup().rounds.size()) return false;
        return state.getSetup().rounds.get(currentRound);
    }
    public boolean isRevealTurnNext(){
        if(currentRound == state.getSetup().rounds.size()) return false;
        return state.getSetup().rounds.get(currentRound);
    }
    public boolean isRevealTurnNextNext(){
        if(currentRound == state.getSetup().rounds.size() - 1) return false;
        return state.getSetup().rounds.get(currentRound+1);
    }

    // Wrapping GameState //
    public Situation advance(Move move) {
        // If the move is a doubleMove, update in parts
        if (move.visit(new Move.FunctionalVisitor<>(m -> false, m -> true))) {
            Iterable<ScotlandYard.Ticket> tickets = move.tickets();
            ScotlandYard.Ticket t1 = Iterables.get(tickets, 0);
            ScotlandYard.Ticket t2 = Iterables.get(tickets, 1);
            this.possibleLocations = updatePossibleLocations(t1);
            this.possibleLocations = updatePossibleLocations(t2);
            return new Situation(state.advance(move), this.possibleLocations);
        }

        return new Situation(state.advance(move), updatePossibleLocations(move));
    }
    public ImmutableSet<Piece> getWinner(){ return state.getWinner(); }
    public ImmutableSet<Move> getAvailableMoves() { return state.getAvailableMoves(); }
    public Optional<Integer> getDetectiveLocation(Piece.Detective d) { return state.getDetectiveLocation(d);}
    public ImmutableSet<Piece> getPlayers() { return state.getPlayers();}
    public Board.GameState getState() { return state;}
}