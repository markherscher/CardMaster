package cards.herscher.cardmaster.fivehundred.ui;

import java.net.Socket;
import java.util.Arrays;

import cards.herscher.cardmaster.R;
import cards.herscher.cardmaster.comm.WifiDirectClient;
import cards.herscher.cardmaster.comm.WifiDirectServer;
import cards.herscher.cardmaster.ui.DialogManager;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class JoinGameActivity extends Activity implements View.OnClickListener
{
    private ImageButton refresh;
    private ListView availableGamesListView;
    private WifiDirectClient wifiClient;
    private DevicePeerAdapter deviceListAdapter;
    private DialogManager dialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fivehundred_join_game_activity);

        refresh = (ImageButton) findViewById(R.id.refresh);
        availableGamesListView = (ListView) findViewById(R.id.availableGamesListView);
        wifiClient = new WifiDirectClient(this);
        dialogManager = new DialogManager(this);

        availableGamesListView.setOnItemClickListener(new DeviceListViewClickListener());
        refresh.setOnClickListener(this);
        wifiClient.addListener(new WifiClientListener());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        wifiClient.open();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        wifiClient.close();
    }

    @Override
    public void onClick(View v)
    {
        if (v == refresh)
        {
            wifiClient.startDiscovery();
            refresh.setVisibility(View.INVISIBLE);
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
                wifiClient.connect(device);
                dialogManager.showProgress("Join Game", "Joining the selected game...");
            }
        }
    }

    private class WifiClientListener implements WifiDirectClient.Listener
    {
        @Override
        public void onPeersAvailable(WifiP2pDevice[] peerDevices)
        {
            refresh.setVisibility(View.VISIBLE);

            deviceListAdapter = new DevicePeerAdapter(JoinGameActivity.this,
                    Arrays.asList(peerDevices));
            availableGamesListView.setAdapter(deviceListAdapter);
        }

        @Override
        public void connectSuccessful(Socket socket, WifiP2pInfo info)
        {
            dialogManager.closeProgress();
            dialogManager.showAlert("Join Game", "The game was joined successfully.");
        }

        @Override
        public void onConnectFailed()
        {
            dialogManager.closeProgress();
            dialogManager.showAlert("Join Game", "An error occurred while joining the game.");
        }

        @Override
        public void onDiscoveryStartFailure(int errorCode)
        {
            // TODO Auto-generated method stub
            
        }
    }
}
