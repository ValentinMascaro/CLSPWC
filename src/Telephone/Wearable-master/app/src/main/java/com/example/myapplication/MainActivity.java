package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Declaration of constants and variables for sensors, update intervals, and data
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor stepSensor;
    private int lightSensorUpdateInterval = 1000; // Delay for the light sensor in milliseconds
    private int stepSensorUpdateInterval = 3000; // Delay for the step sensor in milliseconds
    private int locationUpdateInterval = 5000; // Delay for the location sensor in milliseconds
    private int sendDataInterval = 6000; // Delay to send data in milliseconds
    private JSONData data;
    // node-red
    private String urlString = "http://192.168.0.14:1880/jsonData";
    private double lastLatitude = Double.MIN_VALUE; // Initial value for the first execution
    private double lastLongitude = Double.MIN_VALUE; // Initial value for the first execution
    private double lastLightValue = Double.MIN_VALUE; // Initial value for the first execution
    private double lastStepValue = Double.MIN_VALUE; // Initial value for the first execution

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialization of handlers and sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Initialization for location and JSON data
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        data = new JSONData();

        // Initialization of the xml layout
        setContentView(R.layout.activity_main);

        // Callback for location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // Processing location results
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        double currentLatitude = location.getLatitude();
                        double currentLongitude = location.getLongitude();

                        if (currentLatitude != lastLatitude || currentLongitude != lastLongitude) {
                            // Record only if current values are different from the last recorded values
                            String time = String.valueOf(System.currentTimeMillis());
                            data.addValue("latitude", time, currentLatitude);
                            data.addValue("longitude", time, currentLongitude);

                            // Update the last recorded values
                            lastLatitude = currentLatitude;
                            lastLongitude = currentLongitude;
                            Log.d("MainActivity", "Location changed: " + currentLatitude + ", " + currentLongitude);
                        }
                    }
                }
            }
        };
        Log.d("MainActivity", "Sensors initialized");

    }

    // Definition of a Handler and a task to send data at regular intervals
    private Handler sendDataHandler = new Handler(Looper.getMainLooper());
    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            // Send data
            try {
                postData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Schedule the next send after the specified interval
            sendDataHandler.postDelayed(this, sendDataInterval);
        }
    };

    // Handling activity lifecycle events
    @Override
    protected void onStart() {
        super.onStart();
        // Request location permission at startup
        requestLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listeners
        if (lightSensor != null) {
            // we can use the light sensor
            //
            sensorManager.registerListener(this, lightSensor, lightSensorUpdateInterval);
        }
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, stepSensorUpdateInterval);
        }
        // Start sending data at regular intervals
        sendDataHandler.postDelayed(sendDataRunnable, sendDataInterval);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listeners and stop sending data when paused
        sensorManager.unregisterListener(this);
        sendDataHandler.removeCallbacks(sendDataRunnable);
    }

    // Method to handle sensor changes
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            // Handling light sensor data
            float currentValue = event.values[0];
            if (currentValue != lastLightValue) {
                // Record only if the current value is different from the last recorded value
                data.addValue("luminosity", String.valueOf(System.currentTimeMillis()), currentValue);
                lastLightValue = currentValue; // Update the last recorded value
                Log.d("MainActivity", "Light sensor changed: " + currentValue);
            }
            // modify the value of the light sensor in the xml layout id = luminosityTextView
            TextView luminosityTextView = (TextView) findViewById(R.id.luminosityTextView);
            luminosityTextView.setText("Luminosity = " + String.valueOf(currentValue));

        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Handling step sensor data
            float currentValue = event.values[0];
            if (currentValue != lastStepValue) {
                // Record only if the current value is different from the last recorded value
                data.addValue("podometer", String.valueOf(System.currentTimeMillis()), currentValue);
                lastStepValue = currentValue; // Update the last recorded value
                Log.d("MainActivity", "Step sensor changed: " + currentValue);
            }
        }
    }

    // Methods for handling location permission

    private void requestLocationPermission() {
        // Check and request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        // Start location updates with a request for high accuracy
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(locationUpdateInterval);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handle the response to the location permission request
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method for data transmission
    public void postData() throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        URL url = new URL(urlString);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        // Configure the HTTP request
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setDoOutput(true);

                        String test = data.getJSONString();

                        if(data.isEmpty()){
                            Log.d("MainActivity", "No data to send");
                            return null;
                        }

                        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                            // Send JSON data
                            wr.writeBytes(data.getJSONString());
                            wr.flush();
                        }

                        // WARNING: If no response, it waits indefinitely
                        int responseCode = connection.getResponseCode(); // Get the response code
                        Log.d("MainActivity", "Response code: " + responseCode);

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            // If the response is OK (200), the request was processed successfully
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                String inputLine;
                                StringBuilder response = new StringBuilder();
                                Log.d("MainActivity", "Server response received");

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                                System.out.println("Server response: " + response.toString());
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                Log.e("MainActivity", "Exception during reading response: " + e.getMessage());
                            }
                            // Reset JSON data
                            data.empty();
                        } else {
                            // Handle errors if the server response is not OK
                            Log.e("MainActivity", "Request failed. Response code: " + responseCode);
                            System.out.println("Request failed. Response code: " + responseCode);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("MainActivity", "Exception during HTTP request: " + e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    // Here, you can update the user interface or handle any action after the end of the network operation
                }
            }.execute();
        } else {
            // If no internet connection is available
            Log.w("MainActivity", "No internet connection. Request not sent.");
            System.out.println("No internet connection available. Unable to send the request.");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Handle changes in sensor accuracy
        Log.d("MainActivity", "Sensor accuracy changed: " + sensor.getName() + ", new accuracy: " + i);
    }
}
