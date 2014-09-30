/*
 * Author: Cyrus Xi
 * Purpose: This class represents the Computer player. It implements the 
 * 			"strategy" element.
 * Date: 09/20/14.
 */

package battleship;

import java.util.*;

public class Computer
{
	/* 
	 * Have it store human player's game board.
	 * Though the board will have the player's ship placement, the computer
	 * will not, of course, actually use that information in its strategy (i.e., 
	 * where it chooses to shoot each turn).
	 */
	GameBoard ofOpponent;
	char [][] rawBoard;
	int row;
	int col;

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

	Stack<ShipPoint> possibleHits = new Stack<ShipPoint>();

    ArrayList<String> shipsSunk = new ArrayList<String>();

    ArrayList<String> shipsTargeted = new ArrayList<String>();

    // Whether ship is going horizontally or vertically.
    String direction = "";

    // Array of pointhit arrays.
    ArrayList<ArrayList<Object>> pointsTargeted = new
            ArrayList<ArrayList<Object>>();

	// To convert random col to column letter.
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
		ShipPoint currPoint;

        String message;
        String shipName;

        char type;

        // Array of shipname, row, col.
        ArrayList<Object> pointHit = new ArrayList<Object>();
        boolean sunk = false;

		Random rand;
        int boardIndex;
        int[][] parityBoard = getRightBoard(parity);

		/*
		 * Stack being empty represents Hunt phase (looking for ships);
         * stack not being empty represents Target phase (finishing off ships
         * that were hit).
         */

        // Shoot randomly using appropriate parity board if nothing on stack.
		if (possibleHits.empty())
		{
            System.out.println("HUNT: Shoot randomly");
			/* 
			 * Loop until get random index that results in row & col values
			 * that represent a point not yet shot at.
			 */
			while (true)
			{
				rand = new Random();
                int max = parityBoard.length;
                /*
				 * Exclusive of max value, which is what we want since we're
				 * using the array's length as max.
				 */
                boardIndex = rand.nextInt(max);
				row = parityBoard[boardIndex][0];
				col = parityBoard[boardIndex][1];
				
				/* If haven't shot there yet, exit loop. */
				if (rawBoard[row][col] != 'X' && rawBoard[row][col] != 'O')
				{
					break;
				}
			}
		}
        // Else use stack of possible hits (like a depth-based search).
		else
		{
            System.out.println("TARGET: Use stack, not randomly shoot");
			currPoint = possibleHits.pop();
			row = currPoint.getRow();
			col = currPoint.getCol();
		}
		type = rawBoard[row][col];
		message = ofOpponent.updateBoardAfterShot(false, row+1,
                columnHeaders[col]);

        // If shot was a hit (non-empty message).
        if (!message.equals(""))
        {
            System.out.println("Shot was a hit");
            shipName = getNameFromMessage(message);
            sunk = isSunk(message);

            pointHit.add(shipName);
            pointHit.add(row);
            pointHit.add(col);
            System.out.printf("Adding this point: %s, %d, %d\n", shipName,
                    row, col);
            pointsTargeted.add(pointHit);
            System.out.println("Printing out shipsTargeted");
            for (String name : shipsTargeted)
            {
                System.out.println(name);
            }
            if (!sunk)
            {
                System.out.println("Not sunk");
                // Ensure no duplicates.
                if (!shipsTargeted.contains(shipName))
                {
                    System.out.println("Haven't hit yet; adding");
                    // Add ship hit to list.
                    shipsTargeted.add(shipName);
                    System.out.printf("name: %s\n", shipName);
                    System.out.println("Printing out shipsTargeted");
                    for (String name : shipsTargeted)
                    {
                        System.out.println(name);
                    }

                }
                // Already hit once so can determine direction.
                else
                {
                    System.out.println("Hit already");
                    // Get direction if don't already know.
                    if (direction.equals(""))
                    {
                        direction = getDirection(shipName);
                    }
                    System.out.printf("Direction: %s\n", direction);
                    /*
                     * Now know direction, but could've pushed errant points
                     * before. Correct stack.
                     */
                     updateStack(row, col, direction);
                }
            }
            // If sunk, remove from list and see if need to update parity.
            else
            {
                System.out.println("Sunk!");
                System.out.println("Remove from lists & update parity");
                shipsSunk.add(shipName);
                shipsTargeted.remove(shipName);

                /*
                 * Remove all the points of that ship from list.
                 * Use an iterator to avoid a concurrent modification
                 * exception (caused by removing items from an arraylist
                 * while iterating through it).
                 */
                Iterator<ArrayList<Object>> iter = pointsTargeted.iterator();

                while (iter.hasNext())
                {
                    ArrayList<Object> point = iter.next();

                    if (point.get(0).equals(shipName))
                    {
                        iter.remove();
                    }
                }
                // Reset direction since ship sunk.
                direction = "";
                updateParity();
            }

            /*
             * No ships targeted; would be inefficient to keep popping stack.
             * AI would then keep looking around the perimeter of a ship even
               when it has been sunk.
             */
            if (shipsTargeted.isEmpty())
            {
                System.out.println("Empty stack, go to HUNT phase");
                // Clear stack and return to Hunt phase.
                while (!possibleHits.empty())
                {
                    possibleHits.pop();
                }
            }
        }

        /*
         * When hit a ship, first determine its name and whether it was sunk.
         * If not sunk, append to array IF not already on array. If sunk,
         * delete that ship from array. If array empty, end Target phase.
         */

        rawBoard = ofOpponent.getBoard();
        /*
         * Only add plausible candidates to stack if still in Target phase and
         * if didn't just sink a ship.
         */
        if (!shipsTargeted.isEmpty() && !sunk)
        {
            // If shot was successful.
            if (rawBoard[row][col] == 'X')
            {
                currPoint = new ShipPoint(type, row, col);

                System.out.println("Add points to stack");
                // Add feasible points around successful hit to stack.
                addPointsAround(currPoint, direction);
            }
        }
        /*
         * If still in target phase and did just sink a ship.
         * Go back to a point of another ship that was hit and push possible
         * hits to stack using that.
         */
        else if (!shipsTargeted.isEmpty() && sunk)
        {
            System.out.printf("Sunk: %b\n", sunk);
            ArrayList<Object> pointLeft = pointsTargeted.get(0);
            int rowLeft = (Integer)pointLeft.get(1);
            int colLeft = (Integer)pointLeft.get(2);

            ShipPoint shipPointLeft = new ShipPoint('Z', rowLeft, colLeft);
            addPointsAround(shipPointLeft, direction);
        }
	}

    /**
     * Depending on the parity, get the right 2d array to use.
     *
     * @param parity
     * @return
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

    private String getDirection(String name)
    {
        String toReturn = "";

        System.out.printf("Ship name: %s\n", name);

        ArrayList<ArrayList<Object>> shipPoints = new ArrayList<ArrayList
                <Object>>();
        // Get points hit that belong to same ship.
        for (ArrayList<Object> pointHit : pointsTargeted)
        {
            System.out.printf("Point hit: %s\n", pointHit.get(0));
            // If point belongs to specified ship.

            if (pointHit.get(0).equals(name))
            {
                System.out.printf("Name of ship's point: %s\n",
                        pointHit.get(0));
                shipPoints.add(pointHit);
            }
        }

        // Will be at least two points that belong to that ship.
        ArrayList<Object> firstPoint = shipPoints.get(0);
        ArrayList<Object> secondPoint = shipPoints.get(1);
        System.out.printf("First point: %s %d %d\n", firstPoint.get(0),
                firstPoint.get(1), firstPoint.get(2));
        System.out.printf("Second point: %s %d %d\n", secondPoint.get(0),
                secondPoint.get(1), secondPoint.get(2));

        // If their row values agree, then horizontal direction.
        if (firstPoint.get(1) == secondPoint.get(1))
        {
            System.out.printf("Agree on row: %s\n", firstPoint.get(1));
            toReturn = "Horizontal";
        }
        // Else if column values agree, then vertical direction.
        else if (firstPoint.get(2) == secondPoint.get(2))
        {
            System.out.printf("Agree on col: %s\n", firstPoint.get(2));
            toReturn = "Vertical";
        }
        System.out.printf("Direction: %s\n", toReturn);
        return toReturn;
    }

    private void updateStack(int row, int col, String direction)
    {
        System.out.printf("row, col, direction: %d, %d, %s\n",
                row, col, direction);

        // Use iterator to remove elements from stack whilst iterating.
        Iterator<ShipPoint> iter = possibleHits.iterator();

        if (direction.equals("Vertical"))
        {
            while (iter.hasNext())
            {
                ShipPoint point = iter.next();
                /*
                 * If ship going horizontally, remove all non-vertical
                 * points that belong to that ship.
                 */
                if (point.getCol() != col)
                {
                    System.out.println("Removing horizontal point");
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
                    System.out.println("Removing vertical point");
                    iter.remove();
                }
            }
        }
    }

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
        System.out.printf("Parity is now: %d\n", parity);
    }

	/**
	 * Pushes onto stack the points around origin that are legal and not yet attempted.
	 * 
	 * @param originPoint the point that was hit
	 */
	private void addPointsAround(ShipPoint originPoint, String direction)
	{
		int originRow = originPoint.getRow();
		int originCol = originPoint.getCol();
		ShipPoint currPoint;

        int newRow;
        int newCol;

        System.out.printf("Direction: %s\n", direction);

		/*
		 * North.
		 * Can't go north if at top row already.
		 * And don't go north if ship is going horizontally.
		 */
		if (originRow != 0 && !direction.equals("Horizontal"))
		{
            System.out.println("Adding north point");
            newRow = originRow-1;
            // Placeholder type; would be cheating to look at real type.
            currPoint = new ShipPoint('N', newRow,
                    originCol);
			/* Make sure haven't already shot there. */
            if (rawBoard[newRow][originCol] != 'X' &&
                    rawBoard[newRow][originCol] != 'O')
            {
				/* Push to stack then. */
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
            System.out.println("Adding west point");
            newCol = originCol - 1;
            // Placeholder type; would be cheating to look at real type.
			currPoint = new ShipPoint('W', originRow,
                    newCol);
			/* Make sure haven't already shot there. */
            if (rawBoard[originRow][newCol] != 'X' &&
                    rawBoard[originRow][newCol] != 'O')
            {
				/* Push to stack then. */
                possibleHits.push(currPoint);
            }
		}
		// South.
		if (originRow != 9 && !direction.equals("Horizontal"))
		{
            System.out.println("Adding south point");
            newRow = originRow + 1;
            // Placeholder type; would be cheating to look at real type.
			currPoint = new ShipPoint('T', newRow, originCol);
			/* Make sure haven't already shot there. */
            if (rawBoard[newRow][originCol] != 'X' &&
                    rawBoard[newRow][originCol] != 'O')
			{
				/* Push to stack then. */
				possibleHits.push(currPoint);
			}
		}
		// East.
		if (originCol != 9 && !direction.equals("Vertical"))
		{
            System.out.println("Adding east point");
            newCol = originCol + 1;
            // Placeholder type; would be cheating to look at real type.
			currPoint = new ShipPoint('E', originRow, newCol);
			/* Make sure haven't already shot there. */
            if (rawBoard[originRow][newCol] != 'X' &&
                    rawBoard[originRow][newCol] != 'O')
            {
				/* Push to stack then. */
                possibleHits.push(currPoint);
            }
		}
	}
    // Use parity. Increase parity when all ships with that parity have been
    // sunk. For example, if parity is at 3 (search within every 3rd square),
    // and both 3-size ships have been sunk, parity goes up to 4 because only
    // the 4-ship and 5-ship can be left.

    /*
     * Parity starts at 2. Get a random index and search within the array
     * associated with that board (don't delete board elements in array,
     * they'll already be marked as searched). Keep going until hit a ship.
     * When hit a ship, get what ship it was, and add to stack until that
     * specific ship has been sunk, and continue until all ships hit have
     * been sunk. Increment parity if a ship sunk had size equal to it
     * (unless 3 parity, b/c then need to have sunk both 3-ships). Then
     * discard the stack and start over with hunt phase using new parity
     * board if necessary.
     */

}
