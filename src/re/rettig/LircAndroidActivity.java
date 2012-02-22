package re.rettig;

import java.io.IOException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LircAndroidActivity extends Activity {
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_ENABLE_BT = 3;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static String DEVICE_NAME;
	public static String TOAST;

	private RemoteDescription remoteDescription;
	private RemoteConnectorService remoteConnectorService;
	private BluetoothAdapter bluetoothAdapter;
	protected String connectedDeviceName;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		remoteConnectorService = new RemoteConnectorService(this, handler);
		
		try {
			remoteDescription = new RemoteLoader().load(Environment.getExternalStorageDirectory()+"/remotes/A1294");
			for (final String command:remoteDescription.codes.keySet()){
				Button button = new Button(this);
				button.setText(command);
				((LinearLayout) findViewById(R.id.linearLayout1)).addView(button);
				button.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						Log.d("Command", command);
						try {
							remoteConnectorService.sendPulses(remoteDescription.getPulses(command));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;

		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				remoteConnectorService = new RemoteConnectorService(this, handler);
			} else {
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		remoteConnectorService.connect(device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;

		case R.id.discoverable:
			ensureDiscoverable();
			return true;
		}
		return false;
	}
	
	private void ensureDiscoverable() {
		if (bluetoothAdapter.getScanMode() !=
			BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}
	
	// The Handler that gets information back from the BluetoothChatService
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case RemoteConnectorService.STATE_CONNECTED:
					Log.d("BT","Connected");
					break;
				case RemoteConnectorService.STATE_CONNECTING:
					Log.d("BT","Connecting");
					break;
				case RemoteConnectorService.STATE_LISTEN:
				case RemoteConnectorService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				connectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to " + connectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),	Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
}