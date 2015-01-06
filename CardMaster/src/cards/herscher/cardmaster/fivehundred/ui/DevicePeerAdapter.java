package cards.herscher.cardmaster.fivehundred.ui;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by MarkHerscher on 11/17/2014.
 */
public class DevicePeerAdapter extends ArrayAdapter<WifiP2pDevice>
{

	public DevicePeerAdapter(Context context, List<WifiP2pDevice> devices)
	{
		super(context, android.R.layout.simple_list_item_1, devices);
	}
}
