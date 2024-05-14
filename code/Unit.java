public class Unit {

    public boolean inFightWBase = false;

    public int attackDelay = 0;

    public boolean inFight = false;

    public int era;

    public int type;

    public double hp;

    public double attack;

    public double range;

    public int waitCost;

    public int x_coordinate;

    public int hl;

    public double deathGold;

    public double deathXPtoEnemy;

    double deathXpToAlly;



    public Unit(int era, int type, int hp, int attack, int range, int cost, double deathGold, double deathXpToEnemy, double deathXpToAlly){
        this.era = era;
        this.type = type;
        this.hp = hp;
        this.attack = attack;
        this.range = range;
        waitCost = cost;
        deathXPtoEnemy = deathXpToEnemy;
        this.deathXpToAlly = deathXpToAlly;
        this.deathGold = deathGold;


        if(type != 2){
            hl = 40;
        }
        else {
            if(era == 2){
                hl = 40;
            }
            else if (era == 1 || era == 0){
                hl = 90;
            }

        }
    }

    public static Unit createUnit(int era, int type){
        if(era == 0){
            if (type == 0){
                return new Unit(0,0,120,30,20, 100, 20, 40,10);

            }
            else if(type == 1){
                return new Unit(0,1, 100 , 25  , 150, 125, 33, 66, 17);
            }

            else{
                return new Unit(0,2, 300 ,40, 25, 300, 130, 260, 65);
            }
        }
        if(era == 1){
            if(type == 0){
                return new Unit(1,0,300,55,20, 175, 100, 200, 50);
            }
            else if(type == 1){
                return new Unit(1,1,260,50,150, 250, 150, 300, 75);
            }
            else{
                return new Unit(1,2,700,150,25, 450, 1000, 2000, 500 );
            }
        }
        else{
            if(type == 0){
                return new Unit(2,0,650,150,20, 250,400, 800, 200);
            }
            else if(type == 1){
                return new Unit(2,1,550,145,150, 375, 800, 1600, 400);
            }
            else{
                return new Unit(2,2,1200,350,40,800, 2000 ,4000, 1000);
            }
        }
    }

    public void draw(String file){
        StdDraw.picture(x_coordinate,60, file);
    }

    public Bullet createBullets(Unit unit){
        if(attackDelay >= 20){
            attackDelay = 0;
            return new Bullet(unit.x_coordinate, unit.attack);
        }
        else{
            attackDelay ++;
            return null;
        }
    }
}
