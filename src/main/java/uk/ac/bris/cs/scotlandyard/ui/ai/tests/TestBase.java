package uk.ac.bris.cs.scotlandyard.ui.ai.tests;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Move.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move.SingleMove;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport;

import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.GREEN;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.RED;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.WHITE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.YELLOW;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.DETECTIVE_LOCATIONS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.MRX_LOCATIONS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24ROUNDS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.BUS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.SECRET;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.TAXI;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.UNDERGROUND;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;

/**
 * Base class for all tests. Contains various helper methods for convenience.
 * This is not a test class and contains no tests here.
 */
@SuppressWarnings({"SameParameterValue", "UnstableApiUsage"})
@RunWith(Parameterized.class) abstract class TestBase {

    private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> defaultGraph;

    @Parameterized.Parameter(0) public ScotlandYard.Factory<Board.GameState> gameStateFactory;
    @Parameterized.Parameter(1) public ScotlandYard.Factory<Model> modelFactory;

    @BeforeClass public static void setUp() {
        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                    "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (IOException e) { throw new RuntimeException("Unable to read game graph", e); }
    }

    /**
     * @return the default graph used in the actual game
     */
    @Nonnull static ImmutableValueGraph<Integer, ImmutableSet<Transport>> standardGraph() {
        return defaultGraph;
    }

    @Nonnull static GameSetup standard24RoundSetup() {
        return new GameSetup(defaultGraph, STANDARD24ROUNDS);
    }

    /**
     * @return a working black player with the default tickets and default location, see
     * {@link ScotlandYard#defaultMrXTickets} and {@link ScotlandYard#MRX_LOCATIONS}
     */
    @Nonnull static Player blackPlayer() {
        return new Player(MRX, defaultMrXTickets(), MRX_LOCATIONS.get(0));
    }
    /**
     * @return a working red player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player redPlayer() {
        return new Player(RED, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(1));
    }
    /**
     * @return a working green player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player greenPlayer() {
        return new Player(GREEN, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(2));
    }
    /**
     * @return a working blue player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player bluePlayer() {
        return new Player(BLUE, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(3));
    }
    /**
     * @return a working yellow player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player yellowPlayer() {
        return new Player(YELLOW, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(4));
    }
    /**
     * @return a working white player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player whitePlayer() {
        return new Player(WHITE, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(5));
    }
    /**
     * Create a map of tickets
     *
     * @param taxi amount of tickets for {@link Ticket#TAXI}
     * @param bus amount of tickets for {@link Ticket#BUS}
     * @param underground amount of tickets for {@link Ticket#UNDERGROUND}
     * @param x2 amount of tickets for {@link Ticket#DOUBLE}
     * @param secret amount of tickets for {@link Ticket#SECRET}
     * @return a {@link Map} with ticket counts; never null
     */
    @Nonnull static ImmutableMap<Ticket, Integer> makeTickets(
            int taxi, int bus, int underground, int x2, int secret) {
        return ImmutableMap.of(
                TAXI, taxi,
                BUS, bus,
                UNDERGROUND, underground,
                Ticket.DOUBLE, x2,
                Ticket.SECRET, secret);
    }

   /**
     * Creates a new {@link DoubleMove}
     *
     * @param colour colour for the move
     * @param source the source
     * @param first the first ticket
     * @param firstDestination the first destination
     * @param second the second ticket
     * @param secondDestination the second destination
     * @return a new double move
     */
    @Nonnull static DoubleMove x2(@Nonnull Piece colour, int source,
                                  Ticket first, int firstDestination,
                                  Ticket second, int secondDestination) {
        return new DoubleMove(requireNonNull(colour),
                source, requireNonNull(first), firstDestination,
                requireNonNull(second), secondDestination);
    }
    /**
     * Creates a new {@link SingleMove} of a taxi
     *
     * @param colour colour for the move
     * @param source the source
     * @param destination the destination
     * @return a new taxi ticket move
     */
    @Nonnull static SingleMove taxi(@Nonnull Piece colour, int source, int destination) {
        return new SingleMove(requireNonNull(colour), source, TAXI, destination);
    }
    /**
     * Creates a new {@link SingleMove} of a secret
     *
     * @param colour colour for the move
     * @param source the source
     * @param destination the destination
     * @return a new secret ticket move
     */
    @Nonnull static SingleMove secret(@Nonnull Piece colour, int source, int destination) {
        return new SingleMove(requireNonNull(colour), source, SECRET, destination);
    }
    /**
     * Creates a new {@link SingleMove} of a bus
     *
     * @param colour colour for the move
     * @param source the source
     * @param destination the destination
     * @return a new bus ticket move
     */
    @Nonnull static SingleMove bus(@Nonnull Piece colour, int source, int destination) {
        return new SingleMove(requireNonNull(colour), source, BUS, destination);
    }
    /**
     * Creates a new {@link SingleMove} of an underground
     *
     * @param colour colour for the move
     * @param source the source
     * @param destination the destination
     * @return a new underground ticket move
     */
    @Nonnull static SingleMove underground(@Nonnull Piece colour, int source, int destination) {
        return new SingleMove(requireNonNull(colour), source, UNDERGROUND, destination);
    }
}
