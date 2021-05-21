package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.HashMap;
import java.util.Map;

// Helper class that stores previously seen moves and maps them to scores
public class MoveCache {
    Map<Situation, Map<Move, Integer>> transpositionTable;

    MoveCache() {
        transpositionTable = new HashMap<>();
    }

    public int getScore(Situation situation, Move move, int depth, int alpha, int beta, int mrXLocation, long maxTime, long elapsedTime){

        int score;

        // If the configuration was not seen before make a new Move call
        if (!transpositionTable.containsKey(situation) || !transpositionTable.get(situation).containsKey(move)) {
            score = new Minimax(maxTime - elapsedTime ).searchBestScore(situation.advance(move), depth-1, alpha, beta, true, mrXLocation);
            HashMap<Move, Integer> tempMap = new HashMap<>();
            tempMap.put(move, score);
            transpositionTable.put(situation, tempMap);

        // Fetch the score from the table
        } else {
            score = transpositionTable.get(situation).get(move);
        }
        return score;
    }
}
