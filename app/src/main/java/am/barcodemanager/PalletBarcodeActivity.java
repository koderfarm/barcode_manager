package am.barcodemanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.renderscript.Long4;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import am.barcodemanager.Database.DatabaseHelper;
import am.barcodemanager.adapter.PalletAdpater;
import am.barcodemanager.adapter.rolldataAdapter;
import am.barcodemanager.model.Pallet;
import am.barcodemanager.model.RollInfo;
import am.barcodemanager.network.NetworkStateChecker;

public class PalletBarcodeActivity extends AppCompatActivity implements View.OnClickListener {
    EditText pallet_et, roll_et;
    Button btn_save;
    ImageView btn_pallet;
    ListView rollNumberList;
    //List to store all the names
    List<RollInfo> names;
    List<Pallet> palletList;
    Toolbar toolbar;
    RelativeLayout rl_fetch_data;
    TextView tv_fetch_article, tv_fetch_qty, tv_total_rolls, tv_total_meters, bcode_check;
    private final String LOGTAG = getClass().getName();
    BarcodeManager decoder = null;
    ReadListener listener = null;
    //    private rolldataAdapter rollAdapter;
    private PalletAdpater palletAdpater;
    private DatabaseHelper db;
    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "am.barcodemanager.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;
    ImageView install_apk;
    RelativeLayout rl_count;
    TextView tv_device_id;
    BottomNavigationView bottomNavigationView;
    String androidID;
    ProgressBar progressBar;
    TextView tv_pallet_no;
    String pallet_number;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pallet_barcode);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.appbarColor));
        }
        HttpsTrustManager.allowAllSSL();
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        toolbar = findViewById(R.id.toolbar);
        tv_device_id = findViewById(R.id.tv_device_id);
//        install_apk = findViewById(R.id.install_apk);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        initViews(savedInstanceState);
       /* PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            Log.e("package aname ", "PackageName = " + info.packageName + "\nVersionCode = "
                    + info.versionCode + "\nVersionName = "
                    + info.versionName + "\nPermissions = " + info.permissions);

            Toast.makeText(this,
                    "PackageName = " + info.packageName + "\nVersionCode = "
                            + info.versionCode + "\nVersionName = "
                            + info.versionName + "\nPermissions = " + info.permissions, Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }*/
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        GETDveiceID(androidID);


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

    private void initViews(Bundle savedInstanceState) {
        //initializing views and objects
        //initializing views and objects
        db = new DatabaseHelper(this);
        names = new ArrayList<>();
        palletList = new ArrayList<>();
        rl_count = findViewById(R.id.rl_count);
        progressBar = findViewById(R.id.progressBar1);
//        rl_row_history = findViewById(R.id.rl_row_history);
        btn_save = findViewById(R.id.btn_save);
        btn_pallet = findViewById(R.id.btn_pallet);
        btn_save.setOnClickListener(this);
        btn_pallet.setOnClickListener(this);
        rollNumberList = findViewById(R.id.data_listview);
        pallet_et = findViewById(R.id.edit_pallet_no);
        tv_pallet_no = findViewById(R.id.tv_pallet_no);
        roll_et = findViewById(R.id.edit_roll_no);
        rl_fetch_data = findViewById(R.id.rl_fetch_data);
        tv_fetch_article = findViewById(R.id.tv_fetch_article);
        tv_fetch_qty = findViewById(R.id.tv_fetch_qty);
        tv_total_meters = findViewById(R.id.tv_total_meters);
        tv_total_rolls = findViewById(R.id.tv_total_rolls);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_pallet);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_recents:
                        db.deleteCart(getApplicationContext());
                        Intent intent = new Intent(PalletBarcodeActivity.this, PalletBarcodeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    case R.id.action_history:
                        Intent history_intent = new Intent(PalletBarcodeActivity.this, HistoryActivity.class);
                        history_intent.putExtra("device_name", device_name);
                        history_intent.putExtra("device", androidID);
                        startActivity(history_intent);
                        break;

                }
                return true;
            }
        });

        pallet_et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    roll_et.setVisibility(View.GONE);
                    btn_save.setVisibility(View.GONE);
                    tv_total_meters.setText("0");
                    tv_total_rolls.setText("0");
                    palletList.clear();
                } else {
                    palletList.clear();
                    tv_total_meters.setText("0");
                    tv_total_rolls.setText("0");
                    pallet_number = s.toString().trim();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub7
                int et_pallet_count = pallet_et.getText().length();
                if (et_pallet_count == 4) {
                    rl_count.setVisibility(View.VISIBLE);
                    roll_et.setVisibility(View.VISIBLE);
                    roll_et.clearFocus();
                    roll_et.requestFocus();
                    GET(pallet_et.getText().toString().trim());
                    GETCount(pallet_et.getText().toString().trim());
                }



            }
        });

     /*   roll_et.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                pallet_et.setVisibility(View.GONE);
                tv_pallet_no.setVisibility(View.VISIBLE);
                tv_pallet_no.setText(pallet_number);
            }
        });*/

        roll_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    pallet_et.setVisibility(View.GONE);
                    tv_pallet_no.setVisibility(View.VISIBLE);
                    tv_pallet_no.setText(pallet_number);
                }
            }
        });
        roll_et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (s.toString().trim().length() >= 1) {
                    btn_save.setVisibility(View.VISIBLE);
                } else {
                    btn_save.setVisibility(View.GONE);
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

        Log.e("LENGHT OF LIST", String.valueOf(names.size()));
//        loadNames();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the names again
//                loadNames();
            }
        };

        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
    }

    private void refreshList() {
        palletAdpater.notifyDataSetChanged();
    }

    String url;

    private void postDataUsingVolley(String rollnum, String pno, String usid, String device_id) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving ...");
        progressDialog.show();
        url = "https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp/?bcode=" + rollnum + "&pno=" + pno + "&usid=" + usid + "&device_id=" + device_id;
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
        RequestQueue queue = Volley.newRequestQueue(PalletBarcodeActivity.this, hurlStack);

        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        StringRequest request = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Toast.makeText(PalletBarcodeActivity.this, "Pallet Record Created" + response, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                roll_et.setText("");
                palletList.clear();
                GET(pallet_et.getText().toString().trim());
                GETCount(pallet_et.getText().toString().trim());
                //customdialog();

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                Log.e("Error", error.toString());
                progressDialog.dismiss();
                if (error.toString().contains("TimeoutError")) {
                    roll_et.setText("");
                    pallet_et.setText("");
                    Errorcustomdialog("Check your internet connection", "Timeout");
                } else {
                    roll_et.setText("");
                    pallet_et.setText("");
                    Errorcustomdialog("Check your internet connection", "Internet Connection");
                }
                //                Toast.makeText(AddRollsActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
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
                params.put("device_id", device_id);

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

    public void customdialog() {
        final Dialog dialog = new Dialog(PalletBarcodeActivity.this);
        dialog.setContentView(R.layout.customdialogbox);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void delete(String roll_no1, String pallet, String id) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to delete this item?");

        // add the buttons
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteDataUsingVolley(roll_no1, pallet, id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
    }

    public void Deletedialog() {
        final Dialog dialog = new Dialog(PalletBarcodeActivity.this);
        dialog.setContentView(R.layout.delete_dialog);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                palletList.clear();
                GET(pallet_et.getText().toString().trim());
                GETCount(pallet_et.getText().toString().trim());

            }
        });
        dialog.show();
    }

    String roll_no;

    public void GET(String pallet_number) {
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp/?pno=" + pallet_number, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response.toString());
                Pallet pallet_model;
                try {
                    progressBar.setVisibility(View.GONE);
                    JSONArray obj = response.getJSONArray("items");
                    Log.e("obj", obj.toString());
                    for (int i = 0; i < obj.length(); i++) {
                        JSONObject jsonObject = obj.getJSONObject(i);
                        String lotno = jsonObject.getString("lotno");
                        String rollno = jsonObject.getString("rollno");
                        String prodqty = jsonObject.getString("prodqty");
                        String article_name = jsonObject.getString("article_name");
                        /*tv_fetch_article.setText(art_fancy_name);
                        tv_fetch_qty.setText(prodqty);*/
                        Log.e("lotno", lotno);
                        Log.e("rollno", rollno);
                        Log.e("prodqty", prodqty);
                        Log.e("article_name", article_name);
                        pallet_model = new Pallet(lotno, article_name, rollno, prodqty);
                        palletList.add(pallet_model);


                        // check the other values like this so on..

                    }
                    palletAdpater = new PalletAdpater(getApplicationContext(), R.layout.pallet_data_list_text, palletList);
                    rollNumberList.setAdapter(palletAdpater);
                    rollNumberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
                            Pallet pallet_model1 = (Pallet) myAdapter.getItemAtPosition(position);
                            Log.e("Hello", pallet_model1.getArticle_no());
                            roll_no = pallet_model1.getRoll_no();
                            delete(roll_no, pallet_et.getText().toString().trim(), androidID);

                        }
                    });
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
                progressBar.setVisibility(View.GONE);
                Log.e("ERROR", String.valueOf(error.toString()));
                if (error.toString().contains("TimeoutError")) {
                    roll_et.setText("");
                    Errorcustomdialog("Check your internet connection", "Timeout");
                }
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    String device_name;

    public void GETCount(String pallet_number) {
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp_count/?pno=" + pallet_number, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response.toString());
                rl_count.setVisibility(View.VISIBLE);
                try {
                    JSONArray obj = response.getJSONArray("items");
                    Log.e("obj", obj.toString());
                    for (int i = 0; i < obj.length(); i++) {
                        JSONObject jsonObject = obj.getJSONObject(i);
                        int tot_rolls = jsonObject.getInt("tot_rolls");
                        int tot_mtr = jsonObject.getInt("tot_mtr");
                        tv_total_rolls.setText(String.valueOf(tot_rolls));
                        if (tot_mtr >= 1000) {
                            tv_total_meters.setText(String.valueOf(tot_mtr));
                            tv_total_meters.setTextColor(getResources().getColor(R.color.colorRed));
                            tv_total_meters.setTypeface(tv_total_meters.getTypeface(), Typeface.BOLD);
                            tv_total_meters.setTextSize(20);

                        } else if (String.valueOf(tot_mtr) == "null") {
                            tv_total_meters.setText("0");
                            tv_total_meters.setTextSize(15);
                        } else {
                            tv_total_meters.setText(String.valueOf(tot_mtr));
                            tv_total_meters.setTextColor(getResources().getColor(R.color.black));
                            tv_total_meters.setTextSize(15);
                        }


                        // check the other values like this so on..

                    }
                    //String message = response.getString("message");
                    //Toast.makeText(getApplicationContext(), ""+status+message, Toast.LENGTH_SHORT).show();


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Eaksdasldk", String.valueOf(e.toString()));

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("ERROR", String.valueOf(error.toString()));
                if (error.toString().contains("TimeoutError")) {
                    roll_et.setText("");
                    Errorcustomdialog("Check your internet connection", "Timeout");
                }
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public void GETBARCODEvalidation(String barcode) {
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp_validate/?bcode=" + barcode, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response.toString());
                rl_count.setVisibility(View.VISIBLE);
                try {
                    JSONArray obj = response.getJSONArray("items");
                    Log.e("obj", obj.toString());
                    for (int i = 0; i < obj.length(); i++) {
                        JSONObject jsonObject = obj.getJSONObject(i);
                        int barcode_text = jsonObject.getInt("bcode_check");
                        if (barcode_text >= 1) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PalletBarcodeActivity.this);
                            builder.setTitle("Scanned");
                            builder.setMessage("Barcode already scanned");

                            // add the buttons
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    roll_et.setText("");
                                }
                            });

                            // create and show the alert dialog
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                        } else {
                            postDataUsingVolley(roll_et.getText().toString().trim(), pallet_et.getText().toString().trim(), "", androidID);
                        }


                        // check the other values like this so on..

                    }
                    //String message = response.getString("message");
                    //Toast.makeText(getApplicationContext(), ""+status+message, Toast.LENGTH_SHORT).show();


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Eaksdasldk", String.valueOf(e.toString()));

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("ERROR", String.valueOf(error.toString()));
                if (error.toString().contains("TimeoutError")) {
                    roll_et.setText("");
                    Errorcustomdialog("Check your internet connection", "Timeout");
                }
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public void GETDveiceID(String DEV_ID) {
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp_device/?device_id=" + DEV_ID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response.toString());
                try {
                    device_name = response.getString("div_name");
//                    JSONObject obj = response.getJSONArray("div_name");
                    Log.e("obj", device_name);
                    tv_device_id.setText(device_name);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Eaksdasldk", String.valueOf(e.toString()));

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("ERROR", String.valueOf(error.toString()));
                if (error.toString().contains("TimeoutError")) {
                    Errorcustomdialog("Check your internet connection", "Timeout");
                }
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
/*
   public void GET(String barcode) {
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
                    roll_et.setText("");
                    Errorcustomdialog("Check your internet connection", "Timeout");
                }
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
*/

    public void Errorcustomdialog(String msg, String timeout) {
        final Dialog dialog = new Dialog(PalletBarcodeActivity.this);
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
            }
        });
        dialog.show();
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
                    Log.i("Decode Result", decodeResult.getText());
                    // Change the displayed text to the current received result.
                    roll_et.setText(decodeResult.getText());
//                    GET(decodeResult.getText());
                    postDataUsingVolley(decodeResult.getText(), pallet_et.getText().toString().trim(), "", androidID);
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_save:
                GETBARCODEvalidation(roll_et.getText().toString().trim());
                break;

        }
        Log.e("Weight", roll_et.getText().toString().trim());
//        postDataUsingVolley(roll_et.getText().toString().trim(), pallet_et.getText().toString().trim(), "7426", edit_weight.getText().toString().trim(), shift);
    }


    /**
     * Verifies at Conductor APK path if package version if newer
     *
     * @return True if package found is newer, false otherwise
     */
    /*public static boolean checkIsNewVersion(String conductorApkPath) {

        boolean newVersionExists = false;

        // Decompress found APK's Manifest XML
        // Source: https://stackoverflow.com/questions/2097813/how-to-parse-the-androidmanifest-xml-file-inside-an-apk-package/4761689#4761689
        try {

            if ((new File(conductorApkPath).exists())) {

                JarFile jf = new JarFile(conductorApkPath);
                InputStream is = jf.getInputStream(jf.getEntry("AndroidManifest.xml"));
                byte[] xml = new byte[is.available()];
                int br = is.read(xml);

                //Tree tr = TrunkFactory.newTree();
                String xmlResult = SystemPackageTools.decompressXML(xml);
                //prt("XML\n"+tr.list());

                if (!xmlResult.isEmpty()) {

                    InputStream in = new ByteArrayInputStream(xmlResult.getBytes());

                    // Source: http://developer.android.com/training/basics/network-ops/xml.html
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

                    parser.setInput(in, null);
                    parser.nextTag();

                    String name = parser.getName();
                    if (name.equalsIgnoreCase("Manifest")) {

                        String pakVersion = parser.getAttributeValue(null, "versionName");
                        //NOTE: This is specific to my project. Replace with whatever is relevant on your side to fetch your project's version
                        String curVersion = SharedData.getPlayerVersion();

                        int isNewer = SystemPackageTools.compareVersions(pakVersion, curVersion);

                        newVersionExists = (isNewer == 1);
                    }

                }
            }

        } catch (Exception ex) {
            android.util.Log.e("TAG", "getIntents, ex: "+ex);
            ex.printStackTrace();
        }

        return newVersionExists;
    }*/
    private void DeleteDataUsingVolley(String rollnum, String pno, String device_id) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving ...");
        progressDialog.show();
        //https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp/?rollno=9&pno=123&device_id=
        url = "https://artlive.artisticmilliners.com:8081/ords/art/bscan/insp/?rollno=" + rollnum + "&pno=" + pno + "&device_id=" + device_id;
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
        RequestQueue queue = Volley.newRequestQueue(PalletBarcodeActivity.this, hurlStack);

        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        StringRequest request = new StringRequest(Request.Method.DELETE, url, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Deletedialog();

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                Log.e("Error", error.toString());
                progressDialog.dismiss();
                if (error.toString().contains("TimeoutError")) {
                    roll_et.setText("");
                    pallet_et.setText("");
                    Errorcustomdialog("Check your internet connection", "Timeout");
                } else {
                    roll_et.setText("");
                    pallet_et.setText("");
                    Errorcustomdialog("Check your internet connection", "Internet Connection");
                }
                //                Toast.makeText(AddRollsActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
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
                params.put("device_id", device_id);

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
}
