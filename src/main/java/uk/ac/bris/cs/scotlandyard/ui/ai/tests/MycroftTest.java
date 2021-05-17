package uk.ac.bris.cs.scotlandyard.ui.ai.tests;

import com.google.common.collect.Iterables;
import io.atlassian.fugue.Pair;
import org.junit.Test;

import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.Mycroft;
import uk.ac.bris.cs.scotlandyard.ui.ai.Situation;

import java.util.concurrent.TimeUnit;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;

@SuppressWarnings("UnstableApiUsage")

public class MycroftTest extends TestBase{

    Move.FunctionalVisitor<Integer> getDestination = new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2);

    @Test public void testMrXAvoidsCatchableLocationsIfPossible() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 0, 0, 0, 0), 166),
                new Player(BLUE, defaultDetectiveTickets(), 152),
                new Player(GREEN, defaultDetectiveTickets(), 180));

        Ai Mycroft = new Mycroft();

        assert(Integer.valueOf(183).equals(
                Mycroft.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS)).visit(getDestination)));
    }

    @Test public void testMrXIgnoresDoublesWhenFarAway() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 5, 5, 2, 2), 166),
                new Player(BLUE, defaultDetectiveTickets(), 7),
                new Player(GREEN, defaultDetectiveTickets(), 30));

        Ai mycroft = new Mycroft();

        assert(mycroft.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS)).
                visit(new Move.FunctionalVisitor<>(m -> true, m-> false)));
    }

    @Test public void testMrXSavesSecretsOnRevealTurns() {

        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), rounds(true, false, false)),
                new Player(MRX, makeTickets(0, 1, 0, 2, 2), 165),
                new Player(BLUE, defaultDetectiveTickets(), 12));

        Ai Mycroft = new Mycroft();
        Move move = Mycroft.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS));
        System.out.println(move.toString());
        assert(Mycroft.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS)).
                tickets().iterator().next() !=
                    ScotlandYard.Ticket.SECRET);
    }

    @Test public void testMrXDoesNotReducePossibleLocationsToOneUnlessRevealTurn() {
        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), rounds(false, false, false)),
                new Player(MRX, defaultMrXTickets(), 166),
                new Player(BLUE, defaultDetectiveTickets(), 12));

        Ai Mycroft = new Mycroft();

        Situation situation = new Situation(state);

        Move bestMove = Mycroft.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS));

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

        Ai Mycroft = new Mycroft();

        Move bestMove = Mycroft.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS));

        // Moving to 159 will get MrX stuck. Detectives are placed so as to make 157 look as unappealing as possible

        assert(bestMove.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)) != 159);
    }
}
