package base;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Scanner;
import base.TankSolver;

/*
Initial setup to get working:
  Change ScreenWidth, ScreenHeight
  Make sure TOT_MINES is correct

Hopefully it works then
*/

public class Base {

    //class variable -- use without instantiating
    static public int ScreenWidth = 1920;
    static public int ScreenHeight = 1080;

    static public int BoardWidth = 0;
    static public int BoardHeight = 1;
    static public double BoardPix = 0;
    static public int BoardTopW = 0;
    static public int BoardTopH = 0;
    static public int mouseLocX = ScreenWidth / 2;
    static public int mouseLocY = ScreenHeight / 2;

    static public Scanner scanner = new Scanner(System.in);
    static public Robot robot;
    static public Random rand = new Random();


    // Internal representation of the board state as displayed on the screen.
    // 1-8 means that the square there is that number
    // 0 means that it's actually empty
    // -1 means it's not opened yet
    // -2 means it's outside the boundries of the board
    // -3 means a mine
    // -10 means that something went wrong and we should exit the program
    static public int[][] onScreen = null;

    // List of squares in which we know there are mines
    static public boolean[][] flags = null;

    static public int numMines = 0;
    static public int TOT_MINES = 99;


    static BufferedImage screenShotImage() {

        try {
            Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            ScreenWidth = captureSize.width;
            ScreenHeight = captureSize.height;
            BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
            return bufferedImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    static boolean isDark(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return red + green + blue < 120;
    }

    static int colorDifference(int r1, int g1, int b1, int r2, int g2, int b2) {
        return Math.abs(r1 - r2) + Math.abs(b1 - b2) + Math.abs(g1 - g2);
    }




    // Take a screenshot and try to figure out the board dimensions and stuff like that
    public static void calibrate() {

        // Display this message
        System.out.println("Calibrating Screen...");

        BufferedImage bi = screenShotImage();
        bi.createGraphics();
        Graphics2D g = (Graphics2D) bi.getGraphics();


        int hh = 0; // boardheight of previous column
        int firh = 0; // position of first found
        int firw = 0;
        int lash = 0; // position of last found
        int lasw = 0;
        int tot = 0; // total number of crosses found


        for (int w = 0; w < ScreenWidth; w++) {

            for (int h = 0; h < ScreenHeight; h++) {
                int rgb = bi.getRGB(w, h);

                if (isDark(rgb)) {

                    if (w < 10 || h < 10 || w > ScreenWidth - 10 || h > ScreenHeight - 10)
                        continue;

                    // look for the cross shape to indicate position on board
                    // we consider it a cross if:
                    //   - the square is dark
                    //   - four selected pixels to the N,S,E,W are dark
                    //   - four selected pixels to the NE, SE, NW, SW are not dark
                    if (isDark(bi.getRGB(w + 7, h)))
                        if (isDark(bi.getRGB(w - 7, h)))
                            if (isDark(bi.getRGB(w, h + 7)))
                                if (isDark(bi.getRGB(w, h - 7)))
                                    if (isDark(bi.getRGB(w + 3, h)))
                                        if (isDark(bi.getRGB(w - 3, h)))
                                            if (isDark(bi.getRGB(w, h + 3)))
                                                if (isDark(bi.getRGB(w, h - 3)))
                                                    if (!isDark(bi.getRGB(w - 7, h - 7)))
                                                        if (!isDark(bi.getRGB(w + 7, h - 7)))
                                                            if (!isDark(bi.getRGB(w - 7, h + 7)))
                                                                if (!isDark(bi.getRGB(w + 7, h + 7)))
                                                                    if (!isDark(bi.getRGB(w - 3, h - 3)))
                                                                        if (!isDark(bi.getRGB(w + 3, h - 3)))
                                                                            if (!isDark(bi.getRGB(w - 3, h + 3)))
                                                                                if (!isDark(bi.getRGB(w + 3, h + 3))) {

                                                                                    g.setColor(Color.YELLOW); // for _calibrate.png
                                                                                    g.fillRect(w - 3, h - 3, 7, 7);
                                                                                    tot++;
                                                                                    BoardHeight++;

                                                                                    // Find the position of the first cross
                                                                                    if (firh == 0) {
                                                                                        firh = h;
                                                                                        firw = w;
                                                                                    }

                                                                                    // Note position of the last cross
                                                                                    lash = h;
                                                                                    lasw = w;
                                                                                }
                }


            }

            if (BoardHeight > 1) {
                hh = BoardHeight;
                BoardHeight = 1;
            }
        }

        // Determine boardwidth from total and boardheight
        BoardHeight = hh;
        if (tot % (BoardHeight - 1) == 0)
            BoardWidth = tot / (BoardHeight - 1) + 1;
        else BoardWidth = 0;

        // Determine BoardPix by taking an average
        BoardPix = 0.5 * ((double) (lasw - firw) / (double) (BoardWidth - 2))
                + 0.5 * ((double) (lash - firh) / (double) (BoardHeight - 2));

        // Determine first cell position (where to click)
        int halfsiz = (int) BoardPix / 2;
        BoardTopW = firw - halfsiz + 3;
        BoardTopH = firh - halfsiz + 3;


        System.out.printf("BoardWidth=%d, BoardHeight=%d, BoardPix=%f\n", BoardWidth, BoardHeight, BoardPix);
        System.out.printf("BoardTopW=%d, BoardTopH=%d\n", BoardTopW, BoardTopH);


    }



    static void moveMouse(int mouseX, int mouseY) throws Throwable {

        int distance = Math.max(Math.abs(mouseX - mouseLocX), Math.abs(mouseY - mouseLocY));
        int DELAY = distance / 4;
        int numSteps = DELAY / 5;

        double stepx = (double) (mouseX - mouseLocX) / (double) numSteps;
        double stepy = (double) (mouseY - mouseLocY) / (double) numSteps;

        for (int i = 0; i < numSteps; i++) {
            robot.mouseMove(mouseLocX + (int) (i * stepx), mouseLocY + (int) (i * stepy));
            Thread.sleep(5);
        }
        robot.mouseMove(mouseX, mouseY);
        mouseLocX = mouseX;
        mouseLocY = mouseY;
    }


    // Click on a given square, given i and j
    static void clickOn(int i, int j) throws Throwable {
        int mouseX = BoardTopW + (int) (j * BoardPix);
        int mouseY = BoardTopH + (int) (i * BoardPix);
        moveMouse(mouseX, mouseY);

        robot.mousePress(16);
        Thread.sleep(5);
        robot.mouseRelease(16);
        Thread.sleep(10);
    }


    // Manually flag some mine
    static void flagOn(int i, int j) throws Throwable {
        int mouseX = BoardTopW + (int) (j * BoardPix);
        int mouseY = BoardTopH + (int) (i * BoardPix);
        moveMouse(mouseX, mouseY);
//        onScreen[i][j] = -3;
//        flags[i][j] = true;

        robot.mousePress(4);
        Thread.sleep(5);
        robot.mouseRelease(4);
        Thread.sleep(10);
    }


    // Click on it with both mouse buttons in order to "chord"
    static void chordOn(int i, int j) throws Throwable {
        int mouseX = BoardTopW + (int) (j * BoardPix);
        int mouseY = BoardTopH + (int) (i * BoardPix);
        moveMouse(mouseX, mouseY);

        robot.mousePress(4);
        robot.mousePress(16);
        Thread.sleep(5);
        robot.mouseRelease(4);
        robot.mouseRelease(16);
        Thread.sleep(10);
    }


    // Special method to try to separate 3 from 7
    // which conveniently are the same color
    static int detect_3_7(int[] areapix) {

        // Assume it's length 225 and dimensions 15x15.
        // Classify each pixel as red or not.
        // Since we don't have to deal with 5, we can take a greater liberty
        // in deciding on red pixels.

        boolean redx[][] = new boolean[15][15];
        for (int k = 0; k < 225; k++) {
            int i = k % 15;
            int j = k / 15;
            int rgb = areapix[k];
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            if (colorDifference(red, green, blue, 170, 0, 0) < 100)
                redx[i][j] = true;
        }

    /*
    for(int i=0; i<15; i++){
      for(int j=0; j<15; j++){
        if(redx[i][j]) System.out.print("x");
        else System.out.print(" ");
      }
      System.out.println();
    }
    System.out.println();
    */


        // Look for this pattern in the 3 but not 7:
        // x x
        // x .
    /*
    for(int i=0; i<14; i++){
      for(int j=0; j<14; j++){
        if(redx[i][j] && redx[i+1][j]
             && redx[i][j+1] && !redx[i+1][j+1])
          return 3;
      }
    }
    */

        // . . .
        //   x
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                if (!redx[i][j] && !redx[i][j + 1] && !redx[i][j + 2] && redx[i + 1][j + 1])
                    return 3;
            }
        }

        return 7;

    }


    // Try to read what number's in this position
    static int detect(BufferedImage bi, int i, int j) {
        int mouseX = BoardTopW + (int) (j * BoardPix);
        int mouseY = BoardTopH + (int) (i * BoardPix);

        // Don't take one pixel, take a 15x15 area of pixels
        int areapix[] = new int[225];
        int count = 0;
        for (int ii = mouseX - 7; ii <= mouseX + 7; ii++)
            for (int jj = mouseY - 7; jj <= mouseY + 7; jj++) {
                areapix[count] = bi.getRGB(ii, jj);
                count++;
            }


        boolean hasColorOfOneSquare = false;
        boolean hasColorOfBlank = false;
        boolean isRelativelyHomogenous = true;

        for (int rgb : areapix) {
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            // Detect death
            if (colorDifference(red, green, blue, 110, 110, 110) < 20)
                return -10;

            // Detect flagging of any sort
            if (colorDifference(red, green, blue, 255, 0, 0) < 30)
                return -3;

            if (colorDifference(red, green, blue, 65, 79, 188) < 10) {
                hasColorOfOneSquare = true;
            }
            if (blue > red && blue > green &&
                    colorDifference(red, green, blue, 220, 220, 255) < 200) {
                hasColorOfBlank = true;
            }
            if (colorDifference(red, green, blue, 167, 3, 5) < 20)
                return detect_3_7(areapix);
            if (colorDifference(red, green, blue, 29, 103, 4) < 20) return 2;
            if (colorDifference(red, green, blue, 0, 0, 138) < 20) return 4;
            if (colorDifference(red, green, blue, 124, 1, 3) < 20) return 5;
            if (colorDifference(red, green, blue, 7, 122, 131) < 20) return 6;
        }

        // Determine how 'same' the area is.
        // This is to separate the empty areas which are relatively the same from
        // the unexplored areas which have a gradient of some sort.
        {
            int rgb00 = areapix[0];
            int red00 = (rgb00 >> 16) & 0xFF;
            int green00 = (rgb00 >> 8) & 0xFF;
            int blue00 = rgb00 & 0xFF;
            for (int rgb : areapix) {
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                if (colorDifference(red, green, blue, red00, green00, blue00) > 60) {
                    isRelativelyHomogenous = false;
                    break;
                }
            }
        }


        if (hasColorOfOneSquare && hasColorOfBlank)
            return 1;

        if (hasColorOfBlank && isRelativelyHomogenous)
            return 0;

        return -1;
    }




    // Remove the need for edge detection every fricking time
    static int onScreen(int i, int j) {
        if (i < 0 || j < 0 || i > BoardHeight - 1 || j > BoardWidth - 1)
            return -10;
        return onScreen[i][j];
    }


    // take a screenshot and update the onScreen array
    public static int updateOnScreen() {
        BufferedImage bi = screenShotImage();

        int numMines_t = 0;
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {

                int d = detect(bi, i, j);
                if (d == -10) return d; // death
                onScreen[i][j] = d;

                // Special case for flags
                if (d == -3 || flags[i][j]) {
                    onScreen[i][j] = -1;
                    flags[i][j] = true;
                }
                if (d == -1) {
                    flags[i][j] = false;
                }


                // Update mines count
                if (flags[i][j]) {
                    numMines_t++;
                }

            }
        }

        //if(numMines_t < numMines - 2) exit();
        numMines = numMines_t;
        return 0;
    }


    // tries to detect problems with screenshotting
    public static boolean checkConsistency() {
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {

                int freeSquares = countFreeSquaresAround(onScreen, i, j);
                int numFlags = countFlagsAround(flags, i, j);

                if (onScreen(i, j) == 0 && freeSquares > 0) {
                    return false;
                }
                if ((onScreen(i, j) - numFlags) > 0 && freeSquares == 0) {
                    return false;
                }

            }
        }

        return true;
    }

    //basic method -------------------------------------------------------------------------
    // Handle clicking the first square
    public static void firstSquare() throws Throwable {

        // Check that it's indeed the first square
        robot.mouseMove(0, 0);
        Thread.sleep(20);
        updateOnScreen();
        robot.mouseMove(mouseLocX, mouseLocY);
        boolean isUntouched = true;
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {
                if (onScreen(i, j) != -1)
                    isUntouched = false;
            }
        }
        if (!isUntouched) {
            return;
        }

        // Click the middle
        clickOn(BoardHeight / 2 - 1, BoardWidth / 2 - 1);
        clickOn(BoardHeight / 2 - 1, BoardWidth / 2 - 1);
        Thread.sleep(200);

    }



    // Exactly what it says on the tin
    static void guessRandomly() throws Throwable {
        System.out.println("Attempting to guess randomly");
        while (true) {
            int k = rand.nextInt(BoardHeight * BoardWidth);
            int i = k / BoardWidth;
            int j = k % BoardWidth;

            if (onScreen(i, j) == -1 && !flags[i][j]) {
                clickOn(i, j);
                return;
            }
        }
    }

    // How many flags exist around this square?
    static int countFlagsAround(boolean[][] array, int i, int j) {
        int mines = 0;

        // See if we're on the edge of the board
        boolean oU = false, oD = false, oL = false, oR = false;
        if (i == 0) oU = true;
        if (j == 0) oL = true;
        if (i == BoardHeight - 1) oD = true;
        if (j == BoardWidth - 1) oR = true;

        if (!oU && array[i - 1][j]) mines++;
        if (!oL && array[i][j - 1]) mines++;
        if (!oD && array[i + 1][j]) mines++;
        if (!oR && array[i][j + 1]) mines++;
        if (!oU && !oL && array[i - 1][j - 1]) mines++;
        if (!oU && !oR && array[i - 1][j + 1]) mines++;
        if (!oD && !oL && array[i + 1][j - 1]) mines++;
        if (!oD && !oR && array[i + 1][j + 1]) mines++;

        return mines;
    }

    // How many unopened squares around this square?
    static int countFreeSquaresAround(int[][] board, int i, int j) {
        int freeSquares = 0;

        if (onScreen(i - 1, j) == -1) freeSquares++;
        if (onScreen(i + 1, j) == -1) freeSquares++;
        if (onScreen(i, j - 1) == -1) freeSquares++;
        if (onScreen(i, j + 1) == -1) freeSquares++;
        if (onScreen(i - 1, j - 1) == -1) freeSquares++;
        if (onScreen(i - 1, j + 1) == -1) freeSquares++;
        if (onScreen(i + 1, j - 1) == -1) freeSquares++;
        if (onScreen(i + 1, j + 1) == -1) freeSquares++;

        return freeSquares;
    }

    //temporary function

    static public void attemptFlagMine() throws Throwable{

        for(int i=0; i<BoardHeight; i++){
            for(int j=0; j<BoardWidth; j++){

                if(onScreen(i,j) >= 1){
                    int curNum = onScreen(i,j);

                    // Flag necessary squares
                    if(curNum == countFreeSquaresAround(onScreen,i,j)){
                        for(int ii=0; ii<BoardHeight; ii++){
                            for(int jj=0; jj<BoardWidth; jj++){
                                if(Math.abs(ii-i)<=1 && Math.abs(jj-j)<=1){
                                    if(onScreen(ii,jj) == -1 && !flags[ii][jj]){
                                        flags[ii][jj] = true;
                                        flagOn(ii,jj);
                                    }
                                }
                            }
                        }
                    }


                }
            }
        }

    }

    static public void attemptMove() throws Throwable{

        boolean success = false;
        for(int i=0; i<BoardHeight; i++){
            for(int j=0; j<BoardWidth; j++){

                if(onScreen(i,j) >= 1){

                    // Count how many mines around it
                    int curNum = onScreen[i][j];
                    int mines = countFlagsAround(flags,i,j);
                    int freeSquares = countFreeSquaresAround(onScreen,i,j);


                    // Click on the deduced non-mine squares
                    if(curNum == mines && freeSquares > mines){
                        success = true;

                        // Use the chord or the classical algorithm
                        if(freeSquares - mines > 1){
                            chordOn(i,j);
                            onScreen[i][j] = 0; // hack to make it not overclick a square
                            continue;
                        }

                        // Old algorithm: don't chord
                        for(int ii=0; ii<BoardHeight; ii++){
                            for(int jj=0; jj<BoardWidth; jj++){
                                if(Math.abs(ii-i)<=1 && Math.abs(jj-j)<=1){
                                    if(onScreen(ii,jj) == -1 && !flags[ii][jj]){
                                        clickOn(ii,jj);
                                        onScreen[ii][jj] = 0;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        if(success) return;

        // Bring in the big guns
        TankSolver.Companion.Solver();
    }

}
