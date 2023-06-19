package edu.phystech;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Objects;

class BusyCellException extends Exception {
    BusyCellException() {
        super();
    }
}

class WhiteCellException extends Exception {
    WhiteCellException() {
        super();
    }
}

class InvalidMoveException extends Exception {
    InvalidMoveException() {
        super();
    }
}

class GeneralErrorException extends Exception {
    GeneralErrorException() {
        super();
    }
}

/**
 * Checkers class implements logic of real checkers
 * except that moves can be also backwards.
 */
public final class Checkers {

    ParserAutomat parser;
    private final int boardSize = 8; // board can be extended
    private final String letters = "abcdefgh"; // letters on board
    private final char white = 'w'; // possible chars in board
    private final char black = 'b';
    private final char empty = '_';
    private final char beaten = 'r'; // for beaten pieces
    private final Square[][] board; // stores the checkerboard
    // Если сделать ArrayList<ArrayList<Square>>,
    // то будет некрасивое обращение и присвоение элементов :(
    // Напр.: board.get(i).set(j, new Square(empty));
    // вместо board[i][j] = new Square(empty);

    /**
     * Class that is used in Hashset to conveniently check for turns
     */
    public class Turn {
        private int xfrom;
        private int yfrom;
        private int xto;
        private int yto;
        /**
         * Default constructor
         * @param xfrom
         * x coordinate of "from" move
         * @param yfrom
         * y coordinate of "from" move
         * @param xto
         * x coordinate of "to" move
         * @param yto
         * y coordinate of "to" move
         */
        Turn(int xfrom, int yfrom, int xto, int yto) {
            this.xfrom = xfrom;
            this.yfrom = yfrom;
            this.xto = xto;
            this.yto = yto;
        }

        /**
         * For Hashset
         * @return if turns are equal
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Turn temp = (Turn) o;
            return xfrom == temp.xfrom && yfrom == temp.yfrom && xto == temp.xto && yto == temp.yto;
        }
        /**
         * For Hashset
         * @return hash of turn
         */
        @Override
        public int hashCode() {
            return Objects.hash(xfrom, yfrom, xto, yto);
        }
    }

    /**
     * Automat for parsing input data
     * implements all the parsing logic
     */
    public class ParserAutomat {
        private static final int numberOfStates = 3;
        private int state = 0;
        String line;
        int currentLineIndex = 0;
        boolean isWhiteNow = true;

        /**
         * @param line
         * should be a line of staring position
         * or should be in format "a1-b2" or "a1:c3" for "eat" move
         */
        void parse(String line) throws GeneralErrorException {
            if (state == 0 || state == 1) {
                if (!line.matches("[a-h|A-H][1-8]( [a-h|A-H][1-8])*")) {
                    throw new GeneralErrorException();
                }
                this.line = line;
                return;
            }
            String usualMove = "[a-h|A-H][1-8]-[a-h|A-H][1-8]";
            String eatMove = "[a-h|A-H][1-8](:[a-h|A-H][1-8])+";
            if (!line.matches(usualMove) && !line.matches(eatMove)) {
                throw new GeneralErrorException();
            }
            this.line = line;
            isWhiteNow = true;
            currentLineIndex = 0;
        }

        void nextState() {
            ++state;
            isWhiteNow = !isWhiteNow;
        }

        /**
        * returns next checker in line
        */
        Checker nextChecker() {
            int x = ltoi(line.charAt(currentLineIndex));
            int y = Character.getNumericValue(line.charAt(currentLineIndex + 1)) - 1;
            Square square = new Square(isWhiteNow ? white : black, Character.isUpperCase(line.charAt(currentLineIndex)));
            currentLineIndex += 3;
            return new Checker(x, y, square);
        }

        final boolean outOfCheckers() {
            return currentLineIndex >= line.length();
        }

    }

    /**
     * Checker is used to connect parser and methods
     */
    private class Checker {
        private int x;
        private int y;
        private Square square;
        /**
         * Constructor (default)
         * @param x
         * x coordinate of checker
         * @param y
         * y coordinate of checker
         * @param square
         * square related to this checker
         */
        Checker(int x, int y, Square square) {
            this.x = x;
            this.y = y;
            this.square = square;
        }
    }

    /**
     * Square is a piece of checkers board
     */
    public class Square {
        private char color;
        private boolean isKing;

        /**
         * Constructor (default)
         * @param color
         * color of square
         */
        Square(char color) {
            this.color = color;
            this.isKing = false;
        }
        /**
         * Constructor
         * @param color
         * color of square
         * @param isKing
         * is a square King or not
         */
        Square(char color, boolean isKing) {
            this.color = color;
            this.isKing = isKing;
        }
    }

    /**
     * Default constructor
     * fills all the board with empty squares
     */
    Checkers() {
        board = new Square[boardSize][boardSize];
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                board[i][j] = new Square(empty);
            }
        }
        parser = new ParserAutomat();
    }

    /**
     * Starting placement of checkers
     */
    public void defaultPlacement() {
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                board[i][j] = new Square(empty);
            }
        }

        for (int i = 0; i < boardSize; ++i) {
            if (i % 2 == 0) {
                board[i][0] = new Square(white);
                board[i][2] = new Square(white);
                board[i][6] = new Square(black);
            } else {
                board[i][1] = new Square(white);
                board[i][5] = new Square(black);
                board[i][7] = new Square(black);
            }
        }
    }

    /**
     *
     * @param line
     * placement in format "a1 a3 b2"
     * @param isWhite
     * which placement, black or white
     * @throws GeneralErrorException
     */
    public void inputPlacement(String line, boolean isWhite) throws GeneralErrorException {
        parser.parse(line);
        while (!parser.outOfCheckers()) {
            Checker nextChecker = parser.nextChecker();
            int x = nextChecker.x;
            int y = nextChecker.y;
            board[x][y] = nextChecker.square;
        }
        parser.nextState();
    }

    /**
     * Prints the whole board in normal way
     */
    public void printBoard() {
        System.out.println("  a b c d e f g h");
        for (int i = 0; i < boardSize; ++i) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < boardSize; ++j) {
                System.out.print((board[j][i].isKing ? Character.toUpperCase(board[j][i].color)
                        : board[j][i].color) + " ");
            }
            System.out.println();
        }
    } // for debug

    /**
     * Prints all positions of checkers of a particular color
     * is used primarily for output in system
     * @param isWhite
     * which checkers are printed, black or white
     */
    public void printCheckers(boolean isWhite) {
        char player = isWhite ? white : black;
        boolean flag = false; // for not to display first space
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board[i][j].color == player && board[i][j].isKing) {
                    if (flag) {
                        System.out.print(" ");
                    }
                    flag = true;
                    // prints D6
                    System.out.print(Character.toUpperCase(letters.charAt(i)) + String.valueOf(j + 1));
                }
            }
        }
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board[i][j].color == player && !board[i][j].isKing) {
                    if (flag) {
                        System.out.print(" ");
                    }
                    flag = true;
                    // prints d6
                    System.out.print(letters.charAt(i) + String.valueOf(j + 1));
                }
            }
        }
    }

    /**
     * Return all positions of checkers of a particular color as a string
     * is used primarily for testing
     * @param isWhite
     * which checkers are printed, black or white
     */
    public String printCheckersString(boolean isWhite) {
        char player = isWhite ? white : black;
        boolean flag = false; // for not to display first space
        StringBuilder result = new StringBuilder(new String());
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board[i][j].color == player && board[i][j].isKing) {
                    if (flag) {
                        result.append(" ");
                    }
                    flag = true;
                    // prints D6
                    result.append(Character.toUpperCase(letters.charAt(i))).append(String.valueOf(j + 1));
                }
            }
        }
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board[i][j].color == player && !board[i][j].isKing) {
                    if (flag) {
                        result.append(" ");
                    }
                    flag = true;
                    // prints d6
                    result.append(letters.charAt(i)).append(String.valueOf(j + 1));
                }
            }
        }
        return result.toString();
    }

    /**
     * Handles the next line in input
     * @param sc
     * Scanner that is used for input
     * @throws BusyCellException
     * @throws WhiteCellException
     * @throws GeneralErrorException
     * @throws InvalidMoveException
     */
    public void handleNextLine(Scanner sc) throws BusyCellException, WhiteCellException,
            GeneralErrorException, InvalidMoveException {
        String[] in = sc.nextLine().split(" ");
        String whiteTurn = in[0];
        String blackTurn = in[1];
        parser.parse(whiteTurn);
        doTurn(true);
        parser.parse(blackTurn);
        doTurn(false);
    }

    private boolean validBorders(int coordinate) {
        return 0 <= coordinate && coordinate <= 7;
    }

    private void checkStrictMovesInOneDirection(int i, int j, int sgn1, int sgn2,
                                                HashSet<Turn> strictMoves, boolean isWhiteTurn) {
        char opponent = isWhiteTurn ? black : white;
        if (validBorders(i + sgn1 * 2) && validBorders(j + sgn2 * 2)
                && board[i + sgn1][j + sgn2].color == opponent && board[i + sgn1 * 2][j + sgn2 * 2].color == empty) {
            Turn turn = new Turn(i, j, i + sgn1 * 2, j + sgn2 * 2);
            strictMoves.add(turn);
        }
    }

    private void checkStrictMovesInOneDirectionKing(int i, int j, int sgn1, int sgn2,
                                                    HashSet<Turn> strictMoves, boolean isWhiteTurn) {
        char player = isWhiteTurn ? white : black;
        char opponent = isWhiteTurn ? black : white;
        for (int k = 1; validBorders(i + sgn1 * k) && validBorders(j + sgn2 * k); ++k) {
            if (board[i + sgn1 * k][j + sgn2 * k].color == player
                    || board[i + sgn1 * k][j + sgn2 * k].color == beaten) {
                break;
            }
            if (board[i + sgn1 * k][j + sgn2 * k].color == opponent) {
                for (int l = 1; validBorders(i + sgn1 * (k + l)) && validBorders(j + sgn2 * (k + l)); l++) {
                    int iShifted = i + sgn1 * (k + l);
                    int jShifted = j + sgn2 * (k + l);
                    if (board[iShifted][jShifted].color == opponent
                            || board[iShifted][jShifted].color == player
                            || board[iShifted][jShifted].color == beaten) {
                        break;
                    }
                    if (board[iShifted][jShifted].color == empty) {
                        Turn turn = new Turn(i, j, iShifted, jShifted);
                        strictMoves.add(turn);
                    }
                }
                break;
            }
        }
    }
    private void checkForStrictMoves(HashSet<Turn> strictMoves, boolean isWhiteTurn) {
        char player = isWhiteTurn ? white : black;
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board[i][j].color == player) {
                    if (!board[i][j].isKing) {
                        // checking right up move
                        checkStrictMovesInOneDirection(i, j, 1, 1, strictMoves, isWhiteTurn);
                        // checking right down move
                        checkStrictMovesInOneDirection(i, j, 1, -1, strictMoves, isWhiteTurn);
                        // checking left up move
                        checkStrictMovesInOneDirection(i, j, -1, 1, strictMoves, isWhiteTurn);
                        // checking left down move
                        checkStrictMovesInOneDirection(i, j, -1, -1, strictMoves, isWhiteTurn);
                    } else {
                        // checking right up move
                        checkStrictMovesInOneDirectionKing(i, j, 1, 1, strictMoves, isWhiteTurn);
                        // checking right down move
                        checkStrictMovesInOneDirectionKing(i, j, 1, -1, strictMoves, isWhiteTurn);
                        // checking left up move
                        checkStrictMovesInOneDirectionKing(i, j, -1, 1, strictMoves, isWhiteTurn);
                        // checking left down move
                        checkStrictMovesInOneDirectionKing(i, j, -1, -1, strictMoves, isWhiteTurn);
                    }
                }
            }
        }
    }

    /**
     * Checks if the move is valid or not
     * @param xfrom
     * x coordinate of "from" move
     * @param yfrom
     * y coordinate of "to" move
     * @param xto
     * x coordinate of "from" move
     * @param yto
     * y coordinate of "to" move
     * @param isWhiteTurn
     * which turn it is, black or white
     * @param isKing
     * is the move King's or not
     * @param hasBecomeKingThisTurn
     * is the checker piece became King in this turn
     * @throws BusyCellException
     * if you try to step on busy square
     * @throws WhiteCellException
     * if you try to step on "white" square
     * @throws InvalidMoveException
     * if you have to beat enemy, and you do not do it
     * @throws GeneralErrorException
     * other exceptions
     */
    public void validMove(int xfrom, int yfrom, int xto, int yto, boolean isWhiteTurn,
                          boolean isKing, boolean hasBecomeKingThisTurn) throws GeneralErrorException,
            BusyCellException, WhiteCellException, InvalidMoveException {
        // check if indexes are in board borders
        if (!validBorders(xfrom) || !validBorders(yfrom) || !validBorders(xto) || !validBorders(yto)) {
            throw new GeneralErrorException();
        }
        // check that 'to' square is empty (busy cell)
        if (board[xto][yto].color == white || board[xto][yto].color == black) {
            throw new BusyCellException();
        }
        // check for stepping on dead
        if (board[xto][yto].color == beaten) {
            throw new GeneralErrorException();
        }
        // check for white cell
        if ((xto + yto) % 2 == 1) {
            throw new WhiteCellException();
        }
        // check 'from' square is player's
        if (board[xfrom][yfrom].color != (isWhiteTurn ? white : black)) {
            throw new GeneralErrorException();
        }
        // check that King has Upper Notation
        if (isKing != board[xfrom][yfrom].isKing && !hasBecomeKingThisTurn) {
            throw new GeneralErrorException();
        }

        // hasBecomeKingThisTurn is for case: a6:c8:f5 - becomes king and instantly eats as a king
        if (board[xfrom][yfrom].isKing) {
            HashSet<Turn> strictMoves = new HashSet<Turn>();
            checkForStrictMoves(strictMoves, isWhiteTurn);

            if (xto == xfrom || yto == yfrom) {
                throw new GeneralErrorException();
            }
            boolean usualMove = true;
            int xsgn = xto - xfrom > 0 ? 1 : -1;
            int ysgn = yto - yfrom > 0 ? 1 : -1;
            for (int i = 1; i < Math.abs(xto - xfrom) && i < Math.abs(yto - yfrom); i++) {
                // check for two pieces on the king way
                if (!usualMove && board[xfrom + xsgn * i][yfrom + ysgn * i].color == (isWhiteTurn ? black : white)) {
                    throw new GeneralErrorException();
                }
                // check for same color on the king way
                if (board[xfrom + xsgn * i][yfrom + ysgn * i].color == (isWhiteTurn ? white : black)) {
                    throw new GeneralErrorException();
                }
                // check for a piece on the king way
                if (board[xfrom + xsgn * i][yfrom + ysgn * i].color == (isWhiteTurn ? black : white)) {
                    usualMove = false;
                }
            }

            if (usualMove) {
                // check that we do not have any strict moves
                if (!strictMoves.isEmpty()) {
                    throw new InvalidMoveException();
                }
            } else {
                // check that our move is one of the strict moves
                Turn turn = new Turn(xfrom, yfrom, xto, yto);
                if (!strictMoves.contains(turn)) {
                    throw new InvalidMoveException();
                }
            }
        } else {
            HashSet<Turn> strictMoves = new HashSet<Turn>();
            checkForStrictMoves(strictMoves, isWhiteTurn);
            if (Math.abs(xfrom - xto) == 1) { // usual move
                // check that we do not have any strict moves
                if (!strictMoves.isEmpty()) {
                    throw new InvalidMoveException();
                }
                return;
            } else if (Math.abs(xfrom - xto) == 2) { // 'eat' move
                // check that we jump exactly above over opponent's checker
                if (board[(xfrom + xto) / 2][(yfrom + yto) / 2].color != (isWhiteTurn ? black : white)) {
                    throw new GeneralErrorException();
                }
                // check that our move is one of the strict moves
                Turn turn = new Turn(xfrom, yfrom, xto, yto);
                if (!strictMoves.contains(turn)) {
                    throw new InvalidMoveException();
                }
                return;
            }

            throw new GeneralErrorException();
        }
    }

    private int ltoi(char c) {
        for (int i = 0; i < boardSize; i++) {
            if (c == letters.charAt(i) || c == Character.toUpperCase(letters.charAt(i))) {
                return i;
            }
        }
        return -1;
    } // convert a letter to integer

    /**
     * Does a move without any checks
     * @param xfrom
     * x coordinate of "from" move
     * @param yfrom
     * y coordinate of "to" move
     * @param xto
     * x coordinate of "from" move
     * @param yto
     * y coordinate of "to" move
     * @param isWhiteTurn
     * which turn it is, black or white
     */
    public void doMove(int xfrom, int yfrom, int xto, int yto, boolean isWhiteTurn) {
        board[xfrom][yfrom].color = empty;
        board[xto][yto].color = isWhiteTurn ? white : black;
        board[xto][yto].isKing = board[xfrom][yfrom].isKing;
        board[xfrom][yfrom].isKing = false;
        if (board[xto][yto].isKing) {
            int xsgn = xto - xfrom > 0 ? 1 : -1;
            int ysgn = yto - yfrom > 0 ? 1 : -1;
            for (int i = 1; i < Math.abs(xto - xfrom) && i < Math.abs(yto - yfrom); ++i) {
                if (board[xfrom + xsgn * i][yfrom + ysgn * i].color == (isWhiteTurn ? black : white)) {
                    board[xfrom + xsgn * i][yfrom + ysgn * i].color = beaten;
                }
            }
        } else {
            if (Math.abs(xto - xfrom) == 2) {
                board[(xfrom + xto) / 2][(yfrom + yto) / 2].color = beaten;
            }
            if (isWhiteTurn && yto == boardSize - 1 || !isWhiteTurn && yto == 0) {
                board[xto][yto].isKing = true;
            }
        }
    }

    private void takeOffBeatenPieces() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].color == beaten) {
                    board[i][j] = new Square(empty);
                }
            }
        }
    } // replaces beaten enemies with empty squares

    /**
     * Does a whole turn (combination of moves)
     * for Kings use Upper letters
     * @param isWhiteTurn
     * which turn it is, black or white
     * @throws BusyCellException
     * if you try to step on busy square
     * @throws WhiteCellException
     * if you try to step on "white" square
     * @throws InvalidMoveException
     * if you have to beat enemy, and you do not do it
     * @throws GeneralErrorException
     * other exceptions
     */
    public void doTurn(boolean isWhiteTurn) throws BusyCellException,
            WhiteCellException, GeneralErrorException, InvalidMoveException {
        Checker checker = parser.nextChecker();
        int xfrom = checker.x;
        int yfrom = checker.y;
        int xto;
        int yto;
        boolean isKing = checker.square.isKing;
        boolean hasBecomeKingThisTurn = false;
        while (!parser.outOfCheckers()) {
            checker = parser.nextChecker();
            xto = checker.x;
            yto = checker.y;

            hasBecomeKingThisTurn = isWhiteTurn && yfrom == (boardSize - 1)
                    || !isWhiteTurn && yfrom == 0 || hasBecomeKingThisTurn;
            validMove(xfrom, yfrom, xto, yto, isWhiteTurn, isKing, hasBecomeKingThisTurn);
            doMove(xfrom, yfrom, xto, yto, isWhiteTurn);

            xfrom = xto;
            yfrom = yto;
            isKing = checker.square.isKing;
        }
        takeOffBeatenPieces();
        parser.nextState();
    } // executes a move


    public static void main(String[] args) {
        Checkers checkers = new Checkers();
        try (Scanner sc = new Scanner(System.in)) {
            checkers.inputPlacement(sc.nextLine(), true);
            checkers.inputPlacement(sc.nextLine(), false);
            while (sc.hasNextLine()) {
                checkers.handleNextLine(sc);
            }
            checkers.printCheckers(true);
            System.out.println();
            checkers.printCheckers(false);

        } catch (BusyCellException e) {
            System.out.println("busy cell");
        } catch (WhiteCellException e) {
            System.out.println("white cell");
        } catch (InvalidMoveException e) {
            System.out.println("invalid move");
        } catch (GeneralErrorException e) {
            System.out.println("general error");
        }
    }

}
