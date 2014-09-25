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
	
	Stack<ShipPoint> possibleHits = new Stack<ShipPoint>();


	/* To convert random col to column letter. */
	char[] columnHeaders = "ABCDEFGHIJ".toCharArray();
	
	/**
	 * Very simple Computer constructor.
	 * 
	 * @param ofHuman human player's game board
	 */
	public Computer(GameBoard ofHuman)
	{
		ofOpponent = ofHuman;
	}
	
	/**
	 * Computer plays its turn.
	 * 
	 * @param ofHuman human player's game board
	 */
	public void playOneTurn(GameBoard ofHuman){
		ofOpponent = ofHuman;
		ShipPoint currPoint;
		rawBoard = ofOpponent.getBoard();
		Random rand;
		
		/* Shoot randomly if no possible hits to use. */
		if (possibleHits.empty())
		{
			/* 
			 * Loop until get random row & col values that result in a 
			 * point not yet shot at.
			 */
			while (true)
			{
				/* 
				 * Exclusive of max value, so add 1 to make it inclusive.
				 * Full recipe from stackoverflow: rand.nextInt((max - min) + 1) + min
				 * Board is 10x10 2D array.
				 */
				rand = new Random();
				row = rand.nextInt((9 - 0) + 1);
				rand = new Random();
				col = rand.nextInt((9 - 0) + 1);

                /*
                allowed: 00, 02, 04, 06, 08
                        11, 13, 15, 17, 19
                 */
				
				/* If haven't shot there yet, exit loop. */
				if (rawBoard[row][col] != 'X' && rawBoard[row][col] != 'O')
				{
					break;
				}
			}
		}
		/* Else use stack of possible hits (like a depth-based search). */
		else
		{
			currPoint = possibleHits.pop();
			row = currPoint.getRow();
			col = currPoint.getCol();
		}
		
		ofOpponent.updateBoardAfterShot(false, row+1, columnHeaders[col]);
		
		rawBoard = ofOpponent.getBoard();
		
		/* If shot was successful. */
		if (rawBoard[row][col] == 'X')
		{
			/* shipType doesn't matter for comparison purposes. */
			currPoint = new ShipPoint('R', row, col);
			
			/* Add feasible points around successful hit to stack. */
			addPointsAround(currPoint);
		}	
	}
	
	/**
	 * Pushes onto stack the points around origin that are legal and not yet attempted.
	 * 
	 * @param originPoint the point that was hit
	 */
	private void addPointsAround(ShipPoint originPoint)
	{
		int originRow = originPoint.getRow();
		int originCol = originPoint.getCol();
		ShipPoint currPoint;
		
		/* North. */
		if (originRow != 0)  /* Can't go north if at top row already. */
		{
			currPoint = new ShipPoint('N', originRow-1, originCol);
			/* Make sure haven't already shot there. */
			if (rawBoard[originRow-1][originCol] != 'X' && 
					rawBoard[originRow-1][originCol] != 'O')
			{
				/* Push to stack then. */
				possibleHits.push(currPoint);
			}
		}
		/* West. */
		if (originCol != 0)  /* Can't go west if at leftmost col already. */
		{
			currPoint = new ShipPoint('W', originRow, originCol-1);
			/* Make sure haven't already shot there. */
			if (rawBoard[originRow][originCol-1] != 'X' && 
					rawBoard[originRow][originCol-1] != 'O')
			{
				/* Push to stack then. */
				possibleHits.push(currPoint);
			}
		}
		/* South. */
		if (originRow != 9)  /* Can't go south if at bottom row already. */
		{
			currPoint = new ShipPoint('T', originRow+1, originCol);
			/* Make sure haven't already shot there. */
			if (rawBoard[originRow+1][originCol] != 'X' && 
					rawBoard[originRow+1][originCol] != 'O')
			{
				/* Push to stack then. */
				possibleHits.push(currPoint);
			}
		}
		/* East. */
		if (originCol != 9)  /* Can't go east if at rightmost col already. */
		{
			currPoint = new ShipPoint('E', originRow, originCol+1);
			/* Make sure haven't already shot there. */
			if (rawBoard[originRow][originCol+1] != 'X' && 
					rawBoard[originRow][originCol+1] != 'O')
			{
				/* Push to stack then. */
				possibleHits.push(currPoint);
			}
		}
	}
    // Do parity and use clear path each time.
//    for (int i = 0; i < 5; i++)
//    {
//        // horizontal traversal
//
//    }
}
