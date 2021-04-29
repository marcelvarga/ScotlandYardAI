package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import uk.ac.bris.cs.scotlandyard.model.*;
import static uk.ac.bris.cs.scotlandyard.model.Move.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.SECRET;

// Creates possible locations of Moriarty
// Used by Moriarty to optimise number of possible locations
// Used by Sherlock to aid in capture

public class Situation {
    Board.GameState state;
    ArrayList<Integer> possibleLocations;

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

    private ArrayList<Integer> updatePossibleLocations(Move move) {
        ArrayList<Integer> output = this.possibleLocations;
        if(move.commencedBy() == MRX) {
            for (Integer location : this.possibleLocations) {
                output.addAll(getSingleMovesWithTicket(location, Iterables.get(move.tickets(), 0)));
            }
        } else {
            this.possibleLocations.remove(move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)));

        }
        return output;
    }

    private ArrayList<Integer> getSingleMovesWithTicket(int source, ScotlandYard.Ticket ticket) {
        ArrayList<Integer> output = new ArrayList();
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

    // Use progress instead of advance to update possible locations
    public Situation progress(Move move) {
        return new Situation(state.advance(move), updatePossibleLocations(move));
    }
}
