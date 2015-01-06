package cards.herscher.cardmaster.fivehundred.ui;

import java.io.IOException;
import java.net.Socket;

import cards.herscher.cardmaster.R;
import cards.herscher.cardmaster.comm.WifiDirectServer;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;
import android.widget.Toast;

public class HostGameActivity extends Activity
{
    private WifiP2pManager wifiManager;
    private ListView connectedListView;
    private WifiDirectServer wifiServer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fivehundred_host_game_activity);
        
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        connectedListView = (ListView) findViewById(R.id.connectedListView);
        wifiServer = new WifiDirectServer(this, new WifiServerListener());
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        wifiServer.start();
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        
        wifiServer.stop();
    }
    
    private class WifiServerListener implements WifiDirectServer.Listener
    {
        @Override
        public void onConnectionAvailable(Socket socket)
        {
            Toast.makeText(HostGameActivity.this, "Connect succeeded as server!. Yay.",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStopped()
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

        @Override
        public void onStarted()
        {
            // TODO Auto-generated method stub
            
        }
    }
}
