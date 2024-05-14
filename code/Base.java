import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Base {

    public int type1NeededGold = 15;

     public int unitProgress = 0;

    public int type2NeededGold = 25;

    public int type3NeededGold = 100;

    public Base enemy;

    public Queue<Unit> queue = new LinkedList<>();

    public ArrayList<Unit> units = new ArrayList<>();

    public ArrayList<Bullet> bullets = new ArrayList<>();
    public double gold = 165;

    public double naturalGold = 0.1;

    public double naturalXp = 0.2;

    public double xp = 0;

    public int xpNeeded = 4000;
    public double maxHealth;

    public double health;

    public int era;

    int team;

    Arrow arrow;


    public Base(int team){health = 500; era = 0; this.team = team; maxHealth = 500;
    }


}
