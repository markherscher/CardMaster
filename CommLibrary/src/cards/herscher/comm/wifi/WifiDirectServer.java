package cards.herscher.comm.wifi;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;

/**
 * This class is not thread-safe.
 * 
 */
public class WifiDirectServer
{
    public interface Listener
    {
        public void onConnectionAvailable(Socket socket);

        public void onStarted();

        public void onStopped();

        public void onErrorCreatingServer(IOException e);

        public void onErrorConnectingClient(IOException e);
    }

    // TODO
    private final static int SERVER_PORT = 8322;

    private final Context context;
    private final Listener listener;
    private final IntentFilter intentFilter;
    private final WifiP2pManager wifiManager;
    private final Handler handler;
    private WifiP2pManager.Channel channel;
    private AcceptRunnable acceptRunnable;
    private WifiP2pWaker wifiP2pWaker;
    private boolean isRunning;

    public WifiDirectServer(Context context, Listener listener)
    {
        if (context == null)
        {
            throw new IllegalArgumentException();
        }

        this.context = context;
        this.listener = listener;
        intentFilter = new IntentFilter();
        wifiManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        handler = new Handler();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    public void start()
    {
        if (isRunning)
        {
            throw new IllegalStateException();
        }

        context.registerReceiver(broadcastReceiver, intentFilter);
        isRunning = true;
        acceptRunnable = new AcceptRunnable();
        new Thread(acceptRunnable, "AcceptRunnable").start();
        // wifiP2pWaker = new WifiP2pWaker();
        // wifiP2pWaker.run();
        channel = wifiManager.initialize(context, Looper.getMainLooper(), null);
        wifiManager.createGroup(channel, null);

        if (listener != null)
        {
            listener.onStarted();
        }
    }

    public void stop()
    {
        isRunning = false;

        try
        {
            context.unregisterReceiver(broadcastReceiver);
        }
        catch (IllegalArgumentException e)
        {
        }

        if (channel != null)
        {
            wifiManager.removeGroup(channel, null);
        }

        if (acceptRunnable != null)
        {
            acceptRunnable.stop();
            acceptRunnable = null;
        }

        if (wifiP2pWaker != null)
        {
            wifiP2pWaker.stop();
            wifiP2pWaker = null;
        }
    }

    private class WifiP2pWaker implements Runnable
    {
        private final int DELAY_MS = 1000 * 10;
        private final WifiP2pManager.Channel channel;
        private boolean shouldRun = true;

        public WifiP2pWaker()
        {
            channel = wifiManager.initialize(context, Looper.getMainLooper(), null);
        }

        @Override
        public void run()
        {
            if (shouldRun)
            {
                // Poke the Wifi P2P so it wakes up and is viewable. Otherwise, it is not always
                // discoverable. We don't care about the results.
                wifiManager.discoverPeers(channel, null);
                handler.postDelayed(this, DELAY_MS);
            }
        }

        public void stop()
        {
            shouldRun = false;
            handler.removeCallbacks(this);
            wifiManager.removeGroup(channel, null);
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
            {
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected())
                {

                }
                else
                {
                    // It's a disconnect
                    // TODO
                }
            }
        }
    };

    private class AcceptRunnable implements Runnable
    {
        private ServerSocket serverSocket;
        private boolean keepRunning = true;

        public void stop()
        {
            keepRunning = false;
        }

        @Override
        public void run()
        {
            try
            {
                serverSocket = new ServerSocket(SERVER_PORT);
                serverSocket.setSoTimeout(1000);
            }
            catch (IOException e)
            {
                stop();

                if (listener != null)
                {
                            listener.onErrorCreatingServer(e);
                }
            }

            while (keepRunning)
            {
                Socket socket = null;

                try
                {
                    socket = serverSocket.accept();
                }
                catch (InterruptedIOException e)
                {
                    // No one connected within the timeout period, so loop again
                }
                catch (IOException e)
                {
                    // Don't worry if we were told to stop
                    if (keepRunning)
                    {
                        if (listener != null)
                        {
                            listener.onErrorConnectingClient(e);
                        }
                    }
                }

                if (socket != null)
                {
                    if (listener != null)
                    {
                        listener.onConnectionAvailable(socket);
                    }
                }
            }

            // Exited the run loop, so close the server socket
            if (serverSocket != null)
            {
                try
                {
                    serverSocket.close();
                }
                catch (IOException e)
                {
                }
            }

            if (listener != null)
            {
                listener.onStopped();
            }
        }
    }
}
