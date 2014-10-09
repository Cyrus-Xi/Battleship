/*
 * Author: Cyrus Xi
 * Purpose: This class represents the Computer player. It implements the 
 * 			"strategy" element.
 * Date: 09/20/14.
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

    // 10x10 board.
    BoardSpace [][] boardSpaces = new BoardSpace [10][10];

    /**
     * Row & col of point to be shot at.
     */
    int row;
    int col;

    /**
     * The spacing of shots. E.g., when parity is 2, list of points to be
     * considered are those that are exactly 2 spaces apart.
     */
    int parity = 2;

    /**
     * At minimum (when destroyer not yet sunk), only need to search every
     * "other" space because smallest ship is of size 2.
     */
    int [][] twoParityBoard = new int [][]
        {
            {0, 0}, {0, 2}, {0, 4}, {0, 6}, {0, 8},
            {1, 1}, {1, 3}, {1, 5}, {1, 7}, {1, 9},
            {2, 0}, {2, 2}, {2, 4}, {2, 6}, {2, 8},
            {3, 1}, {3, 3}, {3, 5}, {3, 7}, {3, 9},
            {4, 0}, {4, 2}, {4, 4}, {4, 6}, {4, 8},
            {5, 1}, {5, 3}, {5, 5}, {5, 7}, {5, 9},
            {6, 0}, {6, 2}, {6, 4}, {6, 6}, {6, 8},
            {7, 1}, {7, 3}, {7, 5}, {7, 7}, {7, 9},
            {8, 0}, {8, 2}, {8, 4}, {8, 6}, {8, 8},
            {9, 1}, {9, 3}, {9, 5}, {9, 7}, {9, 9}
        };

    /**
     * When destroyer sunk, only need to search every 3rd space.
     */
    int [][] threeParityBoard = new int [][]
        {
            {0, 0}, {0, 3}, {0, 6}, {0, 9},
            {1, 1}, {1, 4}, {1, 7},
            {2, 2}, {2, 5}, {2, 8},
            {3, 0}, {3, 3}, {3, 6}, {3, 9},
            {4, 1}, {4, 4}, {4, 7},
            {5, 2}, {5, 5}, {5, 8},
            {6, 0}, {6, 3}, {6, 6}, {6, 9},
            {7, 1}, {7, 4}, {7, 7},
            {8, 2}, {8, 5}, {8, 8},
            {9, 0}, {9, 3}, {9, 6}, {9, 9},
        };

    /**
     * When cruiser and submarine sunk, only need to search every 4th space.
     */
    int [][] fourParityBoard = new int [][]
        {
            {0, 0}, {0, 4}, {0, 8},
            {1, 1}, {1, 5}, {1, 9},
            {2, 2}, {2, 6},
            {3, 3}, {3, 7},
            {4, 0}, {4, 4}, {4, 8},
            {5, 1}, {5, 5}, {5, 9},
            {6, 2}, {6, 6},
            {7, 3}, {7, 7},
            {8, 0}, {8, 4}, {8, 8},
            {9, 1}, {9, 5}, {9, 9},
        };

    /**
     * When only carrier left, only need to search every 5th space.
     */
    int [][] fiveParityBoard = new int [][]
        {
            {0, 0}, {0, 5},
            {1, 1}, {1, 6},
            {2, 2}, {2, 7},
            {3, 3}, {3, 8},
            {4, 4}, {4, 9},
            {5, 0}, {5, 5},
            {6, 1}, {6, 6},
            {7, 2}, {7, 7},
            {8, 3}, {8, 8},
            {9, 4}, {9, 9},
        };

    /**
     * A stack of points to be shot at next after a successful shot.
     * This represents the "Target" phase where the AI tries to finish off a
     * ship it has hit. The other phase is the "Hunt" one where the AI tries
     * to find a ship.
     */
    Stack<ShipPoint> possibleHits = new Stack<ShipPoint>();

    /**
     * Ships that the AI has sunk.
     */
    ArrayList<String> shipsSunk = new ArrayList<String>();

    /**
     * Ships currently being targeted.
     */
    ArrayList<String> shipsTargeted = new ArrayList<String>();

    /**
     * Whether opponent's ship is going horizontally or vertically.
     */
    String direction = "";

    /**
     * Array of pointHits, successful shots.
     */
    ArrayList<ArrayList<Object>> pointsTargeted = new
            ArrayList<ArrayList<Object>>();

    /**
     * To convert random col to column letter.
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
        setSpaceCounts();
        ShipPoint currPoint;

        String message;
        char type;

        // Whether ship has been sunk.
        boolean sunk = false;

        // Get right board to be considered.
        int[][] parityBoard = getRightBoard(parity);

        // No stack of possible hits (i.e., not trying to finish off hit
        // ships) so get random row & col values for shot.
        if (possibleHits.isEmpty())
        {
            setHuntShot(parityBoard);
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
                addPointsAround(currPoint, direction);
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
            addPointsAround(shipPointLeft, direction);
        }
    }

    private void setSpaceCounts()
    {
        // Clear counters.
        for (int i = 0; i < 10; ++i)
        {
            for (int j = 0; j < 10; j++)
            {
                boardSpaces[i][j].clearCounter();
            }
        }

        boolean isPlaceable;
        // Ship sizes from biggest to smallest.
        int[] sizes = {5, 4, 3, 3, 2};
        // Try to place each ship first horizontally then vertically.

        // Horizontal placing. i = row index.
        for (int i = 0; i < 10; ++i)
        {
            // For each ship to be placed.
            for (int k = 0; k < 5; ++k)
            {
                // Keep going until go out of bounds. Last legal column index
                // is equivalent to 10 - sizes[k].
                for (int realJ = 0; realJ < 10 - sizes[k]; ++realJ)
                {
                    int tempJ = realJ;
                    // And thus whether must increment space counters.
                    isPlaceable = true;
                    // Go for the length of the current ship to be "placed."
                    for (int p = 0; p < sizes[k]; ++p, tempJ++)
                    {
                        /*
                         * An 'X' is also just an obstruction because since
                         * stack is empty, that ship has already been sunk.
                         */
                        if ((rawBoard[i][tempJ] == 'X') ||
                                (rawBoard[i][tempJ] == 'O'))
                        {
                            isPlaceable = false;
                            break;
                        }

//                        /*
//                         * Trying to finish a ship or ships off.
//                         * Give heavy weighting to possible ship placings
//                         * that go through successful hits.
//                         */
//                        else
//                        {
//
//                        }
                    }
                    // If can place ship over spaces, increment their counters.
                    if (isPlaceable)
                    {
                        // Reset tempJ.
                        tempJ = realJ;
                        for (int q = 0; q < sizes[k]; ++q, tempJ++)
                        {
                            boardSpaces[i][tempJ].incrementCounter();
                        }
                    }
                }
            }
        }

        // Symmetrical to horizontal placing because board is square.
        // Vertical placing. i = col index.
        for (int i = 0; i < 10; ++i)
        {
            // For each ship to be placed.
            for (int k = 0; k < 5; ++k)
            {
                // Keep going until go out of bounds. Last legal column index
                // is equivalent to 10 - sizes[k].
                for (int realJ = 0; realJ < 10 - sizes[k]; ++realJ)
                {
                    int tempJ = realJ;
                    // And thus whether must increment space counters.
                    isPlaceable = true;
                    // Go for the length of the current ship to be "placed."
                    for (int p = 0; p < sizes[k]; ++p, tempJ++)
                    {
                        if ((rawBoard[tempJ][i] == 'X') ||
                                (rawBoard[tempJ][i] == 'O'))
                        {
                            isPlaceable = false;
                            break;
                        }
                    }
                    // If can place ship over spaces, increment their counters.
                    if (isPlaceable)
                    {
                        // Reset tempJ.
                        tempJ = realJ;
                        for (int q = 0; q < sizes[k]; ++q, tempJ++)
                        {
                            boardSpaces[tempJ][i].incrementCounter();
                        }
                    }
                }
            }
        }
        // Print counters.
        System.out.println("Counters:\n");
        for (int i = 0; i < 10; ++i)
        {
            for (int j = 0; j < 10; j++)
            {
                System.out.print(boardSpaces[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Depending on the parity, get the right 2d array to use.
     *
     * @param  parity number of spaces between considered shots
     * @return 2D int array representing shots that should be taken
     */
    private int[][] getRightBoard(int parity)
    {
        int[][] parityBoard = new int[0][0];
        if (parity == 2)
        {
            parityBoard = twoParityBoard;
        }
        else if (parity == 3)
        {
            parityBoard = threeParityBoard;
        }
        else if (parity == 4)
        {
            parityBoard =  fourParityBoard;
        }
        else if (parity == 5)
        {
            parityBoard =  fiveParityBoard;
        }
        return parityBoard;
    }

    /**
     * Using parity board, set random point.
     *
     * @param pBoard parity board to be used in getting random point
     */
    private void setHuntShot(int[][] pBoard)
    {
        ArrayList<BoardSpace> flatList = new ArrayList<BoardSpace>();
        for (int i = 0; i < 10; ++i)
        {
            for (BoardSpace space : boardSpaces[i])
            {
                flatList.add(space);
            }
        }
        /*
         * Sort in descending order based on counter.
         * Now the space with the highest counter is at the beginning.
         */
        Collections.sort(flatList, Collections.reverseOrder());

        row = flatList.get(0).getRow();
        col = flatList.get(0).getCol();

        int index = 1;
        while ((rawBoard[row][col] == 'X') || (rawBoard[row][col] == 'O'))
        {
            row = flatList.get(index).getRow();
            col = flatList.get(index).getCol();
            index++;
        }
    }

    /**
     * Updates on shot being a hit: gets direction, figures if sunk, etc.
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
                if (direction.equals(""))
                {
                    direction = getDirection(shipName);
                }
                /*
                 * Now know direction, but could've pushed errant points
                 * before. Correct stack.
                 */
                updateStack(row, col, direction);
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
        shipsSunk.add(name);
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
        direction = "";
        updateParity();
    }

    /**
     * Update parity based on ships sunk.
     */
    private void updateParity()
    {
        switch (parity)
        {
            case 2:
            {
                // If destroyer sunk, only need to search every 3rd square.
                if (shipsSunk.contains("Destroyer"))
                {
                    parity = 3;
                }
                break;
            }
            case 3:
            {
                // If both 3-ships sunk, only need to search every 4th square.
                if (shipsSunk.contains("Cruiser") && shipsSunk.contains
                        ("Submarine"))
                {
                    parity = 4;
                }
                break;
            }
            case 4:
            {
                // If all ships sunk except carrier, search every 5th square.
                if (shipsSunk.contains("Battleship"))
                {
                    parity = 5;
                }
                break;
            }
            case 5:
            {
                // If get to this point, do nothing; AI has won.
                break;
            }
            default:
            {
                System.out.println("Parity needs to be between 2-5. Exiting");
                System.exit(1);
                break;
            }
        }
    }

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
