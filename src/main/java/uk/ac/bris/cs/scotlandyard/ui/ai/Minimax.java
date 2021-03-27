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

    Minimax(Board.GameState gameState, int steps, int mrXInitialLocation){
        this.gameState = gameState;
        this.steps = steps;
        this.mrXInitialLocation = mrXInitialLocation;

    }

    Pair searchBestMove(Board.GameState state, int depth, boolean isMrX, int mrXLocation, Move move){
        if (depth == 0 || !state.getWinner().isEmpty())
            return new Pair(move, score(state, mrXLocation));

        if(isMrX) {
            Pair maxEval = new Pair(null, minusInfinity);
            for(Move availableMove : state.getAvailableMoves()) {
                Pair eval = searchBestMove(
                        state.advance(availableMove),
                        depth - 1,
                        false,
                        availableMove.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2)),
                        availableMove);
                if(maxEval.getScore() < eval.getScore()) {
                    maxEval.setScore(eval.getScore());
                    maxEval.setMove(eval.getMove());
                }
            }
            return maxEval;
        }
        else {
            Pair minEval = new Pair(null, plusInfinity);
            for(Move availableMove : state.getAvailableMoves()) {
                Pair eval = searchBestMove(
                        state.advance(availableMove),
                        depth - 1,
                        true,
                        mrXLocation,
                        availableMove);
                if(minEval.getScore() > eval.getScore()) {
                    minEval.setScore(eval.getScore());
                    minEval.setMove(eval.getMove());
                }
            }
            return minEval;
        }
    }


    private int score(Board.GameState state, int mrXLocation){
        return (new Dijkstra(state, mrXLocation).getDistToMrX());
    }
    public Move getBestMove(){
        Pair bestMove = searchBestMove(gameState, steps, true, mrXInitialLocation, null);
        return bestMove.getMove();
    }
}
