package be.kuleuven.pylos.player;

//TIJDELIJK OM BOOM WEER TE GEVEN
//gegenereert door chatgpt en enkele dingen aangepast
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import be.kuleuven.pylos.player.Action.*;

public class TreeVisualizer extends JPanel {
    private SearchTreev5 root;

    public TreeVisualizer(SearchTreev5 root) {
        this.root = root;
        setPreferredSize(new Dimension(2400, 1200));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawTree(g, root, getWidth() /2, 30, 200);
    }

    private void drawTree(Graphics g, SearchTreev5 node, int x, int y, int xOffset) {
        if (node == null)
            return;

        // Draw the node
        g.drawString(" " + node.getScore(), x, y);
        //g.drawString("Action: " + (node.action != null ? node.action.getType() : "None"), x, y + 15);
        //if (node.action != null && node.action.getType().equals(ActionType.MOVE)) {
        //    g.drawString("to x: " + (node.action != null ? node.action.getTo().X : "None"), x, y + 30);
        //    g.drawString("to y: " + (node.action != null ? node.action.getTo().Y : "None"), x, y + 45);
        //    g.drawString("to z: " + (node.action != null ? node.action.getTo().Z : "None"), x, y + 60);
        //    g.drawString("S: " + node.getScore(), x, y + 75);
        //}

        // Draw child nodes
        if (node.nodes != null) {
            int childX = x - (node.nodes.size() - 1) * xOffset / 2; // Center children
            for (SearchTreev5 child : node.nodes) {
                g.drawLine(x, y + 20, childX, y + 100); // Connect to child
                drawTree(g, child, childX, y + 100, xOffset / 2); // Draw child
                childX += xOffset; // Move to the next child position
            }
        }
    }

    public static void showTree(SearchTreev5 tree) {
        JFrame frame = new JFrame("Search Tree Visualizer");
        TreeVisualizer visualizer = new TreeVisualizer(tree);
        frame.add(visualizer);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
