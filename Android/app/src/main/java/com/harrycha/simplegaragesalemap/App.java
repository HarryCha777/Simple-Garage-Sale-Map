package com.harrycha.simplegaragesalemap;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {

    // global variables
    public static String myDeviceID;
    public static double myLatitude, myLongitude;
    public static String myAddress;
    public static boolean isUserAdded;
    public static boolean isBuyer; // false for seller
    public static boolean areInterestsUpdated;
    public static boolean isFirstTimeSeller;
    public static boolean finishedSetUp;
    public static boolean hasLocation;
    public static boolean areSalesUpdated;
    public static boolean notifyNearbySales;
    public static int nearbySalesProximity;
    public static GoogleMap mMap;

    public static boolean isStartDateTime, isStartEndDateTimeSet;
    public static long startDate, endDate, beforeChangeStartDate, beforeChangeEndDate, currStartDate, currEndDate;
    public static int startHour, endHour, beforeChangeStartHour, beforeChangeEndHour, currStartHour, currEndHour;
    public static int startMin, endMin, beforeChangeStartMin, beforeChangeEndMin, currStartMin, currEndMin;

    public static List<String> viewSaleDisplayValues;
    public static Marker viewSaleMarker;
    public static boolean isEditing;
    public static String viewWebLink;
    public static Drawable viewImageDrawable;

    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;

    public static SalesHelper salesHelper;
    public static NotifiedSalesHelper notifiedSalesHelper;

    // global methods
    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }

    public boolean hasBannedWord(String text) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("banned_words.txt")));

            String mLine;
            while ((mLine = reader.readLine()) != null) {
                if (text.toLowerCase().contains(mLine.toLowerCase())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void countChar(EditText textEditText, TextView textCounterTextView, int charLimit) {
        final TextWatcher textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textCounterTextView.setText(s.length() + "/" + charLimit);
            }

            public void afterTextChanged(Editable s) {
            }
        };
        textEditText.addTextChangedListener(textWatcher);
    }

    public static String getCurrentTimestamp() {
        Calendar cal = Calendar.getInstance();

        String y = String.format("%04d", cal.get(Calendar.YEAR));
        String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        String d = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
        String h = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
        String min = String.format("%02d", cal.get(Calendar.MINUTE));
        String s = String.format("%02d", cal.get(Calendar.SECOND));
        String millis = String.format("%03d", cal.get(Calendar.MILLISECOND));

        return String.format("%s-%s-%s %s:%s:%s.%s", y, month, d, h, min, s, millis);
    }

    public boolean isAppRunning() {
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Activity.ACTIVITY_SERVICE);
        String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        return packageName.equals(getApplicationContext().getPackageName());
    }

    public static double getDistance(double lat1, double lat2, double lon1, double lon2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c * 1000; // convert to meters
        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }

    // web server methods
    RequestQueue requestQueue;

    void checkNewUser() {
        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/IsNewUser.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.trim().equals("New User")) {
                    addUser();
                } else {
                    isUserAdded = true;
                    startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to check new user.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("DeviceID", myDeviceID);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    void addUser() {
        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/AddUser.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                isUserAdded = true;
                startActivity(new Intent(getApplicationContext(), BuyerSellerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to add new user.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("DeviceID", myDeviceID);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    void updateInterests(boolean antiques, boolean bicycles, boolean books, boolean clothes, boolean exerciseEquipment, boolean games,
                         boolean jewelry, boolean outdoorFurniture, boolean tools, boolean toys, String others) {
        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/UpdateInterests.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                areInterestsUpdated = true;
                if (isBuyer) {
                    startActivity(new Intent(getApplicationContext(), FirstMessageActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    isFirstTimeSeller = true;
                    startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to update interests.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("DeviceID", myDeviceID);
                params.put("IsBuyer", Integer.toString(isBuyer ? 1 : 0));
                params.put("Antiques", Integer.toString(antiques ? 1 : 0));
                params.put("Bicycles", Integer.toString(bicycles ? 1 : 0));
                params.put("Books", Integer.toString(books ? 1 : 0));
                params.put("Clothes", Integer.toString(clothes ? 1 : 0));
                params.put("ExerciseEquipment", Integer.toString(exerciseEquipment ? 1 : 0));
                params.put("Games", Integer.toString(games ? 1 : 0));
                params.put("Jewelry", Integer.toString(jewelry ? 1 : 0));
                params.put("OutdoorFurniture", Integer.toString(outdoorFurniture ? 1 : 0));
                params.put("Tools", Integer.toString(tools ? 1 : 0));
                params.put("Toys", Integer.toString(toys ? 1 : 0));
                params.put("Others", others);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    void checkVersionCode(Activity activity) {
        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/GetMinimumVersionCode.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int minVersionCode = Integer.parseInt(response.trim());

                int currVersionCode = 0;
                try {
                    PackageInfo pInfo = activity.getApplication().getPackageManager().getPackageInfo(getPackageName(), 0);
                    currVersionCode = pInfo.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (minVersionCode > currVersionCode) {
                    startActivity(new Intent(getApplicationContext(), UpdateActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to get minimum version code.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    void checkSale(ConstraintLayout parentConstraintLayout, ProgressBar progressBar) {
        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/GetLastEndDateTime.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (Timestamp.valueOf(response).before((Timestamp.valueOf(getCurrentTimestamp())))) {
                    startActivity(new Intent(getApplicationContext(), PostSaleActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    Toast.makeText(getApplicationContext(), "You currently already have a sale post.", Toast.LENGTH_SHORT).show();
                }

                enableDisableViewGroup(parentConstraintLayout, true);
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to get last end date time.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("DeviceID", myDeviceID);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void addSale(String address, String title, String description,
                        String image1, String image2, String image3,
                        String email, String phoneNumber, String postedDateTime, String startDateTime, String endDateTime) {
        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/AddSale.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                salesHelper.add(myDeviceID, myLatitude, myLongitude, postedDateTime, endDateTime, title, address);
                new MapActivity().showSale(myDeviceID, myLatitude, myLongitude, postedDateTime, title, address);

                Toast.makeText(getApplicationContext(), "The sale has been added!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to add the sale.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("DeviceID", myDeviceID);
                params.put("Latitude", Double.toString(myLatitude));
                params.put("Longitude", Double.toString(myLongitude));
                params.put("Address", myAddress);
                params.put("Title", title);
                params.put("Description", description);
                params.put("Image1", image1);
                params.put("Image2", image2);
                params.put("Image3", image3);
                params.put("Email", email);
                params.put("PhoneNumber", phoneNumber);
                params.put("PostedDateTime", postedDateTime);
                params.put("StartDateTime", startDateTime);
                params.put("EndDateTime", endDateTime);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void editSale(String title, String description,
                         String image1, String image2, String image3,
                         String email, String phoneNumber, String startDateTime, String endDateTime) {
        ArrayList<String> markerInfo = (ArrayList<String>) viewSaleMarker.getTag();
        String viewSalePostedDateTime = markerInfo.get(1);

        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/EditSale.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                salesHelper.edit(viewSalePostedDateTime, endDateTime, title);
                viewSaleMarker.setTitle(title);
                isEditing = false;

                Toast.makeText(getApplicationContext(), "The sale has been updated!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to edit the sale.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("DeviceID", myDeviceID);
                params.put("Title", title);
                params.put("Description", description);
                params.put("Image1", image1);
                params.put("Image2", image2);
                params.put("Image3", image3);
                params.put("Email", email);
                params.put("PhoneNumber", phoneNumber);
                params.put("PostedDateTime", viewSalePostedDateTime);
                params.put("StartDateTime", startDateTime);
                params.put("EndDateTime", endDateTime);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void deleteSale() {
        ArrayList<String> markerInfo = (ArrayList<String>) viewSaleMarker.getTag();
        String viewSalePostedDateTime = markerInfo.get(1);

        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/DeleteSale.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                salesHelper.delete(viewSalePostedDateTime);
                viewSaleMarker.remove();

                Toast.makeText(getApplicationContext(), "The sale has been deleted!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to delete the sale.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("DeviceID", myDeviceID);
                params.put("PostedDateTime", viewSalePostedDateTime);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void viewSale(ConstraintLayout parentConstraintLayout, ProgressBar progressBar) {
        ArrayList<String> markerInfo = (ArrayList<String>) viewSaleMarker.getTag();
        String viewSalePostedDateTime = markerInfo.get(1);

        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/GetSale.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<String> saleColumnList = Arrays.asList(response.split("MARK END COL"));

                // response: "[Link to sale web page]"

                if (saleColumnList.size() == 1) {
                    viewWebLink = response;
                    startActivity(new Intent(getApplicationContext(), WebActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    return;
                }

                // response: "[Address]MARK END COL[Title]MARK END COL[Description]MARK END COL[Image1]MARK END COL[Image2]MARK END COL[Image3]MARK END COL[Email]MARK END COL[PhoneNumber]MARK END COL[StartDateTime]MARK END COL[EndDateTime]"
                // date times, titles, descriptions, images, and emails may have spaces, so "MARK END COL" is used to separate these columns.
                // users are NOT allowed to contain "MARK END COL" in their titles, descriptions, and emails!

                if (saleColumnList.size() != 10) {
                    Toast.makeText(getApplicationContext(), "Error: Please try again.", Toast.LENGTH_LONG).show();
                    enableDisableViewGroup(parentConstraintLayout, true);
                    progressBar.setVisibility(View.GONE);
                    viewSaleMarker.remove();
                    updateSales();
                    return;
                }

                viewSaleDisplayValues = saleColumnList;
                startActivity(new Intent(getApplicationContext(), ViewSaleActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Failed to fetch the sale.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("PostedDateTime", viewSalePostedDateTime);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void updateSales() {
        String url = "http://ec2-34-220-72-130.us-west-2.compute.amazonaws.com/GetAllSales.php";
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<String> saleRowList = Arrays.asList(response.split("MARK END ROW"));
                // date times, titles, and myAddress may have spaces, so "MARK END ROW" is used to separate these rows.
                // users are NOT allowed to contain "MARK END ROW" in their titles and myAddress.

                salesHelper.clear();
                for (int i = 0; i < saleRowList.size(); i++) {
                    String saleRow = saleRowList.get(i);
                    List<String> saleColumnList = Arrays.asList(saleRow.split("MARK END COL"));

                    // saleRow: "[DeviceID]MARK END COL[Latitude]MARK END COL[Longitude]MARK END COL[PostedDateTime]MARK END COL[EndDateTime]MARK END COL[Title]MARK END COL[Address]"
                    // date times, titles, and myAddress may have spaces, so "MARK END COL" is used to separate these columns.
                    // users are NOT allowed to contain "MARK END COL" in their titles and myAddress.

                    if (saleColumnList.size() != 7) continue;

                    double latitude = Double.parseDouble(saleColumnList.get(1));
                    double longitude = Double.parseDouble(saleColumnList.get(2));
                    if (getDistance(myLatitude, latitude, myLongitude, longitude) > 250000) continue;

                    salesHelper.add(saleColumnList.get(0), latitude, longitude,
                            saleColumnList.get(3), saleColumnList.get(4), saleColumnList.get(5), saleColumnList.get(6));
                }
                areSalesUpdated = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Failed to fetch sales.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}
