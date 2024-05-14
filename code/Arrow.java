public class Arrow {
    int team;
    int xCoordinate;

    int yCoordinate;

    int loc = 0;

    public Arrow(int x, int y, int team){
        xCoordinate = x;
        yCoordinate = y;
        this.team = team;
    }

    public void drawArrow(){
        if(team == 0) {
            StdDraw.picture(xCoordinate, yCoordinate, "arrow_left_inverted.png", 150, 50);
        }
        else{
            StdDraw.picture(xCoordinate, yCoordinate, "arrow_left.png", 150, 50);

        }
    }
}
