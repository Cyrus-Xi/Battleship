/*
 * Author: Cyrus Xi
 * Purpose: This class handles the game board setup and its 
 * 			access & modification throughout the game.
 * Date: 09/20/14.
 */

package battleship;

import java.util.*;
import java.io.*;

public class GameBoard 
{	
	/* 10 x 10 board. */
	int numRows = 10;
	int numColumns = 10;
	char[][] board;
	
	/* The 5 ships will take up 17 board spaces. */
	int NUM_SHIPS = 5;
	int NUM_SHIP_POINTS = 17;
	
	/* 
	 * Use ArrayLists instead of raw arrays to easily add to end of list. 
	 * Don't need to keep track of index.
	 */
	ArrayList<ShipPoint> shipPoints = new ArrayList<ShipPoint>(NUM_SHIP_POINTS);
	
	ArrayList<Ship> ships = new ArrayList<Ship>(5);
	
	ArrayList<ShipPoint> rawCarrier = new ArrayList<ShipPoint>(5);
	ArrayList<ShipPoint> rawBattleship = new ArrayList<ShipPoint>(4);
	ArrayList<ShipPoint> rawCruiser = new ArrayList<ShipPoint>(3);
	ArrayList<ShipPoint> rawSubmarine = new ArrayList<ShipPoint>(3);
	ArrayList<ShipPoint> rawDestroyer = new ArrayList<ShipPoint>(2);
	
	/**
	 * Constructs game board with provided file.
	 * 
	 * @param  fname the name of the input file 
	 * @throws FileNotFoundException
	 */
	public GameBoard(String fname) throws FileNotFoundException
	{
		/* The board is a 2-dimensional array of characters. */
		board = new char[numRows][numColumns];
		
		/* Append base filepath to get complete filepath. */
		//String fullFname = "src/" + fname;
		File file = new File(fname);
		
		/* Suppress Eclipse's warning about unclosed scanner. */
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(file);
		//System.out.println("Yay it worked!");
		/* Skip first line of file (column listing). */
		scanner.nextLine();
		
		String line;
		char currChar;
		ShipPoint currShipPoint;
		for (int row = 0; row < numRows; ++row) 
		{
			line = scanner.nextLine();
			//System.out.println(line);
			for (int col = 0; col < numColumns; ++col)
			{
				try 
				{
					/* Offset to skip the numerical row headers. */
					//System.out.printf("{%s}", line.charAt(2+col));
					//System.out.printf("%d, %d\n", row, col);
					currChar = line.charAt(2+col);
					board[row][col] = currChar;
					
					/* Make sure a ship point is here. */
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
					//System.out.println("Catching exception!");
					board[row][col] = ' ';
				}
			}
		}
		setUpShips();
	}
	
	/**
	 * Overloaded constructors. This one does not use a file.
	 * To be called on replays of the game.
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

    public void randomizeBoard()
	{
		Random rand;
		int row;
		int col;
		
		boolean pointsWerePlaced;
		
		/* 0 is North, 1 West, 2 South, 3 East. */
		int dir;
		
		int[] sizes = {5, 4, 3, 3, 2};
		char[] types = {'A', 'B', 'C', 'S', 'D'};
		
		// Place ships in order of size, decreasing.
		for (int i = 0; i < NUM_SHIPS; ++i)
		{
			for (int j = 0; j < numRows; ++j)
			{
				for (int k = 0; k < numColumns; ++k)
				{
					if (board[j][k] == ' ')
					{
						System.out.print('_');
					}
					else
					{
						System.out.print(board[j][k]);
					}
				}
				System.out.println();
			}
			System.out.println("i = " + i + "\n");
			
			while(true)
			{
				rand = new Random();
				
				// Get a random "origin" point for ship.
				row = rand.nextInt((9 - 0) + 1);
				col = rand.nextInt((9 - 0) + 1);

				System.out.printf("row: %d, col: %d\n", row, col);

				/*
				 *  Check if origin is unoccupied.
				 *  If so, get new random row & col values.
				 */
				if (board[row][col] != ' ')
				{
					System.out.println("Occupied! with " + board[row][col]);
					continue;
				}
				else
				{
					rand = new Random();
					
					// Get random direction to place ship in.
					dir = rand.nextInt((3 - 0) + 1) + 0;
					System.out.println("Direction: " + dir);
					
					pointsWerePlaced = placePoints(row, col, dir, sizes[i], 
													types[i]);
					// Not enough space to place ship or path obstructed.
					if (!pointsWerePlaced)
					{
						// Get new random origin and direction.
						continue;
					}
					else
					{
						// Ship placed so move on to next ship.
						break;
					}
				}
			}
		}
	}
	
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
				System.out.println("Direction should be between 0 - 3. Exiting.");
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
	 * @param  row
	 * @param  col
	 * @param  dir 0 for North, 1 West, 2 South, 3 East
	 * @param  shipSize
	 * @return true if successfully placed ship, false otherwise
	 */
	private boolean isPathClear(int row, int col, int dir, int shipSize)
	{
		// Origin point.
		char currPoint;
		
		for (int i = 0; i < shipSize; ++i)
		{
			currPoint = board[row][col];
			// Point is occupied; path not clear.
			if (currPoint != ' ') 
			{
				return false;
				//System.out.println("No clear path.");
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
					// South eastward.
					col++;
					break;
				default:
					System.out.println("Direction should be between 0 - 3. Exiting.");
					System.exit(1);
					break;
			}	
		}
		return true;
	}
	
	/**
	 * Place ship points along direction starting at origin point.
	 * 
	 * @param row  the row of origin point
	 * @param col  the col of origin point
	 * @param dir  the direction to place ship in
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
					System.out.println("Direction should be between 0 - 3. Exiting.");
					System.exit(1);
					break;
			}	
		}
	}
	
	/**
	 * Updates game board after a shot.
	 * 
	 * @param belongsToComputer true if board being updated is computer's
	 * @param rawRow            user-inputted row number
	 * @param rawCol            user-inputted col letter
	 */
	public void updateBoardAfterShot(boolean belongsToComputer, int rawRow, char rawCol)
	{
		// Subtract 1 because board is 0-indexed.
		int row = rawRow - 1;
		// Convert column letter to numerical index.
		int col = "ABCDEFGHIJ".indexOf(rawCol);
		
		/* A miss. */
		if (board[row][col] == ' ') 
		{
			/* Mark it as a miss. */
			board[row][col] = 'O';
			if (belongsToComputer)
			{
				System.out.printf("\n%d%s was a miss. Better luck next time!\n", rawRow, rawCol);
			}
			else
			{
				System.out.printf("The computer missed with %d%s!\n", rawRow, rawCol);
			}
		}
		/* Already shot here. Neither hit nor miss. */
		else if (board[row][col] == 'X' || board[row][col] == 'O')
		{
			System.out.printf("\nAlready shot at %d%s!\n", rawRow, rawCol);
		}
		/* Else must be a hit. */
		else
		{
			/* Get appropriate string to be outputted. */
			if (belongsToComputer)
			{
				String message = getHitMessage(true, row, col);
				System.out.printf("\n%d%s was a hit. %s", rawRow, rawCol, message);
			}
			else
			{
				String message = getHitMessage(false, row, col);
				System.out.printf("The computer hit with %d%s. %s", rawRow, 
									rawCol, message);
			}
			/* Mark it as a hit. */
			board[row][col] = 'X';
		}
	}
	
	/**
	 * Gets appropriate successful shot message and updates the hit ship object.
	 * 
	 * @param belongsToComputer true if board belongs to computer, false otherwise.
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
		 * even when sunk, because each ship is just an object containing an array
		 * of ShipPointTuples. Being sunk merely means the ship has an empty array
		 * of the tuples.
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
				System.out.printf("Something went wrong! %s isn't a legal ship type.", type);
				System.exit(1);
				break;
		}
		/* Update ship, i.e., remove hit ship point from array. */
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
	 * <p>
	 * Called after game board initialization. 
	 */
	public void setUpShips()
	{
		ShipPoint currShipTuple;
		char shipType;
		
		/* Add each ship point to its ship array. */
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
					System.out.println("Something went wrong! Check that you have the "
							+ "right number & type of ships placed and the right number "
							+ "of points per ship.");
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
	 * @return char[][] a 2D array representing the game board
	 */
	public char[][] getBoard()
	{
		return board;
	}
	
	/**
	 * Returns String representation of each ship object on board.
	 * 
	 * @return String 
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
	 * 
	 * Must iterate through and check whether each ship sunk manually, because
	 * ships.isEmpty() will return false even if every ship has been sunk.
	 * A ship's sinking does not remove it from the ships array; it just means
	 * it contains an empty array of ShipPointTuples.
	 * 
	 * @return boolean true if there are no ships left. Other player has won.
	 */
	public boolean noShipsLeft()
	{
		Ship currShip;
		
		for (int i = 0; i < NUM_SHIPS; ++i)
		{
			currShip = ships.get(i);
			/* If a single ship is not yet sunk, still ships left. */
			if (!currShip.isSunk())
			{
				return false;
			}
		}
		/* True if all ships sunk. */
		return true;
	}
	
	@Override
	/** 
	 * Replaces default toString() with a much more reader-friendly version. 
	 * 
	 * @return String representation of game board
	 */
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		
		/* Include column header. */
		result.append("  ABCDEFGHIJ\n");
		
		for (int i = 0; i < numRows; ++i) 
		{
			/* Include row header. */
			if (i != 9) 
			{
				/* Need blank space because of the 10th row. */
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
