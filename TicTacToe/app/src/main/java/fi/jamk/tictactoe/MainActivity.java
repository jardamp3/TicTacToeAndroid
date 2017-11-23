package fi.jamk.tictactoe;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity
{

    private int[][] gameField;
    private boolean playerCross;
    private boolean playerRing;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // player with cross starts
        playerCross = true;
        playerRing = false;

        // inicialize game field
        // null = empty place
        // 1 = cross
        // 2 = ring
        gameField = new int[7][7];
    }

    public void onFieldClick(View v){

        int winner = 0;
        int idOfButton = v.getId();
        Button clickedBtn = (Button)findViewById(idOfButton);
        int tagOfButton = Integer.parseInt(clickedBtn.getTag().toString());


        Log.d("Clicked tag:", Integer.toString(tagOfButton));


        // choose if cross or ring will be shown
        if(playerCross){
            clickedBtn.setTextColor(Color.parseColor("#FF0000"));
            clickedBtn.setText("X");

            playerCross = false;
            playerRing = true;

            gameField[tagOfButton/7][tagOfButton%7] = 1;

        }
        else if (playerRing){
            clickedBtn.setText("O");
            clickedBtn.setTextColor(Color.parseColor("#0000FF"));

            playerCross = true;
            playerRing = false;

            gameField[tagOfButton/7][tagOfButton%7] = 2;
        }

        winner = checkEnd(tagOfButton/7,tagOfButton%7);
        if (winner!=0)
        {
            //open dialog
        }
    }

    public int checkEnd(int x, int y)
    {
        int counter = 1;
        if (gameField[x][y] == gameField[x][y + 1])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (gameField[x][y] == gameField[x][y + 2])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
            }
        }
        return 0;
    }
}
