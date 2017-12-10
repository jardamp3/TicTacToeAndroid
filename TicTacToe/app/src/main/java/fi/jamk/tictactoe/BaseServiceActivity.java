package fi.jamk.tictactoe;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import org.jetbrains.annotations.Nullable;

/**
 * Created by seide on 28.11.2017.
 */

public abstract class BaseServiceActivity extends Activity
{
    BluetoothService btService;
    boolean isBound = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_basic);

        Intent i = new Intent(this, BluetoothService.class);
        bindService(i, btserviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(btserviceConnection);
        super.onDestroy();
    }

    private ServiceConnection btserviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            BluetoothService.BTLocalBinder binder = (BluetoothService.BTLocalBinder)iBinder;
            btService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            isBound = false;
        }
    };
}
