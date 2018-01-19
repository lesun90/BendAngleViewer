package com.bendangle.bluefruit.le.connect.app;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.bendangle.bluefruit.le.connect.R;
import com.bendangle.bluefruit.le.connect.app.settings.ConnectedSettingsActivity;
import com.bendangle.bluefruit.le.connect.app.settings.MqttUartSettingsActivity;
import com.bendangle.bluefruit.le.connect.ble.BleManager;
import com.bendangle.bluefruit.le.connect.mqtt.MqttManager;
import com.bendangle.bluefruit.le.connect.mqtt.MqttSettings;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;


public class UartActivity extends UartInterfaceActivity implements BleManager.BleManagerListener, MqttManager.MqttManagerListener {
    // Log
    private final static String TAG = UartActivity.class.getSimpleName();

    // Activity request codes (used for onActivityResult)
    private static final int kActivityRequestCode_ConnectedSettingsActivity = 0;
    private static final int kActivityRequestCode_MqttSettingsActivity = 1;

    // Constants
    private final static String kPreferences = "UartActivity_prefs";
    private final static String kPreferences_eol = "eol";
    private final static String kPreferences_echo = "echo";
    private final static String kPreferences_asciiMode = "ascii";

    private int mTxColor;
    private int mRxColor;
    private int mMqttSubscribedColor;

    // UI
    private Switch mEchoSwitch;
    private Switch mEolSwitch;
    private EditText mangleData;
    private MenuItem mMqttMenuItem;
    private Handler mMqttMenuItemAnimationHandler;

    // Data
    private boolean mShowDataInHexFormat;

    private SpannableStringBuilder mAsciiSpanBuffer;
    private SpannableStringBuilder mHexSpanBuffer;

    private DataFragment mRetainedDataFragment;

    private MqttManager mMqttManager;

    private EditText angleTextView;
    private EditText sensor0TextView;
    private EditText sensor1TextView;
    private EditText dataLogTextView;

    private Button resetBtn;
    private int resetBtnState = 0;
    private int elapsed = 0;

    private String dataBuffer;
    private String angleData;
    private int nrSensor;
    private int sensorID;
    private String sensor0Data;
    private String sensor1Data;
    CountDownTimer countDownTimer;

    //info Code
    private int INFO_CODE;
    //info data
    private String serialNumberText = "";
    private String pipelengthText = "";
    private String pipeNumberText = "";
    private String heatNumberText = "";
    private String latitudeText = "";
    private String longitudeText = "";
    private String chainAgeText = "";
    private String ovalityText = "";
    private String temperatureText = "";
    private String noteText = "";
    private int nPull = 0;
    private String currentLogData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart);

        mBleManager = BleManager.getInstance(this);
        restoreRetainedDataFragment();

        // Choose UI controls component based on available width
        {
//            LinearLayout headerLayout = (LinearLayout) findViewById(R.id.headerLayout);
//            ViewGroup controlsLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_uart_singleline_controls, headerLayout, false);
//            controlsLayout.measure(0, 0);
//            int controlWidth = controlsLayout.getMeasuredWidth();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int rootWidth = size.x;

//            if (controlWidth > rootWidth)       // control too big, use a smaller version
//            {
//                controlsLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_uart_multiline_controls, headerLayout, false);
//            }
//            //Log.d(TAG, "width: " + controlWidth + " baseWidth: " + rootWidth);
//
//            headerLayout.addView(controlsLayout);
        }

        // Get default theme colors
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        mTxColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorControlActivated, typedValue, true);
        mRxColor = typedValue.data;

        //theme.resolveAttribute(R.attr.colorControlHighlight, typedValue, true);
        //mMqttSubscribedColor = typedValue.data;
        mMqttSubscribedColor = Color.parseColor("#555555");

        // Read preferences
        SharedPreferences preferences = getSharedPreferences(kPreferences, MODE_PRIVATE);
        final boolean echo = preferences.getBoolean(kPreferences_echo, true);
        final boolean eol = preferences.getBoolean(kPreferences_eol, true);
        final boolean asciiMode = preferences.getBoolean(kPreferences_asciiMode, true);

//        // UI
//        mEchoSwitch = (Switch) findViewById(R.id.echoSwitch);
//        mEchoSwitch.setChecked(echo);
//        mEolSwitch = (Switch) findViewById(R.id.eolSwitch);
//        mEolSwitch.setChecked(eol);

//        RadioButton asciiFormatRadioButton = (RadioButton) findViewById(R.id.asciiFormatRadioButton);
//        asciiFormatRadioButton.setChecked(asciiMode);
//        RadioButton hexFormatRadioButton = (RadioButton) findViewById(R.id.hexFormatRadioButton);
//        hexFormatRadioButton.setChecked(!asciiMode);
//        mShowDataInHexFormat = !asciiMode;

        mangleData = (EditText) findViewById(R.id.angleData);
        mangleData.setKeyListener(null);     // make it not editable

        angleTextView = (EditText) findViewById(R.id.angleData);
        angleTextView.setKeyListener(null);
        sensor0TextView = (EditText) findViewById(R.id.sensor0);
        sensor0TextView.setKeyListener(null);
        sensor1TextView = (EditText) findViewById(R.id.sensor1);
        sensor1TextView.setKeyListener(null);
        dataLogTextView = (EditText) findViewById(R.id.dataLog);
        dataLogTextView.setKeyListener(null);

        resetBtn = (Button) findViewById(R.id.resetBtn);

        countDownTimer = new CountDownTimer(5000, 1) {
            public void onTick(long millisUntilFinished) {
                if (resetBtnState == 1)
                {
                    elapsed = (int) millisUntilFinished;
                    String resetBtnText = String.format("%.1f",(double)elapsed/1000) + " s - Press to cancel";
                    resetBtn.setText(resetBtnText);
                }
            }
            public void onFinish() {
                if (resetBtnState == 1)
                {
                    uartSendData("f", false);
                    resetBtnState = 0;
                    elapsed = 0;
                    String resetBtnText = "RESET";
                    resetBtn.setText(resetBtnText);
                }
            }
        };

        angleData = "No Data";
        sensor0Data = getString(R.string.dataNotUpdated);
        sensor1Data = getString(R.string.dataNotUpdated);
        updateUI();

        // Continue
        onServicesDiscovered();

        // Mqtt init
        mMqttManager = MqttManager.getInstance(this);
        if (MqttSettings.getInstance(this).isConnected()) {
            mMqttManager.connectFromSavedSettings(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Setup listeners
        mBleManager.setBleListener(this);

        mMqttManager.setListener(this);
        updateMqttStatus();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save preferences
//        SharedPreferences preferences = getSharedPreferences(kPreferences, MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putBoolean(kPreferences_echo, mEchoSwitch.isChecked());
//        editor.putBoolean(kPreferences_eol, mEolSwitch.isChecked());
//        editor.putBoolean(kPreferences_asciiMode, !mShowDataInHexFormat);
//
//        editor.commit();
    }

    @Override
    public void onDestroy() {
        // Disconnect mqtt
        if (mMqttManager != null) {
            mMqttManager.disconnect();
        }

        // Retain data
        saveRetainedDataFragment();

        super.onDestroy();
    }

    public void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public void onClickExport(View view) {
        String exportTextInfo =
                "Serial Number: " + serialNumberText + "\n" +
                "Number of pull: " + nPull + "\n" +
                "Pipe length: " + pipelengthText + "\n" +
                "Pipe number: " + pipeNumberText + "\n" +
                "Heat number: " + heatNumberText + "\n" +
                "Latitude: " + latitudeText + "\n" +
                "Longitude: " + longitudeText + "\n" +
                "Chain age: " + chainAgeText + "\n" +
                "Ovality: " + ovalityText + "\n" +
                "Temperature: " + temperatureText + "\n"+
                "Note: " + noteText + "\n";
        String exportText = "LOG DATA: " + "\n"+ currentLogData + "\n" +"----------"+ "\n"+
                "Information:" + "\n" + exportTextInfo;

        if (exportText != null && exportText.length() > 0) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, exportText);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.uart_share_subject));     // subject will be used if sent to an email app
            sendIntent.setType("text/*");       // Note: don't use text/plain because dropbox will not appear as destination
            // startActivity(sendIntent);
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.uart_sharechooser_title)));      // Always show the app-chooser
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.uart_share_empty))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    public void onClickReset(View view) {
        if (resetBtnState == 0)
        {
            resetBtnState = 1;
            countDownTimer.start();
            uartSendData("s", false);
            String resetBtnText = String.format("%.1f",(double)elapsed/1000) + " s - Press to cancel";
            resetBtn.setText(resetBtnText);
        }
        else if (resetBtnState == 1)
        {
            uartSendData("b", false);
            resetBtnState = 0;
            elapsed = 0;
            countDownTimer.cancel();
            String resetBtnText = "RESET";
            resetBtn.setText(resetBtnText);
        }
        //Log.d(TAG, "resetBtnState:" + resetBtnState);

    }

    public void onClickAddData(View view) {
        nPull++;
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String lastData = dataLogTextView.getText().toString();
        currentLogData = currentDateTimeString + " : " + angleTextView.getText().toString() + " degree" + "\n" + lastData;
        dataLogTextView.setText(currentLogData);

    }

    public void onClickDeleteLast(View view) {
        if (currentLogData =="")
        {
            return;
        }

        String[] dataLines;
        dataLines = currentLogData.split("\n");
        String newLogData="";
        for (int i = 1; i < dataLines.length;i++)
        {
            newLogData += dataLines[i] + "\n";
        }
        currentLogData = newLogData;
        dataLogTextView.setText(currentLogData);
        nPull--;
    }

    public void onClickDeleteAll(View view) {
        if (currentLogData =="")
        {
            return;
        }
        currentLogData = "";
        dataLogTextView.setText(currentLogData);
        nPull = 0;
    }


    private void uartSendData(String data, boolean wasReceivedFromMqtt) {
        // MQTT publish to TX
        MqttSettings settings = MqttSettings.getInstance(UartActivity.this);
        if (!wasReceivedFromMqtt) {
            if (settings.isPublishEnabled()) {
                String topic = settings.getPublishTopic(MqttUartSettingsActivity.kPublishFeed_TX);
                final int qos = settings.getPublishQos(MqttUartSettingsActivity.kPublishFeed_TX);
                mMqttManager.publish(topic, data, qos);
            }
        }

//        // Add eol
//        if (mEolSwitch.isChecked()) {
//            // Add newline character if checked
//            data += "\n";
//        }

        // Send to uart
        if (!wasReceivedFromMqtt || settings.getSubscribeBehaviour() == MqttSettings.kSubscribeBehaviour_Transmit) {
            sendData(data);
        }

//        // Show on UI
//        if (mEchoSwitch.isChecked()) {      // Add send data to visible buffer if checked
//            int color = wasReceivedFromMqtt ? mMqttSubscribedColor : mTxColor;       // mTxColor for standard input or mqttsubscribedcolor when is something that should not be published to mqtt (it has been received from a mqqt subscribed feed=
//            addTextToSpanBuffer(mAsciiSpanBuffer, data, color);
//            addTextToSpanBuffer(mHexSpanBuffer, asciiToHex(data), color);
//        }

        updateUI();
    }

    public void onClickCopy(View view) {
        String text = mShowDataInHexFormat ? mHexSpanBuffer.toString() : mAsciiSpanBuffer.toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("UART", text);
        clipboard.setPrimaryClip(clip);
    }

    public void onClickClear(View view) {
        mAsciiSpanBuffer.clear();
        mHexSpanBuffer.clear();
        updateUI();
    }

    public void onClickShare(View view) {
        String textToSend = (mShowDataInHexFormat ? mHexSpanBuffer : mAsciiSpanBuffer).toString();

        if (textToSend != null && textToSend.length() > 0) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, textToSend);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.uart_share_subject));     // subject will be used if sent to an email app
            sendIntent.setType("text/*");       // Note: don't use text/plain because dropbox will not appear as destination
            // startActivity(sendIntent);
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.uart_sharechooser_title)));      // Always show the app-chooser
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.uart_share_empty))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    public void onClickFormatAscii(View view) {
        mShowDataInHexFormat = false;
        updateUI();
    }

    public void onClickFormatHex(View view) {
        mShowDataInHexFormat = true;
        updateUI();
    }


    // region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_uart, menu);

//        mMqttMenuItem = menu.findItem(R.id.action_mqttsettings);
//        mMqttMenuItemAnimationHandler = new Handler();
//        mMqttMenuItemAnimationRunnable.run();

        return true;
    }

    private Runnable mMqttMenuItemAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            updateMqttStatus();
            mMqttMenuItemAnimationHandler.postDelayed(mMqttMenuItemAnimationRunnable, 500);
        }
    };
    private int mMqttMenuItemAnimationFrame = 0;

    private void updateMqttStatus() {
        if (mMqttMenuItem == null) return;      // Hack: Sometimes this could have not been initialized so we don't update icons

        MqttManager mqttManager = mMqttManager.getInstance(this);
        MqttManager.MqqtConnectionStatus status = mqttManager.getClientStatus();

        if (status == MqttManager.MqqtConnectionStatus.CONNECTING) {
            final int kConnectingAnimationDrawableIds[] = {R.drawable.mqtt_connecting1, R.drawable.mqtt_connecting2, R.drawable.mqtt_connecting3};
            mMqttMenuItem.setIcon(kConnectingAnimationDrawableIds[mMqttMenuItemAnimationFrame]);
            mMqttMenuItemAnimationFrame = (mMqttMenuItemAnimationFrame + 1) % kConnectingAnimationDrawableIds.length;
        } else if (status == MqttManager.MqqtConnectionStatus.CONNECTED) {
            mMqttMenuItem.setIcon(R.drawable.mqtt_connected);
            mMqttMenuItemAnimationHandler.removeCallbacks(mMqttMenuItemAnimationRunnable);
        } else {
            mMqttMenuItem.setIcon(R.drawable.mqtt_disconnected);
            mMqttMenuItemAnimationHandler.removeCallbacks(mMqttMenuItemAnimationRunnable);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            startHelp();
            return true;
        }
//        else if (id == R.id.action_connected_settings) {
//            startConnectedSettings();
//            return true;
//        } else if (id == R.id.action_refreshcache) {
//            if (mBleManager != null) {
//                mBleManager.refreshDeviceCache();
//            }
//        } else if (id == R.id.action_mqttsettings) {
//            Intent intent = new Intent(this, MqttUartSettingsActivity.class);
//            startActivityForResult(intent, kActivityRequestCode_MqttSettingsActivity);
//        }
//        Log.d(TAG, "=-=-=--=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=:");


        return super.onOptionsItemSelected(item);
    }

    private void startConnectedSettings() {
        // Launch connected settings activity
        Intent intent = new Intent(this, ConnectedSettingsActivity.class);
        startActivityForResult(intent, kActivityRequestCode_ConnectedSettingsActivity);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == INFO_CODE && resultCode == RESULT_OK && intent != null)
        {
            serialNumberText = intent.getExtras().getString("serialNumber");
            pipelengthText = intent.getExtras().getString("pipelength");
            pipeNumberText = intent.getExtras().getString("pipeNumber");
            heatNumberText = intent.getExtras().getString("heatNumber");
            latitudeText = intent.getExtras().getString("latitude");
            longitudeText = intent.getExtras().getString("longitude");
            chainAgeText = intent.getExtras().getString("chainAge");
            ovalityText = intent.getExtras().getString("ovality");
            temperatureText = intent.getExtras().getString("temperature");
            noteText = intent.getExtras().getString("note");
        }
//        if (requestCode == kActivityRequestCode_ConnectedSettingsActivity && resultCode == RESULT_OK) {
//            finish();
//        } else if (requestCode == kActivityRequestCode_MqttSettingsActivity && resultCode == RESULT_OK) {
//
//        }
    }

    private void startHelp() {
        // Launch app help activity
        Intent intent = new Intent(this, CommonHelpActivity.class);
        intent.putExtra("serialNumber", serialNumberText);
        intent.putExtra("pipelength",   pipelengthText);
        intent.putExtra("pipeNumber",   pipeNumberText);
        intent.putExtra("heatNumber",   heatNumberText);
        intent.putExtra("latitude",     latitudeText);
        intent.putExtra("longtitude",   longitudeText);
        intent.putExtra("chainAge",     chainAgeText);
        intent.putExtra("ovality",      ovalityText);
        intent.putExtra("temperature",  temperatureText);
        intent.putExtra("note",         noteText);
        startActivityForResult(intent, INFO_CODE);
    }
    // endregion

    // region BleManagerListener
    @Override
    public void onConnected() {

    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDisconnected() {
        //Log.d(TAG, "Disconnected. Back to previous activity");
        finish();
    }

    @Override
    public void onServicesDiscovered() {
        mUartService = mBleManager.getGattService(UUID_SERVICE);

        mBleManager.enableNotification(mUartService, UUID_RX, true);
    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        // UART RX
        if (characteristic.getService().getUuid().toString().equalsIgnoreCase(UUID_SERVICE)) {
            if (characteristic.getUuid().toString().equalsIgnoreCase(UUID_RX)) {
                final String data = new String(characteristic.getValue(), Charset.forName("UTF-8"));

//                addTextToSpanBuffer(mAsciiSpanBuffer, data, mRxColor);
//                addTextToSpanBuffer(mHexSpanBuffer, asciiToHex(data), mRxColor);

                dataBuffer += data;
                int i = dataBuffer.indexOf('\n');

                if (i == -1)
                {
                    return;
                }

                String[] components;
                components = dataBuffer.split(",");
                dataBuffer = "";

                if (components.length != 4)
                {
                    return;
                }
                nrSensor = Integer.parseInt(components[1]);
                sensorID = Integer.parseInt(components[2]);
                double angle = Double.parseDouble(components[3]);
                if (nrSensor == 2)
                {
                    if ((sensorID == 0) && (angle<=90) && (angle >= -90) )
                    {
                        angleData = Double.toString(angle);
                        sensor0Data = getString(R.string.dataUpdated);
                        sensor1Data = getString(R.string.dataNotUpdated);
                    }
                    else if ((sensorID == 1)&& (angle<=90) && (angle >= -90) )
                    {
                        angleData = Double.toString(angle);
                        sensor0Data = getString(R.string.dataNotUpdated);
                        sensor1Data = getString(R.string.dataUpdated);
                    }
                    else
                    {
                        angleData = "No Data";
                        sensor0Data = getString(R.string.dataNotUpdated);
                        sensor1Data = getString(R.string.dataNotUpdated);
                    }

                }
                if (nrSensor == 1)
                {
                    if (sensorID == 0)
                    {
                        angleData = "No Data";
                        sensor0Data = getString(R.string.dataUpdated);
                        sensor1Data = getString(R.string.dataNotUpdated);
                    }
                    if (sensorID == 1)
                    {
                        angleData = "No Data";
                        sensor0Data = getString(R.string.dataNotUpdated);
                        sensor1Data = getString(R.string.dataUpdated);
                    }
                }
                if (nrSensor == 0)
                {
                    angleData = "No Data";
                    sensor0Data = getString(R.string.dataNotUpdated);
                    sensor1Data = getString(R.string.dataNotUpdated);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();

                        // MQTT publish to RX
                        MqttSettings settings = MqttSettings.getInstance(UartActivity.this);
                        if (settings.isPublishEnabled()) {
                            String topic = settings.getPublishTopic(MqttUartSettingsActivity.kPublishFeed_RX);
                            final int qos = settings.getPublishQos(MqttUartSettingsActivity.kPublishFeed_RX);
                            mMqttManager.publish(topic, data, qos);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }

    // endregion

    private void addTextToSpanBuffer(SpannableStringBuilder spanBuffer, String text, int color) {
        final int from = spanBuffer.length();
        spanBuffer.append(text);
        spanBuffer.setSpan(new ForegroundColorSpan(color), from, from + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void updateUI() {
//        mangleData.setText(mShowDataInHexFormat ? mHexSpanBuffer : mAsciiSpanBuffer);
//        mangleData.setSelection(0, mangleData.getText().length());        // to automatically scroll to the end
        angleTextView.setText(angleData);
        sensor0TextView.setText(sensor0Data);
        sensor1TextView.setText(sensor1Data);
    }

    private String asciiToHex(String text) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            String charString = String.format("0x%02X", (byte) text.charAt(i));

            stringBuffer.append(charString + " ");
        }
        return stringBuffer.toString();
    }


    // region DataFragment
    public static class DataFragment extends Fragment {
        private boolean mShowDataInHexFormat;
        private SpannableStringBuilder mAsciiSpanBuffer;
        private SpannableStringBuilder mHexSpanBuffer;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    private void restoreRetainedDataFragment() {
        // find the retained fragment
        FragmentManager fm = getFragmentManager();
        mRetainedDataFragment = (DataFragment) fm.findFragmentByTag(TAG);

        if (mRetainedDataFragment == null) {
            // Create
            mRetainedDataFragment = new DataFragment();
            fm.beginTransaction().add(mRetainedDataFragment, TAG).commit();

            mAsciiSpanBuffer = new SpannableStringBuilder();
            mHexSpanBuffer = new SpannableStringBuilder();
        } else {
            // Restore status
            mShowDataInHexFormat = mRetainedDataFragment.mShowDataInHexFormat;
            mAsciiSpanBuffer = mRetainedDataFragment.mAsciiSpanBuffer;
            mHexSpanBuffer = mRetainedDataFragment.mHexSpanBuffer;
        }
    }

    private void saveRetainedDataFragment() {
        mRetainedDataFragment.mShowDataInHexFormat = mShowDataInHexFormat;
        mRetainedDataFragment.mAsciiSpanBuffer = mAsciiSpanBuffer;
        mRetainedDataFragment.mHexSpanBuffer = mHexSpanBuffer;
    }
    // endregion


    // region MqttManagerListener

    @Override
    public void onMqttConnected() {
        updateMqttStatus();
    }

    @Override
    public void onMqttDisconnected() {
        updateMqttStatus();
    }

    @Override
    public void onMqttMessageArrived(String topic, MqttMessage mqttMessage) {
        final String message = new String(mqttMessage.getPayload());

        //Log.d(TAG, "Mqtt messageArrived from topic: " +topic+ " message: "+message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uartSendData(message, true);       // Don't republish to mqtt something received from mqtt
            }
        });

    }

    // endregion
}
