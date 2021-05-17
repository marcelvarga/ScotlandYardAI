package uk.ac.bris.cs.scotlandyard.ui.ai.tests;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.Dijkstra;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.RED;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;


/**
 * Tests Dijkstra functionality
 */
public class DijkstraTest extends TestBase{

    final int DEFAULT_DISTANCE = 1000;
    @Test public void testDijkstraBreaksEarly() {
        var mrX = new Player(MRX, defaultMrXTickets(), 104);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 117);

        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), ImmutableList.of(true)),
                mrX, blue);

        Dijkstra dijkstra = new Dijkstra(state.getSetup().graph, new ArrayList<>(List.of(mrX.location())), blue.location(), true);

        Multiset<Integer> distances = HashMultiset.create();
        distances.addAll(dijkstra.getDistances());

        assert(distances.count(DEFAULT_DISTANCE) > 150);
    }
    @Test public void testDijkstraDoesNotBreakEarly() {
        var mrX = new Player(MRX, defaultMrXTickets(), 104);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 117);

        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), ImmutableList.of(true)),
                mrX, blue);

        Dijkstra dijkstra = new Dijkstra(state.getSetup().graph, new ArrayList<>(List.of(mrX.location())), blue.location(), false);

        Multiset<Integer> distances = HashMultiset.create();
        distances.addAll(dijkstra.getDistances());

        assert(distances.count(DEFAULT_DISTANCE) == 1);
    }
    @Test public void testDijkstraExactValues() {
        var mrX = new Player(MRX, defaultMrXTickets(), 176);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 177);
        var red = new Player(RED, defaultDetectiveTickets(), 189);

        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), ImmutableList.of(true)),
                mrX, blue, red);

        Dijkstra dijkstra = new Dijkstra(state.getSetup().graph, new ArrayList<>(List.of(blue.location(), red.location())), mrX.location(), false);
        ArrayList<Integer> distances = new ArrayList<>(dijkstra.getDistances());
        assert(distances.get(mrX.location()) == 1);
        assert(distances.get(blue.location()) == 0);
        assert(distances.get(red.location()) == 0);
    }
    @Test public void testDijkstraStopsWithTwoSources() {

        var mrX = new Player(MRX, defaultMrXTickets(), 176);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 177);
        var red = new Player(RED, defaultDetectiveTickets(), 189);

        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), ImmutableList.of(true)),
                mrX, blue, red);

        Dijkstra dijkstra = new Dijkstra(state.getSetup().graph, new ArrayList<>(List.of(blue.location(), red.location())), mrX.location(), true);

        Multiset<Integer> distances = HashMultiset.create();
        distances.addAll(dijkstra.getDistances());
        assert(distances.count(DEFAULT_DISTANCE) > 150);
    }
    @Test public void testDijkstraStopsWhenOneSourceFindsTarget(){

        var mrX = new Player(MRX, defaultMrXTickets(), 93);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 7);
        var red = new Player(RED, defaultDetectiveTickets(), 67);

        GameState state = gameStateFactory.build(
                new GameSetup(standardGraph(), ImmutableList.of(true)),
                mrX, blue, red);

        Dijkstra dijkstra = new Dijkstra(state.getSetup().graph, new ArrayList<>(List.of(blue.location(), red.location())), mrX.location(), true);

        ArrayList<Integer> distances = new ArrayList<>(dijkstra.getDistances());
        Multiset<Integer> distancesSet = HashMultiset.create();
        distancesSet.addAll(distances);

        assert(distancesSet.count(DEFAULT_DISTANCE) > 50);
        assert(distances.get(mrX.location()) == 2);
        assert(distances.get(blue.location()) == 0);
        assert(distances.get(red.location()) == 0);
        assert(distances.get(171) == DEFAULT_DISTANCE);
        assert(distances.get(92) == DEFAULT_DISTANCE);

    }
}
