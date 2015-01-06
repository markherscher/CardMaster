package cards.herscher.cardmaster.comm;

import java.io.IOException;
import java.net.Socket;

import cards.herscher.cardmaster.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by MarkHerscher on 11/11/2014.
 */
public class WifiDirectConnector
{
    public interface Listener
    {
        // TODO: on stopped and started
        public void connectSuccessful(Socket socket);

        public void connectFailed();
    }

    private final static String TAG = "WifiDirectConnector";
    private final static int SERVER_PORT = 8322;

    private final Context context;
    private final Handler handler;
    private final WifiP2pManager manager;
    private final IntentFilter intentFilter;
    private final Listener listener;
    private WifiP2pManager.Channel channel;
    private boolean isConnecting;

    public WifiDirectConnector(Context context, Handler handler, WifiP2pManager manager,
            Listener listener)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("context cannot be null");
        }

        if (manager == null)
        {
            throw new IllegalArgumentException("manager cannot be null");
        }

        this.context = context;
        this.handler = handler;
        this.manager = manager;
        this.listener = listener;
        intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    public void connect(WifiP2pDevice device, WifiP2pManager.Channel channel)
    {
        if (isConnecting)
        {
            throw new IllegalStateException("already connecting");
        }

        if (channel == null)
        {
            throw new IllegalArgumentException();
        }

        this.channel = channel;
        isConnecting = true;
        context.registerReceiver(broadcastReceiver, intentFilter);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        manager.connect(channel, config, new WifiP2pManager.ActionListener()
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
        if (isConnecting)
        {
            context.unregisterReceiver(broadcastReceiver);
            isConnecting = false;

            listenerNotify(new Runnable()
            {
                public void run()
                {
                    listener.connectFailed();
                }
            });
        }
    }

    private void listenerNotify(Runnable r)
    {
        if (listener != null)
        {
            if (handler == null)
            {
                r.run();
            }
            else
            {
                handler.post(r);
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
            {
                if (isConnecting)
                {
                    NetworkInfo networkInfo = (NetworkInfo) intent
                            .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                    if (networkInfo.isConnected())
                    {
                        // We are connected with the other device via P2P, request connection
                        // info to find group owner IP
                        manager.requestConnectionInfo(channel, connectListener);
                    }
                    else
                    {
                        // It's a disconnect
                        handleConnectFailed();
                    }
                }
            }
        }
    };

    private WifiP2pManager.ConnectionInfoListener connectListener = new WifiP2pManager.ConnectionInfoListener()
    {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info)
        {
            isConnecting = false;

            AsyncTask.execute(new Runnable()
            {
                public void run()
                {
                    Socket socket = null;

                    try
                    {
                        socket = new Socket(info.groupOwnerAddress, SERVER_PORT);
                    }
                    catch (IOException e)
                    {
                        Logger.e(TAG, "Failed to open socket: %s", e.getMessage());
                    }

                    if (socket != null)
                    {
                        final Socket finalSocket = socket;
                        listenerNotify(new Runnable()
                        {
                            public void run()
                            {
                                listener.connectSuccessful(finalSocket);
                            }
                        });
                    }
                    else
                    {
                        handleConnectFailed();
                    }
                }
            });

        }
    };
}
