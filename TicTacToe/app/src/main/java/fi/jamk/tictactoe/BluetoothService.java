package fi.jamk.tictactoe;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import static android.content.ContentValues.TAG;

/**
 * Created by seide on 25.11.2017.
 */

public class BluetoothService extends Service
{
    private final IBinder btBinder = new BTLocalBinder();
    private IServiceCallbacks serviceCallbacks;

    private boolean CONTINUE_READ_WRITE = true;
    private boolean CONNECTION_ENSTABLISHED = false;

    private static String NAME = "fi.jamk.tictactoe"; //id of app
    private static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //SerialPortService ID // MY_UUID is the app's UUID string, also used by the client code.

    private Thread serverThread;
    private Thread clientThread;
    private Thread writterThread;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket socket;
    private InputStream is;
    private OutputStream os;
    private BluetoothDevice remoteDevice;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<String> listDevices;
    private boolean wantToBeServer;

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return btBinder;
    }

    // Get paired devices
    public void getDevices(){
        pairedDevices = btAdapter.getBondedDevices(); //list of devices
        listDevices = new ArrayList<String>();

        for(BluetoothDevice bt : pairedDevices) {
            listDevices.add(0, bt.getName());
            Log.d("Paired device:", bt.getName());
        }
    }

    // Opens right thread for server or client
    public void openConnection() {
        CONTINUE_READ_WRITE = true; //writer tiebreaker
        socket = null; //resetting if was used previously
        is = null; //resetting if was used previously
        os = null; //resetting if was used previously

        if(pairedDevices.isEmpty() || remoteDevice == null) {
            Toast.makeText(this, "Paired device is not selected, choose one", Toast.LENGTH_SHORT).show();
            return;
        }

        if(wantToBeServer) {
            serverThread = new Thread(serverListener);
            serverThread.start();
        }
        else {
            clientThread = new Thread(clientConnecter);
            clientThread.start();
        }
    }

    // Closes the connection (kills server and client threads), remote device still kept
    public void closeConnection(){
        CONTINUE_READ_WRITE = false;

        if(wantToBeServer)
            serverThread.interrupt();
        else //CLIENT
            clientThread.interrupt();

        writterThread.interrupt();
    }

    // Thread for flushing otput stream
    private Runnable writter = new Runnable() {
        @Override
        public void run() {
            //reads from open stream
            while (CONTINUE_READ_WRITE){
                try {
                    os.flush();
                    Thread.sleep(2000);
                } catch (Exception e) {
                    Log.e(TAG, "Writer failed in flushing output stream...");
                    CONTINUE_READ_WRITE = false;
                }
            }
        }
    };

    // SERVER THREAD
    private Runnable serverListener = new Runnable()
    {
        public void run()
        {
            serviceCallbacks.waitForOpponent();
            try {
                Log.d("SERVER:", "Trying to run");
                BluetoothServerSocket tmpsocket = btAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                socket = tmpsocket.accept();
                CONNECTION_ENSTABLISHED = true; //protect from failing
                Log.d("TrackingFlow", "Listening...");
            } catch (Exception ie) {
                Log.e(TAG, "Socket's accept method failed", ie);
                ie.printStackTrace();
            }

            Log.d(TAG, "Server is ready for listening...");
            serviceCallbacks.opponentReady();

            //reading part
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
                writterThread = new Thread(writter);
                writterThread.start();

                int bytes;
                byte[] buffer = new byte[1024];

                //Keep reading the messages while connection is open...
                while(CONTINUE_READ_WRITE)  {
                    bytes = is.read(buffer);
                    String readedString = new String(buffer, 0, bytes);

                    Log.d("Read on server: ", readedString);

                    if (serviceCallbacks != null) {
                        serviceCallbacks.recieveData(readedString);
                    }
                }
            }
            catch(IOException e){
                Log.d(TAG, "Server not connected...");
                e.printStackTrace();
            }
        }
    };

    // CLIENT THREAD
    private Runnable clientConnecter = new Runnable() {
        @Override
        public void run() {
            // loading dialog for waiting
            serviceCallbacks.waitForOpponent();

            // Obtaining socket
            try{
                socket = remoteDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch(IOException e) {
                Log.e("CLIENT:", "Socket's create() method failed", e);
            }

            // Connection socket
            while(true){
                try{
                    Log.d("CLIENT:", "Trying to connect socket...");
                    socket.connect();
                    break;
                } catch(IOException e) {
                    Log.e("CLIENT:", "Failed to connect socket", e);
                }
            }

            try {
                CONNECTION_ENSTABLISHED = true; //protect from failing
                Log.d(TAG, "Client is connected...");


                os = socket.getOutputStream();
                is = socket.getInputStream();
                writterThread = new Thread(writter);
                writterThread.start();

                Log.d(TAG, "Preparation for reading was done");

                int bytes;
                byte[] buffer = new byte[1024];

                // dissmis loading dialog
                serviceCallbacks.opponentReady();

                //Keep reading the messages while connection is open...
                while(CONTINUE_READ_WRITE) {
                    bytes = is.read(buffer);
                    String readedString = new String(buffer, 0, bytes);

                    Log.d("Read on client: ", readedString.toString());

                    if (serviceCallbacks != null) {
                        serviceCallbacks.recieveData(readedString.toString());
                    }
                }
            } catch (IOException e) {
                Log.e("CLIENT:", "Error with streams", e);
                e.printStackTrace();
            }
        }
    };

    // Method for sending a string to other device - writes to output stream
    public void sendString(String text) {
        if(CONNECTION_ENSTABLISHED == false) {
            Toast.makeText(getApplicationContext(), "Connection between devices is not ready.", Toast.LENGTH_SHORT).show(); //usually problem server-client decision
        }
        else {
            byte[] b = text.getBytes();
            try {
                os.write(b);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Not sent", Toast.LENGTH_SHORT).show(); //usually problem server-client decision
            }
        }
    }

    // Turning on BT when is off
    public boolean isBtEnabled() {
        if (!btAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getApplicationContext().startActivity(turnOn);
            return false;
        } else {
            getDevices();
            return true;
        }
    }


    // Local Binder class
    public class BTLocalBinder extends Binder{
        BluetoothService getService(){
            return BluetoothService.this;
        }
    }

    // Setting callbacks - for call methods from activities implementing IServiceCallbacks interface
    public void setCallbacks(IServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    public ArrayList<String> getListDevices() {
        return this.listDevices;
    }

    // Setting remote device by name
    public void setRemoteDevice(String name){
        for(BluetoothDevice bt : pairedDevices) {
            if(name.equals(bt.getName())) {
                remoteDevice = bt;
            }
        }
        Log.d("Selected device:", remoteDevice.getName());
    }

    // Setting if device will be server or client
    public void setWantToBeServer(boolean choice){
        this.wantToBeServer = choice;
        Log.d("Want to be server:", Boolean.toString(wantToBeServer));
    }
}

