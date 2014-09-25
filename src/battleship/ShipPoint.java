/*
 * Author: Cyrus Xi
 * Purpose: A helper class that represents a placed ship point. 
 * 			It's a "tuple" of the ship type (A, B, etc.) and its coordinates.
 * Date: 09/20/14.
 */

package battleship;

public class ShipPoint
{
	int row;
	int col;
	char shipType;
	
	/* For more easily readable string output later. */
	char[] columnHeaders = "ABCDEFGHIJ".toCharArray();
	
	/**
	 * Constructs ShipPointTuple from type and board coordinates.
	 * 
	 * @param type char that represents ship's type
	 * @param theRow  index of point on board
	 * @param theCol  index of point on board
	 */
	public ShipPoint(char type, int theRow, int theCol)
	{
		row = theRow;
		col = theCol;
		shipType = type;
	}
	
	/**
	 * Returns ship's type.
	 * 
	 * @return char type representation
	 */
	public char getType()
	{
		return shipType;
	}
	
	/**
	 * Returns point's row-coordinate.
	 * 
	 * @return int row index on board
	 */
	public int getRow()
	{
		return row;
	}
	
	/**
	 * Returns point's col-coordinate.
	 * 
	 * @return int col index on board
	 */
	public int getCol()
	{
		return col;
	}
	
	/** 
	 * Eclipse auto-generated. Implemented for usage of ArrayList methods elsewhere.
	 *
	 * @see    java.lang.Object#hashCode()
	 * @return int hash code
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	/** 
	 * Eclipse auto-generated. Implemented for usage of ArrayList methods elsewhere.
	 * 
	 * @see    java.lang.Object#equals(java.lang.Object)
	 * @return boolean true if ShipPointTuples considered equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) 
		{
			return true;
		}
		if (obj == null) 
		{
			return false;
		}
		if (getClass() != obj.getClass()) 
		{
			return false;
		}
		ShipPoint other = (ShipPoint) obj;
		if (col != other.col) {
			return false;
		}
		if (row != other.row) {
			return false;
		}
		return true;
	}

	@Override
	/** 
	 * Replaces default toString() with a much more reader-friendly version. 
	 * 
	 * @return String representation of a ShipPointTuple
	 */
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		
		/* rowX+1 and columnHeaders[colY] to correspond to the game board the user sees. */
		result.append(String.format("(%d, %s)", row+1, columnHeaders[col]));
		
		return result.toString();
	}
}
