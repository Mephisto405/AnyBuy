package app.anybuy.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;

import Object.LinkedList;
import Object.Node;
import Object.Order;
import app.anybuy.Clients.SocketClient;
import app.anybuy.R;



public class SellActivity extends AppCompatActivity {

    String maxOrder = "";
    String minOrder = "";

    LinearLayout linearLayout;
    private TextView textView;

    String sessionID;
    // use to get the location
    protected FusedLocationProviderClient mFusedLocationClient;

    double lattitude = -1;
    double longitude = -1;

    //use to get the address from the location
    List<Address> addresses;
    Geocoder geocoder;

    static String userCountryCode = null;

    //setter and getter to store the country code
    public static void setUserCountryCode(String str) {userCountryCode = str;}
    public static String getUserCountryCode() {return userCountryCode;}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return true;
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        final Button orderButton = (Button) findViewById(R.id.getOrderButtonID);

        final int[] id = {0};


        linearLayout = (LinearLayout) findViewById(R.id.sellpageLinearLayoutID);

        //get the location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //if no permission to access the location, ask again
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SellActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return;
        }

        //get the address from the location
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {

                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    //textView.setText("altetude: " + location.getLatitude() + " \n Longtitude: " + location.getLongitude());

                    lattitude = location.getLatitude();
                    longitude = location.getLongitude();

                    geocoder = new Geocoder(SellActivity.this, Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(lattitude, longitude, 1);
                        //get the info of the user
                        // String address = addresses.get(0).getAddressLine(0);
                        //String area = addresses.get(0).getLocality();
                        //String city = addresses.get(0).getAdminArea();
                        //String countryName = addresses.get(0).getCountryName();

                        setUserCountryCode(addresses.get(0).getCountryCode());

                        //String postalCode = addresses.get(0).getPostalCode();

                       
                    } catch (Exception e) {
                        System.out.println("location error");
                    }
                }

            }

        });



        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderButton.setText("Next Orders");
                System.out.println(getUserCountryCode() + " heyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");


                String data = "";
                sessionID = MainActivity.getID();
                LinkedList l = new LinkedList();
                l.insert("lop");
                l.insert(sessionID);
                l.insert(getUserCountryCode());
                l.insert(10);

                System.out.println("noooooooooooooooooooooooooooooooooooo");

                try {

                    Object o = SocketClient.Run(l);
                    if (o.getClass().equals("".getClass())) System.out.println((String) o);
                    else if (o.getClass().equals(new LinkedList().getClass())) {
                        LinkedList l1 = (LinkedList) o;

                        Node temp = l1.end;

                        temp = temp.getPrev().getPrev();
                        int i = 0;

                        // go through the first 10 orders from the order to newest
                        while (temp != null) {

                            Order od = (Order) temp.getObject();


                            System.out.println(od.getImage() + " " + od.getBrand() + " " + od.getProduct() +
                                    " " + od.getQuantity() + " " + od.getCountry() + " " + od.getTimestamp());


                            data = "Product Name: " + od.getProduct() + "\nBrand Name: " + od.getBrand() +
                                    "\nQuantity: " + od.getQuantity() + "\nCountry Code: " + od.getCountry() + "\nOrder Number: " + od.getImage() + "\n \n";

                            // to get different ids I created a string that gets the last 3 chards of each order number (getImage()) and converts it into int and set the int to the textveiws id
                           String getStrID = od.getImage().length() > 3 ? od.getImage().substring(od.getImage().length() - 3) : od.getImage();

                            // create a text view for each order
                                textView = new TextView(SellActivity.this);
                            // put the data in the text view
                                textView.setText(data);
                                // give it an id
                                textView.setId(Integer.parseInt(getStrID));
                                //place it nicely under one another
                                textView.setPadding(0, 50, 0, 0);

                            // if clicked any of the textviews, open the offer page
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(SellActivity.this, OfferActivity.class);
                                    startActivity(intent);
                                }
                            });

                            // add the text view to our layout
                            linearLayout.addView(textView);


                            if (temp.getNext().getNext() == null) maxOrder = ((Order)temp.getObject()).getImage();

                            //go to the next linked list or order
                            temp = temp.getPrev();

                            // just check and see if the ideas are correct
                            System.out.println("the idea is :" + textView.getId());
                            i++;
                        }


                    } else System.out.println("lop function returned sth else.");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("helllllllll yeaaaaaaaaaaaaaaaaaaaaaaaa");
            }
        });



    }

    // tryed to make a function for it but failed, feel free to try
}