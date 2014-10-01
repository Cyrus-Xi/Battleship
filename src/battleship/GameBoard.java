/*
 * Author: Cyrus Xi
 * Purpose: This class handles the game board setup and its access &
 * modification throughout the game.
 * Date: 09/20/14.
 */

package battleship;

import java.util.*;
import java.io.*;

/**
 * Represents a Gameboard object in Battleship.
 */
public class GameBoard 
{
    /**
     * 10 rows.
     */
	int numRows = 10;
    /**
     * 10 columns.
     */
	int numColumns = 10;
    /**
     * Actual primitive board is a 2D char array.
     */
	char[][] board;

    /**
     * 5 ships.
     */
	int NUM_SHIPS = 5;
    /**
     * The 5 ships take up 17 ship points.
     */
	int NUM_SHIP_POINTS = 17;
	
	/* 
	 * Use ArrayLists instead of raw arrays to easily add to end of list. 
	 * Don't need to keep track of index.
	 */

    /**
     * Array of ShipPoints.
     */
	ArrayList<ShipPoint> shipPoints = new ArrayList<ShipPoint>(NUM_SHIP_POINTS);

    /**
     * Array of Ship objects.
     */
	ArrayList<Ship> ships = new ArrayList<Ship>(5);

    /**
     * Array of ShipPoints that represents a carrier.
     */
	ArrayList<ShipPoint> rawCarrier = new ArrayList<ShipPoint>(5);
    /**
     * Array of ShipPoints that represents a battleship.
     */
	ArrayList<ShipPoint> rawBattleship = new ArrayList<ShipPoint>(4);
    /**
     * Array of ShipPoints that represents a cruiser.
     */
	ArrayList<ShipPoint> rawCruiser = new ArrayList<ShipPoint>(3);
    /**
     * Array of ShipPoints that represents a submarine.
     */
	ArrayList<ShipPoint> rawSubmarine = new ArrayList<ShipPoint>(3);
    /**
     * Array of ShipPoints that represents a destroyer.
     */
	ArrayList<ShipPoint> rawDestroyer = new ArrayList<ShipPoint>(2);
	
	/**
	 * Constructs game board with provided file.
	 * 
	 * @param  fname the name of the input file 
	 * @throws FileNotFoundException
	 */
	public GameBoard(String fname) throws FileNotFoundException
	{
		// The board is a 2-dimensional array of characters.
		board = new char[numRows][numColumns];
		
		// Append base filepath if running from command line..
		//fname = "src/" + fname;
		File file = new File(fname);
		
		// Suppress Eclipse's warning about unclosed scanner.
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(file);

		// Skip first line of file (column listing).
		scanner.nextLine();
		
		String line;
		char currChar;
		ShipPoint currShipPoint;

		for (int row = 0; row < numRows; ++row) 
		{
			line = scanner.nextLine();
			for (int col = 0; col < numColumns; ++col)
			{
				try 
				{
					// Offset to skip the numerical row headers.
					currChar = line.charAt(2+col);
					board[row][col] = currChar;
					
					// Make sure a ship point is here.
					if (currChar != ' ') 
					{
						currShipPoint = new ShipPoint(currChar, row, col);
						shipPoints.add(currShipPoint);
					}
				}
				/* 
				 * Handle case(s) where user didn't enter end-of-row 
				 * blank spaces.
				 */
				catch (StringIndexOutOfBoundsException e) 
				{
					board[row][col] = ' ';
				}
			}
		}
        // Instantiate ship objects from board.
		setUpShips();
	}
	
	/**
	 * Overloaded constructors. This one does not use a file. To be called on
     * replays of the game.
     *
     * @param random whether board should be randomized or not
	 */
	public GameBoard(boolean random)
	{	
		// Populate board with blank spaces.
		board = new char[numRows][numColumns];
		for (int j = 0; j < numRows; ++j)
		{
			for (int k = 0; k < numColumns; ++k)
			{
				board[j][k] = ' ';
			}
		}

        if (random)
        {
            randomizeBoard();
            setUpShips();
        }
	}

    /**
     * Randomizes board's ship placement.
     */
    private void randomizeBoard()
	{
		Random rand;
		int row;
		int col;
		
		boolean pointsWerePlaced;
		
		// 0 is North, 1 West, 2 South, 3 East.
		int dir;
		
		int[] sizes = {5, 4, 3, 3, 2};
		char[] types = {'A', 'B', 'C', 'S', 'D'};
		
		// Place ships in order of size, decreasing.
		for (int i = 0; i < NUM_SHIPS; ++i)
		{
			while(true)
			{
				rand = new Random();
				
				// Get a random "origin" point for ship.
				row = rand.nextInt((9 - 0) + 1);
				col = rand.nextInt((9 - 0) + 1);

				/*
				 *  Check if origin is unoccupied.
				 *  If occupied, get new random row & col values.
				 */
				if (board[row][col] == ' ')
				{
					rand = new Random();
					
					// Get random direction to place ship in.
                    // max - min + 1 (to make max inclusive).
					dir = rand.nextInt((3 - 0) + 1);

                    // Try to place ship in that direction.
					pointsWerePlaced = placePoints(row, col, dir, sizes[i], 
													types[i]);

					// Not enough space to place ship or path obstructed.
					if (pointsWerePlaced)
                    {
						// Enough space; ship placed so move on to next ship.
						break;
					}
				}
			}
		}
	}

    /**
     * Tries to place ship points from specified point in specified direction.
     *
     * @param row  row of origin point
     * @param col  col of origin point
     * @param dir  direction to place ship in
     * @param size ship size
     * @param type ship type
     * @return     true if placed ship points successfully
     */
	public boolean placePoints(int row, int col, int dir, int size,
								char type)
	{
		// Check that there's enough space in specified direction.
		switch (dir)
		{
			case 0:
				if (row < (size - 1))
				{
					return false;
				}
				break;
			case 1:
				if (col < (size - 1)) 
				{
					return false; 
				}
				break;
			case 2:
				if (row > (9 - size + 1))
				{
					return false;
				}
				break;
			case 3:
				if (col > (9 - size + 1))
				{
					return false;
				}
				break;
			default:
				System.out.println("Direction should be between 0 - 3. " +
                        "Exiting.");
				System.exit(1);
				break;
		}
		// Also check that the path is clear for the ship to be placed.
		if (!isPathClear(row, col, dir, size))
		{
			return false;
		}
		// Clear to place ship's points.
		else
		{
			populateInDir(row, col, dir, size, type);
			return true;
		}
	}
	
	/**
	 * Returns whether path is clear for ship to be placed.
	 * 
	 * @param row      row of origin point
	 * @param col      col of origin point
	 * @param dir      0 for North, 1 West, 2 South, 3 East
	 * @param shipSize ship's size
	 * @return         true if can place ship, false otherwise
	 */
	private boolean isPathClear(int row, int col, int dir, int shipSize)
	{
		// Origin point.
		char currPoint;

        // Use temporary marker to see if path is clear to place ship.
		for (int i = 0; i < shipSize; ++i)
		{
			currPoint = board[row][col];

			// Point is occupied; path not clear.
			if (currPoint != ' ') 
			{
				return false;
			}
			// Search in appropriate direction.
			switch (dir)
			{
				case 0:
					// Search northward.
					row--;
					break;
				case 1:
					// Search westward.
					col--;
					break;
				case 2:
					// Search southward.
					row++;
					break;
				case 3:
					// Search eastward.
					col++;
					break;
				default:
					System.out.println("Direction should be between 0 - 3. " +
                            "Exiting.");
					System.exit(1);
					break;
			}	
		}
		return true;
	}
	
	/**
	 * Place ship points along direction starting at origin point.
	 * 
	 * @param row  row of origin point
	 * @param col  col of origin point
	 * @param dir  direction to place ship in
	 * @param size size of ship, i.e., number of points to place
	 * @param type type of ship
	 */
	private void populateInDir(int row, int col, int dir, int size, char type)
	{
		ShipPoint currShipPoint;
		
		for (int i = 0; i < size; ++i)
		{
			board[row][col] = type;
			currShipPoint = new ShipPoint(type, row, col);
			shipPoints.add(currShipPoint);
			switch (dir)
			{
				case 0:
					// Place northward.
					row--;
					break;
				case 1:
					// Place westward.
					col--;
					break;
				case 2:
					// Place southward.
					row++;
					break;
				case 3:
					// Place eastward.
					col++;
					break;
				default:
					System.out.println("Direction should be between 0 - 3. " +
                            "Exiting.");
					System.exit(1);
					break;
			}	
		}
	}
	
	/**
	 * Updates game board after a shot.
	 * 
	 * @param belongsToComputer true if board being updated is computer's
	 * @param rawRow            user-input row number
	 * @param rawCol            user-input col letter
	 */
	public String updateBoardAfterShot(boolean belongsToComputer, int rawRow,
                                  char rawCol)
	{
		// Subtract 1 because board is 0-indexed.
		int row = rawRow - 1;
		// Convert column letter to numerical index.
		int col = "ABCDEFGHIJ".indexOf(rawCol);

        // Message to be printed.
        String message = "";
		
		// A miss.
		if (board[row][col] == ' ') 
		{
			// Mark as a miss.
			board[row][col] = 'O';

            // Print out differing messages based on whether computer's or not.
			if (belongsToComputer)
			{
				System.out.printf("\n%d%s was a miss. Better luck next " +
                        "time!\n", rawRow, rawCol);
			}
			else
			{
				System.out.printf("The computer missed with %d%s!\n",
                        rawRow, rawCol);
			}
		}
		// Already shot here. Neither hit or miss.
		else if (board[row][col] == 'X' || board[row][col] == 'O')
		{
			System.out.printf("\nAlready shot at %d%s!\n", rawRow, rawCol);
		}
		// Else must be a hit.
		else
		{
			// Get appropriate string to be outputted.
			if (belongsToComputer)
			{
				message = getHitMessage(true, row, col);
				System.out.printf("\n%d%s was a hit. %s", rawRow,
                        rawCol, message);
			}
			else
			{
				message = getHitMessage(false, row, col);
				System.out.printf("The computer hit with %d%s. %s", rawRow,
                        rawCol, message);
			}
			// Mark as a hit.
			board[row][col] = 'X';
		}
        // For the benefit of the computer's strategy.
        return message;
	}
	
	/**
	 * Gets appropriate successful shot message and updates the hit ship object.
	 * 
	 * @param belongsToComputer true if board belongs to computer
	 * @param row               row index of hit
	 * @param col				col index of hit
	 * @return String           message to be outputted
	 */
	private String getHitMessage(boolean belongsToComputer, int row, int col)
	{
		StringBuilder message = new StringBuilder();
		if (belongsToComputer)
		{
			message.append("Congratulations, you struck a ");
		}
		else
		{
			message.append("\nUnfortunately, it struck your ");
		}
		
		char type = board[row][col];
		Ship currShip = null;
		
		/* 
		 * Access currShip by index. Ships will always be in same position
		 * even when sunk, because each ship is just an object containing an
		 * array of ShipPoints. Being sunk merely means the ship has an empty
		 * array of the points.
		 */
		switch (type) 
		{
			case 'A':
				message.append("Carrier");
				currShip = ships.get(0);
				break;
			case 'B':
				message.append("Battleship");
				currShip = ships.get(1);
				break;
			case 'C':
				message.append("Cruiser");
				currShip = ships.get(2);
				break;
			case 'S':
				message.append("Submarine");
				currShip = ships.get(3);
				break;
			case 'D':
				message.append("Destroyer");
				currShip = ships.get(4);
				break;
			default:
				System.out.printf("Something went wrong! %s isn't a legal " +
                        "ship type.", type);
				System.exit(1);
				break;
		}
		// Update ship, i.e., remove hit ship point from array.
		currShip.updateShipAfterShot(row, col);

		if (currShip.isSunk()) 
		{
			message.append(" and sunk it!\n");
		}
		else
		{
			message.append(".. but did not sink it.\n");
		}
		return message.toString();
	}
	
	/**
	 * Find each ship on the board and create appropriate Ship objects.
	 * Called after game board initialization. 
	 */
	public void setUpShips()
	{
		ShipPoint currShipTuple;
		char shipType;
		
		// Add each ship point to its ship array.
		for (int i = 0; i < NUM_SHIP_POINTS; ++i)
		{
			currShipTuple = shipPoints.get(i);
			shipType = currShipTuple.getType();
			switch (shipType) 
			{
				case 'A':
					rawCarrier.add(currShipTuple);
					break;
				case 'B':
					rawBattleship.add(currShipTuple);
					break;
				case 'C':
					rawCruiser.add(currShipTuple);
					break;
				case 'S':
					rawSubmarine.add(currShipTuple);
					break;
				case 'D':
					rawDestroyer.add(currShipTuple);
					break;
				default:
					System.out.println("Something went wrong! Check that " +
                            "you have the right number & type of ships " +
                            "placed and the right number of points per ship.");
					System.exit(1);
					break;
			}
		}
		
		/* 
		 * Create ship objects from arrays of ship points and 
		 * add ship objects to ships array. 
		 */
		Ship carrier = new Ship(rawCarrier);
		ships.add(carrier);
		Ship battleship = new Ship(rawBattleship);
		ships.add(battleship);
		Ship cruiser = new Ship(rawCruiser);
		ships.add(cruiser);
		Ship submarine = new Ship(rawSubmarine);
		ships.add(submarine);
		Ship destroyer = new Ship(rawDestroyer);
		ships.add(destroyer);
	}
	
	/**
	 * Returns the underlying 2D array.
	 * 
	 * @return a 2D array representing the game board
	 */
	public char[][] getBoard()
	{
		return board;
	}
	
	/**
	 * Returns String representation of each ship object on board.
	 * 
	 * @return representation of each ship
	 */
	public String getShips()
	{
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < 5; ++i)
		{
			result.append(ships.get(i));
			result.append("\n");
		}
		return result.toString();
		
	}
	
	/**
	 * Checks if there are any ships left on board. If not, returns true.
	 * <p>
	 * Must iterate through and check whether each ship sunk manually, because
	 * ships.isEmpty() will return false even if every ship has been sunk.
	 * A ship's sinking does not remove it from the ships array; it just means
	 * it contains an empty array of ShipPoints.
	 * 
	 * @return true if there are no ships left. Other player has won.
	 */
	public boolean areNoShipsLeft()
	{
		Ship currShip;
		
		for (int i = 0; i < NUM_SHIPS; ++i)
		{
			currShip = ships.get(i);
			// If a single ship is not yet sunk, still ships left.
			if (!currShip.isSunk())
			{
				return false;
			}
		}
		// True if all ships sunk.
		return true;
	}

	/** 
	 * Replaces default toString() with a much more reader-friendly version. 
	 * 
	 * @return representation of game board
	 */
    @Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		
		// Include column header.
		result.append("  ABCDEFGHIJ\n");
		
		for (int i = 0; i < numRows; ++i) 
		{
			// Include row header.
			if (i != 9) 
			{
				// Need blank space because of the 10th row.
				result.append(" ");
			}
			result.append(i+1);
			
			for (int j = 0; j < numColumns; ++j)
			{
				result.append(board[i][j]);
			}
			result.append("\n");
		}
		return result.toString();
	}

}
