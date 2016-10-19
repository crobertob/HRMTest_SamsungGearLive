/*
 * Copyright (C) 2014 Marc Lester Tan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marctan.hrmtest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.util.Log;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MyActivity extends Activity implements SensorEventListener{

    private static final String TAG = MyActivity.class.getName();

    private TextView rate;
    private TextView accuracy;
    private TextView sensorInformation;
    private static final int SENSOR_TYPE_HEART_RATE = 65538; // wellness passive sensor
    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;
    private CountDownLatch latch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        latch = new CountDownLatch(1);
        final ViewStub stub = (ViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnInflateListener(new ViewStub.OnInflateListener() {
            @Override
            public void onInflate(ViewStub stub, View inflated) {
                rate = (TextView) stub.findViewById(R.id.rate);
                rate.setText("Reading...");

                accuracy = (TextView) stub.findViewById(R.id.accuracy);
                sensorInformation = (TextView) stub.findViewById(R.id.sensor);

                latch.countDown();
            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE_HEART_RATE);

        if (mHeartRateSensor == null) {
            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            for (Sensor availableSensor : sensors) {
                Log.i(TAG, availableSensor.getName() + ": " + availableSensor.getType());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSensorManager.registerListener(this, this.mHeartRateSensor, 3);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        try {
            latch.await();
            if(sensorEvent.values[0] > 0){
                Log.d(TAG, "sensor event: " + sensorEvent.accuracy + " = " + sensorEvent.values[0]);
                rate.setText(String.valueOf(sensorEvent.values[0]));
                accuracy.setText("Accuracy: "+sensorEvent.accuracy);
                sensorInformation.setText(sensorEvent.sensor.toString());
            }

        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "accuracy changed: " + i);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mSensorManager.unregisterListener(this);
    }
}
