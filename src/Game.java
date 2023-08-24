import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game extends JPanel implements KeyListener, ActionListener {

    Timer timer = new Timer(25, this);

    static final double DEG_TO_RAD = Math.toRadians(1), PI2 = Math.PI*2, P2 = Math.PI/2, P3 = (Math.PI*3)/2;

    static final int WIDTH = 960, HEIGHT = 523;

    static final int checkOffset = 15;
    static final Color BACKGROUND = Color.LIGHT_GRAY;

    static final double POINT_DISTANCE = 15, PLAYER_STEP = 5, ROTATE_STEP = DEG_TO_RAD*3;
    static final int SQUARE_SIZE = 64;

    static boolean w = false, a = false, s = false, d = false;

    static double pa = P2;

    static double posX = SQUARE_SIZE*1.5, posY = SQUARE_SIZE*1.5;
    static double deltaX = Math.cos(pa), deltaY = Math.sin(pa);

    static JFrame game;

    public Game(){
        timer.start();
    }

    static final int[][] WORLD = new int[][]{
            {1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1}
    };

    public static void main(String[] args) {
        game = new JFrame("rendering sucks");
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setBackground(BACKGROUND);
        game.setResizable(false);
        Game g = new Game();
        game.add(g);
        game.addKeyListener(g);
        game.setSize(WIDTH,HEIGHT);
        game.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        drawBG(g);
        drawRays(g);
    }

    protected double distance(double x1, double y1, double x2, double y2, double a){
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    protected void drawRays(Graphics g){
        g.setColor(Color.GREEN);
        Graphics2D g2d = (Graphics2D) g;
        int r, mx, my, dof;
        double rayX, rayY, rayAngle = pa - DEG_TO_RAD*30, xOffset = 0, yOffset = 0, disT = 0;
        if(rayAngle < 0) rayAngle += PI2;
        if(rayAngle > PI2) rayAngle -= PI2;
        for(r = 0; r < 240; r++){
            //HORIZONTAL CHECK
            dof = 0;
            double disH = Integer.MAX_VALUE, hx = posX, hy = posY;
            double aTan = -1/Math.tan(rayAngle);
            if (rayAngle > Math.PI) {
                rayY = (((int) posY >> 6) << 6) - 0.0001;
                rayX = (posY - rayY) * aTan + posX;
                yOffset = -64;
                xOffset = -yOffset * aTan;
            } else if (rayAngle < Math.PI) {
                rayY = (((int) posY >> 6) << 6) + 64;
                rayX = (posY - rayY) * aTan + posX;
                yOffset = 64;
                xOffset = -yOffset * aTan;
            } else {
                rayX = posX;
                rayY = posY;
                dof = 8;
            }
            while (dof < 8) {
                mx = (int) rayX >> 6;
                my = (int) rayY >> 6;
                if (mx < WORLD[0].length && my < WORLD.length && mx > -1 && my > -1 && WORLD[my][mx] > 0) {
                    hx = rayX;
                    hy = rayY;
                    disH = distance(posX, posY, hx, hy, rayAngle);
                    dof = 8;
                } else {
                    rayX += xOffset;
                    rayY += yOffset;
                    dof++;
                }
            }
            //VERTICAL CHECK
            dof = 0;
            double disV = Integer.MAX_VALUE, vx = posX, vy = posY;
            double nTan = -Math.tan(rayAngle);
            if (rayAngle > P2 && rayAngle < P3) {
                rayX = (((int) posX >> 6) << 6) - 0.0001;
                rayY = (posX - rayX) * nTan + posY;
                xOffset = -64;
                yOffset = -xOffset * nTan;
            } else if (rayAngle < P2 || rayAngle > P3) {
                rayX = (((int) posX >> 6) << 6) + 64;
                rayY = (posX - rayX) * nTan + posY;
                xOffset = 64;
                yOffset = -xOffset * nTan;
            } else {
                rayX = posX;
                rayY = posY;
                dof = 8;
            }
            while (dof < 8) {
                mx = (int) rayX >> 6;
                my = (int) rayY >> 6;
                if (mx < WORLD[0].length && my < WORLD.length && mx > -1 && my > -1 && WORLD[my][mx] > 0) {
                    vx = rayX;
                    vy = rayY;
                    disV = distance(posX, posY, vx, vy, rayAngle);
                    dof = 8;
                } else {
                    rayX += xOffset;
                    rayY += yOffset;
                    dof++;
                }
            }
            g2d.setStroke(new BasicStroke(2));
            if(disV < disH) { rayX = vx; rayY = vy; g2d.setColor(new Color(116, 218, 188)); disT = disV; }
            if(disH < disV) { rayX = hx; rayY = hy; g2d.setColor(new Color(91, 171, 147)); disT = disH; }
//            g2d.drawLine((int)posX, (int)posY, (int)rayX, (int)rayY);
            //DRAW WALLS
            double ca = pa - rayAngle;
            if(ca < 0) ca += PI2;
            if(ca > PI2) ca -= PI2;
            disT *= Math.cos(ca);
            double lineH = (SQUARE_SIZE*480)/disT;
            if(lineH > 480) lineH = 480;
            double lineO = (240-lineH/2);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(r*4, (int)lineO, r*4, (int)(lineH+lineO));
            rayAngle += DEG_TO_RAD/4;
            if(rayAngle < 0) rayAngle += PI2;
            if(rayAngle > PI2) rayAngle -= PI2;
        }
    }

    protected void drawBG(Graphics g){
        g.setColor(new Color(157, 175, 235));
        g.fillRect(0,0,WIDTH,HEIGHT/2);
        g.setColor(new Color(87, 131, 181));
        g.fillRect(0,HEIGHT/2,WIDTH,HEIGHT/2);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_W -> w = true;
            case KeyEvent.VK_S -> s = true;
            case KeyEvent.VK_A -> a = true;
            case KeyEvent.VK_D -> d = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_W -> w = false;
            case KeyEvent.VK_S -> s = false;
            case KeyEvent.VK_A -> a = false;
            case KeyEvent.VK_D -> d = false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tick();
    }

    protected void tick(){
        if(d){
            pa += ROTATE_STEP;
            if (pa > PI2) pa -= PI2;
            deltaY = Math.sin(pa);
            deltaY = Math.sin(pa);
        }
        if(a){
            pa -= ROTATE_STEP;
            if (pa < 0) pa += PI2;
            deltaX = Math.cos(pa);
            deltaY = Math.sin(pa);
        }

        int xOffset, yOffset;
        if(deltaX < 0) xOffset = -checkOffset; else xOffset = checkOffset;
        if(deltaY < 0) yOffset = -checkOffset; else yOffset = checkOffset;

        int ipx = (int) (posX/SQUARE_SIZE), ipx_add_xo = (int) (posX + xOffset)/SQUARE_SIZE, ipx_sub_xo = (int) (posX - xOffset)/SQUARE_SIZE;
        int ipy = (int) (posY/SQUARE_SIZE), ipy_add_yo = (int) (posY + yOffset)/SQUARE_SIZE, ipy_sub_yo = (int) (posY - yOffset)/SQUARE_SIZE;

        if(w){
            if(WORLD[ipy_add_yo][ipx] == 0) posY += Math.sin(pa) * PLAYER_STEP;
            if(WORLD[ipy][ipx_add_xo] == 0) posX += Math.cos(pa) * PLAYER_STEP;
        }
        if(s){
            if(WORLD[ipy_sub_yo][ipx] == 0) posY -= Math.sin(pa)*PLAYER_STEP;
            if(WORLD[ipy][ipx_sub_xo] == 0) posX -= Math.cos(pa)*PLAYER_STEP;
        }
        repaint();
    }
}
