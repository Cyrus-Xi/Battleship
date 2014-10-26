/*
 * Author:
 *      Cyrus Xi
 * Purpose:
 *      This is the main class. It handles the game loop, board output, etc.
 * Date:
 *      09/20/14.
 * 
 * To build and run from command line:
 *      Be in project root directory (above src and bin).
 *      To compile:
 *          Type: javac -Xlint -d bin src/battleship/*.java
 *          ("battleship" is the package name. To maintain the bin & src
 *          distinction, this compiles all the .java files in src/battleship and
 *          then puts the .class files into the bin directory.)
 *      To run:
 *          Uncomment this line -- fname = "src/" + fname; -- in GameBoard class.
 *          Type: java -cp bin battleship.Game
 *          (This first gets the right ships.txt file, then temporarily sets
 *          classpath to bin folder and runs the Game class in the battleship
 *          package.)
 */

package battleship;

import java.io.*;
import java.util.*;

/**
 * Represents a Battleship game.
 */
public class Game 
{
	/**
	 * Reads console input from user.
	 */
	static Scanner reader = new Scanner(System.in);
	/**
	 * The Computer player.
	 */
	static Computer computer;

	/**
	 * Just calls primary game loop method with appropriate argument.
	 *
	 * @throws FileNotFoundException if no initial AI ships text file
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		// true because first playthrough of game.
		playGame(true);
	}

	/**
	 * The main game loop.
	 *
	 * @param firstTime              true if first game
	 * @throws FileNotFoundException if no initial AI ships text file
	 */
	private static void playGame(Boolean firstTime) throws FileNotFoundException
	{
		GameBoard computerGameBoard;
		GameBoard playerGameBoard;

		// First playthrough has AI use provided ships text file.
		if (firstTime)
		{
			computerGameBoard = new GameBoard("ships.txt");
		}
		else
		{
			// Give computer a random gameboard.
			computerGameBoard = new GameBoard(true);
		}

		// Instantiate gameboard for player and let him/her place ships.
		playerGameBoard = new GameBoard(false);
		customizeBoard(playerGameBoard, computerGameBoard);

		computer = new Computer(playerGameBoard);

		do
		{
			playOneTurn(playerGameBoard, computerGameBoard);
		}
		// Loop until one game board has no ships left.
		while (!playerGameBoard.areNoShipsLeft() &&
				!computerGameBoard.areNoShipsLeft());

		if (computerGameBoard.areNoShipsLeft())
		{
			System.out.println("\nCongratulations, you've won! Nice job.");
		}
		else if (playerGameBoard.areNoShipsLeft())
		{
			System.out.println("\nSorry, the computer has beaten you. " +
					"Better luck next time!");
		}

		// Find out if user wants to play again.
		System.out.printf("\nDo you want to play again? Enter %s " +
				"or %s: ", "Y", "N");
		char response = reader.next().charAt(0);
		if (response == 'Y' || response == 'y')
		{
			System.out.println("Okay, new game commencing. Good luck!");
			// Call itself, but with argument false since no longer first time.
			playGame(false);
		}
		else
		{
			System.out.println("Okay, thanks for playing!");
		}
	}

	/**
	 * Lets user customize their board's ship placement.
	 *
	 * @param board human player's board to be customized
	 * @param ai    AI's board
	 */
	private static void customizeBoard(GameBoard board, GameBoard ai)
	{
		String[] shipNames = new String[] {"Carrier", "Battleship",
				"Cruiser", "Submarine", "Destroyer"};
		char[] types = new char[] {'A', 'B', 'C', 'S', 'D'};
		int[] sizes = new int[] {5, 4, 3, 3, 2};
		String dirs = "NWSE";
		String cols = "ABCDEFGHIJ";
		String legalCols = "ABCDEFGHIJ";

		int row;
		char col;
		char dir;

		// Loop through ships to be placed.
		for (int i = 0; i < 5; ++i)
		{
			outputGameBoards(board, ai);
			System.out.println("\nFor your " + shipNames[i] + " -- ");
			// Loop until user enters legal coordinates for his/her shot.
			while (true)
			{
				System.out.print("Enter the row number of its \"origin\" " +
						"point: ");
				try
				{
					row = reader.nextInt();
					if (row < 1 || row > 10)
					{
						System.out.println("Please enter a number between " +
								"1 and 10.\n");
						continue;
					}
					System.out.print("Enter the column letter of its origin " +
							"point: ");
					// Because there's no Scanner.nextChar method.
					col = reader.next().charAt(0);
					/*
					 * Allow lowercase cols to be inputted while keeping
					 * uppercase for program logic.
					 */
					col = Character.toString(col).toUpperCase().charAt(0);

					// Column letter input is legal.
					if (legalCols.contains(Character.toString(col)))
					{
						System.out.printf("Enter the direction to place your" +
								" %s in (N, W, S, E): ", shipNames[i]);
						dir = reader.next().charAt(0);
						dir = Character.toString(dir).toUpperCase().charAt(0);

						// Check that direction is legal.
						if (dirs.contains(Character.toString(dir)))
						{
							// Try placing ship.
							if (board.placePoints(row-1, cols.indexOf(col),
									dirs.indexOf(dir), sizes[i], types[i]))
							{
								// It worked.
								break;
							}
							else
							{
								// Didn't work; tell user to try again.
								System.out.println("Your ship placement " +
										"interferes with other ships' " +
										"placement or goes off the board. " +
										"Please try again.\n");
							}

						}
						else
						{
							System.out.println("Please enter a legal " +
									"direction (N, W, S, " +
									"E). Starting over.\n");
						}
					}
					// Else, have user try again.
					else
					{
						System.out.println("Please enter a legal column " +
								"letter. Starting over.\n");
					}
				}
				// User didn't enter an int for the row. Have user try again.
				catch (InputMismatchException e)
				{
					System.out.println("Please enter a legal row number.\n");

					// Consume the invalid token. Otherwise infinite loop.
					reader.next();
				}
			}
		}
		// Instantiate ship objects from board.
		board.setUpShips();
	}
	
	/**
	 * Plays a single turn (human and computer).
	 * 
	 * @param ofHuman    human player's game board
	 * @param ofComputer computer's game board
	 */
	static void playOneTurn(GameBoard ofHuman, GameBoard ofComputer)
	{
		outputGameBoards(ofHuman, ofComputer);
		outputShips(ofHuman);

		String legalCols = "ABCDEFGHIJ";
		int row;
		char col;
		
		// Loop until user enters legal coordinates for his/her shot.
		while(true)
		{
			System.out.print("Enter the row number to shoot at: ");
			try
			{
				row = reader.nextInt();
				
				if (row < 1 || row > 10) 
				{
					System.out.println("Please enter a number between 1 and 10.\n");
					continue;
				}
				
				System.out.print("Enter the column letter to shoot at: ");
				col = reader.next().charAt(0);

				/*
				 * Allow lowercase columns to be inputted while keeping
				 * uppercase for program logic.
				 */
				col = Character.toString(col).toUpperCase().charAt(0);

				// Column letter input is legal.
				if (legalCols.contains(Character.toString(col)))
				{
                    // But user has already shot here.
                    if (ofComputer.getChar(row, col) == 'X' || ofComputer.getChar
                            (row, col) == 'O')
                    {
                        System.out.println("You've already shot here! " +
                                "Try again.\n");
                    }
                    else
                    {
                        // Legal shot.
                        break;
                    }
				}
				// Else, have user try again.
				else
				{
					System.out.println("Please enter a legal column letter. " +
							"Starting over.\n");
				}
			}
			// User didn't enter an int for the row. Have user try again.
			catch (InputMismatchException e)
			{
				System.out.println("Please enter a legal row number.\n");
				
				// Consume the invalid token. Otherwise infinite loop.
				reader.next();  
			}
		}
		ofComputer.updateBoardAfterShot(true, row, col);
		
		System.out.println("\nComputer's turn.\n");
		computer.playOneTurn(ofHuman);
		
	}
	
	/**
	 * Output game boards side-by-side.
	 * 
	 * @param ofHuman    human player's game board
	 * @param ofComputer computer's game board
	 */
	static void outputGameBoards(GameBoard ofHuman, GameBoard ofComputer) 
	{
		/* 
		 * Split each game board by line and put into array. 
		 * Account for different systems' line separators.
		 */
		String[] ofHumanLines = ofHuman.toString().split("\\r?\\n");
		String[] ofComputerLines = ofComputer.toString().split("\\r?\\n");
		
		String temp;

		// Hide the computer's ships from player. 
		for (int i = 1; i < ofComputerLines.length; ++i)
		{
			temp = ofComputerLines[i];
			// Use regex to replace each ship point marker with an empty space.
			ofComputerLines[i] = temp.replaceAll("[ABCDS]+?", " ");
		}
	
		System.out.printf("\n%-20s%-20s\n\n", "Your Board", "Computer's Board");
		
		for (int lineIndex = 0; lineIndex < ofComputerLines.length; ++lineIndex) 
		{
			// Print line by line.
			System.out.printf("%-20s%-20s\n", ofHumanLines[lineIndex], 
					ofComputerLines[lineIndex]);
		}
	}
	
	/**
	 * Output human player's extant ships / ship points.
	 * 
	 * @param ofHuman    human player's game board
	 */
	static void outputShips(GameBoard ofHuman)
	{
		System.out.println("\nYour ships:");
		System.out.println(ofHuman.getShips());
	}
}
