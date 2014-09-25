/*
 * Author: Cyrus Xi
 * Purpose: Represents a Ship. 
 * Date: 09/20/14.
 */

package battleship;

import java.util.*;

public class Ship 
{
	ArrayList<ShipPoint> primitiveShip;
	String name;
	
	/**
	 * Construct ship from list of ShipPointTuples.
	 * 
	 * @param shipPoints list of ShipPointTuples
	 */
	public Ship(ArrayList<ShipPoint> shipPoints) 
	{
		primitiveShip = shipPoints;
		/* The specific point used in determining shipType doesn't matter. */
		char type = shipPoints.get(0).getType();
		switch (type) 
		{
			case 'A':
				name = "Carrier";
				break;
			case 'B':
				name = "Battleship";
				break;
			case 'C':
				name = "Cruiser";
				break;
			case 'S':
				name = "Submarine";
				break;
			case 'D':
				name = "Destroyer";
				break;
			default:
				System.out.printf("Something went wrong! %s isn't a legal ship type.", type);
				System.exit(1);
				break;
		}
	}
	
	/**
	 * Returns whether ship has sunk, i.e., whether all its points have been hit.
	 * 
	 * @return boolean true if ship has been sunk
	 */
	public boolean isSunk()
	{
		return primitiveShip.isEmpty();
	}
	
	/**
	 * Remove hit ship point from ship's list of points.
	 * 
	 * @param row int value of hit
	 * @param col int value of hit
	 */
	public void updateShipAfterShot(int row, int col)
	{
		/* shipType argument doesn't matter, since equals() just cares about coordinates. */
		ShipPoint hit = new ShipPoint('Z', row, col);
		primitiveShip.remove(hit);
	}

	@Override
	/** 
	 * Replaces default toString() with a much more reader-friendly version. 
	 * 
	 * @return String representation of Ship object
	 */
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append(String.format("%s:", name));
		for (int i = 0; i < primitiveShip.size(); ++i)
		{
			result.append(" ");
			result.append(primitiveShip.get(i));
			
			/* Comma after each tuple except the last one. */
			if (i < primitiveShip.size()-1) 
			{
				result.append(",");
			}
		}
		return result.toString();
	}
}
