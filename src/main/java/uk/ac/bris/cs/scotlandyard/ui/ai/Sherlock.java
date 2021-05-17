package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Sherlock implements Ai {
    @Nonnull
    @Override
    public String name() {
        return "Sherlock";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        var moves = board.getAvailableMoves().asList();
        // Picks a random move
        return moves.get(new Random().nextInt(moves.size()));
    }
}
