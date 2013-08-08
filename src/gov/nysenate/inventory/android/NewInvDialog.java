package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.widget.CheckBox;

//...
@SuppressLint("ValidFragment")
public class NewInvDialog extends DialogFragment  {

 public static TextView tvKeywordsToBlock;
 public static EditText etNewItemComments;
 SenateActivity senateActivity;
 public static ListView commodityList = null;
 private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
 public String title = null;
 public String msg = null;
 GestureDetector gestureDectector = null;
 List<CommodityDialogListener> listeners = new ArrayList<CommodityDialogListener>();
 Commodity currentCommodity = null;
 CommodityListViewAdapter adapter = null;
 int position = -1;
 String nusenate = null;
 
 public NewInvDialog(SenateActivity senateActivity, String title, String msg) {
     this.senateActivity = senateActivity;
     this.title = title;
     this.msg = msg;
 }

 @Override
 public Dialog onCreateDialog(Bundle savedInstanceState) {
      
     LayoutInflater inflater = getActivity().getLayoutInflater();
     View dialogView = inflater.inflate(R.layout.dialog_newinvitem, null);
     tvKeywordsToBlock = (TextView) dialogView.findViewById(R.id.tvKeywordsToBlock);
     commodityList = (ListView) dialogView.findViewById(R.id.searchResults);
     etNewItemComments = (EditText)dialogView.findViewById(R.id.etNewItemComments);
     if (senateActivity.dialogKeywords!=null) {
         tvKeywordsToBlock.setText(senateActivity.dialogKeywords);
         senateActivity.getDialogDataFromServer();
         checkKeywordsFound();
         senateActivity.dialogKeywords = null;
     }
     if (senateActivity.dialogComments!=null) {
         etNewItemComments.setText(senateActivity.dialogComments);
         senateActivity.dialogComments = null;
     }
     adapter = (CommodityListViewAdapter)commodityList.getAdapter();
     //adapter.unselectRow();
     
     if (commodityList==null) {
         Log.i("commodityList", "*****commodityList IS NULL");
     }
     else {
         Log.i("commodityList", "commodityList IS NOT NULL");
     }

     // Use the Builder class for convenient dialog construction
     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
     builder.setView(dialogView)
            .setTitle(title)
            .setMessage(Html.fromHtml(msg))
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    currentCommodity.setDecomments(etNewItemComments.getText().toString());
                    for (CommodityDialogListener commodityDialogListener : listeners)
                        commodityDialogListener.commoditySelected(position, currentCommodity);                    
                    dismiss();
                }
            })            
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    cancelMsg();
                }
            });
     
     gestureDectector = new GestureDetector(senateActivity, new GestureListener());  
     commodityList.setOnTouchListener(new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDectector.onTouchEvent(event);
            return false;
        }

      });
   
     // Create the AlertDialog object and return it
     return builder.create();
 }
 
 public void setNusenate(String nusenate) {
     this.nusenate = nusenate;
 }
 
 public String getNusenate() {
     return this.nusenate;
 }
  
 public void addListener(CommodityDialogListener addListener) {
     listeners.add(addListener);
 } 
 
 public void checkKeywordsFound() {
     String keywords = tvKeywordsToBlock.getText().toString();
     String[] keywordList = keywords.split(","); 
     StringBuilder s = new StringBuilder();
     adapter = (CommodityListViewAdapter)commodityList.getAdapter();
     int currentWordCount = 0;
     for (int x=0;x<keywordList.length;x++) {
         currentWordCount = adapter.wordRowCount(keywordList[x]);
         
         if (currentWordCount>0) {
             s.append("<font color='#005500'><b>");
         }
         else {
             s.append("<font color='red'>");
         }
         
         s.append(keywordList[x]);
         
         if (currentWordCount>0) {
             s.append("</b></font>");
         }
         else {
             s.append("</font>");
         }
         
         if (x!=keywordList.length-1) {
             s.append(",");
         }

     }
     tvKeywordsToBlock.setText(Html.fromHtml(s.toString()));
     if (senateActivity.dialogSelectedRow>-1) {
         adapter.setRowSelected(senateActivity.dialogSelectedRow);
         currentCommodity = adapter.getCommodityAt(senateActivity.dialogSelectedRow);
         senateActivity.dialogSelectedRow = -1;
     }
 }
 
  private void doNotShowAgain() {
     // persist shared preference to prevent dialog from showing again.
     Log.d("MainActivity", "TODO: Persist shared preferences.");
 }
 
 public void cancelMsg() {  
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(senateActivity);

         String cancelTitle =  "Cancel";

         String  cancelMsg = "***WARNING: Senate Tag# will not be added if you cancel. Do you want to cancel?";

         // set title
         alertDialogBuilder.setTitle(cancelTitle);

         // set dialog message
         alertDialogBuilder.setMessage(Html.fromHtml(cancelMsg))
                 .setCancelable(false)
                 .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         // if this button is clicked, just close
                         // the dialog box and do nothing
                         dismiss();
                     }
                 })
                 .setNegativeButton("No", new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         // if this button is clicked, we need to reopen the original Dialog
                         senateActivity.dialogTitle = title;
                         senateActivity.dialogMsg = msg;
                         if (adapter==null) {
                             senateActivity.dialogSelectedRow = -1;
                         }
                         else {
                             senateActivity.dialogSelectedRow = adapter.rowSelected;
                         }
                         senateActivity.dialogComments = etNewItemComments.getText().toString();
                         senateActivity.dialogKeywords = tvKeywordsToBlock.getText().toString();
                         
                         senateActivity.reOpenNewInvDialog();
                     }
                 });

         // create alert dialog
         AlertDialog alertDialog = alertDialogBuilder.create();

         // show it
         alertDialog.show();
 }
  
  
 public class GestureListener extends GestureDetector.SimpleOnGestureListener {

     public boolean onDown(MotionEvent e) {
         return true;
     }

     public boolean onDoubleTap(MotionEvent e) {
         position = commodityList.pointToPosition((int)e.getX(), (int)e.getY());
         
         if(position<0) {
             return false;
         }
         
         adapter = (CommodityListViewAdapter)commodityList.getAdapter();
         
         currentCommodity = adapter.getCommodityAt(position);
         adapter.setRowSelected(position);
         
         //Log.d("Double_Tap", "Yes, Clicked on "+currencCommodity.getDecommodityf());
         // Notify everybody that may be interested.
         
         return true;
     }
 }  
 
}