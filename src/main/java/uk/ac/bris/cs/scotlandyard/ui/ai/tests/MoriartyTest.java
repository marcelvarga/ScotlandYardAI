package uk.ac.bris.cs.scotlandyard.ui.ai.tests;

import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Pair;
import org.junit.Test;

import org.junit.runners.Parameterized;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.Moriarty;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;


public class MoriartyTest extends TestBase{

    @Parameterized.Parameters

    @Test public void testMrXAvoidsCatchableLocationsIfPossible() {

        var mrX = new Player(MRX, makeTickets(5, 0, 0, 0, 0), 166);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 152);
        var green = new Player(BLUE, defaultDetectiveTickets(), 180);
        Move.FunctionalVisitor<Integer> getDestination = new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2);

        // only secret moves if only secret move ticket left
        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), ImmutableList.of(true)),
                mrX, blue, green);

        Ai Moriarty = new Moriarty();

        assertEquals(183,
                Optional.ofNullable(Moriarty.pickMove(state, new Pair<>(25L, TimeUnit.SECONDS)).visit(getDestination)));
    }

}
