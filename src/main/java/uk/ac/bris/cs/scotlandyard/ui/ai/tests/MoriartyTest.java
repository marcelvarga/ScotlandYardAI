package uk.ac.bris.cs.scotlandyard.ui.ai.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import io.atlassian.fugue.Pair;
import org.junit.Test;

import org.junit.runners.Parameterized;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.Moriarty;
import uk.ac.bris.cs.scotlandyard.ui.ai.Situation;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;


public class MoriartyTest extends TestBase{

    Move.FunctionalVisitor<Integer> getDestination = new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2);

    @Test public void testMrXAvoidsCatchableLocationsIfPossible() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
            new Player(MRX, makeTickets(5, 0, 0, 0, 0), 166),
            new Player(BLUE, defaultDetectiveTickets(), 152),
            new Player(GREEN, defaultDetectiveTickets(), 180));

        Ai Moriarty = new Moriarty();

        assert(Integer.valueOf(183).equals(
                Moriarty.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS)).visit(getDestination)));
    }

    @Test public void testMrXIgnoresDoublesWhenFarAway() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 5, 5, 2, 2), 166),
                new Player(BLUE, defaultDetectiveTickets(), 7),
                new Player(GREEN, defaultDetectiveTickets(), 30));

        Ai Moriarty = new Moriarty();
        Move move = Moriarty.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS));
        assert(!Iterables.contains(
                Moriarty.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS)).tickets(),
                ScotlandYard.Ticket.SECRET));
    }

    @Test public void testMrXSavesSecretsOnRevealTurns() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, defaultMrXTickets(), 166),
                new Player(BLUE, defaultDetectiveTickets(), 12));

        Ai Moriarty = new Moriarty();

        assert(!Iterables.contains(
                Moriarty.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS)).tickets(),
                ScotlandYard.Ticket.SECRET));
    }

    @Test public void testMrXDoesNotReducePossibleLocationsToOneUnlessRevealTurn() {
        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), rounds(false, false, false)),
                new Player(MRX, defaultMrXTickets(), 166),
                new Player(BLUE, defaultDetectiveTickets(), 12));

        Ai Moriarty = new Moriarty();

        Situation situation = new Situation(state);

        Move bestMove = Moriarty.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS));

        int numLocations = situation.advance(bestMove).numPossibleLocations();

        assert(numLocations != 1);
    }

    @Test public void testMrXDoesNotGetStuckIfPossible() {
        GameState state = gameStateFactory.build(
                standard24RoundSetup(),
                new Player(MRX, makeTickets(1, 2, 0, 0, 0), 170),
                new Player(BLUE, defaultDetectiveTickets(), 185),
                new Player(GREEN, defaultDetectiveTickets(), 156),
                new Player(RED, defaultDetectiveTickets(), 159));

        Ai Moriarty = new Moriarty();

        Move bestMove = Moriarty.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS));

        // Moving to 159 will get MrX stuck. Detectives are placed so as to make 157 look as unappealing as possible

        assert(bestMove.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)) != 159);
    }
}
