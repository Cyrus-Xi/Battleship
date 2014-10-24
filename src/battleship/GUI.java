package battleship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.*;


public class GUI extends JFrame implements ActionListener
{
    private static int numRows = 10;
    private static int numCols = 10;
    private static JButton[][] board;

    public static void main(String[] args)
    {
        GUI demo = new GUI();
        // Exit appropriately.
        demo.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
    }

    public GUI()
    {
        super("GUI Demo");
        this.setSize(800, 800);
        this.setLayout(new GridLayout(numRows + 2, numCols + 2));

        board = new JButton[numRows][numCols];
        JButton currButton;
        for (int i = 0; i < numRows; ++i)
        {
            for (int j = 0; j < numCols; j++)
            {
                currButton = board[i][j];
                currButton = new JButton("(" + i + "," + j +")");
                currButton.addActionListener(this);
                currButton.setToolTipText(i + ", " + j);
                this.add(currButton);
            }
        }

        //this.pack();
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent st)
    {
        System.out.println("SOMETHING");
    }
}
