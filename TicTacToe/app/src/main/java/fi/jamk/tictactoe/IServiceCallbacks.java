package fi.jamk.tictactoe;

/**
 * Created by seide on 29.11.2017.
 */


// Interface for calling this methods from BluetoothService
public interface IServiceCallbacks
{
    void recieveData(String buttonTag);
    void waitForOpponent();
    void opponentReady();
}
