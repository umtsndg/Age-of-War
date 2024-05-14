import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;


public class Game {

    public static Clip clip;

    public static int tempL = 0;

    public static int tempR = 0;

    static {
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public static int musicPlaying = 1;


    public static boolean gameOver = false;

    public static int leftReference = 80;


    public static boolean win = false;
    public static void runGame() throws LineUnavailableException {

        //Setting canvas properties
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize(1577,600);
        StdDraw.setXscale(0,1577 * 2);
        StdDraw.setYscale(0,1200);


        while(true){

            //Setting Restart settings
            Base left = new Base(0);
            Base right = new Base(1);
            right.health = 500;
            left.enemy = right;
            right.enemy = left;
            left.arrow = new Arrow(320,1120, 0);
            right.arrow  = new Arrow(3154 - 320,1120, 1);
            gameOver = false;
            win = false;
            String musicPath = "music.wav";
            playMusic(musicPath);


            while (true){
                boolean tmp = false; // If this becomes true game ends

                //Pause screen and its properties
                if(StdDraw.isKeyPressed(KeyEvent.VK_ESCAPE)){
                    clip.stop();
                    while (true){
                        StdDraw.setPenColor(Color.orange);
                        StdDraw.filledRectangle(1577,600,350,200);
                        StdDraw.setPenColor(new Color(150,75,0));
                        StdDraw.rectangle(1577,600,350,200);
                        StdDraw.setPenColor(Color.black);
                        StdDraw.text(1577,700,"Game Paused. Press LMB or RMB to continue.");
                        StdDraw.text(1577,500,"Press F11 to restart");
                        StdDraw.show();
                        if(StdDraw.isMousePressed()){
                            clip.start();
                            break;
                        }
                        if(StdDraw.isKeyPressed(KeyEvent.VK_F11)){
                            tmp = true;
                            break;
                        }
                    }
                }

                //Music Setting
                if(StdDraw.isKeyPressed(KeyEvent.VK_M)){
                    if(musicPlaying == 0){
                        clip.start();
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                        musicPlaying = 1;
                    }
                    else{
                        clip.stop();
                        musicPlaying = 0;
                    }
                }

                //Creating the base Image
                drawBase();
                createUI(left,right);

                //Handling arrow movement
                leftArrowMovement(left);
                rightArrowMovement(right);


                //Reducing the cooldown of unit spawn if there are in queues
                if(!left.queue.isEmpty()){
                    left.unitProgress +=10;
                }
                if(!right.queue.isEmpty()){
                    right.unitProgress +=10;
                }

                //Spawning units if they are ready
                if(left.queue.peek() != null) {
                    if (left.unitProgress >= left.queue.peek().waitCost){
                        Unit unit = left.queue.poll();
                        unit.x_coordinate = 300;
                        left.units.add(unit);
                        left.unitProgress = 0;

                    }
                }

                if(right.queue.peek() != null) {
                    if (right.unitProgress >= right.queue.peek().waitCost){
                        Unit unit = right.queue.poll();
                        unit.x_coordinate = 3154 - 300;
                        right.units.add(unit);
                        right.unitProgress = 0;
                    }
                }



                for(Unit unit: left.units){
                    if(unit.x_coordinate + unit.hl + unit.range >= 3154 - leftReference - 250){
                        unit.inFightWBase = true;
                    }
                    else{
                        unit.inFightWBase = false;
                    }
                }

                for(Unit unit: right.units){
                    if(unit.x_coordinate - unit.hl - unit.range <= leftReference + 250){
                        unit.inFightWBase = true;
                    }
                    else{
                        unit.inFightWBase = false;
                    }
                }

                //Determining if units are in fight range
                for(Unit unit : left.units){
                    try {
                        if (!right.units.isEmpty() && right.units.get(0).x_coordinate - right.units.get(0).hl < unit.x_coordinate + unit.hl + unit.range) {
                            unit.inFight = true;
                            unit.inFightWBase = false;
                        }
                        else{
                            unit.inFight = false;
                        }
                    }
                    catch (Exception ignored){

                    }

                    //Moving the units if they are not fighting
                    if(!unit.inFight && !unit.inFightWBase) {
                        if (left.units.indexOf(unit) == 0) {
                            unit.x_coordinate += 5;
                        }
                        else if (left.units.get(left.units.indexOf(unit) - 1).x_coordinate - left.units.get(left.units.indexOf(unit) - 1).hl - (unit.x_coordinate + unit.hl) > 10 ) {
                            unit.x_coordinate += 5;
                        }
                    }


                    //Drawing the unit
                    String s = findFile(unit);
                    unit.draw(s + ".png");
                }

                //Same thing but for right side
                for(Unit unit : right.units){
                    try {
                        if (!left.units.isEmpty() && left.units.get(0).x_coordinate + left.units.get(0).hl > unit.x_coordinate - unit.hl - unit.range) {
                            unit.inFight = true;
                            unit.inFightWBase = false;
                        }
                        else{
                            unit.inFight = false;
                        }
                    }
                    catch (Exception ignored){

                    }
                    if(!unit.inFight && !unit.inFightWBase) {
                        if (right.units.indexOf(unit) == 0) {
                            unit.x_coordinate -= 5;
                        } else if  (unit.x_coordinate - unit.hl - (right.units.get(right.units.indexOf(unit) - 1).x_coordinate + right.units.get(right.units.indexOf(unit) - 1).hl) > 10) {
                            unit.x_coordinate -= 5;
                        }
                    }

                }

                for(Unit unit : right.units){
                    String s = findFile(unit);
                    unit.draw(s + " inverted.png");
                }


                //Creating bullets if there are units in fighting range and they are ready to shoot
                for(Unit unit: left.units){
                    if (unit.inFight){
                        Bullet bullet = unit.createBullets(unit);
                        if(bullet != null) {
                            bullet.toBase = false;
                            left.bullets.add(bullet);
                        }
                    }
                    else if(unit.inFightWBase){
                        Bullet bullet = unit.createBullets(unit);
                        if(bullet != null){
                            bullet.toBase = true;
                            left.bullets.add(bullet);
                        }
                    }
                }

                for(Unit unit: right.units){
                    if (unit.inFight){
                        Bullet bullet = unit.createBullets(unit);
                        if(bullet != null) {
                            right.bullets.add(bullet);
                        }
                    }
                    else if(unit.inFightWBase){
                        Bullet bullet = unit.createBullets(unit);
                        if(bullet != null){
                            bullet.toBase = true;
                            right.bullets.add(bullet);
                        }
                    }
                }


                //Moving the bullets and interacting them with units
                moveBullets(left.bullets, left, right, 0);
                moveBullets(right.bullets,right,left,1);


                //Drawing the bullets
                for( Bullet bullet: left.bullets){
                    bullet.draw();
                }

                for( Bullet bullet: right.bullets){
                    bullet.draw();
                }


                //Increasing the gold and xp in each iteration
                left.gold += left.naturalGold;
                right.gold += right.naturalGold;
                left.xp += left.naturalXp;
                right.xp += left.naturalXp;


                //Win condition
                if(left.health <= 0 || right.health <= 0){
                    win = true;
                }

                //Win screen
                if(win){
                    while (true){
                        StdDraw.setPenColor(Color.orange);
                        StdDraw.filledRectangle(1577,600,350,200);
                        StdDraw.setPenColor(new Color(150,75,0));
                        StdDraw.rectangle(1577,600,350,200);
                        StdDraw.setPenColor(Color.black);
                        String s = null;
                        if(left.health <= 0){
                            s = "Right";
                        }
                        else if(right.health <= 0){
                            s = "Left";
                        }

                        StdDraw.text(1577,700,s + " side won! Press F11 to restart");
                        StdDraw.text(1577,500,"Press ESC to quit.");
                        StdDraw.show();
                        if(StdDraw.isKeyPressed(KeyEvent.VK_ESCAPE)){
                            System.exit(0);
                        }
                        if(StdDraw.isKeyPressed(KeyEvent.VK_F11)){
                            tmp = true;
                            break;
                        }

                    }
                }
                if(tmp){
                    clip.close();
                    break;
                }


                StdDraw.show();
                StdDraw.pause(30);
            }

        }

    }

    public static void drawBase(){
        StdDraw.clear(new Color(66,184,255));
        StdDraw.picture(1577,211, "background.png");
    }

    public static void createUI(Base left, Base right){
        StdDraw.setPenColor(StdDraw.ORANGE);
        StdDraw.filledRectangle(130,800,110,400);
        StdDraw.setPenColor(new Color(150,75,0));
        StdDraw.setPenRadius(0.012);
        StdDraw.rectangle(130, 800, 110,400);
        StdDraw.setPenRadius(0.002);
        left.arrow.drawArrow();
        StdDraw.setPenColor(Color.yellow);
        StdDraw.picture(450, 1120,"gold.png", 45,45);
        StdDraw.textLeft(480, 1120, String.valueOf((int)left.gold));
        StdDraw.setPenColor(Color.black);
        StdDraw.textLeft(420,1070, "EXP = ");
        StdDraw.setPenColor(Color.red);
        StdDraw.textLeft( 520,1070, String.valueOf((int)left.xp));


        StdDraw.setPenColor(StdDraw.ORANGE);
        StdDraw.filledRectangle(3024,800,110,400);
        StdDraw.setPenColor(new Color(150,75,0));
        StdDraw.setPenRadius(0.012);
        StdDraw.rectangle(3154 - 130, 800, 110,400);
        StdDraw.setPenRadius(0.002);
        right.arrow.drawArrow();
        StdDraw.setPenColor(Color.yellow);
        StdDraw.picture(3154 -450, 1120,"gold.png", 45,45);
        StdDraw.textRight(3154 -480, 1120, String.valueOf((int)right.gold));
        StdDraw.setPenColor(Color.black);
        StdDraw.textRight(3154 -420,1070, "= EXP ");
        StdDraw.setPenColor(Color.red);
        StdDraw.textRight(3154 - 520,1070, String.valueOf((int)right.xp));
        StdDraw.setPenColor(Color.black);
        StdDraw.rectangle(180,600,5,150);
        if(left.queue.peek() != null){
            StdDraw.setPenColor(Color.green);
            StdDraw.filledRectangle(180, 450 + ((double) left.unitProgress / left.queue.peek().waitCost) * 150, 5 , ((double) left.unitProgress / left.queue.peek().waitCost) * 150);

        }

        StdDraw.setPenColor(Color.green);
        for(int i = 0; i < left.queue.size(); i++){
            StdDraw.filledRectangle(200,455 + 20 * i, 5,5);
        }


        StdDraw.setPenColor(Color.black);
        StdDraw.rectangle(200,455, 5,5);
        StdDraw.rectangle(200,475, 5,5);
        StdDraw.rectangle(200,495, 5,5);
        StdDraw.rectangle(200,515, 5,5);
        StdDraw.rectangle(200,535, 5,5);

        StdDraw.rectangle(3154 - 180,600,5,150);
        if(right.queue.peek() != null){
            StdDraw.setPenColor(Color.green);
            StdDraw.filledRectangle(3154 - 180, 450 + ((double) right.unitProgress / right.queue.peek().waitCost) * 150, 5 , ((double) right.unitProgress / right.queue.peek().waitCost) * 150);

        }

        StdDraw.setPenColor(Color.green);
        for(int i = 0; i < right.queue.size(); i++){
            StdDraw.filledRectangle(3154 - 200,455 + 20 * i, 5,5);
        }


        StdDraw.setPenColor(Color.black);
        StdDraw.rectangle(3154 -200,455, 5,5);
        StdDraw.rectangle(3154 - 200,475, 5,5);
        StdDraw.rectangle(3154 - 200,495, 5,5);
        StdDraw.rectangle(3154 - 200,515, 5,5);
        StdDraw.rectangle(3154 - 200,535, 5,5);

        StdDraw.setPenColor(Color.black);
        StdDraw.text(160, 1120, left.type1NeededGold + "G");
        StdDraw.text(160, 1040, left.type2NeededGold + "G");
        StdDraw.text(160, 960, left.type3NeededGold + "G");
        if(left.xpNeeded > 0) {
            StdDraw.text(180, 880, left.xpNeeded + "xp");
        }
        else{
            StdDraw.text(180, 880, "-");
        }

        StdDraw.text(3154 - 160, 1120, right.type1NeededGold + "G");
        StdDraw.text(3154 - 160, 1040, right.type2NeededGold + "G");
        StdDraw.text(3154 - 160, 960, right.type3NeededGold +"G");
        if(right.xpNeeded > 0) {
            StdDraw.textLeft(3154 - 230, 880, right.xpNeeded + "xp");
        }
        else{
            StdDraw.text(3154 - 180, 880, "-");
        }

        StdDraw.picture(leftReference,880,"evolve.png");
        StdDraw.picture(3154 - leftReference,880,"evolve.png");



        if(left.era == 0){
            StdDraw.picture(leftReference,180,"base_1.png");
            StdDraw.picture(leftReference,1120, "1 1.png");
            StdDraw.picture(leftReference,1040, "1 2.png");
            StdDraw.picture(leftReference,960, "1 3.png");
        }
        else if(left.era == 1){
            StdDraw.setPenColor(Color.black);
            StdDraw.picture(leftReference,180,"base_2.png");
            StdDraw.picture(leftReference,1120, "2 1.png");
            StdDraw.picture(leftReference,1040, "2 2.png");
            StdDraw.picture(leftReference,960, "2 3.png");
        }

        else{
            StdDraw.setPenColor(Color.black);
            StdDraw.picture(leftReference,180,"base_3.png");
            StdDraw.picture(leftReference,1120, "3 1.png");
            StdDraw.picture(leftReference,1040, "3 2.png");
            StdDraw.picture(leftReference,960, "3 3.png");
        }
        StdDraw.setPenColor(Color.black);
        StdDraw.filledRectangle(120,600,20,150);
        StdDraw.setPenColor(Color.red);
        StdDraw.filledRectangle(120,450 + (left.health / left.maxHealth) * 150,20,(left.health / left.maxHealth) * 150);
        StdDraw.setPenColor(Color.black);
        StdDraw.rectangle(120,600,20,150);
        StdDraw.text(120,780, String.valueOf(Integer.parseInt(String.valueOf((int)left.health))));


        if(right.era == 0){
            StdDraw.picture(1577 * 2 - 120, 180, "base_1_inverted.png");
            StdDraw.setPenColor(Color.black);
            StdDraw.picture(3154 - leftReference,1120, "1 1.png");
            StdDraw.picture(3154 - leftReference,1040, "1 2.png");
            StdDraw.picture(3154 - leftReference,960, "1 3.png");
        }
        else if(right.era == 1){
            StdDraw.setPenColor(Color.black);
            StdDraw.picture(3154 - leftReference,180,"base_2_inverted.png");
            StdDraw.picture(3154 - leftReference,1120, "2 1.png");
            StdDraw.picture(3154 - leftReference,1040, "2 2.png");
            StdDraw.picture(3154 - leftReference,960, "2 3.png");
        }
        else{
            StdDraw.setPenColor(Color.black);
            StdDraw.picture(3154 - leftReference,180,"base_3_inverted.png");
            StdDraw.picture(3154 - leftReference,1120, "3 1.png");
            StdDraw.picture(3154 - leftReference,1040, "3 2.png");
            StdDraw.picture(3154 - leftReference,960, "3 3.png");
        }
        StdDraw.setPenColor(Color.black);
        StdDraw.filledRectangle(3154 - 120,600,20,150);
        StdDraw.setPenColor(Color.red);
        StdDraw.filledRectangle(3154 - 120,450 + (right.health / right.maxHealth) * 150,20,(right.health / right.maxHealth) * 150);
        StdDraw.setPenColor(Color.black);
        StdDraw.rectangle(3154 - 120,600,20,150);
        StdDraw.text(3154 - 120,780, String.valueOf(Integer.parseInt(String.valueOf((int)right.health))));

    }

    public static void doEvent(Base base){
        if(base.arrow.loc == 3 && base.xp >= base.xpNeeded){
            base.era ++;
            if(base.era == 1){
                base.xpNeeded = 14000;
                base.health = 1100;
                base.maxHealth = 1100;
                base.naturalGold *= 3;
                base.naturalXp *= 2;
                base.type1NeededGold = 50;
                base.type2NeededGold = 75;
                base.type3NeededGold = 500;
            }
            else if(base.era == 2){
                base.xpNeeded = -1;
                base.health = 2000;
                base.maxHealth = 2000;
                base.naturalGold *= 4;
                base.naturalXp *= 3;
                base.type1NeededGold = 200;
                base.type2NeededGold = 400;
                base.type3NeededGold = 1000;
            }
        }
        else if(base.arrow.loc == 0 && base.gold >=base.type1NeededGold && base.queue.size() < 5){
            base.gold -= base.type1NeededGold;
            base.queue.add(Unit.createUnit(base.era,0));
        }
        else if(base.arrow.loc == 1 && base.gold >= base.type2NeededGold && base.queue.size() < 5){
            base.gold -= base.type2NeededGold;
            base.queue.add(Unit.createUnit(base.era,1));
        }
        else if( base.arrow.loc == 2 && base.gold >= base.type3NeededGold && base.queue.size() < 5){
            base.gold -=base.type3NeededGold;
            base.queue.add(Unit.createUnit(base.era, 2));
        }
    }

    public static void moveBullets(ArrayList<Bullet> bullets, Base ally, Base enemy, int team) {
        if (team == 0) {
            int tmp = 0;
                for (Bullet bullet : bullets) {
                    if(!bullet.toBase) {
                        try {
                            if (bullet.xCoordinate + 4 - (enemy.units.get(0).x_coordinate - enemy.units.get(0).hl) > 0) {
                                enemy.units.get(0).hp -= bullet.attack;
                                tmp = 1;
                                if (enemy.units.get(0).hp <= 0) {
                                    Unit dead = enemy.units.remove(0);
                                    ally.gold += dead.deathGold;
                                    ally.xp += dead.deathXPtoEnemy;
                                    enemy.xp += dead.deathXpToAlly;
                                }
                            } else {
                                bullet.xCoordinate += 10;
                            }
                        } catch (Exception e) {
                            bullets.remove(0);
                        }
                    }
                    else{
                        try{
                            if( bullet.xCoordinate + 4 >= 3154 - leftReference - 140){
                                enemy.health -= bullet.attack;
                                tmp = 1;
                                if(enemy.health <= 0){
                                    win = true;
                                }
                            }
                            else{
                                bullet.xCoordinate += 10;
                            }
                        }
                        catch (Exception e){
                            bullets.remove(0);
                        }
                    }
                }
                if(tmp == 1){
                    bullets.remove(0);
                }
        }
        else{
            int tmp = 0;
            for (Bullet bullet : bullets) {
                if(!bullet.toBase) {
                    try {
                        if (bullet.xCoordinate - 4 - (enemy.units.get(0).x_coordinate + enemy.units.get(0).hl) < 0) {
                            enemy.units.get(0).hp -= bullet.attack;
                            tmp = 1;
                            if (enemy.units.get(0).hp <= 0) {
                                Unit dead = enemy.units.remove(0);
                                ally.gold += dead.deathGold;
                                ally.xp += dead.deathXPtoEnemy;
                                enemy.xp += dead.deathXpToAlly;
                            }
                        } else {
                            bullet.xCoordinate -= 10;
                        }
                    } catch (Exception e) {
                        bullets.remove(0);
                    }
                }
                else{
                    try{
                        if( bullet.xCoordinate - 4 <= leftReference + 140){
                            enemy.health -= bullet.attack;
                            tmp = 1;
                            if(enemy.health <= 0){
                                win = true;
                            }
                        }
                        else{
                            bullet.xCoordinate -= 10;
                        }
                    }
                    catch (Exception e){
                        bullets.remove(0);
                    }
                }
            }
            if (tmp == 1){
                bullets.remove(0);
            }
        }
    }

    public static void leftArrowMovement(Base left){
        if (StdDraw.isKeyPressed(KeyEvent.VK_Q)){
            left.arrow.yCoordinate = 1120;
            left.arrow.loc = 0;
        }
        else if(StdDraw.isKeyPressed(KeyEvent.VK_W)){
            left.arrow.yCoordinate = 1040;
            left.arrow.loc = 1;
        }
        else if(StdDraw.isKeyPressed(KeyEvent.VK_E)){
            left.arrow.yCoordinate = 960;
            left.arrow.loc = 2;
        }
        else if(StdDraw.isKeyPressed(KeyEvent.VK_R)){
            left.arrow.yCoordinate = 880;
            left.arrow.loc = 3;
        }

        else if(StdDraw.isKeyPressed(KeyEvent.VK_SPACE) && tempL == 0){
            tempL = 1;
            doEvent(left);
        }
        if(!StdDraw.isKeyPressed(KeyEvent.VK_SPACE)){
            tempL = 0;
        }
    }

    public static void rightArrowMovement(Base right){
        if (StdDraw.isKeyPressed(KeyEvent.VK_NUMPAD4)){
            right.arrow.yCoordinate = 1120;
            right.arrow.loc = 0;
        }
        else if(StdDraw.isKeyPressed(KeyEvent.VK_NUMPAD5)){
            right.arrow.yCoordinate = 1040;
            right.arrow.loc = 1;
        }
        else if(StdDraw.isKeyPressed(KeyEvent.VK_NUMPAD6)){
            right.arrow.yCoordinate = 960;
            right.arrow.loc = 2;
        }
        else if(StdDraw.isKeyPressed(KeyEvent.VK_NUMPAD2)){
            right.arrow.yCoordinate = 880;
            right.arrow.loc = 3;
        }

        else if(StdDraw.isKeyPressed(KeyEvent.VK_ENTER) && tempR == 0){
            tempR = 1;
            doEvent(right);
        }
        if(!StdDraw.isKeyPressed(KeyEvent.VK_ENTER)){
            tempR = 0;
        }
    }

    public static void playMusic(String location){
        try{
            File musicPath = new File(location);

            if(musicPath.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip.open(audioInput);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                musicPlaying = 1;
            }
        }
        catch (Exception ignored){
        }
    }

    public static String findFile(Unit unit){
        if(unit.era == 0){
            if(unit.type == 0){
                return "troop 1 1";
            }
            else if (unit.type == 1){
                return "troop 1 2";
            }
            else{
                return "troop 1 3";
            }
        }
        else if(unit.era == 1){
            if(unit.type == 0){
                return "troop 2 1";
            }
            else if (unit.type == 1){
                return "troop 2 2";
            }
            else{
                return "troop 2 3";
            }
        }
        else{
            if(unit.type == 0){
                return "troop 3 1";
            }
            else if (unit.type == 1){
                return "troop 3 2";
            }
            else{
                return "troop 3 3";
            }
        }
    }
}
