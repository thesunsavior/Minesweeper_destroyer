import java.awt.*;
import base.Base;

public class MSolver {


  /*
    todo:
     - a straight forward algorithm
     - Backtracking
     - probability solver
     - end game cases
   */
/*
Important  function

Int mouseLocX : X location of the mouse
Int mouseLocY : Y location of the mouse
Void moveMouse (int x,  int y) : Move mouse to coordinate (x,y)
Void clickOn (int i, int j ) :     Click on a given square, given i and j
void flagOn(int i, int j) : Manually flag some mine
void chordOn(int i, int j) : double click ( search for "chord minesweeper")

 static int[][] onScreen :  status of a square
    1-8 means that the square there is that number
     0 means that it's actually empty
    -1 means it's not opened yet
    -2 means it's outside the boundries of the board
     -3 means a mine
     -10 means that something went wrong and we should exit the program

    boolean[][] flags:   List of squares in which we know there are mines
    int countFlagsAround(boolean[][] array, int i, int j) :     How many flags exist around this square?
    int countFreeSquaresAround(int[][] board, int i, int j): count unopened square  around  square (i,j )

    int BoardWidth : number of columns of the board
    int BoardHeigth: number of rows of the board
 */
    public static void main(String[] args) throws Throwable {
        Thread.sleep(2000);
        Base.robot = new Robot();

        // Keep these as these are the most common settings
        Base.BoardWidth = 30;
        Base.BoardHeight = 16;


        Base.BoardPix = 42.035714;
        Base.BoardTopW = 193;
        Base.BoardTopH = 134;

        // Determine board height and width and position
        Base.calibrate();
        if (Base.BoardWidth < 9 || Base.BoardHeight < 9 || Base.BoardWidth > 30 || Base.BoardWidth > 30) {
            System.out.println("Calibration Failed.");
            return;
        }


        // Initialize internal constructs
        Base.onScreen = new int[Base.BoardHeight][Base.BoardWidth];
        Base.flags = new boolean[Base.BoardHeight][Base.BoardWidth];
        for (int i = 0; i < Base.BoardHeight; i++) for (int j = 0; j < Base.BoardWidth; j++) Base.flags[i][j] = false;

        // Debugging: is it reading correctly?


        Base.firstSquare();
        for (int c = 0; c < 1000000; c++) {
            int status = Base.updateOnScreen();
            if (!Base.checkConsistency()) {
                Base.robot.mouseMove(0, 0);
                status = Base.updateOnScreen();
                Base.robot.mouseMove(Base.mouseLocX, Base.mouseLocY);
                if (status == -10) exit();
                continue;
            }
            // Exit on death
            if (status == -10) exit();

            Base.attemptFlagMine();
            Base.attemptMove();
        }


    }

    static public void exit() {
        // For any reason, we want to exit
        //System.out.println("Steps: " + numMines);
        System.exit(0);
    }


    // suplement ------collapse this -------------
    // Copied from http://stackoverflow.com/questions/156275/
    class Pair<A, B> {
        private A first;
        private B second;

        public Pair(A first, B second) {
            super();
            this.first = first;
            this.second = second;
        }

        public int hashCode() {
            int hashFirst = first != null ? first.hashCode() : 0;
            int hashSecond = second != null ? second.hashCode() : 0;

            return (hashFirst + hashSecond) * hashSecond + hashFirst;
        }

        public boolean equals(Object other) {
            if (other instanceof Pair) {
                Pair otherPair = (Pair) other;
                return
                        ((this.first == otherPair.first ||
                                (this.first != null && otherPair.first != null &&
                                        this.first.equals(otherPair.first))) &&
                                (this.second == otherPair.second ||
                                        (this.second != null && otherPair.second != null &&
                                                this.second.equals(otherPair.second))));
            }

            return false;
        }

        public String toString() {
            return "(" + first + ", " + second + ")";
        }

        public A getFirst() {
            return first;
        }

        public void setFirst(A first) {
            this.first = first;
        }

        public B getSecond() {
            return second;
        }

        public void setSecond(B second) {
            this.second = second;
        }
    }
}

