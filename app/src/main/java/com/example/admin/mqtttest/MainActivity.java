package com.example.admin.mqtttest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Executors;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class MainActivity extends AppCompatActivity implements SerialInputOutputManager.Listener, View.OnClickListener {
    public class Constants {
        public static final int NUM_BUTTONS = 3;
    }
    TextView textFromServer;
    EditText inputToServer;
    Button[] buttons = new Button[Constants.NUM_BUTTONS];
    MQTTService mqttService;
    ImageView imgView;

    GraphView graphValue;
    DataPoint[] valueList = new DataPoint[1];
    LineGraphSeries<DataPoint> seriesValue;
    int time = 0;

    private View decorView;

    UsbSerialPort port;
    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    private void initUSBPort(){
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        if (availableDrivers.isEmpty()) {
            Log.d("UART", "UART is not available");

        }else {
            Log.d("UART", "UART is available");

            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {

                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
                manager.requestPermission(driver.getDevice(), usbPermissionIntent);

                manager.requestPermission(driver.getDevice(), PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));

                return;
            } else {

                port = driver.getPorts().get(0);
                try {
                    Log.d("UART", "openned succesful");
                    port.open(connection);
                    port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    //port.write("ABC#".getBytes(), 1000);

                    SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, this);
                    Executors.newSingleThreadExecutor().submit(usbIoManager);

                } catch (Exception e) {
                    Log.d("UART", "There is error");
                }
            }
        }

    }

    private void sendDataMQTT(String data){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);

        byte[] b = data.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        Log.d("ABC","Publish :" + msg);
        try {
            mqttService.mqttAndroidClient.publish("<Your topic>", msg);
        } catch (MqttException e){

        }
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "MQTT Test", Toast.LENGTH_LONG).show();

        textFromServer = findViewById(R.id.textReceived);
        inputToServer = findViewById(R.id.inputToServer);
        imgView = findViewById(R.id.bulbon);

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0) {
                    hideSystemBar();
                }
            }
        });

        graphValue = findViewById(R.id.graphValue);

        initUSBPort();

        mqttService = new MQTTService(this);
        mqttService.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String data_to_microbit = message.toString();
                try {
                    port.write(data_to_microbit.getBytes(),1000);
                } catch (Exception e) {
                }
                textFromServer.setText(data_to_microbit);
                switch (data_to_microbit) {
                    case "0":
                        imgView.setVisibility(View.INVISIBLE);
                        break;
                    case "1":
                        imgView.setVisibility(View.VISIBLE);
                        break;
                }
                if (time == 0) {
                    valueList[0] = new DataPoint(time, Integer.valueOf(message.toString()));
                    seriesValue = new LineGraphSeries<>(valueList);
                    showDataOnGraph(seriesValue, graphValue);
                } else {
                    DataPoint newPoint = new DataPoint(time, Integer.valueOf(message.toString()));
                    seriesValue.appendData(newPoint, false, 1000, true);
                    showDataOnGraph(seriesValue, graphValue);
                }
                time += 1;
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


        for (int i = 0; i < Constants.NUM_BUTTONS - 1; i++) {
            String buttonID = "button" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
            buttons[i].setOnClickListener(this);
        }
        buttons[2] = findViewById(R.id.sendButton);
        buttons[2].setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Do something in response to button click
        String dataToServer;
        try {
            switch (view.getId()) {
                case R.id.button1:
                    dataToServer = "1";
                    sendDataMQTT(dataToServer);
                    break;
                case R.id.button0:
                    dataToServer = "0";
                    sendDataMQTT(dataToServer);
                    break;
                case R.id.sendButton:
                    dataToServer = inputToServer.getText().toString();
                    sendDataMQTT(dataToServer);
                    break;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(hideSystemBar());
        }
    }

    private int hideSystemBar() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    private String buffer = "";
    @Override
    public void onNewData(byte[] data) {
        buffer += new String(data);
        //Log.d("UART", "Received: " + new String(data));
        if (buffer.contains("#") && buffer.contains("!")) {
            try {
                int index_soc = buffer.indexOf("#");
                int index_eoc = buffer.indexOf("!");
                String sentData = buffer.substring(index_soc + 1, index_eoc);
                buffer = "";
                sendDataMQTT(sentData);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onRunError(Exception e) {

    }

    private void showDataOnGraph(LineGraphSeries<DataPoint> series, GraphView graph){
        if(graph.getSeries().size() > 0){
            graph.getSeries().remove(0);
        }
        graph.addSeries(series);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
    }

    private void sendDataToThingSpeak(String ID, String value){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //String apiURL = "https://api.thingspeak.com/update?api_key=0324U6WNIBX28W4G&field" + ID + "=" + value;
        String apiURL = "https://api.thingspeak.com/update?api_key=0324U6WNIBX28W4G&field1=" + ID + "&field2=" + value;
        Request request = builder.url(apiURL).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {

            }
        });
    }
}
