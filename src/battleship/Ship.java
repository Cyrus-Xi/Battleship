/*
 * Author: Cyrus Xi
 * Purpose: Represents a Ship. 
 * Date: 09/20/14.
 */

package battleship;

import java.util.*;

/**
 * Represents a Ship object.
 */
public class Ship 
{
    /**
     * Ship is just an array of ShipPoints.
     */
	ArrayList<ShipPoint> primitiveShip;
    /**
     * Name of ship.
     */
	String name;
	
	/**
	 * Construct ship from list of ShipPoints.
	 * 
	 * @param shipPoints list of ShipPoints
	 */
	public Ship(ArrayList<ShipPoint> shipPoints) 
	{
		primitiveShip = shipPoints;
		// The specific point used in determining shipType doesn't matter.
		char type = shipPoints.get(0).getType();
        // From char type, get actual name.
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
				System.out.printf("Something went wrong! %s isn't a legal " +
                        "ship type.", type);
				System.exit(1);
				break;
		}
	}
	
	/**
	 * Returns whether ship has sunk, i.e., whether all its points have been
     * hit.
	 * 
	 * @return true if ship has been sunk
	 */
	public boolean isSunk()
	{
		return primitiveShip.isEmpty();
	}
	
	/**
	 * Remove hit ship point from ship's list of points.
	 * 
	 * @param row row value of hit
	 * @param col col value of hit
	 */
	public void updateShipAfterShot(int row, int col)
	{
		/*
         *shipType argument doesn't matter, since overridden equals() just
         * cares about coordinates.
         */
		ShipPoint hit = new ShipPoint('Z', row, col);
		primitiveShip.remove(hit);
	}

	/** 
	 * Replaces default toString() with a much more reader-friendly version. 
	 * 
	 * @return representation of Ship object
	 */
    @Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append(String.format("%s:", name));
		for (int i = 0; i < primitiveShip.size(); ++i)
		{
			result.append(" ");
			result.append(primitiveShip.get(i));
			
			// Comma after each tuple except the last one.
			if (i < primitiveShip.size()-1) 
			{
				result.append(",");
			}
		}
		return result.toString();
	}
}
