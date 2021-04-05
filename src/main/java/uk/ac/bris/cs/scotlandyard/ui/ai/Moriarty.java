package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board;

public class Moriarty implements Ai {

	@Nonnull @Override public String name() { return "Moriarty"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {

		var moves = board.getAvailableMoves().asList();

		//bestNode is now the node with the most optimistic outcome
		if(!moves.get(0).commencedBy().isMrX()) throw new IllegalArgumentException("It's not mrX's turn!");

		System.out.println("--------------------------------------- New call --------------------------------------------------------------------");
		return new Minimax().getBestMove((Board.GameState) board, 5, moves.get(0).source(), timeoutPair.left());
		//return new Minimax((Board.GameState) board, 2, moves.get(0).source(), timeoutPair.left()).getBestMove();
	}
}
