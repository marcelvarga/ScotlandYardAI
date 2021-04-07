package uk.ac.bris.cs.scotlandyard.ui.ai.tests;

import uk.ac.bris.cs.scotlandyard.ui.ai.Moriarty;

class MoriartyTest {

    @org.junit.jupiter.api.Test
    void name() {
        Moriarty m = new Moriarty();
        assert(m.name().equals("Moriarty"));
    }

    @org.junit.jupiter.api.Test
    void pickMove() {
        //This is practically untestable in most situations
    }
}