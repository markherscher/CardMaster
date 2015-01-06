package cards.herscher.cardmaster.comm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Looper;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cards.herscher.cardmaster.Logger;

/**
 * This class is not thread-safe.
 */
public class WifiDirectClient
{
    public interface Listener
    {
        public void onPeersAvailable(WifiP2pDevice[] peerDevices);

        public void onDiscoveryStartFailure(int errorCode);

        public void connectSuccessful(Socket socket, WifiP2pInfo info);

        // TODO: REASON
        public void onConnectFailed();
    }

    private final static int SERVER_PORT = 8322;
    private final static String TAG = "WifiDirectClient";

    private final Context context;
    private final WifiP2pManager wifiManager;
    private final List<Listener> listeners;
    private final IntentFilter intentFilter;
    private WifiP2pManager.Channel channel;
    private boolean isOpen;
    private boolean isConnecting;
    private boolean allowDiscovery;

    public WifiDirectClient(Context context)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("context cannot be null");
        }

        this.context = context;
        wifiManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        listeners = new CopyOnWriteArrayList<Listener>();
        intentFilter = new IntentFilter();

        // TODO VERIFY ALL THESE
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    public void addListener(Listener l)
    {
        if (l != null && !listeners.contains(l))
        {
            listeners.add(l);
        }
    }

    public void removeListener(Listener l)
    {
        listeners.remove(l);
    }

    public boolean isOpen()
    {
        return isOpen;
    }

    public void open()
    {
        if (!isOpen)
        {
            isOpen = true;
            context.registerReceiver(broadcastReceiver, intentFilter);
            channel = wifiManager.initialize(context, Looper.getMainLooper(), null);
        }
    }

    public void close()
    {
        if (isOpen)
        {
            isOpen = false;
            allowDiscovery = false;
            wifiManager.removeGroup(channel, null);

            try
            {
                context.unregisterReceiver(broadcastReceiver);
            }
            catch (IllegalArgumentException e)
            {
            }
        }
    }

    public void startDiscovery()
    {
        if (!isOpen)
        {
            throw new IllegalStateException("must be open to discover");
        }

        allowDiscovery = true;
        wifiManager.discoverPeers(channel, new WifiP2pManager.ActionListener()
        {
            public void onSuccess()
            {
            }

            public void onFailure(int i)
            {
                // TODO: NOTIFY HERE?
                Logger.e(TAG, "Failed to start discovery: %d", i);
            }
        });
    }

    public void stopDiscovery()
    {
        allowDiscovery = false;
    }

    public void connect(WifiP2pDevice device)
    {
        if (!isOpen)
        {
            throw new IllegalStateException("must be open to connect");
        }

        if (isConnecting)
        {
            throw new IllegalStateException("already connecting");
        }

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;
        wifiManager.connect(channel, config, new WifiP2pManager.ActionListener()
        {

            @Override
            public void onSuccess()
            {
                // WiFiDirectBroadcastReceiver will notify us.
            }

            @Override
            public void onFailure(int reason)
            {
                handleConnectFailed();
            }
        });
    }

    private void handleConnectFailed()
    {
        if (isOpen && isConnecting)
        {
            isConnecting = false;

            for (Listener l : listeners)
            {
                l.onConnectFailed();
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (!isOpen)
            {
                return;
            }

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
            {
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected())
                {
                    // We are connected with the other device via P2P, request connection
                    // info to find group owner IP
                    wifiManager.requestConnectionInfo(channel,
                            new WifiP2pManager.ConnectionInfoListener()
                            {
                                public void onConnectionInfoAvailable(WifiP2pInfo info)
                                {
                                    new AsyncSocketCreator().execute(info);
                                }
                            });
                }
                else
                {
                    // It's a disconnect
                    handleConnectFailed();
                }
            }
            else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
            {
                // TODO: DO MORE HERE?
                switch (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, 0))
                {
                    case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                        Logger.i(TAG, "Wifi P2P is enabled");
                        break;

                    case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                        Logger.e(TAG, "Wifi P2P is disabled");
                        break;
                }
            }
            else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
            {
                // Request available peers from the manager
                if (allowDiscovery)
                {
                    wifiManager.requestPeers(channel, peerListener);
                }
            }
        }
    };

    private WifiP2pManager.PeerListListener peerListener = new WifiP2pManager.PeerListListener()
    {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList)
        {
            if (!isOpen || !allowDiscovery)
            {
                return;
            }

            Collection<WifiP2pDevice> deviceList = wifiP2pDeviceList.getDeviceList();
            WifiP2pDevice[] devices = deviceList.toArray(new WifiP2pDevice[deviceList.size()]);

            for (Listener l : listeners)
            {
                l.onPeersAvailable(devices);
            }
        }
    };

    private class AsyncSocketCreator extends AsyncTask<WifiP2pInfo, Void, Void>
    {
        private WifiP2pInfo info;
        private Socket socket;

        @Override
        protected Void doInBackground(WifiP2pInfo... params)
        {
            socket = null;
            info = params[0];

            if (info != null)
            {
                try
                {
                    this.socket = new Socket(info.groupOwnerAddress, SERVER_PORT);
                }
                catch (IOException e)
                {
                    Logger.e(TAG, "Failed to open socket: %s", e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (!isOpen)
            {
                return;
            }

            if (socket != null)
            {
                isConnecting = false;

                for (Listener l : listeners)
                {
                    l.connectSuccessful(socket, info);
                }
            }
            else
            {
                handleConnectFailed();
            }
        }
    }
}
