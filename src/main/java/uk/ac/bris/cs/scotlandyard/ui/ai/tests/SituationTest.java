package uk.ac.bris.cs.scotlandyard.ui.ai.tests;

import org.junit.Test;

import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.Situation;

import java.util.Arrays;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;


public class SituationTest extends TestBase {

    @Test public void testSituationInitializes() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 0, 0, 0, 0), 166),
                new Player(BLUE, defaultDetectiveTickets(), 155));

        new Situation(state);
    }

    @Test public void testSituationInitializesCorrectly() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 0, 0, 0, 0), 166),
                new Player(BLUE, defaultDetectiveTickets(), 155));

        Situation s = new Situation(state);

        assert(s.numPossibleLocations() == 12);
    }

    @Test public void testSituationAdvancesCorrectlyWithSingleMoves() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 5, 5, 0, 0), 166),
                new Player(BLUE, defaultDetectiveTickets(), 155));

        Situation s = new Situation(state);
        s = s.advance(new Move.SingleMove(MRX, 166, ScotlandYard.Ticket.TAXI, 183));

        assert(s.getPossibleLocations().containsAll(Arrays.asList(
                22, 32, 36, 38, 39, 46, 48, 52, 55, 58, 59, 60, 61, 65, 67, 68, 70, 72, 77, 79, 86, 89, 97, 105, 107, 114, 115, 116, 126, 128, 133, 134, 140, 151, 153, 157, 159, 181, 183, 185, 187
        )));
    }

    @Test public void testSituationAdvancesCorrectlyWithDoubleMoves() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 5, 5, 5, 0), 166),
                new Player(BLUE, defaultDetectiveTickets(), 155));

        Situation s = new Situation(state);
        s = s.advance(new Move.DoubleMove(MRX, 166, ScotlandYard.Ticket.TAXI, 153, ScotlandYard.Ticket.UNDERGROUND,163));

        assert(s.getPossibleLocations().containsAll(Arrays.asList(
                1, 13, 46, 67, 74, 79, 89, 93, 111, 128, 140, 153, 163, 185
        )));
    }

    @Test public void testSituationAdvancesCorrectlyWithSecretMoves() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 5, 5, 5, 5), 132),
                new Player(BLUE, defaultDetectiveTickets(), 155));

        Situation s = new Situation(state);
        s = s.advance(new Move.SingleMove(MRX, 132, ScotlandYard.Ticket.SECRET, 114));

        assert(s.getPossibleLocations().containsAll(Arrays.asList(
                114, 140
        )));
    }

    @Test public void testSituationRecognisesRevealTurn() {

        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 5, 5, 5, 5), 166),
                new Player(BLUE, defaultDetectiveTickets(), 156));

        Situation s = new Situation(state);
        s = s.advance(new Move.DoubleMove(MRX, 166, ScotlandYard.Ticket.TAXI, 153, ScotlandYard.Ticket.UNDERGROUND,163));
        s = s.advance(new Move.SingleMove(BLUE, 156, ScotlandYard.Ticket.TAXI, 157));
        s = s.advance(new Move.SingleMove(MRX, 163, ScotlandYard.Ticket.UNDERGROUND, 153));

        System.out.println(s.getPossibleLocations());

        assert(s.getPossibleLocations().contains(153));
    }
}
