package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;

public class Minimax {


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

    private int searchBestScore(Board.GameState state, int depth, boolean isMrX, int mrXLocation){
        if (depth == 0 || !state.getWinner().isEmpty())
            return score(state, mrXLocation);

        if(isMrX) {
            int maxEval = minusInfinity;
            ArrayList<Move> movesToCheck = movesFilter(state, mrXLocation);
            for(Move currMove : movesToCheck)
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
            boolean isLastPlayer = checkIfLastPlayer(state);
            int changeDepth = 0;
            if(isLastPlayer) changeDepth = 1;

            for (Move currMove : state.getAvailableMoves()){
                int eval = searchBestScore(
                        state.advance(currMove),
                        depth - changeDepth,
                        isLastPlayer,
                        mrXLocation);
                if (minEval >= eval) {
                    minEval = eval;
                }
            }
            return minEval;
        }
    }


    private int score(Board.GameState state, int mrXLocation){
        ArrayList<Move> moves = new ArrayList<>(state.getAvailableMoves().asList());
        for(Move move : moves)
            assert(move.commencedBy().isMrX());
        return 100 * (new Dijkstra(state, mrXLocation).getDistToMrX()) + state.getAvailableMoves().size();
    }

    public Move getBestMove(){
        searchBestScore(gameState, 2, true, mrXInitialLocation);
        return bestMove;
    }

    private int getDest(Move move) {
        return move.visit(new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2));
    }

    // Checks the available moves of the state to see if there are other players left to make a move
    // In the current round
    private boolean checkIfLastPlayer(Board.GameState state) {
        ArrayList<Move> moves = new ArrayList<>(state.getAvailableMoves().asList());
        if (moves.isEmpty()) return true;
        Piece firstPiece = moves.get(0).commencedBy();
        for (Move move : moves){
            if (move.commencedBy() != firstPiece) return false;
        }
        return true;
    }

    private ArrayList<Move> movesFilter(Board.GameState state, int mrXLocation){
        ArrayList<Move> allMoves = new ArrayList<>(state.getAvailableMoves().asList());
        ArrayList<Move> temp = new ArrayList<>();

        // Omit doubleMoves if mrX isn't close to being caught (detective more than 2 nodes away)
        if(new Dijkstra(state, mrXLocation).getDistToMrX() > 2){
            for(Move move : allMoves){
                boolean isSingleMove = move.visit(new Move.FunctionalVisitor<>(m -> true, m -> false));
                if(isSingleMove)
                temp.add(move);
            }
            return temp;
        }
        return allMoves;
    }
}
