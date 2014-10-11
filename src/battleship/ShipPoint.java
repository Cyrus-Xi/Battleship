/*
 * Author: Cyrus Xi
 * Purpose: A helper class that represents a placed ship point. 
 * 			It's a "tuple" of the ship type (A, B, etc.) and its coordinates.
 * Date: 09/20/14.
 */

package battleship;

/**
 * Represents a point of a Ship on the board.
 */
public class ShipPoint
{
    /**
     * The row & col value of the point.
     */
	int row;
	int col;
    /**
     * The char ship type that the point is.
     */
	char shipType;

    /**
     * For more easily readable string output later.
     */
	char[] columnHeaders = "ABCDEFGHIJ".toCharArray();
	
	/**
	 * Constructs ShipPoint from type and board coordinates.
	 * 
	 * @param type    char that represents ship's type
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
     * Returns long version of ship's type (i.e., its name).
     *
     * @return the ship's name
     */
    public String getLongType()
    {
        switch (shipType)
        {
            case 'A': return "Carrier";
            case 'B': return "Battleship";
            case 'C': return "Cruiser";
            case 'S': return "Submarine";
            case 'D': return "Destroyer";
            case ' ': return "Unknown";
            default:
            {
                System.out.println("Something went wrong with the ship type. " +
                        "Exiting.");
                System.exit(1);
                break;
            }
        }
        // Should never reach here.
        return "";
    }

	/**
	 * Returns point's row-coordinate.
	 * 
	 * @return row index on board
	 */
	public int getRow()
	{
		return row;
	}
	
	/**
	 * Returns point's col-coordinate.
	 * 
	 * @return col index on board
	 */
	public int getCol()
	{
		return col;
	}
	
	/** 
	 * Eclipse auto-generated. Implemented for usage of ArrayList methods
     * elsewhere.
	 *
	 * @see    java.lang.Object#hashCode()
	 * @return int hash code
	 */
	@Override
	public int hashCode()
    {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	/** 
	 * Eclipse auto-generated. Implemented for usage of ArrayList methods
     * elsewhere.
	 * 
	 * @see    java.lang.Object#equals(java.lang.Object)
	 * @return true if ShipPoints considered equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj)
    {
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
        return other.col == col && other.row == row;
    }

	/** 
	 * Replaces default toString() with a much more reader-friendly version. 
	 * 
	 * @return representation of a ShipPointTuple
	 */
    @Override
	public String toString()
	{
        /*
		 * rowX+1 and columnHeaders[colY] to correspond to the game board the
		 * user sees.
		 */
        return String.format("(%d, %s)", row + 1, columnHeaders[col]);
	}
}
