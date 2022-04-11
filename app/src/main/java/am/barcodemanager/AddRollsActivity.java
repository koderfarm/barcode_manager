package am.barcodemanager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.datalogic.device.ErrorManager;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import am.barcodemanager.Database.DatabaseHelper;
import am.barcodemanager.adapter.rolldataAdapter;
import am.barcodemanager.model.RollInfo;
import am.barcodemanager.network.NetworkStateChecker;
import am.barcodemanager.singleton.VolleySingleton;

public class AddRollsActivity extends AppCompatActivity implements View.OnClickListener {
    String PALLET, shift, weight;
    TextView tv_show_shift, tv_show_pallet;
    Toolbar toolbar;
    EditText txtBarcodeValue, edit_weight;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    Button btn_add_rolls;
    //database helper object

    //View objects
    private ListView rollNumberList;

    //List to store all the names
    private List<RollInfo> names;

    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;
    //adapterobject for list view
    private rolldataAdapter rollAdapter;
    public static final String URL_SAVE_NAME = "";
    Button btn_save;
    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "am.barcodemanager.datasaved";
    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;
    private final String LOGTAG = getClass().getName();
    BarcodeManager decoder = null;
    ReadListener listener = null;
    String url;
    TextView tv_fetch_article, tv_fetch_qty;
    RelativeLayout rl_fetch_data;
    ProgressBar progressBar;
    private DatabaseHelper db;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rolls);
        HttpsTrustManager.allowAllSSL();
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        initViews(savedInstanceState);
    }

    private void initViews(Bundle savedInstanceState) {
        //initializing views and objects
        names = new ArrayList<>();
        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        rollNumberList = (ListView) findViewById(R.id.pending_listview);
        txtBarcodeValue = findViewById(R.id.edit_pallet_no);
        edit_weight = findViewById(R.id.edit_weight);
        rl_fetch_data = findViewById(R.id.rl_fetch_data);
        tv_show_pallet = findViewById(R.id.tv_show_pallet);
        tv_show_shift = findViewById(R.id.tv_show_shift);
        tv_fetch_article = findViewById(R.id.tv_fetch_article);
        tv_fetch_qty = findViewById(R.id.tv_fetch_qty);
        progressBar = findViewById(R.id.progressBar1);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_pallet);

        if (getIntent().getStringExtra("pallet") != null) {
            PALLET = getIntent().getStringExtra("pallet");
            tv_show_pallet.setText("Pallet no. " + PALLET);
        }
        if (getIntent().getStringExtra("shift") != null) {
            shift = getIntent().getStringExtra("shift");
            tv_show_shift.setText("Shift " + shift);
        }
        txtBarcodeValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() == 0) {
                    edit_weight.setVisibility(View.GONE);
                    btn_save.setVisibility(View.GONE);
                } else {
                    edit_weight.setVisibility(View.VISIBLE);
                    btn_save.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_recents:
                        Intent intent = new Intent(AddRollsActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    /*case R.id.action_favorites:
                        Toast.makeText(MainActivity.this, "Favorites", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_nearby:
                        Toast.makeText(MainActivity.this, "Nearby", Toast.LENGTH_SHORT).show();
                        break;*/

                }
                return true;
            }
        });

    }


    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("artlive.artisticmilliners.com:8081", session);
            }
        };
    }

    private TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0) {
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkClientTrusted", e.toString());
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0) {
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkServerTrusted", e.toString());
                        }
                    }
                }
        };
    }

    private SSLSocketFactory getSSLSocketFactory()
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = getResources().openRawResource(R.raw.artisticmilliners); // this cert file stored in \app\src\main\res\raw folder path

        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
    }

    public void GET(String barcode) {
        progressBar.setVisibility(View.VISIBLE);
        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory());
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return httpsURLConnection;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this, hurlStack);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp/?bcode=" + barcode, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response.toString());
                progressBar.setVisibility(View.GONE);
                rl_fetch_data.setVisibility(View.VISIBLE);

                try {
                    JSONArray obj = response.getJSONArray("items");
                    Log.e("obj", obj.toString());
                    for (int i = 0; i < obj.length(); i++) {
                        JSONObject jsonObject = obj.getJSONObject(i);
                        String art_fancy_name = jsonObject.getString("art_fancy_name");
                        String prodqty = jsonObject.getString("prodqty");
                        tv_fetch_article.setText(art_fancy_name);
                        tv_fetch_qty.setText(prodqty);
                        Log.e("FANCY ART NAME", art_fancy_name);

                        // check the other values like this so on..

                    }
                    //String message = response.getString("message");
                    //Toast.makeText(getApplicationContext(), ""+status+message, Toast.LENGTH_SHORT).show();


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("E", String.valueOf(e.toString()));

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("ERROR", String.valueOf(error.toString()));
                if (error.toString().contains("TimeoutError")) {
                    progressBar.setVisibility(View.GONE);
                    txtBarcodeValue.setText("");
                    edit_weight.setText("");
                    Errorcustomdialog("Check your internet connection", "Timeout");
                }
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void postDataUsingVolley(String rollnum, String pno, String usid, String weight, String shif) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving ...");
        progressDialog.show();
        url = "https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp/?bcode=" + rollnum + "&pno=" + pno + "&usid=" + usid + "&aweight=" + weight + "&shift_code=" + shif;
        Log.e("URL SAVE", url);
        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory());
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return httpsURLConnection;
            }
        };
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(AddRollsActivity.this, hurlStack);

        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        StringRequest request = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Toast.makeText(AddRollsActivity.this, "Pallet Record Created" + response, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                customdialog();

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                Log.e("Error", error.toString());
                progressDialog.dismiss();
                if (error.toString().contains("TimeoutError")) {
                    progressBar.setVisibility(View.GONE);
                    txtBarcodeValue.setText("");
                    edit_weight.setText("");
                    Errorcustomdialog("Check your internet connection", "Timeout");
                }//                Toast.makeText(AddRollsActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // below line we are creating a map for
                // storing our values in key and value pair.
                Map<String, String> params = new HashMap<String, String>();

                // on below line we are passing our key
                // and value pair to our parameters.
                params.put("bcode", rollnum);
                params.put("pno", pno);
                params.put("usid", usid);
                params.put("aweight", weight);
                params.put("shift_code", shif);

                // at last we are
                // returning our params.
                return params;
            }
        };
        //10000 is the time in milliseconds adn is equal to 10 sec
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // below line is to make
        // a json object request.
        queue.add(request);
    }

    public void Errorcustomdialog(String msg, String timeout) {
        final Dialog dialog = new Dialog(AddRollsActivity.this);
        dialog.setContentView(R.layout.error_dialogbox);
        Button dialogButton = (Button) dialog.findViewById(R.id.buttonOk);
        TextView error_heading = (TextView) dialog.findViewById(R.id.error_heading);
        TextView dialogtext = (TextView) dialog.findViewById(R.id.text_error);
        dialogtext.setText(msg);
        error_heading.setText(timeout);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                startActivity(new Intent(AddRollsActivity.this, MainActivity.class));
            }
        });
        dialog.show();
    }

    public void customdialog() {
        final Dialog dialog = new Dialog(AddRollsActivity.this);
        dialog.setContentView(R.layout.customdialogbox);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                progressBar.setVisibility(View.GONE);
                txtBarcodeValue.setText("");
                edit_weight.setText("");
               /* Intent intent = new Intent(AddRollsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        Log.e("Weight", edit_weight.getText().toString().trim());
        postDataUsingVolley(txtBarcodeValue.getText().toString().trim(), PALLET, "7426", edit_weight.getText().toString().trim(), shift);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(LOGTAG, "onResume");

        // If the decoder instance is null, create it.
        if (decoder == null) {
            // Remember an onPause call will set it to null.
            Log.i("Decodeer", "onResume");
            decoder = new BarcodeManager();
        }

        // From here on, we want to be notified with exceptions in case of errors.
        ErrorManager.enableExceptions(true);

        try {

            // Create an anonymous class.
            listener = new ReadListener() {


                // Implement the callback method.
                @Override
                public void onRead(DecodeResult decodeResult) {
                    Log.i("adsaksdj", decodeResult.getText());
                    // Change the displayed text to the current received result.
                    txtBarcodeValue.setText(decodeResult.getText());
                    GET(decodeResult.getText());
                }

            };

            // Remember to add it, as a listener.
            decoder.addReadListener(listener);

        } catch (DecodeException e) {
            Log.e(LOGTAG, "Error while trying to bind a listener to BarcodeManager", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(LOGTAG, "onPause");

        // If we have an instance of BarcodeManager.
        if (decoder != null) {
            try {
                // Unregister our listener from it and free resources.
                decoder.removeReadListener(listener);

                // Let the garbage collector take care of our reference.
                decoder = null;
            } catch (Exception e) {
                Log.e(LOGTAG, "Error while trying to remove a listener from BarcodeManager", e);
            }
        }
    }


}
