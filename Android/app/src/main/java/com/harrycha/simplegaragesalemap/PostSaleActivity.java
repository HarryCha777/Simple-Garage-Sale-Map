package com.harrycha.simplegaragesalemap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.regex.Pattern;

import static java.lang.StrictMath.max;

public class PostSaleActivity extends AppCompatActivity {

    ConstraintLayout parentConstraintLayout;
    ProgressBar progressBar;

    EditText titleEditText, descriptionEditText, emailEditText, phoneNumberEditText;
    TextView titleCounterTextView, descriptionCounterTextView, imageCounterTextView, emailCounterTextView, phoneNumberCounterTextView;
    TextView startDateTimeTextView, endDateTimeTextView;
    Button addImageButton, startButton, endButton, postSaleButton;

    Bitmap[] bitmaps = new Bitmap[3];
    ImageView[] images = new ImageView[3];
    Button[] cancelImageButtons = new Button[3];
    final int IMG_REQUEST = 2;
    int imageIndex;

    // When returned from DateTimeActivity, display changed date time.
    @Override
    protected void onResume() {
        super.onResume();

        if (App.isStartEndDateTimeSet) {
            startDateTimeTextView.setText(getAmericanDateTime(App.startDate, App.startHour, App.startMin));
            endDateTimeTextView.setText(getAmericanDateTime(App.endDate, App.endHour, App.endMin));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_sale);
        initViews();

        if (App.isEditing) {
            titleEditText.setText(App.viewSaleDisplayValues.get(1));
            descriptionEditText.setText(App.viewSaleDisplayValues.get(2));

            for (int i = 0; i < 3; i++) {
                String imageInString = App.viewSaleDisplayValues.get(3 + i);
                if (!imageInString.equals("")) {
                    bitmaps[imageIndex] = stringToBitmap(imageInString);
                    images[imageIndex].setVisibility(View.VISIBLE);
                    images[imageIndex].setImageBitmap(bitmaps[imageIndex]);
                    cancelImageButtons[imageIndex].setVisibility(View.VISIBLE);

                    imageIndex++;
                    imageCounterTextView.setText(imageIndex + "/3");
                    if (imageIndex == 3) {
                        addImageButton.setVisibility(View.GONE);
                    }
                }
            }

            startDateTimeTextView.setText(getAmericanDateTime(App.viewSaleDisplayValues.get(8)));
            endDateTimeTextView.setText(getAmericanDateTime(App.viewSaleDisplayValues.get(9)));
            App.startDate = App.currStartDate = App.beforeChangeStartDate = Timestamp.valueOf(App.viewSaleDisplayValues.get(8)).getTime();
            App.endDate = App.currEndDate = App.beforeChangeEndDate = Timestamp.valueOf(App.viewSaleDisplayValues.get(9)).getTime();
            App.startHour = App.currStartHour = App.beforeChangeStartHour = Timestamp.valueOf(App.viewSaleDisplayValues.get(8)).getHours();
            App.endHour = App.currEndHour = App.beforeChangeEndHour = Timestamp.valueOf(App.viewSaleDisplayValues.get(9)).getHours();
            App.startMin = App.currStartMin = App.beforeChangeStartMin = Timestamp.valueOf(App.viewSaleDisplayValues.get(8)).getMinutes();
            App.endMin = App.currEndMin = App.beforeChangeEndMin = Timestamp.valueOf(App.viewSaleDisplayValues.get(9)).getMinutes();
            App.isStartEndDateTimeSet = true;

            emailEditText.setText(App.viewSaleDisplayValues.get(6));
            phoneNumberEditText.setText(App.viewSaleDisplayValues.get(7));
            postSaleButton.setText("Edit Sale");
        } else {
            Calendar currCal = Calendar.getInstance();
            App.startDate = App.currStartDate = App.beforeChangeStartDate = App.endDate = App.currEndDate = App.beforeChangeEndDate = currCal.getTimeInMillis();
            App.startHour = App.currStartHour = App.beforeChangeStartHour = App.endHour = App.currEndHour = App.beforeChangeEndHour = currCal.get(Calendar.HOUR_OF_DAY);
            App.startMin = App.currStartMin = App.beforeChangeStartMin = App.endMin = App.currEndMin = App.beforeChangeEndMin = currCal.get(Calendar.MINUTE);
            App.isStartEndDateTimeSet = false;
        }

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        cancelImageButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageIndex == 1) {
                    bitmaps[0] = null;
                    images[0].setVisibility(View.GONE);
                    cancelImageButtons[0].setVisibility(View.GONE);
                } else if (imageIndex == 2) {
                    bitmaps[0] = bitmaps[1];
                    bitmaps[1] = null;
                    images[0].setImageBitmap(bitmaps[0]);
                    images[1].setVisibility(View.INVISIBLE);
                    cancelImageButtons[1].setVisibility(View.GONE);
                } else { // imageIndex == 3
                    bitmaps[0] = bitmaps[1];
                    bitmaps[1] = bitmaps[2];
                    bitmaps[2] = null;
                    images[0].setImageBitmap(bitmaps[0]);
                    images[1].setImageBitmap(bitmaps[1]);
                    images[2].setVisibility(View.INVISIBLE);
                    cancelImageButtons[2].setVisibility(View.GONE);
                }

                imageIndex--;
                imageCounterTextView.setText(imageIndex + "/3");
                addImageButton.setVisibility(View.VISIBLE);
            }
        });
        cancelImageButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageIndex == 2) {
                    bitmaps[1] = null;
                    images[1].setVisibility(View.INVISIBLE);
                    cancelImageButtons[1].setVisibility(View.GONE);
                } else { // imageIndex == 3
                    bitmaps[1] = bitmaps[2];
                    bitmaps[2] = null;
                    images[1].setImageBitmap(bitmaps[1]);
                    images[2].setVisibility(View.INVISIBLE);
                    cancelImageButtons[2].setVisibility(View.GONE);
                }

                imageIndex--;
                imageCounterTextView.setText(imageIndex + "/3");
                addImageButton.setVisibility(View.VISIBLE);
            }
        });
        cancelImageButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmaps[2] = null;
                images[2].setVisibility(View.INVISIBLE);
                cancelImageButtons[2].setVisibility(View.GONE);

                imageIndex--;
                imageCounterTextView.setText(imageIndex + "/3");
                addImageButton.setVisibility(View.VISIBLE);
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.isStartDateTime = true;
                App.beforeChangeStartDate = App.currStartDate = App.startDate;
                App.beforeChangeStartHour = App.currStartHour = App.startHour;
                App.beforeChangeStartMin = App.currStartMin = App.startMin;
                App.beforeChangeEndDate = App.currEndDate = App.endDate;
                App.beforeChangeEndHour = App.currEndHour = App.endHour;
                App.beforeChangeEndMin = App.currEndMin = App.endMin;
                startActivity(new Intent(getApplicationContext(), DateTimeActivity.class));
            }
        });
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.performClick();
            }
        });
        postSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titleEditText.getText().toString().equals("")) {
                    Toast.makeText(PostSaleActivity.this, "The title cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (descriptionEditText.getText().toString().equals("")) {
                    Toast.makeText(PostSaleActivity.this, "The description cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (((App) getApplication()).hasBannedWord(titleEditText.getText().toString())) {
                    Toast.makeText(PostSaleActivity.this, "The title contains a banned word. Please try a different wording.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (((App) getApplication()).hasBannedWord(descriptionEditText.getText().toString())) {
                    Toast.makeText(PostSaleActivity.this, "The description contains a banned word. Please try a different wording.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!App.isStartEndDateTimeSet) {
                    Toast.makeText(PostSaleActivity.this, "The start and end time need to be set.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (((App) getApplication()).hasBannedWord(emailEditText.getText().toString())) {
                    Toast.makeText(PostSaleActivity.this, "The email address contains a banned word. Please try a different wording.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!emailEditText.getText().toString().equals("") && !isValidEmail(emailEditText.getText().toString())) {
                    Toast.makeText(PostSaleActivity.this, "The email address is invalid.", Toast.LENGTH_SHORT).show();
                    return;
                }

                App.enableDisableViewGroup(parentConstraintLayout, false);
                progressBar.setVisibility(View.VISIBLE);

                if (App.isEditing) {
                    ((App) getApplication()).editSale(titleEditText.getText().toString(), descriptionEditText.getText().toString(),
                            bitmapToString(bitmaps[0]), bitmapToString(bitmaps[1]), bitmapToString(bitmaps[2]),
                            emailEditText.getText().toString(), phoneNumberEditText.getText().toString(),
                            getTimestamp(App.startDate, App.startHour, App.startMin), getTimestamp(App.endDate, App.endHour, App.endMin));
                } else {
                    ((App) getApplication()).addSale(App.myAddress,
                            titleEditText.getText().toString(), descriptionEditText.getText().toString(),
                            bitmapToString(bitmaps[0]), bitmapToString(bitmaps[1]), bitmapToString(bitmaps[2]),
                            emailEditText.getText().toString(), phoneNumberEditText.getText().toString(),
                            App.getCurrentTimestamp(), getTimestamp(App.startDate, App.startHour, App.startMin), getTimestamp(App.endDate, App.endHour, App.endMin));
                }
            }
        });
    }

    void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (App.isEditing)
            getSupportActionBar().setTitle("Edit Sale");
        else
            getSupportActionBar().setTitle("Post Sale");

        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);
        progressBar = findViewById(R.id.progressBar);
        titleEditText = findViewById(R.id.titleEditText);
        titleCounterTextView = findViewById(R.id.titleCounterTextView);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        descriptionCounterTextView = findViewById(R.id.descriptionCounterTextView);
        images[0] = findViewById(R.id.image1);
        images[1] = findViewById(R.id.image2);
        images[2] = findViewById(R.id.image3);
        cancelImageButtons[0] = findViewById(R.id.cancelImage1Button);
        cancelImageButtons[1] = findViewById(R.id.cancelImage2Button);
        cancelImageButtons[2] = findViewById(R.id.cancelImage3Button);
        imageCounterTextView = findViewById(R.id.imageCounterTextView);
        addImageButton = findViewById(R.id.addImageButton);
        startButton = findViewById(R.id.startButton);
        startDateTimeTextView = findViewById(R.id.startDateTimeTextView);
        endButton = findViewById(R.id.endButton);
        endDateTimeTextView = findViewById(R.id.endDateTimeTextView);
        emailEditText = findViewById(R.id.emailEditText);
        emailCounterTextView = findViewById(R.id.emailCounterTextView);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        phoneNumberCounterTextView = findViewById(R.id.phoneNumberCounterTextView);
        postSaleButton = findViewById(R.id.postSaleButton);

        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);
        images[0].setVisibility(View.GONE);
        images[1].setVisibility(View.INVISIBLE); // invisible, not gone, to fix image positions
        images[2].setVisibility(View.INVISIBLE); // invisible, not gone, to fix image positions
        cancelImageButtons[0].setVisibility(View.GONE);
        cancelImageButtons[1].setVisibility(View.GONE);
        cancelImageButtons[2].setVisibility(View.GONE);

        App.countChar(titleEditText, titleCounterTextView, 100);
        App.countChar(descriptionEditText, descriptionCounterTextView, 1000);
        App.countChar(emailEditText, emailCounterTextView, 100);
        App.countChar(phoneNumberEditText, phoneNumberCounterTextView, 20);
    }

    String getAmericanDateTime(long dateInMillis, int hour, int min) {
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(dateInMillis);

        String amPm = "AM";
        if (hour >= 12) {
            amPm = "PM";
            hour -= 12;
        }
        if (hour == 0) hour = 12;
        String minString = Integer.toString(min);
        if (min < 10) minString = "0" + minString;
        return (setCal.get(Calendar.MONTH) + 1) + "/" + setCal.get(Calendar.DAY_OF_MONTH) + "/" + (setCal.get(Calendar.YEAR) % 100) + "    " +
                hour + ":" + minString + " " + amPm;
    }

    String getAmericanDateTime(String dateTime) {
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(timestamp.getTime());
        int hour = timestamp.getHours();
        int min = timestamp.getMinutes();

        String amPm = "AM";
        if (hour >= 12) {
            amPm = "PM";
            hour -= 12;
        }
        if (hour == 0) hour = 12;
        String minString = Integer.toString(min);
        if (min < 10) minString = "0" + minString;
        return (setCal.get(Calendar.MONTH) + 1) + "/" + setCal.get(Calendar.DAY_OF_MONTH) + "/" + (setCal.get(Calendar.YEAR) % 100) + "    " +
                hour + ":" + minString + " " + amPm;
    }

    String getTimestamp(long dateInMillis, int hour, int min) {
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(dateInMillis);

        String monthString = Integer.toString(setCal.get(Calendar.MONTH) + 1);
        if (setCal.get(Calendar.MONTH) + 1 < 10) monthString = "0" + monthString;
        String dayString = Integer.toString(setCal.get(Calendar.DAY_OF_MONTH));
        if (setCal.get(Calendar.DAY_OF_MONTH) < 10) dayString = "0" + dayString;
        String hourString = Integer.toString(hour);
        if (hour < 10) hourString = "0" + hourString;
        String minString = Integer.toString(min);
        if (min < 10) minString = "0" + minString;
        return setCal.get(Calendar.YEAR) + "-" + monthString + "-" + dayString +
                " " + hourString + ":" + minString + ":00.000";
    }

    void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri path = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                bitmaps[imageIndex] = handleRotatedImage(bitmap, path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            images[imageIndex].setVisibility(View.VISIBLE);
            images[imageIndex].setImageBitmap(bitmaps[imageIndex]);
            cancelImageButtons[imageIndex].setVisibility(View.VISIBLE);

            imageIndex++;
            imageCounterTextView.setText(imageIndex + "/3");
            if (imageIndex == 3) {
                addImageButton.setVisibility(View.GONE);
            }
        }
    }

    // If image is rotated while being retrieved, rotate it back to original orientation.
    Bitmap handleRotatedImage(Bitmap bitmap, Uri path) {
        Bitmap rotatedBitmap = null;
        try {
            InputStream input = getApplicationContext().getContentResolver().openInputStream(path);
            ExifInterface ei;
            if (Build.VERSION.SDK_INT > 23)
                ei = new ExifInterface(input);
            else
                ei = new ExifInterface(path.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
        } catch (Exception e) {
        }
        return rotatedBitmap;
    }

    Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    String bitmapToString(Bitmap bitmap) {
        if (bitmap == null) return "";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }

    Bitmap stringToBitmap(String string) {
        byte[] imageBytes = Base64.decode(string, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return bitmap;
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null) return false;
        return pat.matcher(email).matches();
    }

    // on action bar's back button pressed
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        App.isEditing = false;
        finish();
    }
}

