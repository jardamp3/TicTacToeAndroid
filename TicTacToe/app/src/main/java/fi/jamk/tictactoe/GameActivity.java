package fi.jamk.tictactoe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends BaseServiceActivity implements IServiceCallbacks
{
    private int[][] gameField;
    private boolean playerCrossTurn;
    private boolean playerRingTurn;
    private List<Button> btnList;
    private ProgressDialog progressDialog;
    private boolean isCross;
    private String opponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        opponentName = intent.getExtras().getString("opponentName");

        // player with cross starts
        playerCrossTurn = true;
        playerRingTurn = false;

        // inicialize game field
        // null = empty place
        // 1 = cross
        // 2 = ring
        gameField = new int[7][7];
        btnList = new ArrayList<>();


        // save all buttons in game field into list
        for (int i = 0; i <= 48; i++) {
            int id = GameActivity.this.getResources().getIdentifier("_"+i, "id", GameActivity.this.getPackageName());
            btnList.add(0, (Button) findViewById(id));
        }


        // server-client choose dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage("What do you want to be?")
                .setPositiveButton("Cross", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        btService.setWantToBeServer(false);
                        btService.openConnection();
                        btService.setCallbacks(GameActivity.this);
                        isCross = true;
                    }
                })
                .setNegativeButton("Ring", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        btService.setWantToBeServer(true);
                        btService.openConnection();
                        btService.setCallbacks(GameActivity.this);
                        isCross = false;
                    }
                })
                .create();

        builder.show();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        btService.closeConnection();
        Log.d("Game activity:", "ending");
    }

    public void onFieldClick(View v){


        int idOfButton = v.getId();
        Button clickedBtn = (Button)findViewById(idOfButton);
        String tagOfClickedButton = clickedBtn.getTag().toString();

        reloadGameField(getButtonByTag(tagOfClickedButton));

        Log.d("Clicked tag:", tagOfClickedButton);
        btService.sendString(tagOfClickedButton);

    }

    public Button getButtonByTag(String tag){
        for(Button btn : btnList){
            if(btn.getTag().toString().equals(tag)){
                return btn;
            }
        }
        return null;
    }

    public void reloadGameField(Button btn){
        int winner = 0;
        int tagOfButton = Integer.parseInt(btn.getTag().toString());
        btn.setClickable(false);
        // choose if cross or ring will be shown
        if(playerCrossTurn){
            btn.setTextColor(Color.parseColor("#FF0000"));
            btn.setText("X");

            playerCrossTurn = false;
            playerRingTurn = true;

            gameField[tagOfButton/7][tagOfButton%7] = 1;

        }
        else if (playerRingTurn){
            btn.setText("O");
            btn.setTextColor(Color.parseColor("#0000FF"));

            playerCrossTurn = true;
            playerRingTurn = false;

            gameField[tagOfButton/7][tagOfButton%7] = 2;
        }

        changeTurn();

        winner = checkEnd(tagOfButton/7,tagOfButton%7);
        if (winner!=0)
        {
            String text;
            if(winner == 3) {
                text = "you almost won, but it's a tie";
                db.execSQL("UPDATE Scores SET ties = ties +1 WHERE name='" + opponentName + "';");
            }
            else if((winner == 1 && isCross) || (winner == 2 && !isCross)){
                text = "Game over. You lose :-(";
                db.execSQL("UPDATE Scores SET wins = wins +1 WHERE name='" + opponentName + "';");
            }
            else {
                text = "Congratulations. You win ! :-)";
                db.execSQL("UPDATE Scores SET looses = looses +1 WHERE name='" + opponentName + "';");
            }

            // server-client choose dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setMessage(text)
                    .setPositiveButton("Back to menu", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("Play again", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            startActivity(getIntent());
                        }
                    })
                    .create();

            builder.show();
        }
    }


    // TODO: changing turn
    public void changeTurn(){
        if((!playerCrossTurn && isCross) || (!playerRingTurn && !isCross)){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    public void recieveData(String buttonTag)
    {
        final String text = buttonTag;

        runOnUiThread(new Runnable() {
            @Override
            public void run() { //Show message on UIThread
                //Toast.makeText(GameActivity.this, text, Toast.LENGTH_SHORT).show();
                reloadGameField(getButtonByTag(text));
            }
        });

    }

    @Override
    public void waitForOpponent()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() { //Show message on UIThread
                progressDialog = new ProgressDialog(GameActivity.this);
                progressDialog.setMessage("Waiting for opponent...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        });
    }

    @Override
    public void opponentReady()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() { //Show message on UIThread
                progressDialog.dismiss();
            }
        });
    }


    // --- GAME LOGIC ---
    private int checkEnd(int x, int y) {
        int winner;

        winner = check_horizontal(x,y);
        if(winner !=0)
            return winner;
        winner = check_vertical(x,y);
        if(winner !=0)
            return winner;
        winner = check_diagonal_t2b(x,y);
        if(winner !=0)
            return winner;
        winner = check_diagonal_b2t(x,y);
        if(winner !=0)
            return winner;
        winner = check_tie();
        if(winner !=0)
            return  winner;

        return winner;
    }

    private int check_horizontal(int x, int y)
    {
        int counter = 1;

        //check horizontal

        //check right side
        if (y != 6 && gameField[x][y] == gameField[x][y + 1])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (y != 5 && gameField[x][y] == gameField[x][y + 2])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
                if (y != 4 && gameField[x][y] == gameField[x][y + 3])
                {
                    counter++;
                    if (counter >= 5)
                    {
                        return gameField[x][y];
                    }
                    if (y != 3 && gameField[x][y] == gameField[x][y + 4])
                    {
                        counter++;
                        if (counter >= 5)
                        {
                            return gameField[x][y];
                        }
                    }
                }
            }
        }
        //check left side
        if (y != 0 && gameField[x][y] == gameField[x][y - 1])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (y != 1 && gameField[x][y] == gameField[x][y - 2 ])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
                if (y != 2 && gameField[x][y] == gameField[x][y - 3])
                {
                    counter++;
                    if (counter >= 5)
                    {
                        return gameField[x][y];
                    }
                    if (y != 3 && gameField[x][y] == gameField[x][y - 4])
                    {
                        counter++;
                        if (counter >= 5)
                        {
                            return gameField[x][y];
                        }
                    }
                }
            }
        }
        return 0;
    }

    private int check_vertical(int x, int y)
    {
        int counter = 1;

        //check vertical
        //check top
        if (x != 6 && gameField[x][y] == gameField[x + 1][y])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (x != 5 && gameField[x][y] == gameField[x + 2][y])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
                if (x != 4 && gameField[x][y] == gameField[x + 3][y])
                {
                    counter++;
                    if (counter >= 5)
                    {
                        return gameField[x][y];
                    }
                    if (x != 3 && gameField[x][y] == gameField[x + 4][y])
                    {
                        counter++;
                        if (counter >= 5)
                        {
                            return gameField[x][y];
                        }
                    }
                }
            }
        }

        //check bottom
        if (x != 0 && gameField[x][y] == gameField[x - 1][y])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (x != 1 && gameField[x][y] == gameField[x - 2][y])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
                if (x != 2 && gameField[x][y] == gameField[x - 3][y])
                {
                    counter++;
                    if (counter >= 5)
                    {
                        return gameField[x][y];
                    }
                    if (x != 3 && gameField[x][y] == gameField[x - 4][y])
                    {
                        counter++;
                        if (counter >= 5)
                        {
                            return gameField[x][y];
                        }
                    }
                }
            }
        }

        return 0;
    }

    private int check_diagonal_t2b(int x, int y)
    {

        int counter = 1;
        //check diagonal from left top to bottom right
        //check right side
        if (y != 6 && x != 6 && gameField[x][y] == gameField[x + 1][y + 1])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (y != 5 && x != 5 && gameField[x][y] == gameField[x + 2][y + 2])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
                if (y != 4 && x != 4 && gameField[x][y] == gameField[x + 3][y + 3])
                {
                    counter++;
                    if (counter >= 5)
                    {
                        return gameField[x][y];
                    }
                    if (y != 3 && x != 3 && gameField[x][y] == gameField[x + 4][y + 4])
                    {
                        counter++;
                        if (counter >= 5)
                        {
                            return gameField[x][y];
                        }
                    }
                }
            }
        }

        //check left side
        if (y != 0 && x != 0 && gameField[x][y] == gameField[x - 1][y - 1])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (y != 1 && x != 1 && gameField[x][y] == gameField[x - 2][y - 2])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
                if (y != 2 && x != 2 && gameField[x][y] == gameField[x - 3][y - 3])
                {
                    counter++;
                    if (counter >= 5)
                    {
                        return gameField[x][y];
                    }
                    if (y != 3 && x != 3 && gameField[x][y] == gameField[x - 4][y - 4])
                    {
                        counter++;
                        if (counter >= 5)
                        {
                            return gameField[x][y];
                        }
                    }
                }
            }
        }
        return 0;
    }

    private int check_diagonal_b2t(int x, int y)
    {
        int counter = 1;
        //check diagonal from left bottom to right top
        //check right side
        if (y != 6 && x != 0 && gameField[x][y] == gameField[x - 1][y + 1])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (y != 5 && x != 1 && gameField[x][y] == gameField[x - 2][y + 2])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
                if (y != 4 && x != 2 && gameField[x][y] == gameField[x - 3][y + 3])
                {
                    counter++;
                    if (counter >= 5)
                    {
                        return gameField[x][y];
                    }
                    if (y != 3 && x != 3 && gameField[x][y] == gameField[x - 4][y + 4])
                    {
                        counter++;
                        if (counter >= 5)
                        {
                            return gameField[x][y];
                        }
                    }
                }
            }
        }

        //check left side
        if (y != 0 && x != 6 && gameField[x][y] == gameField[x + 1][y - 1])
        {
            counter++;
            if (counter >= 5)
            {
                return gameField[x][y];
            }
            if (y != 1 && x != 5 && gameField[x][y] == gameField[x + 2][y - 2])
            {
                counter++;
                if (counter >= 5)
                {
                    return gameField[x][y];
                }
                if (y != 2 && x != 4 && gameField[x][y] == gameField[x + 3][y - 3])
                {
                    counter++;
                    if (counter >= 5)
                    {
                        return gameField[x][y];
                    }
                    if (y != 3 && x != 3 && gameField[x][y] == gameField[x + 4][y - 4])
                    {
                        counter++;
                        if (counter >= 5)
                        {
                            return gameField[x][y];
                        }
                    }
                }
            }
        }
        return 0;
    }
    private int check_tie()
    {
        //check for an emty field as long as there is one the game is continuing if not its a tie
        for(int i=0;i<7;i++)
            for(int j=0;j<7;j++)
                if(gameField[i][j]==0)
                    return 0;
        return 3;
    }
}
