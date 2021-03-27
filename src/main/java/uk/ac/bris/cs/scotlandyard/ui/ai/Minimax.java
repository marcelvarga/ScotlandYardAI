package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.model.*;

public class Minimax {

    private class Pair{
        private Move move;
        private int score;

        Pair(Move move, int score){
            this.move = move;
            this.score = score;
        }

        public Move getMove() {
            return move;
        }

        public int getScore() {
            return score;
        }

        public void setMove(Move move) {
            this.move = move;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }

    Board.GameState gameState;
    int steps;
    int mrXInitialLocation;
    final int minusInfinity = -10000;
    final int plusInfinity  = +10000;
    Move bestMove;

    Minimax(Board.GameState gameState, int steps, int mrXInitialLocation){
        this.gameState = gameState;
        this.steps = steps;
        this.mrXInitialLocation = mrXInitialLocation;

    }

    int searchBestScore(Board.GameState state, int depth, boolean isMrX, int mrXLocation){
        if (depth == 0 || !state.getWinner().isEmpty())
            return score(state, mrXLocation);

        if(isMrX) {
            int maxEval = minusInfinity;
            for(Move currMove : state.getAvailableMoves())
                if(currMove.visit(new Move.FunctionalVisitor<>(m -> true, m -> true))){
                int eval = searchBestScore(
                        state.advance(currMove),
                        depth - 1,
                        false,
                        getDest(currMove));
                if(maxEval < eval) {
                    maxEval = eval;
                    bestMove = currMove;
                }
            }
            return maxEval;
        }
        else {
            int minEval = plusInfinity;
            for (Move currMove : state.getAvailableMoves()){
                int eval = searchBestScore(
                        state.advance(currMove),
                        depth - 1,
                        true,
                        mrXLocation);
                if (minEval >= eval) {
                    minEval = eval;
                }
            }
            return minEval;
        }
    }


    private int score(Board.GameState state, int mrXLocation){

        return (new Dijkstra(state, mrXLocation).getDistToMrX()) + state.getAvailableMoves().size();
    }

    public Move getBestMove(){
        searchBestScore(gameState, 2, true, mrXInitialLocation);
        return bestMove;
    }
    private int getDest(Move move) {
        return move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2));
    }
}
