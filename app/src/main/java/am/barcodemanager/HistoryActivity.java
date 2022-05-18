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
import androidx.recyclerview.widget.RecyclerView;

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
import am.barcodemanager.adapter.HistoryAdapter;
import am.barcodemanager.adapter.PalletAdpater;
import am.barcodemanager.adapter.rolldataAdapter;
import am.barcodemanager.model.History;
import am.barcodemanager.model.Pallet;
import am.barcodemanager.model.RollInfo;
import am.barcodemanager.network.NetworkStateChecker;
import am.barcodemanager.singleton.VolleySingleton;

public class HistoryActivity extends AppCompatActivity {
    HistoryAdapter historyAdapter;
    ListView history_listview;
    List<History> historyList = new ArrayList<>();
    History _history_model;
    String device,device_name;
    Toolbar toolbar;
    TextView tv_device_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        HttpsTrustManager.allowAllSSL();
        toolbar = findViewById(R.id.toolbar);
        tv_device_id = findViewById(R.id.tv_device_id);
//        install_apk = findViewById(R.id.install_apk);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        initViews(savedInstanceState);
    }

    private void initViews(Bundle savedInstanceState) {
        //initializing views and objects
        history_listview = findViewById(R.id.listview_history);
        if (getIntent().getStringExtra("device") != null) {
            device = getIntent().getStringExtra("device");
            GET(device);
        }if (getIntent().getStringExtra("device_name") != null) {
            device_name = getIntent().getStringExtra("device_name");
            tv_device_id.setText(device_name);
        }

    }


    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("art.artisticmilliners.com:8081", session);
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

    public void GET(String device_id) {
        ProgressDialog dialog = new ProgressDialog(HistoryActivity.this);
        dialog.setMessage("please wait...");
        dialog.show();
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://art.artisticmilliners.com:8081/ords/art/bscan/insp_history/?device_id=" + device_id, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response.toString());
                try {
                    JSONArray obj = response.getJSONArray("items");
                    Log.e("obj", ""+obj.length());
                   if (obj.length() == 0){
                       dialog.dismiss();
                       customdialog("No Data Found", "Error");
                   }else {
                       for (int i = 0; i < obj.length(); i++) {
                           JSONObject jsonObject = obj.getJSONObject(i);
                           String scan_date = jsonObject.getString("scan_date");
                           String pallotno = jsonObject.getString("pallotno");
                           String total_rolls = jsonObject.getString("total_rolls");
                           String total_mtrs = jsonObject.getString("total_mtrs");
                           String transfered = jsonObject.getString("transfered");
                        /*tv_fetch_article.setText(art_fancy_name);
                        tv_fetch_qty.setText(prodqty);*/
                           Log.e("pallotno", pallotno);
                           Log.e("total_rolls", total_rolls);
                           Log.e("scan_date", scan_date);
                           Log.e("total_mtrs", total_mtrs);
                           _history_model = new History(scan_date, pallotno, total_rolls, total_mtrs, transfered);
                           historyList.add(_history_model);
                           dialog.dismiss();


                           // check the other values like this so on..

                       }
                       historyAdapter = new HistoryAdapter(getApplicationContext(), R.layout.history_list_data_text, historyList);
                       history_listview.setAdapter(historyAdapter);
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
                dialog.dismiss();
                if (error.toString().contains("TimeoutError")) {
                    Errorcustomdialog("Check your internet connection", "Timeout");
                }else {
                    Errorcustomdialog("Check your internet connection", "Internet Connection");
                }
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        requestQueue.add(jsonObjectRequest);
    }


    public void Errorcustomdialog(String msg, String timeout) {
        final Dialog dialog = new Dialog(HistoryActivity.this);
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

    public void customdialog(String msg, String timeout) {
        final Dialog dialog = new Dialog(HistoryActivity.this);
        dialog.setContentView(R.layout.error_dialogbox);
        Button dialogButton = (Button) dialog.findViewById(R.id.buttonOk);
        TextView error_heading = (TextView) dialog.findViewById(R.id.error_heading);
        TextView dialogtext = (TextView) dialog.findViewById(R.id.text_error);
        dialogtext.setText(msg);
        error_heading.setText(timeout);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, PalletBarcodeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        dialog.show();
    }

}
