package com.owlbeatsmusic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class Main {

    // .mml - mindmaplang

    static class Item {
        ArrayList<Integer> connections = new ArrayList<>();
        int digit;
        boolean isAssignedLevel = false;
        StringBuilder content;
        Point coordinates = new Point();

        public Item(int digit, String content) {
            this.digit = digit;
            this.content = new StringBuilder(content);
        }
    }

    enum Token {
        DOT,
        COLON,
        DIGIT,
        LITERAL
    }

    static String input = OLib.Fil.fileToString(new File("src/com/owlbeatsmusic/source.mml")).replace(System.lineSeparator(), "") + " ";
    static ArrayList<Token> tokenizedInput = new ArrayList<>();
    static ArrayList<Item> items = new ArrayList<>();
    static final int GRID_WIDTH  = 31;
    static final int GRID_HEIGHT = 31;
    static int CELL_WIDTH  = 50;
    static int CELL_HEIGHT = 50;
    static int[][] grid = new int[GRID_HEIGHT][GRID_WIDTH];
    static ArrayList<Item[]> connectedItems = new ArrayList<>(); // [From, To]

    static int oldOffsetX = 0;
    static int oldOffsetY = 0;
    static int offsetX = 0;
    static int offsetY = 0;
    static int startDragMouseX = 0;
    static int startDragMouseY = 0;
    static int endDragMouseX = 0;
    static int endDragMouseY = 0;
    static boolean isDragging = false;

    public static int readDigit(int index) {
        StringBuilder digit = new StringBuilder("0");

        int l = 0;
        while (index + l < input.length()-1) {
            if (!Character.isDigit(input.charAt(index+l))) break;
            digit.append(input.charAt(index + l));
            l++;
        }

        return Integer.parseInt(digit.toString());
    }

    public static void parse() {

        int index = 0;
        int digit;
        items.add(new Item(-1, ""));

        for (int i = 0; i < tokenizedInput.size(); i++) {
            if (tokenizedInput.get(i) == Token.DOT) {

                // START (NEW ITEM)
                if (tokenizedInput.get(i+1) == Token.DIGIT) {
                    int tempDigit = readDigit(i+1);
                    if (tokenizedInput.get(i+1+String.valueOf(tempDigit).length()) == Token.COLON) {
                        digit = tempDigit;
                        i += String.valueOf(digit).length()+1;
                        index++;
                        items.add(new Item(digit, ""));

                    }
                }

                // HYPERLINK (CONNECTION)
                else if (tokenizedInput.get(i+1) == Token.DOT & tokenizedInput.get(i+2) == Token.DIGIT) {
                    items.get(index).connections.add(readDigit(i+2));
                    i += String.valueOf(readDigit(i+2)).length()+1;
                }

                else {
                    items.get(index).content.append(".");
                }
            }
            else {
                items.get(index).content.append(input.charAt(i));
            }
        }
        items.remove(0);
        for (Item item : items) {
            System.out.println("digit \t\t\t:\t" + item.digit);
            System.out.println("content \t\t:\t" + item.content);
            System.out.println("connections \t:\t" + item.connections);
            System.out.println();
        }
    }

    public static void tokenize() {
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '.') tokenizedInput.add(Token.DOT);
            if (input.charAt(i) == ':') tokenizedInput.add(Token.COLON);
            if (Character.isDigit(input.charAt(i))) tokenizedInput.add(Token.DIGIT);
            if (Character.isAlphabetic(input.charAt(i)) | input.charAt(i) == ' ') tokenizedInput.add(Token.LITERAL); 
        }
    }

    public static void renderMap() {
        System.out.println("\n\n# RENDERING \n");
        ArrayList<ArrayList<Item>> levels = new ArrayList<>();
        levels.add(new ArrayList<>());

        for (Item item : items) {
            if (item.connections.size() == 0) {
                levels.get(0).add(item);
            }
            for (int connection : item.connections) {
                connectedItems.add(new Item[]{item, items.get(connection)});
                System.out.println("[" + item.digit + ", " + items.get(connection-1).digit + "]");
            }
        }

        int i = 0;
        while (i < items.size()) {
            for (Item[] itemConnection : connectedItems) {
                if (levels.size() == i+1) levels.add(new ArrayList<>());
                for (Item topLevelItem : levels.get(i)) {
                    if ((itemConnection[1].digit - 1) == topLevelItem.digit & !itemConnection[0].isAssignedLevel) {
                        levels.get(i+1).add(itemConnection[0]);
                        itemConnection[0].isAssignedLevel = true;
                    }
                }
            }
            i++;
        }

        for (ArrayList<Item> itemList : levels) {
            for (Item item : itemList) {
                System.out.print(item.digit + ", ");
            }
            System.out.println();
        }

        int j = 0;
        for (int l = 0; l < levels.size(); l++) {
            if (levels.get(l).size() == 0) continue;
            for (int item = 0; item < levels.get(l).size(); item++) {
                int x =(CELL_WIDTH/2)-levels.get(l).size()+item;
                int y = 2+l+j;
                grid[y][x] = levels.get(l).get(item).digit;
                levels.get(l).get(item).coordinates.x = (CELL_WIDTH  / 2) + CELL_WIDTH  * x;
                levels.get(l).get(item).coordinates.y = (CELL_HEIGHT / 2) + CELL_HEIGHT * y;
            }
            j++;
        }

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == 0) System.out.print(".  ");
                else System.out.print(grid[y][x] + "  ");
            }
            System.out.println();
        }

    }

    public static void renderWindow() throws  AWTException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        JFrame root = new JFrame();
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        root.setSize(1200, 631);
        root.setTitle("mml");
        root.setResizable(false);
        root.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        root.setBackground(Color.decode("#212121"));
        root.setLayout(new GridBagLayout());
        root.setLayout(new GridLayout(1, 0));
        GridBagConstraints gbc = new GridBagConstraints();


        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout());
        leftPanel.setBorder(new EmptyBorder(70, 70, 70, 70));
        leftPanel.setBackground(Color.decode("#212121"));
        root.add(leftPanel, gbc);


        JPanel rightPanel = new MindMapRendererPanel();
        rightPanel.setBackground(Color.WHITE);
        root.add(rightPanel, gbc);


        JTextPane textPane = new JTextPane();

        JButton runButton = new JButton("Render");
        runButton.addActionListener(e -> {
            tokenizedInput = new ArrayList<>();
            items = new ArrayList<>();
            input = textPane.getText().replaceAll(System.lineSeparator(), "") + " ";
            tokenize();
            parse();
            renderMap();
            rightPanel.update(rightPanel.getGraphics());
        });
        rightPanel.add(runButton);

        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.RED);
        StyleConstants.setBackground(keyWord, Color.YELLOW);
        StyleConstants.setBold(keyWord, true);

        textPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        textPane.setBounds(0,0,460,660);
        textPane.setBackground(Color.decode("#212121"));
        textPane.setCaretColor(Color.WHITE);
        textPane.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBar(new JScrollBar());
        scrollPane.setBackground(Color.decode("#212121"));
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.add(textPane);
        textPane.setPreferredSize(new Dimension(460, 660));
        textPane.setFont(new Font("Consolas", Font.BOLD,20));
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        root.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                scrollPane.updateUI();
                textPane.updateUI();
            }
        });
        root.addMouseWheelListener(e -> {
            CELL_WIDTH  -= e.getUnitsToScroll();
            CELL_HEIGHT -= e.getUnitsToScroll();
            rightPanel.update(rightPanel.getGraphics());
        });
        root.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                isDragging = true;
                endDragMouseX = e.getX();
                endDragMouseY = e.getY();
                offsetX = oldOffsetX + endDragMouseX-startDragMouseX;
                offsetY = oldOffsetY + endDragMouseY-startDragMouseY;
                //System.out.println(offsetX + ", " + offsetY);
                rightPanel.update(rightPanel.getGraphics());
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
        root.addMouseListener(new MouseListener() {
            @Override public void mousePressed(MouseEvent e) {
                startDragMouseX = e.getX();
                startDragMouseY = e.getY();
                oldOffsetX = offsetX;
                oldOffsetY = offsetY;
                System.out.println("old : " + oldOffsetX + ", " + oldOffsetY);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                System.out.println("cur : " + offsetX + ", " + offsetY);
                System.out.println("new : " + offsetX + ", " + offsetY);
                startDragMouseX = 0;
                startDragMouseY = 0;
                endDragMouseX = 0;
                endDragMouseY = 0;
                rightPanel.update(rightPanel.getGraphics());
            }

            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });
        root.setVisible(true);

        Robot updater = new Robot();
        updater.keyPress(KeyEvent.VK_ENTER);
        updater.keyPress(KeyEvent.VK_BACK_SPACE);
    }

    public static void main(String[] args) throws AWTException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        renderWindow();
        //tokenize();
        //parse();

    }

}
