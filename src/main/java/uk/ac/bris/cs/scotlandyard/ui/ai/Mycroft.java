package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

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
        return getBestMove((Board.GameState) board, 3, moves.get(0).source(), timeoutPair.left());
    }

    @Nonnull
    private Move getBestMove(Board.GameState board, int maxDepth, int mrXLocation, long timeLeft) {

        // Guess the best move
        // Currently picks one at random
        var moves = board.getAvailableMoves().asList();
        Move bestMove = moves.get(new Random().nextInt(moves.size()));

        int bestScore = new Minimax().score(board.advance(bestMove), mrXLocation, board.getAvailableMoves().size());

        for (int depth = 2; depth <= maxDepth; depth++) {
            System.out.println("Depth: " + depth);
            bestMove = mtd_f(board, depth, mrXLocation, moves.size(), bestMove, bestScore);
            bestScore = new Minimax().score(board.advance(bestMove), mrXLocation, board.getAvailableMoves().size());
        }

        return bestMove;
    }

    private Move alphaBetaMemorise(Board.GameState board, int depth, int alpha, int beta, boolean isMrX, int mrXLocation, int numMoves) {
        Move bestMove = null;
        int bestScore = 0;
        int score;

        for (Move move : board.getAvailableMoves()) {
            score = new Minimax().searchBestScore(board.advance(move), depth, alpha, beta, isMrX, mrXLocation, numMoves);
            alpha = max(alpha, score);

            // If the score is better, set the move as the best one
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;

                // Add memory in here
            }
        }

        return bestMove;
    }

    private Move mtd_f(Board.GameState board, int depth, int mrXLocation, int numMoves, Move bestMove, int bestScore) {
        int ceiling = 1000000000;
        int floor = -1000000000;
        int beta;

        while (floor < ceiling) {
            beta = max(bestScore, floor + 1);
            bestMove = alphaBetaMemorise(board, depth, beta - 1, beta, true, mrXLocation, numMoves);
            bestScore = new Minimax().score(board.advance(bestMove), mrXLocation, board.getAvailableMoves().size());

            if (bestScore < beta) {
                ceiling = bestScore;
            } else {
                floor = bestScore;
            }
        }
        return bestMove;
    }
}
