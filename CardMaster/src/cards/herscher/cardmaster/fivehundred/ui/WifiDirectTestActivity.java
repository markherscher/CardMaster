package cards.herscher.cardmaster.fivehundred.ui;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;

import cards.herscher.cardmaster.Logger;
import cards.herscher.cardmaster.R;
import cards.herscher.comm.wifi.WifiDirectClient;
import cards.herscher.comm.wifi.WifiDirectServer;

/**
 * Created by MarkHerscher on 11/11/2014.
 */
public class WifiDirectTestActivity extends Activity implements View.OnClickListener
{
    private WifiDirectClient client;
    private WifiDirectServer server;
    private Button scanButton;
    private Button disconnectButton;
    private TextView status;
    private Button serverButton;
    private ListView deviceListView;
    private WifiP2pDevice[] peerDevices;
    private DevicePeerAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_test);

        client = new WifiDirectClient(this);
        server = new WifiDirectServer(this, new WifiServerListener());
        scanButton = (Button) findViewById(R.id.scanButton);
        disconnectButton = (Button) findViewById(R.id.disconnect);
        serverButton = (Button) findViewById(R.id.serverButton);
        status = (TextView) findViewById(R.id.status);
        deviceListView = (ListView) findViewById(R.id.deviceListView);

        client.open();
        client.addListener(new WifiDirectClientListener());
        scanButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);
        serverButton.setOnClickListener(this);
        deviceListView.setOnItemClickListener(new DeviceListViewClickListener());
    }

    public void onResume()
    {
        super.onResume();
    }

    public void onPause()
    {
        super.onPause();

        client.close();
    }

    @Override
    public void onClick(View view)
    {
        if (view == scanButton)
        {
            client.open();
            client.startDiscovery();
            status.setText("Refreshing...");
        }
        else if (view == serverButton)
        {
            server.start();
            serverButton.setVisibility(View.INVISIBLE);
        }
        else if (view == disconnectButton)
        {
            client.close();
        }
    }

    private class DeviceListViewClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
        {
            WifiP2pDevice device = deviceListAdapter.getItem(position);
            if (device != null)
            {
                // TODO: HANDLE CHANNEL NULL. In fact I hate how Channel is accessed. Provide as
                // listener result?

                client.connect(device);

                scanButton.setVisibility(View.INVISIBLE);
                disconnectButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private class WifiDirectClientListener implements WifiDirectClient.Listener
    {
        @Override
        public void connectSuccessful(Socket socket, WifiP2pInfo info)
        {
            Toast.makeText(WifiDirectTestActivity.this, "Connect succeeded as client!. Yay.",
                    Toast.LENGTH_LONG).show();

            new Thread(new ReadRunnable(socket)).start();
            new Thread(new WriteRunnable(socket)).start();
        }

        @Override
        public void onPeersAvailable(WifiP2pDevice[] peerDevices)
        {
            // status.setText("Refresh complete.");
            WifiDirectTestActivity.this.peerDevices = peerDevices;

            // List cannot be modified
            deviceListAdapter = new DevicePeerAdapter(WifiDirectTestActivity.this,
                    Arrays.asList(peerDevices));
            deviceListView.setAdapter(deviceListAdapter);
        }

        @Override
        public void onConnectFailed()
        {
            scanButton.setVisibility(View.VISIBLE);
            disconnectButton.setVisibility(View.INVISIBLE);

            // TODO: NOTIFY
            Toast.makeText(WifiDirectTestActivity.this, "Connect FAILED!. Boo.", Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onDiscoveryStartFailure(int errorCode)
        {
            // TODO Auto-generated method stub
            
        }
    }

    private class WifiServerListener implements WifiDirectServer.Listener
    {
        @Override
        public void onConnectionAvailable(Socket socket)
        {
            Toast.makeText(WifiDirectTestActivity.this, "Connect succeeded as server!. Yay.",
                    Toast.LENGTH_LONG).show();

            new Thread(new ReadRunnable(socket)).start();
            new Thread(new WriteRunnable(socket)).start();
        }

        @Override
        public void onStopped()
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStarted()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onErrorCreatingServer(IOException e)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onErrorConnectingClient(IOException e)
        {
            // TODO Auto-generated method stub
            
        }
    }

    private class WriteRunnable implements Runnable
    {
        private final Socket socket;

        public WriteRunnable(Socket socket)
        {
            this.socket = socket;
            try
            {
                socket.setSoTimeout(10000);
            }
            catch (SocketException e)
            {
            }
        }

        @Override
        public void run()
        {
            OutputStream outStream = null;
            Random rand = new Random();

            try
            {
                outStream = socket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }

            while (true)
            {
                byte[] randData = new byte[6];
                rand.nextBytes(randData);

                Logger.i("SenderRunnable", "Sending bytes: %s", Arrays.toString(randData));

                try
                {
                    outStream.write(randData);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }

                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }

    private class ReadRunnable implements Runnable
    {
        private final Socket socket;

        public ReadRunnable(Socket socket)
        {
            this.socket = socket;
        }

        @Override
        public void run()
        {
            InputStream inStream = null;

            try
            {
                inStream = socket.getInputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }

            byte[] inData = new byte[1024];

            while (true)
            {
                try
                {
                    int count = inStream.read(inData);
                    Logger.i("ReadRunnable", "Received bytes: %s",
                            Arrays.toString(Arrays.copyOf(inData, count)));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }

            }
        }
    }
}
