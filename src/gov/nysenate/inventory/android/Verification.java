package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Verification extends SenateActivity
{
    public final static String loc_code_intent = "gov.nysenate.inventory.android.loc_code_str";
    public final static String timeout_intent = "gov.nysenate.inventory.android.timeoutFrom";
    public EditText loc_code;
    public TextView loc_details;
    public String res = null;
    public String status = null;
    public String loc_code_str = null;
    static ClearableAutoCompleteTextView autoCompleteTextView1;
    public ArrayList<String> locCodeList = new ArrayList<String>();
    static Button btnVerify1Cont;
    static Button btnVerify1Cancel;
    // TextView tvLocCd;
    TextView tvDescript;
    TextView tvCount;
    TextView tvOffice;
    Activity currentActivity;
    String timeoutFrom = "verification";

    public final int LOCCODELIST_TIMEOUT = 101,
            LOCATIONDETAILS_TIMEOUT = 102;

    String URL = "";

    public static ProgressBar progBarVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        registerBaseActivityReceiver();
        currentActivity = this;

        loc_code = (EditText) findViewById(R.id.preferencePWD);
        // code for the autocomplete arraylist of location

        // Button Setup
        btnVerify1Cont = (Button) findViewById(R.id.btnVerify1Cont);
        btnVerify1Cont.getBackground().setAlpha(255);
        btnVerify1Cancel = (Button) findViewById(R.id.btnVerify1Cancel);
        btnVerify1Cancel.getBackground().setAlpha(255);

        // Data TextView Setup
        // tvLocCd= (TextView) findViewById(R.id.tvLocCd);
        tvDescript = (TextView) findViewById(R.id.tvDescript);
        tvCount = (TextView) findViewById(R.id.tvCount);
        tvOffice = (TextView) findViewById(R.id.tvOffice);

        // ProgressBar Setup
        progBarVerify = (ProgressBar) this.findViewById(R.id.progBarVerify);

        getLocCodeList();
        // code for textwatcher

        loc_code = (EditText) findViewById(R.id.preferencePWD);
        // loc_code.addTextChangedListener(filterTextWatcher);
        autoCompleteTextView1.addTextChangedListener(filterTextWatcher);
        autoCompleteTextView1
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        int duration = Toast.LENGTH_SHORT;
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                autoCompleteTextView1.getWindowToken(), 0);
                        autoCompleteTextView1.setSelection(0);
                    }
                });

        loc_details = (TextView) findViewById(R.id.textView2);

    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("NO SERVER RESPONSE");

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();

                        CharSequence text = "No action taken due to NO SERVER RESPONSE";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private TextWatcher filterTextWatcher = new TextWatcher()
    {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (autoCompleteTextView1.getText().toString().length() >= 3) {
                getLocationDetails();
            }
        }
    };

    public void locationDetailTimeout() {
        System.out.println("TIME OUT SCREEN");
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, LOCATIONDETAILS_TIMEOUT);
    }

    public void locCodeListTimeout() {
        System.out.println("TIME OUT SCREEN");
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        System.out.println("TIME OUT SCREEN INTENT");
        startActivityForResult(intentTimeout, LOCCODELIST_TIMEOUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case LOCCODELIST_TIMEOUT:
            if (resultCode == RESULT_OK) {
                getLocCodeList();
                break;
            }
        case LOCATIONDETAILS_TIMEOUT:
            if (resultCode == RESULT_OK) {
                getLocationDetails();
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_verification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            Toast toast = Toast.makeText(getApplicationContext(), "Going Back",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            NavUtils.navigateUpFromSameTask(this);

            overridePendingTransition(R.anim.in_left, R.anim.out_right);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void getLocationDetails() {
        // loc_details.setText(loc_code.getText().toString());

        String barcodeNumberDetails[] = autoCompleteTextView1.getText()
                .toString().trim().split("-");
        String barcode_num = barcodeNumberDetails[0];// this will be
                                                     // passed to the
                                                     // server
        loc_code_str = barcodeNumberDetails[0];// this will be passed to
                                               // next activity with
                                               // intent

        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocationDetails?barcode_num="
                            + barcode_num);
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    System.out.println("FOUND:"
                            + res.indexOf("Session timed out")
                            + " Verification RES1:" + res);
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        locationDetailTimeout();
                        return;
                    }

                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                try {
                    JSONObject object = (JSONObject) new JSONTokener(
                            res).nextValue();
                    // tvLocCd.setText( object.getString("cdlocat"));
                    tvOffice.setText(object.getString("cdrespctrhd"));
                    tvDescript.setText(object.getString("adstreet1")
                            .replaceAll("&#34;", "\"")
                            + " ,"
                            + object.getString("adcity").replaceAll(
                                    "&#34;", "\"")
                            + ", "
                            + object.getString("adstate").replaceAll(
                                    "&#34;", "\"")
                            + " "
                            + object.getString("adzipcode").replaceAll(
                                    "&#34;", "\""));
                    tvCount.setText(object.getString("nucount"));

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    // tvLocCd.setText( "!!ERROR: "+e.getMessage());
                    tvOffice.setText("!!ERROR: " + e.getMessage());
                    tvDescript.setText("Please contact STS/BAC.");
                    tvCount.setText("N/A");

                    e.printStackTrace();
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            status = "yes1";
        } else {
            // display error
            status = "no";
        }
        System.out.println("RESPONSE FROM URL:" + res);

        // loc_details.setText(res);

        // loc_details.append("\n"+loc_code.getText().toString());
        // autoCompleteTextView1.setText(barcode_num);
    }
    
    public void getLocCodeList() {
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";
            // Get the URL from the properties

            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocCodeList");
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        locCodeListTimeout();
                        return;
                    }

                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                // code for JSON

                String jsonString = resr1.get().trim().toString();
                JSONArray jsonArray = new JSONArray(jsonString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    locCodeList.add(jsonArray.getString(i).toString());
                }
                Collections.sort(locCodeList);
                // System.out.println("**********LOCATION CODES COUNT:"
                // + locCodeList.size());
                //
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        locCodeList);

                autoCompleteTextView1 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
                autoCompleteTextView1.setThreshold(1);
                autoCompleteTextView1.setAdapter(adapter);

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            status = "yes1";
        } else {
            // display error
            status = "no";
        }
        
    }

    public void continueButton(View view) {
        String currentLocation = Verification.autoCompleteTextView1.getText()
                .toString();
        int duration = Toast.LENGTH_SHORT;
        if (currentLocation.trim().length() == 0) {
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "!!ERROR: You must first pick a location.", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        } else if (locCodeList.indexOf(currentLocation) == -1) {
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "!!ERROR: Location Code \"" + currentLocation
                            + "\" is invalid.", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        } else {
            progBarVerify.setVisibility(View.VISIBLE);
            btnVerify1Cont.getBackground().setAlpha(45);
            Intent intent = new Intent(this, ListtestActivity.class);
            intent.putExtra(loc_code_intent, loc_code_str);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }

    }

    public void cancelButton(View view) {

        btnVerify1Cancel.getBackground().setAlpha(45);
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_left, R.anim.out_right);

    }

    /*
     * @Override public void onBackPressed() { super.onBackPressed();
     * overridePendingTransition(R.anim.in_left, R.anim.out_right); }
     */

}
