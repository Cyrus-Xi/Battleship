/*
 * Author:
 *      Cyrus Xi
 * Purpose:
 *      This class represents the Computer player. It implements an adaptive
 *      targeting algorithm based on a quasi-probability density function of
 *      extant ships. Part of the algorithm is inspired from this:
 * 		http://www.datagenetics.com/blog/december32011/index.html
 * Strategy details:
 *          There are two phases to the Computer's strategy: the Hunt and the
 *      Target phases. In the Hunt phase, the Computer is trying to hit a
 *      ship. Once a ship has been hit, we are in the Target phase where
 *      the Computer is trying to finish off and sink an opponent's ship
 *      that has been struck.
 *
 *          During the Hunt phase, the Computer uses something like a probability
 *      density function to calculate the most likely places in which an
 *      enemy ship will be located. Each turn, the Computer goes through the
 *      board (which, as the game continues, is increasingly filled with
 *      misses and sunk ships) and tries to place each extant ship in each
 *      possible board location, first horizontally then vertically. If the
 *      ship can be placed (i.e., doesn't go off the board or through missed shots or
 *      already sunk ships), then each BoardSpace in that placing gets its
 *      counter attribute incremented by 1. The BoardSpace with the highest
 *      counter value is the mostly likely spot for an enemy ship to pass
 *      through. If there are multiple BoardSpaces with the highest counter
 *      value, then the tie is won by the one with the highest sum of
 *      neighboring BoardSpace counter values.
 *
 *          When a ship has been struck, the Computer goes on to the Target
 *      phase, which can also be loosely divided into two phases. In the
 *      first mini-phase, the Computer does not know the struck ship's
 *      orientation (horizontal or vertical) so it simply adds the four
 *      cardinal direction points (or fewer depending on obstructions) around
 *      the successful shot to a stack and pops the stack for the next shot.
 *      Once two points of a ship have been hit, the Computer can determine the
 *      orientation of the ship and move on to the next mini-phase, which is
 *      just the completion of the sinking of that ship based on its
 *      now-known orientation.
 * Date:
 *      09/20/14.
 */

package battleship;

import java.util.*;

/**
 * Represents the Computer player.
 */
public class Computer
{
    /**
     * Human player's board.
     * <p>
     * Though the board will have the player's ship placement, the computer
     * will not, of course, actually use that information in its strategy.
     */
    GameBoard ofOpponent;

    /**
     * The primitive/actual board.
     */
    char [][] rawBoard;

    /**
     * 10x10 array of BoardSpaces to implement probability density function.
     */
    BoardSpace [][] boardSpaces = new BoardSpace [10][10];

    /**
     * The lengths of non-sunk ships.
     * <p>
     * Double brace initialization for simplicity.
     */
    ArrayList<Integer> extantShipLengths = new ArrayList<Integer>()
    {{
        add(5);
        add(4);
        add(3);
        add(3);
        add(2);
    }};

    /**
     * Row & col of point to be shot at.
     */
    int row;
    int col;

    /**
     * A stack of points to be shot at next after a successful shot.
     * This represents the "Target" phase where the AI tries to finish off a
     * ship it has hit. The other phase is the "Hunt" one where the AI tries
     * to find a ship.
     */
    Stack<ShipPoint> possibleHits = new Stack<ShipPoint>();

    /**
     * Ships currently being targeted.
     */
    ArrayList<String> shipsTargeted = new ArrayList<String>();

    /**
     * Whether opponent's ship is going horizontally or vertically.
     */
    String orientation = "";

    /**
     * Array of pointHits, successful shots.
     */
    ArrayList<ArrayList<Object>> pointsTargeted = new
            ArrayList<ArrayList<Object>>();

    /**
     * To convert col value to column letter.
     */
    char[] columnHeaders = "ABCDEFGHIJ".toCharArray();

    /**
     * Very simple Computer constructor.
     *
     * @param ofHuman human player's game board
     */
    public Computer(GameBoard ofHuman)
    {
        ofOpponent = ofHuman;
        rawBoard = ofOpponent.getBoard();

        // Initialize boardSpaces with right row & col values.
        for (int i = 0; i < 10; ++i)
        {
            for (int j = 0; j < 10; ++j)
            {
                boardSpaces[i][j] = new BoardSpace(i, j);
            }
        }
    }

    /**
     * Computer plays its turn.
     *
     * @param ofHuman human player's game board
     */
    public void playOneTurn(GameBoard ofHuman){
        ofOpponent = ofHuman;

        // Update board each turn.
        rawBoard = ofOpponent.getBoard();

        // Set counts "horizontally" first. Calls itself for vertical counts.
        setBoardSpaces(true);

        ShipPoint currPoint;
        String message;
        char type;

        // Whether ship has been sunk.
        boolean sunk = false;

        /*
         * Hunt phase (i.e., not trying to finish off ship) so get row & col
         * values from probability density function.
         */
        if (possibleHits.isEmpty())
        {
            setHuntShot();
        }
        // Else use stack of possible hits (like a depth-based search).
        else
        {
            currPoint = possibleHits.pop();
            row = currPoint.getRow();
            col = currPoint.getCol();
        }
        type = rawBoard[row][col];

        // Get information about shot.
        message = ofOpponent.updateBoardAfterShot(false, row+1,
                columnHeaders[col]);

        // If shot was a hit (non-empty message).
        if (!message.equals(""))
        {
            sunk = updateOnHit(message);
        }

        // Update board.
        rawBoard = ofOpponent.getBoard();

        /*
         * Only add plausible candidates to stack if still in Target phase and
         * if didn't just sink a ship. The latter because though sinking shot
          * must have been successful, don't need to look around that ship.
         */
        if (!shipsTargeted.isEmpty() && !sunk)
        {
            // If shot was successful.
            if (rawBoard[row][col] == 'X')
            {
                currPoint = new ShipPoint(type, row, col);
                // Add feasible points around successful hit to stack.
                addPointsAround(currPoint, orientation);
            }
        }
        /*
         * If still in target phase and did just sink a ship.
         * Go back to a point of another ship that was hit and push possible
         * hits to stack using that.
         */
        else if (!shipsTargeted.isEmpty())
        {
            ArrayList<Object> pointLeft;
            pointLeft = pointsTargeted.get(0);
            int rowLeft = (Integer)pointLeft.get(1);
            int colLeft = (Integer)pointLeft.get(2);

            ShipPoint shipPointLeft = new ShipPoint('Z', rowLeft, colLeft);
            addPointsAround(shipPointLeft, orientation);
        }
    }

    /**
     * Helps implements the probability density function. Tries every extant
     * ship's possible board placements to set BoardSpace counter values.
     * <p>
     * For simplicity's/modularity's sake, calls itself for the vertical
     * "placing."
     *
     * @param isHorizontal true if "placing" horizontally, false if vertically
     */
    private void setBoardSpaces(boolean isHorizontal)
    {
        // Reset BoardSpace values at the start of each full turn.
        if (isHorizontal)
        {
            for (int i = 0; i < 10; ++i)
            {
                for (int j = 0; j < 10; j++)
                {
                    boardSpaces[i][j].clearCounter();
                    boardSpaces[i][j].clearNeighboringSum();
                }
            }
        }

        boolean isPlaceable;

        for (int row = 0; row < 10; ++row)
        {
            // For each extant ship.
            for (int index = 0; index < extantShipLengths.size(); ++index)
            {
                int length = extantShipLengths.get(index);
                for (int col = 0; col < 10; ++col)
                {
                    isPlaceable = isPlaceable(row, col, length, isHorizontal);
                    // If can place ship over spaces, increment their counters.
                    if (isPlaceable)
                    {
                        setSpaceCounters(row, col, length, isHorizontal);
                    }
                }
            }
        }

        // Uncomment to print BoardSpace counters.
//        System.out.println("Counters:\n");
//        for (int i = 0; i < 10; ++i)
//        {
//            for (int j = 0; j < 10; j++)
//            {
//                System.out.print(boardSpaces[i][j]);
//            }
//            System.out.println();
//        }

        // Now try to place ships vertically but make sure not to loop again.
        if (isHorizontal)
        {
            setBoardSpaces(false);
        }
    }

    /**
     * Calculates whether ship of given length can be placed.
     *
     * @param row
     * @param col
     * @param length
     * @param isHorizontal true if going horizontally, false if vertically
     * @return
     */
    private boolean isPlaceable(int row, int col, int length,
                                boolean isHorizontal)
    {
        boolean toReturn = true;
        char curr;
        for (int i = 0; i < length; ++i)
        {
            // Went off the edge.
            if (row > 9 || col > 9)
            {
                toReturn = false;
                break;
            }
            curr = rawBoard[row][col];
            if (curr == 'X' || curr == 'O')
            {
                toReturn = false;
                break;
            }
            if (isHorizontal)
            {
                col++;
            }
            // Go vertically.
            else
            {
                row++;
            }
        }
        return toReturn;
    }

    private void setSpaceCounters(int row, int col, int length,
                                  boolean isHorizontal)
    {
        for (int i = 0; i < length; ++i)
        {
            boardSpaces[row][col].incrementCounter();
            if (isHorizontal)
            {
                col++;
            }
            // Go vertically.
            else
            {
                row++;
            }
        }
    }

    /**
     * Get the best position to shoot at next in the Hunt phase based on the
     * above described probability density function.
     */
    private void setHuntShot()
    {
        setSpaceNeighborSums();

        ArrayList<BoardSpace> flatList = new ArrayList<BoardSpace>();
        for (int i = 0; i < 10; ++i)
        {
            for (BoardSpace space : boardSpaces[i])
            {
                flatList.add(space);
            }
        }
        /*
         * Sort in descending order based on counter then on neighbor sum.
         * Now the BoardSpace with the highest counter etc. is at the beginning.
         */
        Collections.sort(flatList, Collections.reverseOrder());

        row = flatList.get(0).getRow();
        col = flatList.get(0).getCol();

        // Just in case have already shot there.
        int index = 0;
        while ((rawBoard[row][col] == 'X') || (rawBoard[row][col] == 'O'))
        {
            index++;
            row = flatList.get(index).getRow();
            col = flatList.get(index).getCol();
        }
    }

    private void setSpaceNeighborSums()
    {
        for (int row = 0; row < 10; ++row)
        {
            for (int col = 0; col < 10; ++col)
            {
                BoardSpace curr = boardSpaces[row][col];
                int sum = 0;
                // Add north neighbor's counter.
                if (row != 0)
                {
                    sum += boardSpaces[row-1][col].getCounter();
                }
                // East neighbor.
                if (col != 9)
                {
                    sum += boardSpaces[row][col+1].getCounter();
                }
                // South neighbor.
                if (row != 9)
                {
                    sum += boardSpaces[row+1][col].getCounter();
                }
                // West neighbor.
                if (col != 0)
                {
                    sum += boardSpaces[row][col-1].getCounter();
                }
                curr.setNeighboringSum(sum);
            }
        }
    }

    /**
     * Updates on shot being a hit: gets direction, figures out if sunk, etc.
     *
     * @param message the message printed from a shot
     * @return        true if ship was sunk from hit
     */
    private boolean updateOnHit(String message)
    {
        String shipName;
        boolean sunk;

        // Array of ship type, row, col.
        ArrayList<Object> pointHit = new ArrayList<Object>();

        // Get ship name and whether it was sunk.
        shipName = getNameFromMessage(message);
        sunk = isSunk(message);

        pointHit.add(shipName);
        pointHit.add(row);
        pointHit.add(col);
        pointsTargeted.add(pointHit);

        // Ship hit but not sunk.
        if (!sunk)
        {
            // Ensure no duplicates.
            if (!shipsTargeted.contains(shipName))
            {
                // Add ship hit to list.
                shipsTargeted.add(shipName);
            }
            // Already hit once so can determine direction.
            else
            {
                /*
                 * Get direction if don't already know, i.e.,
                 * don't change direction if already know it.
                 */
                if (orientation.equals(""))
                {
                    orientation = getDirection(shipName);
                }
                /*
                 * Now know direction, but could've pushed errant points
                 * before. Correct stack.
                 */
                updateStack(row, col, orientation);
            }
        }
        // If sunk, update.
        else
        {
            updateOnSunkShip(shipName);
        }
        /*
         * No ships targeted; would be inefficient to keep popping stack.
         * AI would then keep looking around the perimeter of a ship even
           when it has been sunk.
         */
        if (shipsTargeted.isEmpty())
        {
            // Clear stack and return to Hunt phase.
            while (!possibleHits.empty())
            {
                possibleHits.pop();
            }
        }
        return sunk;
    }

    /**
     * Returns the name of the ship that was hit.
     *
     * @param message part of the message that is printed
     * @return        name of ship hit
     */
    private String getNameFromMessage(String message)
    {
        String toReturn = "";
        if (message.contains("Carrier"))
        {
            toReturn = "Carrier";
        }
        else if (message.contains("Battleship"))
        {
            toReturn = "Battleship";
        }
        else if (message.contains("Cruiser"))
        {
            toReturn = "Cruiser";
        }
        else if (message.contains("Submarine"))
        {
            toReturn = "Submarine";
        }
        else if (message.contains("Destroyer"))
        {
            toReturn = "Destroyer";
        }
        return toReturn;
    }

    /**
     * Returns whether ship was sunk or not.
     *
     * @param message part of the message that is printed
     * @return        true if ship was sunk
     */
    private boolean isSunk(String message)
    {
        boolean toReturn = false;

        // Based on the getHitMessage method.
        if (message.contains("but did not sink"))
        {
            toReturn = false;
        }
        else if (message.contains("and sunk"))
        {
            toReturn = true;
        }
        return toReturn;
    }

    /**
     * From hitting a ship twice, can get what direction it is lying in.
     *
     * @param name name of ship hit
     * @return     direction ship is lying in
     */
    private String getDirection(String name)
    {
        String toReturn = "";

        ArrayList<ArrayList<Object>> shipPoints = new ArrayList<ArrayList
                <Object>>();

        // Get points hit that belong to same ship.
        for (ArrayList<Object> pointHit : pointsTargeted)
        {
            // If point belongs to specified ship.
            if (pointHit.get(0).equals(name))
            {
                shipPoints.add(pointHit);
            }
        }
        // Will be at least two points that belong to that ship.
        ArrayList<Object> firstPoint = shipPoints.get(0);
        ArrayList<Object> secondPoint = shipPoints.get(1);

        // If their row values agree, then horizontal direction.
        if (firstPoint.get(1) == secondPoint.get(1))
        {
            toReturn = "Horizontal";
        }
        // Else if column values agree, then vertical direction.
        else if (firstPoint.get(2) == secondPoint.get(2))
        {
            toReturn = "Vertical";
        }
        return toReturn;
    }

    /**
     * Removes errant points from stack after finding out direction.
     *
     * @param row       successful shot's row
     * @param col       successful shot's col
     * @param direction direction ship is lying in
     */
    private void updateStack(int row, int col, String direction)
    {
        // Use iterator to remove elements from stack whilst iterating.
        Iterator<ShipPoint> iter = possibleHits.iterator();

        if (direction.equals("Vertical"))
        {
            while (iter.hasNext())
            {
                ShipPoint point = iter.next();
                /*
                 * If ship going vertically, remove all non-vertical
                 * points that belong to that ship.
                 */
                if (point.getCol() != col)
                {
                    iter.remove();
                }
            }
        }
        else if (direction.equals("Horizontal"))
        {
            while (iter.hasNext())
            {
                ShipPoint point = iter.next();
                /*
                 * If ship going horizontally, remove all non-horizontal
                 * points that belong to that ship.
                 */
                if (point.getRow() != row)
                {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Ship sunk so update lists and see if need to update parity.
     *
     * @param name sunk ship's name
     */
    private void updateOnSunkShip(String name)
    {
        //shipsSunk.add(name);
        shipsTargeted.remove(name);

        /*
         * Remove all the points of that ship from list.
         * Use an iterator to avoid a concurrent modification
         * exception (caused by removing items from an arrayList
         * while iterating through it).
         */
        Iterator<ArrayList<Object>> iter = pointsTargeted.iterator();

        while (iter.hasNext())
        {
            ArrayList<Object> point = iter.next();
            if (point.get(0).equals(name))
            {
                iter.remove();
            }
        }
        // Reset direction since ship sunk.
        orientation = "";
        updateExtantShipLengths(name);
    }

    private void updateExtantShipLengths(String shipName)
    {
        switch (shipName)
        {
            case "Carrier":
            {
                /*
                 * Cast to Integer because want to remove 5 itself,
                 * not whatever is at position 5.
                 */
                extantShipLengths.remove((Integer)5);
                break;
            }
            case "Battleship":
            {
                extantShipLengths.remove((Integer)4);
                break;
            }
            // Cruiser and Submarine both length 3.
            case "Cruiser":
            case "Submarine":
            {
                extantShipLengths.remove((Integer)3);
                break;
            }
            case "Destroyer":
            {
                extantShipLengths.remove((Integer)2);
                break;
            }
            default:
            {
                System.out.println("Illegal ship name. Exiting.");
                System.exit(1);
                break;
            }
        }
    }

    /**
     * Update parity based on ships sunk.
     */
//    private void updateParity()
//    {
//        switch (parity)
//        {
//            case 2:
//            {
//                // If destroyer sunk, only need to search every 3rd square.
//                if (shipsSunk.contains("Destroyer"))
//                {
//                    parity = 3;
//                }
//                break;
//            }
//            case 3:
//            {
//                // If both 3-ships sunk, only need to search every 4th square.
//                if (shipsSunk.contains("Cruiser") && shipsSunk.contains
//                        ("Submarine"))
//                {
//                    parity = 4;
//                }
//                break;
//            }
//            case 4:
//            {
//                // If all ships sunk except carrier, search every 5th square.
//                if (shipsSunk.contains("Battleship"))
//                {
//                    parity = 5;
//                }
//                break;
//            }
//            case 5:
//            {
//                // If get to this point, do nothing; AI has won.
//                break;
//            }
//            default:
//            {
//                System.out.println("Parity needs to be between 2-5. Exiting");
//                System.exit(1);
//                break;
//            }
//        }
//    }

    /**
     * Pushes onto stack the points around origin hit that are legal and not
     * yet attempted.
     *
     * @param originPoint the point that was hit
     * @param direction   direction of ship if known
     */
    private void addPointsAround(ShipPoint originPoint, String direction)
    {
        int originRow = originPoint.getRow();
        int originCol = originPoint.getCol();
        ShipPoint currPoint;
        int newRow;
        int newCol;

        /*
         * North.
         * Can't go north if at top row already.
         * And don't go north if ship is going horizontally.
         */
        if (originRow != 0 && !direction.equals("Horizontal"))
        {
            newRow = originRow-1;
            // Placeholder type; would be cheating to look at real type.
            currPoint = new ShipPoint('N', newRow,
                    originCol);
            // Make sure haven't already shot there.
            if (rawBoard[newRow][originCol] != 'X' &&
                    rawBoard[newRow][originCol] != 'O')
            {
                // Push to stack then.
                possibleHits.push(currPoint);
            }
        }
        /*
         * West.
         * Can't go west if at leftmost col already.
         * And don't go west if ship is going vertically.
         */
        if (originCol != 0 && !direction.equals("Vertical"))
        {
            newCol = originCol - 1;
            // Placeholder type; would be cheating to look at real type.
            currPoint = new ShipPoint('W', originRow,
                    newCol);
            // Make sure haven't already shot there.
            if (rawBoard[originRow][newCol] != 'X' &&
                    rawBoard[originRow][newCol] != 'O')
            {
                // Push to stack then.
                possibleHits.push(currPoint);
            }
        }
        // South.
        if (originRow != 9 && !direction.equals("Horizontal"))
        {
            newRow = originRow + 1;
            currPoint = new ShipPoint('T', newRow, originCol);
            // Make sure haven't already shot there.
            if (rawBoard[newRow][originCol] != 'X' &&
                    rawBoard[newRow][originCol] != 'O')
            {
                possibleHits.push(currPoint);
            }
        }
        // East.
        if (originCol != 9 && !direction.equals("Vertical"))
        {
            newCol = originCol + 1;
            currPoint = new ShipPoint('E', originRow, newCol);
            // Make sure haven't already shot there.
            if (rawBoard[originRow][newCol] != 'X' &&
                    rawBoard[originRow][newCol] != 'O')
            {
                possibleHits.push(currPoint);
            }
        }
    }
}
