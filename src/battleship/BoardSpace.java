package battleship;

import com.sun.istack.internal.NotNull;

/**
 * Created by Suryc11 on 10/9/14.
 */

public class BoardSpace implements Comparable<BoardSpace>
{
    int row;
    int col;
    int counter;

    public BoardSpace(int theRow, int theCol)
    {
        row = theRow;
        col = theCol;
    }

    public int getRow()
    {
        return row;
    }

    public int getCol()
    {
        return col;
    }

    public int getCounter()
    {
        return counter;
    }

    public void incrementCounter()
    {
        counter++;
    }

    public void clearCounter()
    {
        counter = 0;
    }

    /**
     * Heavy weighting for spaces in possible ship placings that pass through
     * hits. Weighting appropriately influenced by how many hits the placing
     * passes through.
     */
    public void heavilyIncrementCounter(int numHits)
    {
        counter += (50 * numHits);
    }

    @Override
    public String toString()
    {
        return "[" + String.valueOf(counter) + "] ";
    }

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

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

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
}
