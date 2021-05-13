package uk.ac.bris.cs.scotlandyard.ui.ai.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import io.atlassian.fugue.Pair;
import org.junit.Test;

import org.junit.runners.Parameterized;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.Moriarty;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.GREEN;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
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

        /*GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, defaultMrXTickets(), 166),
                new Player(BLUE, defaultDetectiveTickets(), 7),
                new Player(GREEN, defaultDetectiveTickets(), 30));*/
        GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, defaultMrXTickets(), 166),
                new Player(BLUE, defaultDetectiveTickets(), 7),
                new Player(GREEN, defaultDetectiveTickets(), 30));
        /* working one
       GameState state = gameStateFactory.build(standard24RoundSetup(),
                new Player(MRX, makeTickets(5, 0, 0, 0, 0), 166),
                new Player(BLUE, defaultDetectiveTickets(), 151),
                new Player(GREEN, defaultDetectiveTickets(), 180));*/

        Ai Moriarty = new Moriarty();
        Move move = Moriarty.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS));
        System.out.println("wassup baby");
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

}
