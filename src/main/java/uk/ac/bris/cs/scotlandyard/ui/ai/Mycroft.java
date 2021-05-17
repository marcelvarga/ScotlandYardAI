package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board;

import static java.lang.Math.max;
@SuppressWarnings("unused")
public class Mycroft implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "Mycroft";
    }

    long startTime;
    long maxTime;
    MoveCache moveCache = new MoveCache();

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {

        var moves = board.getAvailableMoves().asList();
        return getBestMove(new Situation((Board.GameState) board), 3, moves.get(0).source(), timeoutPair.left());
    }

    @SuppressWarnings("SameParameterValue")
    @Nonnull
    private Move getBestMove(Situation situation, int maxDepth, int mrXLocation, long maxTime) {

        startTime = System.currentTimeMillis();
        this.maxTime = maxTime;

        // Set a random move to start off with
        Move placeholderMove = new Minimax(maxTime).filterMrXMoves(situation, mrXLocation).get(0);
        int placeholderScore = new Minimax(maxTime).score(situation.advance(placeholderMove), mrXLocation);

        // Calculate the best first move with a depth 1
        Move bestMove = mtd_f(situation, 1, mrXLocation, placeholderMove, placeholderScore);
        int bestScore = new Minimax(maxTime).score(situation.advance(bestMove), mrXLocation);

        Move move;
        int score;

        // Use iterative deepening
        for (int depth = 2; depth <= maxDepth && !timeIsUp(); depth++) {
            move = mtd_f(situation, depth, mrXLocation, bestMove, bestScore);
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            score = new Minimax(maxTime - elapsedTime).score(situation.advance(move), mrXLocation);

            // Update bestMove and bestScore if the move generated is better
            if (bestScore < score) {
                bestMove = move;
                bestScore = score;
            }
        }

        return bestMove;
    }

    private Move alphaBetaStarter(Situation situation, int depth, int alpha, int beta, int mrXLocation) {
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        Minimax minimax = new Minimax(maxTime - elapsedTime);

        ArrayList<Move> filteredMoves = minimax.filterMrXMoves(situation, mrXLocation);
        Move bestMove = filteredMoves.get(0);
        int bestScore = minimax.score(situation.advance(bestMove), mrXLocation);

        int score;

        // For every move, get the best score from the situation
        for (Move move : filteredMoves) {
            elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            //score = new Minimax(maxTime - elapsedTime ).searchBestScore(situation.advance(move), depth-1, alpha, beta, true, mrXLocation);
            score = moveCache.getScore(situation, move, depth-1, alpha, beta, mrXLocation, maxTime, elapsedTime);
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

    private Move mtd_f(Situation situation, int depth, int mrXLocation, Move bestMove, int bestScore) {
        int ceiling = 1000000000;
        int floor = -1000000000;
        int beta;

        while (floor < ceiling) {
            beta = max(bestScore, floor + 1);
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

            // Perform a null-window search on the current situation
            bestMove = alphaBetaStarter(situation, depth, beta - 1, beta, mrXLocation);
            bestScore = new Minimax(maxTime - elapsedTime).score(situation.advance(bestMove), mrXLocation);

            // Change the bounds based on the outcome
            if (bestScore < beta) {
                ceiling = bestScore;
            } else {
                floor = bestScore;
            }
        }
        return bestMove;
    }

    // Returns true if time's nearly up
    private boolean timeIsUp(){
        return (System.currentTimeMillis() - startTime > (maxTime - 5) * 1000);
    }
}