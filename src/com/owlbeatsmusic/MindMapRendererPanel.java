package com.owlbeatsmusic;

import javax.swing.*;
import java.awt.*;


public class MindMapRendererPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean alternate = false;
        for (int y = 0; y < Main.GRID_HEIGHT; y++) {
            for (int x = 0; x < Main.GRID_HEIGHT; x++) {
                g.setColor(Color.decode("#ebeced"));
                if (alternate) g.setColor(Color.decode("#f5f5f5"));
                if (Main.grid[y][x] != 0) g.setColor(Color.pink);
                alternate = !alternate;
                g.fillRect(x*Main.CELL_WIDTH+Main.offsetX,y*Main.CELL_HEIGHT+Main.offsetY, Main.CELL_WIDTH, Main.CELL_HEIGHT);
            }
        }

        // Draw lines
        for (Main.Item[] i : Main.connectedItems) {
            Point from = i[0].coordinates;
            Point to   = Main.items.get(i[1].digit-2).coordinates;

            if (from.y == to.y) {
                g.setColor(Color.DARK_GRAY);
                g.drawArc(Main.offsetX+from.x, Main.offsetY+from.y - Main.CELL_HEIGHT, (to.x- from.x), 30, 0, 180);
            }
            if (from.y-to.y < 0) {
                g.setColor(Color.BLACK);
                g.drawLine(Main.offsetX+from.x, Main.offsetY+from.y+(Main.CELL_HEIGHT/2), Main.offsetX+to.x, Main.offsetX+to.y-(Main.CELL_HEIGHT/2));
            }
            if (from.y-to.y > 0) {
                g.setColor(Color.BLACK);
                g.drawLine(Main.offsetX+from.x, Main.offsetY+from.y-(Main.CELL_HEIGHT/2), Main.offsetX+to.x, Main.offsetY+to.y+(Main.CELL_HEIGHT/2));
            }
        }
    }
}
