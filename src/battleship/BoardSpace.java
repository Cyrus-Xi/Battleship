/*
 * Author:
 *      Cyrus Xi
 * Purpose:
 *      Helper class for AI's strategy. Represents a GameBoard space with
 *      a counter attached.
 * Date:
 *      10/08/14.
 */

package battleship;

public class BoardSpace implements Comparable<BoardSpace>
{
    /**
     * Row & col values for space.
     */
    int row;
    int col;

    /**
     * The number of possible ships that could be placed through this space.
     */
    int counter;

    /**
     * Simple gameboard space constructor.
     *
     * @param theRow row value of space
     * @param theCol col value of space
     */
    public BoardSpace(int theRow, int theCol)
    {
        row = theRow;
        col = theCol;
    }

    /**
     * Get row value.
     *
     * @return the row value
     */
    public int getRow()
    {
        return row;
    }

    /**
     * Get col value.
     *
     * @return the col value
     */
    public int getCol()
    {
        return col;
    }

    /**
     * Get counter value.
     *
     * @return the counter value
     */
    public int getCounter()
    {
        return counter;
    }

    /**
     * Increment counter value by 1.
     */
    public void incrementCounter()
    {
        counter++;
    }

    /**
     * Reset counter value to 0.
     */
    public void clearCounter()
    {
        counter = 0;
    }

    /**
     * Replaces default toString() with a much more reader-friendly version.
     *
     * @return representation of BoardSpace object
     */
    @Override
    public String toString()
    {
        return "[" + String.valueOf(counter) + "] ";
    }

    /**
     * For use in Collections.sort().
     *
     * @param thatSpace the BoardSpace being compared to
     * @return          -1 if lesser than, 0 if equal, 1 if greater than
     */
    @Override
    public int compareTo(BoardSpace thatSpace)
    {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this == thatSpace)
        {
            return EQUAL;
        }

        // Comparison based on counter.
        if (this.counter < thatSpace.counter)
        {
            return BEFORE;
        }
        else if (this.counter > thatSpace.counter)
        {
            return AFTER;
        }

       return EQUAL;
    }

    /**
     * For use in comparison.
     *
     * @param that the object being compared to
     * @return     true if BoardSpaces are equal
     */
    @Override
    public boolean equals(Object that)
    {
        if (this == that)
        {
            return true;
        }
        // False if object isn't even a BoardSpace.
        if (!(that instanceof BoardSpace))
        {
            return false;
        }
        else
        {
            BoardSpace thatSpace = (BoardSpace)that;
            if (this.counter == thatSpace.counter)
            {
                return true;
            }
        }
        // Else not equal.
        return false;
    }

    /**
     * For use in comparison and to go along with overriding equals().
     *
     * @return the hashcode
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
}
