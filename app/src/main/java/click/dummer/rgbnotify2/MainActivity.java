
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * June 2017, Modified by Jochen Peters (Deadlockz - Krefeld)
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package click.dummer.rgbnotify2;




import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import click.dummer.rgbnotify2.R;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "rgbnotify2";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    private String[] states = {
            "red to green",
            "fade to yellow",
            "red blink",
            "green blink",
            "all colors",
            "blink red to just green"
    };
    public long stateIndex = 0;
    public ArrayAdapter<String> adapter_state;
    private boolean startDisco = false;
    Handler handler = new Handler();

    public SeekBar colorBar;
    public SeekBar whiteBar;
    public int wbar;
    public int red   = 0;
    public int green = 0;
    public int blue  = 0;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;
    private Spinner spinnerState;
    private ToggleButton toggleDisco;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend = (Button) findViewById(R.id.sendButton);
        btnSend.setBackgroundColor(Color.RED);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();

        colorBar = (SeekBar) findViewById(R.id.colorBar);
        whiteBar = (SeekBar) findViewById(R.id.whiteBar);
        toggleDisco = (ToggleButton) findViewById(R.id.toggleDisco);

        spinnerState = (Spinner) findViewById(R.id.spinner);
        adapter_state = new ArrayAdapter<String>(this,  android.R.layout.simple_spinner_item, states);
        spinnerState.setAdapter(adapter_state);
        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
               spinnerState.setSelection(position);
               stateIndex = id;
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {

           }
        });

        toggleDisco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (! btnSend.isEnabled() ) return;
                if (! toggleDisco.isChecked() ) return;
                switch ((int) stateIndex) {
                    case 0:
                        startDisco = true;
                        redToGreen();
                        break;
                    case 1:
                        startDisco = true;
                        fadeToYellow();
                        break;
                    case 2:
                        startDisco = true;
                        redBlink();
                        break;
                    case 3:
                        startDisco = true;
                        greenBlink();
                        break;
                    case 4:
                        startDisco = true;
                        allColors();
                        break;
                    case 5:
                        startDisco = true;
                        redBlinkToGreen();
                        break;
                    default:
                }
            }
        });

        whiteBar.setMax(255);
        whiteBar.setBackgroundColor(Color.BLACK);
        whiteBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                wbar = seekBar.getProgress();
                whiteBar.setBackgroundColor(Color.argb(220, wbar, wbar, wbar));
                if (red   < wbar) red   = wbar;
                if (green < wbar) green = wbar;
                if (blue  < wbar) blue  = wbar;
                btnSend.setBackgroundColor(Color.argb(220, red, green, blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateEdtMessage();
            }
        });

        colorBar.setMax(639);
        colorBar.setBackgroundColor(Color.RED);
        colorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int wh = seekBar.getProgress();
                red   = 0;
                green = 0;
                blue  = 0;
                if (wh < 128) {
                    green = wh * 2;
                    red   = 255;
                } else if (wh < 256) {
                    red   = 255 - 2*(wh-128);
                    green = 255;
                } else if (wh < 384) {
                    blue  = 2*(wh-256);
                    green = 255;
                } else if (wh < 512) {
                    blue  = 255;
                    green = 255 - 2*(wh-384);
                } else {
                    blue = 255;
                    red  = 2*(wh-512);
                }
                colorBar.setBackgroundColor(Color.argb(220, red, green, blue));
                if (red   < wbar) red   = wbar;
                if (green < wbar) green = wbar;
                if (blue  < wbar) blue  = wbar;
                btnSend.setBackgroundColor(Color.argb(220, red, green, blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateEdtMessage();
            }
        });
       
        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	if (btnConnectDisconnect.getText().equals("Connect")){
                		
                		//Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                		
            			Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            			startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        			} else {
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
        					mService.disconnect();
        					
        				}
        			}
                }
            }
        });
        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	EditText editText = (EditText) findViewById(R.id.sendText);
            	String message = editText.getText().toString();
            	byte[] value;
				try {
					//send data to service
					value = message.getBytes("UTF-8");
					mService.writeRXCharacteristic(value);
					//Update the log with time stamp
					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
					listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
               	 	edtMessage.setText("");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }
        });
     
        // Set initial UI state
        
    }

    void redToGreen() {
        if (startDisco == true) {
            red = 255;
            green = 0;
            blue = 0;
            startDisco = false;
        }
        if (green < 255) {
            if (green > 255) green = 255;
            if (red < 0)     red   = 0;
            updateEdtMessage();
            btnSend.callOnClick();
            green += 9;
            red   -= 9;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    redToGreen();
                }
            }, 300);
        } else {
            green = 255;
            red = 0;
            toggleDisco.setChecked(false);
        }
    }

    void fadeToYellow() {
        if (startDisco == true) {
            red = 0;
            green = 0;
            blue = 0;
            startDisco = false;
        }
        if (red < 255) {
            if (red > 255) red = 255;
            updateEdtMessage();
            btnSend.callOnClick();
            green += 4;
            red   += 9;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fadeToYellow();
                }
            }, 300);
        } else {
            green = 128;
            red = 255;
            toggleDisco.setChecked(false);
        }
    }

    void redBlink() {
        if (! toggleDisco.isChecked() ) return;
        if (startDisco == true) {
            red = 0;
            green = 0;
            blue = 0;
            startDisco = false;
        } else {
            red = 255;
            green = 0;
            blue = 0;
            startDisco = true;
        }
        updateEdtMessage();
        btnSend.callOnClick();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {redBlink();}
        }, 800);
    }

    void greenBlink() {
        if (! toggleDisco.isChecked() ) return;
        if (startDisco == true) {
            red = 0;
            green = 0;
            blue = 0;
            startDisco = false;
        } else {
            red = 0;
            green = 255;
            blue = 0;
            startDisco = true;
        }
        updateEdtMessage();
        btnSend.callOnClick();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {greenBlink();}
        }, 800);
    }

    void allColors() {
        if (! toggleDisco.isChecked() ) return;
        int wh = colorBar.getProgress();
        if (startDisco == true) {
            wh += 16;
        } else {
            wh -= 16;
        }

        if (wh > colorBar.getMax()) {
            wh = colorBar.getMax()-1;
            startDisco = false;
        }
        if (wh < 1) {
            wh = 1;
            startDisco = true;
        }

        colorBar.setProgress(wh);
        updateEdtMessage();
        btnSend.callOnClick();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {allColors();}
        }, 300);
    }

    void redBlinkToGreen() {
        if (startDisco == true) {
            red = 255;
            green = 0;
            blue = 0;
            startDisco = false;
        }
        if (green < 255) {
            if (green > 255) green = 255;
            updateEdtMessage();
            btnSend.callOnClick();
            green += 5;
            if (green%10 == 0) {
                red = 0;
            } else {
                red = 255;
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    redBlinkToGreen();
                }
            }, 200);
        } else {
            green = 255;
            red = 0;
            toggleDisco.setChecked(false);
        }
    }


    private void updateEdtMessage() {
        edtMessage.setText(rgbToString(red, green, blue));
    }
    
    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_CONNECT_MSG");
                             btnConnectDisconnect.setText("Disconnect");
                             edtMessage.setEnabled(true);
                             btnSend.setEnabled(true);
                             ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                             listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                             mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
          //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             edtMessage.setEnabled(false);
                             btnSend.setEnabled(false);
                             ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                             listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
          
          //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }
          //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
              
                 final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                 runOnUiThread(new Runnable() {
                     public void run() {
                         try {
                             String text = new String(txValue, "UTF-8");
                             String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             listAdapter.add("["+currentDateTimeString+"] RX: "+text);

                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }
                     }
                 });
             }
           //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
            
            
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mService.connect(deviceAddress);
                            

            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  
    }

    public void sendOff(View view) {
        if (! btnSend.isEnabled() ) return;

        String message = "0";
        byte[] value;
        try {
            //send data to service
            value = message.getBytes("UTF-8");
            mService.writeRXCharacteristic(value);
            //Update the log with time stamp
            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
            listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
            edtMessage.setText("");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("UART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }

    static public String rgbToString(int r, int g, int b) {
        int col=65; // A
        String colorCode = "";
        // red ABCDEFGHIJ
        if (r >= 0) {
            colorCode += (char) (col + (0.039215 * r));
        }
        // green KLMNOPQRS
        if (g >= 0) {
            colorCode += (char) (col + 10 + (0.0352 * g));
        }
        // blue TUVWXYZ
        if (b >= 0) {
            colorCode += (char) (col + 19 + (0.0272 * b));
        }
        return colorCode;
    }
}
