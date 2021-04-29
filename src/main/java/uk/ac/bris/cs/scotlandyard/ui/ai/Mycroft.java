package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board;

import static java.lang.Math.max;

public class Mycroft implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "Mycroft";
    }

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {

        var moves = board.getAvailableMoves().asList();
        return getBestMove(new Situation((Board.GameState) board), 3, moves.get(0).source(), timeoutPair.left());
    }

    @Nonnull
    private Move getBestMove(Situation situation, int maxDepth, int mrXLocation, long timeLeft) {

        // Guess the best move
        // Currently picks one at random
        var moves = situation.getAvailableMoves().asList();

        // Set a random move to start off with
        Move placeholderMove = moves.get(new Random().nextInt(moves.size()));
        int placeholderScore = new Minimax().score(situation.advance(placeholderMove), mrXLocation, situation.getAvailableMoves().size());

        // Calculate the best first move with a depth 1
        Move bestMove = mtd_f(situation, 1, mrXLocation, moves.size(), placeholderMove, placeholderScore);
        int bestScore = new Minimax().score(situation.advance(bestMove), mrXLocation, situation.getAvailableMoves().size());

        Move move;
        int score;

        // Use iterative deepening
        for (int depth = 2; depth <= maxDepth; depth++) {
            System.out.println("Depth: " + depth);
            move = mtd_f(situation, depth, mrXLocation, moves.size(), bestMove, bestScore);
            score = new Minimax().score(situation.advance(bestMove), mrXLocation, situation.getAvailableMoves().size());

            if (bestScore < score) {
                bestMove = move;
                bestScore = score;
            }
        }

        return bestMove;
    }

    private Move alphaBetaMemorise(Situation situation, int depth, int alpha, int beta, boolean isMrX, int mrXLocation, int numMoves) {
        ImmutableSet<Move> possibleMoves = situation.getAvailableMoves();
        Move bestMove = possibleMoves.iterator().next();
        int bestScore = new Minimax().score(situation.advance(bestMove), mrXLocation, situation.getAvailableMoves().size());;
        int score;

        for (Move move : situation.getAvailableMoves()) {
            score = new Minimax().searchBestScore(situation.advance(move), depth-1, alpha, beta, isMrX, mrXLocation, numMoves);
            alpha = max(alpha, score);

            // If the score is better, set the move as the best one
            if (score > bestScore) {
                System.out.println("Found better move with score: " + score);
                bestScore = score;
                bestMove = move;

                // Add memory in here
            }
        }

        return bestMove;
    }

    private Move mtd_f(Situation situation, int depth, int mrXLocation, int numMoves, Move bestMove, int bestScore) {
        int ceiling = 1000000000;
        int floor = -1000000000;
        int beta;

        while (floor < ceiling) {
            beta = max(bestScore, floor + 1);
            bestMove = alphaBetaMemorise(situation, depth, beta - 1, beta, true, mrXLocation, numMoves);
            bestScore = new Minimax().score(situation.advance(bestMove), mrXLocation, situation.getAvailableMoves().size());

            if (bestScore < beta) {
                ceiling = bestScore;
            } else {
                floor = bestScore;
            }
        }
        return bestMove;
    }
}