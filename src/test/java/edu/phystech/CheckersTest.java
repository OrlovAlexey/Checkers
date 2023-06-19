package edu.phystech;

import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;

public class CheckersTest {
    @Test
    void doTurnTest() {
        // regular expressions
        Assertions.assertThatExceptionOfType(GeneralErrorException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.doTurn("a1-w1", true);
                });
        Assertions.assertThatExceptionOfType(GeneralErrorException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.doTurn("a1-b2-a3", true);
                });
        Assertions.assertThatExceptionOfType(GeneralErrorException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.doTurn("A1:W1", true);
                });
        Assertions.assertThatExceptionOfType(GeneralErrorException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.doTurn("a1:b2:c3-d4", true);
                });


        // long turn
        Assertions.assertThatNoException()
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.defaultPlacement();
                    checkers.doTurn("a3:b4:c5:d4", true);
                });

        // boards equality
        Assertions.assertThatNoException()
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.defaultPlacement();
                    checkers.doTurn("a3:b4", true);
                    checkers.doTurn("b6:a5", false);
                    checkers.doTurn("g3:h4", true);
                    checkers.doTurn("h6:g5", false);
                    Checkers checkersTrue = new Checkers();
                    checkersTrue.inputPlacement("a1 b2 b4 c1 c3 d2 e1 e3 f2 g1 h2 h4", true);
                    checkersTrue.inputPlacement("a5 a7 b8 c7 d6 d8 e7 f6 f8 g5 g7 h8", false);

                    Assertions.assertThat(checkers.printCheckersString(true)).isEqualTo(checkersTrue.printCheckersString(true));
                    Assertions.assertThat(checkers.printCheckersString(false)).isEqualTo(checkersTrue.printCheckersString(false));
                });
        // King moves
        Assertions.assertThatNoException()
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.inputPlacement("H8", true);
                    checkers.inputPlacement("c3 e5", false);
                    checkers.printBoard();
                    checkers.doTurn("H8-D4", true);
                    checkers.printBoard();
                    checkers.doTurn("D4-B2", true);
                    checkers.printBoard();
                    checkers.doTurn("B2:A1", true);
                    checkers.printBoard();
                    Checkers checkersTrue = new Checkers();
                    checkersTrue.inputPlacement("A1", true);

                    Assertions.assertThat(checkers.printCheckersString(true)).isEqualTo(checkersTrue.printCheckersString(true));
                    Assertions.assertThat(checkers.printCheckersString(false)).isEqualTo(checkersTrue.printCheckersString(false));
                });
    }
    @Test
    void doMoveTest() {
        { // go straight to Kings
            Checkers checkers = new Checkers();
            Assertions.assertThatNoException().isThrownBy(() -> checkers.inputPlacement("a1", true));
            for (int i = 0; i < 7; i++) {
                checkers.doMove(i, i, i + 1, i + 1, true);
            }
            Checkers checkersTrue = new Checkers();
            Assertions.assertThatNoException().isThrownBy(() -> checkersTrue.inputPlacement("H8", true));
            Assertions.assertThat(checkers.printCheckersString(true)).isEqualTo(checkersTrue.printCheckersString(true));
            Assertions.assertThat(checkers.printCheckersString(false)).isEqualTo(checkersTrue.printCheckersString(false));
        }
        { // couple of moves
            Checkers checkers = new Checkers();
            checkers.defaultPlacement();
            checkers.doMove(0, 2, 1, 3, true);
            checkers.doMove(1, 5, 0, 4, false);
            checkers.doMove(2, 2, 3, 3, true);
            checkers.doMove(0, 4, 2, 2, false);
            checkers.doMove(2, 2, 4, 4, false);
            checkers.doMove(2, 2, 4, 4, false);
            checkers.doMove(2, 2, 4, 4, false);
            Assertions.assertThat(checkers.printCheckersString(true)).isEqualTo("a1 b2 c1 d2 e1 e3 f2 g1 g3 h2");
            Assertions.assertThat(checkers.printCheckersString(false)).isEqualTo("a7 b8 c7 d6 d8 e5 e7 f6 f8 g7 h6 h8");
        }
    }
    @Test
    void validMoveTest() {
        // going from empty cell
        Assertions.assertThatExceptionOfType(GeneralErrorException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.validMove(0, 0, 1, 1, true, false, false);
                });
        // going to busy cell
        Assertions.assertThatExceptionOfType(BusyCellException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.defaultPlacement();
                    checkers.doTurn("a3:b4", true);
                    checkers.doTurn("b4:c5", true);
                    checkers.validMove(2, 4, 3, 5, true, false, false);
                });
        // white cell
        Assertions.assertThatExceptionOfType(WhiteCellException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.defaultPlacement();
                    checkers.validMove(0, 0, 1, 2, true, false, false);
                });
        // invalid borders
        Assertions.assertThatExceptionOfType(GeneralErrorException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.defaultPlacement();
                    checkers.validMove(0, 2, 0, 9, true, false, false);
                });
        // going from opponents cell
        Assertions.assertThatExceptionOfType(GeneralErrorException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.defaultPlacement();
                    checkers.validMove(1, 5, 2, 4, true, false, false);
                });
        // going for 2 cell on usual move
        Assertions.assertThatExceptionOfType(GeneralErrorException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.defaultPlacement();
                    checkers.validMove(2, 2, 4, 4, true, false, false);
                });
        // invalid move
        Assertions.assertThatExceptionOfType(InvalidMoveException.class)
                .isThrownBy(() -> {
                    Checkers checkers = new Checkers();
                    checkers.defaultPlacement();
                    checkers.doMove(1, 5, 2, 4, false);
                    checkers.doMove(4, 2, 3, 3, true);
                    checkers.validMove(7, 5, 6, 4, false, false, false);
                });
    }
}
