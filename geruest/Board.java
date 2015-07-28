import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Board extends JPanel implements ActionListener {

    private final int DELAY = 10;
    private Timer timer;
    private Player player;
    private Enemy enemy;
    private CopyOnWriteArrayList<Point2D> currentPointList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Polygon> polyList = new CopyOnWriteArrayList<>();
    private boolean[][] allPoints = new boolean[200][200];
    public int percentage = 0;
    private final String[] LOSTMSGS = new String[]{"C´mon man, u can do better!", "Das war suboptimal..", "Spielst du um zu gewinnen, oder um diese Nachricht zu sehen?",
            "You´re embarrassing yourself!", "That´s not how you play this game", "Your score is 0.", "Vielleicht beim nächsten Mal, aber ich glaube eher nicht", "Dein wievielter " +
            "Versuch war das jetzt? Ich hab aufgehört mitzuzählen", "You lost. Again.", "Here I stand again, THE LOSER!"};
    //array of messages. one of those is shown if the game was lost.


    public Board() {
        initBoard();
    }

    private void initBoard() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.white);

        player = new Player();
        enemy = new Enemy();

        timer = new Timer(DELAY, this);
        timer.start();

        polyList.add(new Polygon(new int[]{0, 200, 200, 0}, new int[]{0, 0, 1, 1}, 4));
        polyList.add(new Polygon(new int[]{193, 200, 200, 193}, new int[]{0, 0, 200, 200}, 4));
        polyList.add(new Polygon(new int[]{0, 200, 200, 0}, new int[]{171, 171, 200, 200}, 4));
        polyList.add(new Polygon(new int[]{0, 1, 1, 0}, new int[]{0, 0, 200, 200}, 4));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);

        Toolkit.getDefaultToolkit().sync();
    }

    private void doDrawing(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int x = player.getX();
        int y = player.getY();

        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);


        //System.out.println("x: " + x + " y: " + y);

        if (x >= 0 && x < 200 && y >= 0 && y < 200) {
            allPoints[x][y] = true;
            percentage++;
            currentPointList.add(new Point(x, y));
        }

        g2d.setStroke(new BasicStroke(2));

        g2d.setColor(Color.red);
        drawLine(g2d);
        g2d.setColor(Color.blue);
        drawPolys(g2d);
        //checkPolys();
        g2d.setColor(Color.blue);
        g2d.fillOval(x, y, player.getWidth(), player.getHeight());

        g2d.setColor(Color.red);
        g2d.fillOval(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());

        //System.out.println(polyList.size());
    }

    private void drawPolys(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        for (Polygon p : polyList) {
            g2d.fill(p);
        }
    }

    private void drawLine(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        for (Point2D c : currentPointList) {
            g2d.drawLine((int) c.getX(), (int) c.getY(), (int) c.getX(), (int) c.getY());
        }
    }

    public void checkPolys() {
        int size = currentPointList.size();
        //System.out.println("Size:" + size);

        for (Polygon p : polyList) {
            if (p.contains(currentPointList.get(size - 1))) {
                addPoly(currentPointList);
                floodCompare(currentPointList.get(currentPointList.size()-1));
                currentPointList.clear();
                return;
            }

        }
    }

    private void floodCompare(Point2D p2d) {
        int x = (int) p2d.getX();
        int y = (int) p2d.getY();
        int[] leftUp = null;
        int[] leftDown = null;
        int[] rightUp = null;
        int[] rightDown = null;
        LinkedList leftDownPoints = new LinkedList<>();
        LinkedList leftUpPoints = new LinkedList<>();
        LinkedList rightDownPoints = new LinkedList<>();
        LinkedList rightUpPoints = new LinkedList<>();
        if (x > 0) {
            if (y > 0 && !allPoints[x - 1][y - 1]) {
                leftUp = new int[]{x - 1, y - 1};
            }
            if (y < 199 && !allPoints[x - 1][y + 1]) {
                leftDown = new int[]{x - 1, y + 1};
            }
        }
        if (x < 199) {
            if (y > 0 && !allPoints[x + 1][y - 1]) {
                rightUp = new int[]{x + 1, y - 1};
            }
            if (y < 199 && !allPoints[x + 1][y + 1]) {
                rightDown = new int[]{x + 1, y + 1};
            }
        }
        if (leftUp != null) {
            leftUpPoints = floodList(leftUp, new LinkedList<>(), new boolean[200][200]);
        }
        if (leftDown != null) {
            leftDownPoints = floodList(leftDown, new LinkedList<>(), new boolean[200][200]);
        }
        if (rightUp != null) {
            rightUpPoints = floodList(rightUp, new LinkedList<>(), new boolean[200][200]);
        }
        if (rightDown != null) {
            rightDownPoints = floodList(rightDown, new LinkedList<>(), new boolean[200][200]);
        }
        floodFill(getSecond(leftUpPoints, leftDownPoints, rightUpPoints, rightDownPoints));
    }

    private LinkedList floodList(int[] cords, LinkedList<Point2D> wraps, boolean[][] visited) {
        int x = cords[0];
        int y = cords[1];
        visited[x][y] = true;
        if (x > 0) {
            if (allPoints[x - 1][y]) {
                wraps.add(new Point(x - 1, y));
            } else if (!visited[x - 1][y]) {
                floodList(new int[]{x - 1, y}, wraps, visited);
            }
        }
        if (x < 199) {
            if (allPoints[x + 1][y]) {
                wraps.add(new Point(x + 1, y));
            } else if (!visited[x + 1][y]) {
                floodList(new int[]{x + 1, y}, wraps, visited);
            }
        }
        if (y > 0) {
            if (allPoints[x][y - 1]) {
                wraps.add(new Point(x, y - 1));
            } else if (!visited[x][y - 1]) {
                floodList(new int[]{x, y - 1}, wraps, visited);
            }
        }
        if (y < 199) {
            if (allPoints[x][y + 1]) {
                wraps.add(new Point(x, y + 1));
            } else if (!visited[x][y + 1]) {
                floodList(new int[]{x, y + 1}, wraps, visited);
            }
        }
        return wraps;
    }

    private LinkedList getSecond(LinkedList leftUpPoints, LinkedList leftDownPoints, LinkedList rightUpPoints, LinkedList rightDownPoints) {
        LinkedList helper1;
        LinkedList helper2;
        if (leftUpPoints.size() > rightUpPoints.size()) {
            helper1 = leftUpPoints;
        } else {
            helper1 = rightUpPoints;
        }
        if (leftDownPoints.size() > rightDownPoints.size()) {
            helper2 = leftUpPoints;
        } else {
            helper2 = rightUpPoints;
        }
        return helper1.size() > helper2.size() ? helper1 : helper2;
    }

    private void floodFill(LinkedList points) {
        addPoly(points);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        player.move();
        enemy.move();
        repaint();

        //checks if >=80% have been filled, and shows a popup.
        if(didIWinYet()){
            JOptionPane.showMessageDialog(null, "WINRAR! Your Score is " + String.format("%,d", percentage) + "!", "You Win!", 3);
            System.exit(0);
        }

    }

    private void addPoly(CopyOnWriteArrayList pointList) {
        int[] x = new int[pointList.size()];
        int[] y = new int[pointList.size()];
        int n = pointList.size();
        for (int i = 0; i < n; i++) {
            x[i] = (int) this.currentPointList.get(i).getX();
            y[i] = (int) this.currentPointList.get(i).getY();
        }
        this.polyList.add(new Polygon(x, y, n));
    }

    private void addPoly(LinkedList pointList) {
        int[] x = new int[pointList.size()];
        int[] y = new int[pointList.size()];
        int n = pointList.size();
        for (int i = 0; i < n-1; i++) {
            x[i] = (int) this.currentPointList.get(i).getX();
            y[i] = (int) this.currentPointList.get(i).getY();
        }
        this.polyList.add(new Polygon(x, y, n));
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);
            checkPolys();
        }
    }

    //calculates the percentage of the filled area. if over 80% of the area are filled, the player wins
    //the game and a message pops up (see keyPressed() method)
    private boolean didIWinYet(){
        return ((percentage*100)/(200*200) >= 80);
    }

    public boolean isFilled(int x, int y){
        return allPoints[x][y];
    }

}
