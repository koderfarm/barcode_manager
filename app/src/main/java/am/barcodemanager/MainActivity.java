package am.barcodemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.datalogic.device.ErrorManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText txtBarcodeValue;
    String intentData = "";
//    private SpaceNavigationView spaceNavigationView;
    Button btn_add_rolls;
    final int sdk = android.os.Build.VERSION.SDK_INT;
    String Vlue;
    private final String LOGTAG = getClass().getName();

    BarcodeManager decoder = null;
    ReadListener listener = null;
    Spinner spinner_shift;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        initViews(savedInstanceState);

    }

    private void initViews(Bundle savedInstanceState) {
        spinner_shift = findViewById(R.id.shiftspinner);
        btn_add_rolls = findViewById(R.id.btn_proceed);
        txtBarcodeValue = findViewById(R.id.edit_pallet_no);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);


        Vlue = txtBarcodeValue.getText().toString();
        ArrayAdapter<String> myadapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.itemselect));
        myadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_shift.setAdapter(myadapter);
        txtBarcodeValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() == 0) {
                    btn_add_rolls.setVisibility(View.GONE);
                } else {
                    btn_add_rolls.setVisibility(View.VISIBLE);
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
        btn_add_rolls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("pallet value", txtBarcodeValue.getText().toString().trim());
                Log.e("pallet value123", intentData);
                Log.e("pallet dfjsdj", spinner_shift.getSelectedItem().toString());
                if (spinner_shift.getSelectedItem().toString().contains("Select Shift")) {
                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.error_dialogbox);
                    Button dialogButton = (Button) dialog.findViewById(R.id.buttonOk);
                    TextView dialogtext = (TextView) dialog.findViewById(R.id.text_error);
                    dialogtext.setText("Select Shift");
//        dialogtext.setText(msg);
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
//                startActivity(new Intent(AddRollsActivity.this, MainActivity.class));
                        }
                    });
                    dialog.show();
                } else {
                    startActivity(new Intent(MainActivity.this, AddRollsActivity.class).putExtra("pallet", txtBarcodeValue.getText().toString().trim()).putExtra("shift", spinner_shift.getSelectedItem().toString()));
                }

            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_recents:
                        Toast.makeText(MainActivity.this, "History", Toast.LENGTH_SHORT).show();
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
        /*spaceNavigationView = (SpaceNavigationView) findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("", R.drawable.ic_baseline_home_24));
        spaceNavigationView.addSpaceItem(new SpaceItem("History", R.drawable.ic_baseline_history_24));
        spaceNavigationView.shouldShowFullBadgeText(false);
        spaceNavigationView.setBackgroundColor(Color.parseColor("#225662"));
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
//                Log.d("onCentreButtonClick ", "onCentreButtonClick");
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
//                Log.d("onItemClick ", "" + itemIndex + " " + itemName);
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
//                Log.d("onItemReselected ", "" + itemIndex + " " + itemName);
            }
        });

        spaceNavigationView.setSpaceOnLongClickListener(new SpaceOnLongClickListener() {
            @Override
            public void onCentreButtonLongClick() {
//                Toast.makeText(MainActivity.this, "onCentreButtonLongClick", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(int itemIndex, String itemName) {
//                Toast.makeText(MainActivity.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();
            }
        });
        spaceNavigationView.showIconOnly();*/
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        spaceNavigationView.onSaveInstanceState(outState);
//    }


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