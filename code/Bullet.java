import java.awt.*;

public class Bullet {
    public int xCoordinate;

    public double attack;

    public boolean toBase;

    public Bullet(int xCoordinate, double attack){
        this.attack = attack;
        this.xCoordinate = xCoordinate;
    }

    public void draw(){
        StdDraw.setPenColor(Color.black);
        StdDraw.filledCircle(xCoordinate, 120, 4);
    }
}
