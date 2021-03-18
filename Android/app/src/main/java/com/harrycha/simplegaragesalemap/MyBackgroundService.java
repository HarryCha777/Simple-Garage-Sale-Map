package com.harrycha.simplegaragesalemap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

public class MyBackgroundService extends Service implements LocationListener {
    LocationManager locationManager;

    boolean foregroundNotificationTurnedOn;

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (App.notifyNearbySales) turnOnForegroundNotification();
    }

    void turnOnForegroundNotification() {
        final String CHANNEL_ID = "appChannel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        Notification foregroundNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Ready to notify you of nearby sales.")
                .setContentText("This option can be disabled in the settings.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        startForeground(1, foregroundNotification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Runnable getLocationAndNotifyNearbySalesRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runGetLocationHandler();
                    if (App.notifyNearbySales && !foregroundNotificationTurnedOn) {
                        turnOnForegroundNotification();
                        foregroundNotificationTurnedOn = true;
                    }
                    if (!App.notifyNearbySales && foregroundNotificationTurnedOn) {
                        stopForeground(true);
                        foregroundNotificationTurnedOn = false;
                    }
                    if (App.notifyNearbySales) notifyNearbySales();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        Thread getLocationServiceThread = new Thread(getLocationAndNotifyNearbySalesRunnable);
        getLocationServiceThread.start();

        Runnable updateSalesRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (((App) getApplication()).isAppRunning() && App.hasLocation && !App.areSalesUpdated) {
                        ((App) getApplication()).updateSales();

                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        };
        Thread updateSalesServiceThread = new Thread(updateSalesRunnable);
        updateSalesServiceThread.start();

        return Service.START_STICKY;
    }

    void notifyNearbySales() {
        Timestamp currDateTime = Timestamp.valueOf(App.getCurrentTimestamp());
        Cursor data = App.salesHelper.get();
        while (data.moveToNext()) {
            String deviceID = data.getString(1);
            double latitude = data.getDouble(2);
            double longitude = data.getDouble(3);
            String postedDateTime = data.getString(4);
            String endDateTime = data.getString(5);
            String title = data.getString(6);

            if (!App.myDeviceID.equals(deviceID)
                    && !App.notifiedSalesHelper.isNotified(postedDateTime)
                    && currDateTime.before(Timestamp.valueOf(endDateTime)) // if sale is not over yet
                    && App.getDistance(App.myLatitude, latitude, App.myLongitude, longitude) < App.nearbySalesProximity) {
                showNotification("A sale is found near you!", title);
                App.notifiedSalesHelper.add(postedDateTime);
            }
        }
    }

    private void showNotification(String title, String content) {
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "channelID");
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.logo);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(content);
        mBuilder.setPriority(Notification.PRIORITY_MAX);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel("channelID", "ChannelName", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        int uniqueNumber = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE); // unique number allows multiple notifications
        mNotificationManager.notify(uniqueNumber, mBuilder.build());
    }

    Handler getLocationHandler = new Handler();

    public void runGetLocationHandler() {
        Runnable doDisplayError = new Runnable() {
            public void run() {
                getLocation();
            }
        };
        getLocationHandler.post(doDisplayError);
    }

    void getLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        App.myLatitude = location.getLatitude();
        App.myLongitude = location.getLongitude();

        ((App) getApplication()).editor.putFloat("Last Latitude", (float) App.myLatitude);
        ((App) getApplication()).editor.putFloat("Last Longitude", (float) App.myLongitude);
        ((App) getApplication()).editor.apply();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            App.myAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0).getAddressLine(0);
            App.hasLocation = true;
        } catch (Exception e) {
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (((App) getApplication()).isAppRunning() && App.finishedSetUp) {
            App.finishedSetUp = false;
            startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
