package uk.ac.bris.cs.scotlandyard.ui.ai.tests;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Model;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24ROUNDS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;

/**
 * Base class for all tests. Contains various helper methods for convenience.
 * Copied from the model
 */

@SuppressWarnings({"DefaultAnnotationParam", "SameParameterValue", "UnstableApiUsage"})
@RunWith(Parameterized.class)
abstract class TestBase {

    private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> defaultGraph;
    @Parameterized.Parameter(0)
    public ScotlandYard.Factory<Board.GameState> gameStateFactory;
    @Parameterized.Parameter(1)
    public ScotlandYard.Factory<Model> modelFactory;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<ScotlandYard.Factory<?>[]> data() {
        return uk.ac.bris.cs.scotlandyard.ai.ModelFactories.factories().stream()
                .map(a -> new ScotlandYard.Factory<?>[]{a.getKey().get(), a.getValue().get()})
                .collect(ImmutableList.toImmutableList());
    }

    @BeforeClass
    public static void setUp() {
        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                    "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read game graph", e);
        }
    }

    /**
     * @return the default graph used in the actual game
     */
    @Nonnull
    static ImmutableValueGraph<Integer, ImmutableSet<Transport>> standardGraph() {
        return defaultGraph;
    }

    @Nonnull
    static GameSetup standard24RoundSetup() {
        return new GameSetup(defaultGraph, STANDARD24ROUNDS);
    }

    /**
     * Create a map of tickets
     *
     * @param taxi        amount of tickets for {@link Ticket#TAXI}
     * @param bus         amount of tickets for {@link Ticket#BUS}
     * @param underground amount of tickets for {@link Ticket#UNDERGROUND}
     * @param x2          amount of tickets for {@link Ticket#DOUBLE}
     * @param secret      amount of tickets for {@link Ticket#SECRET}
     * @return a {@link Map} with ticket counts; never null
     */
    @Nonnull
    static ImmutableMap<Ticket, Integer> makeTickets(
            int taxi, int bus, int underground, int x2, int secret) {
        return ImmutableMap.of(
                TAXI, taxi,
                BUS, bus,
                UNDERGROUND, underground,
                Ticket.DOUBLE, x2,
                Ticket.SECRET, secret);
    }

    /**
     * @param rounds the reveal/hidden rounds as a boolean
     * @return a list of rounds
     */
    @Nonnull
    static ImmutableList<Boolean> rounds(Boolean... rounds) {
        return ImmutableList.copyOf(rounds);
    }

}
