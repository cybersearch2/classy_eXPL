package au.com.cybersearch2.telegen;

import javax.inject.Inject;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import au.com.cybersearch2.classytask.AsyncBackgroundTask;


public class DisplayDetailsDialog extends DialogFragment 
{
    public static final String KEY_TITLE = "Telegen.Title";
    public static final String KEY_CONTEXT = "Telegen.Context";
    public static final String KEY_CONTENT = "Telegen.Content";
    public static final CharSequence DIALOG_TITLE = "Troubleshooting";
    
    protected Dialog dialog;
    @Inject
    Context context;
    @Inject
    TelegenLogic telegenLogic;
  
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    
         final View view = inflater.inflate(R.layout.display_details, container, false); 

         String title = getArguments().getString(KEY_TITLE);
         String detailContext = getArguments().getString(KEY_CONTEXT);
         String content = getArguments().getString(KEY_CONTENT);
         if ((title != null) && (title.length() > 0))
         {
             TextView tv1 = (TextView)view.findViewById(R.id.detail_title);
             tv1.setText(title);
         }
         if ((detailContext != null) && (detailContext.length() > 0))
         {
             TextView tv2 = (TextView)view.findViewById(R.id.detail_context);
             tv2.setText(detailContext);
        }
        if ((content != null) && (content.length() > 0))
        {
             TextView tv3 = (TextView)view.findViewById(R.id.detail_content);
             tv3.setText(content);
        }
        // Watch for button clicks.
        final Button button = (Button)view.findViewById(R.id.button_next);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final AsyncBackgroundTask responder =  new AsyncBackgroundTask(context)
                {
                    String check;
                    
                    @Override
                    public Boolean loadInBackground()
                    {
                        check = telegenLogic.getNextCheck();
                        return Boolean.TRUE;
                    }

                    @Override
                    public void onLoadComplete(Loader<Boolean> loader, Boolean success)
                    {
                        TextView tv3 = (TextView)view.findViewById(R.id.detail_content);
                        tv3.setText(check);
                        if (telegenLogic.getCurrentCheck().equals(TelegenLogic.CALL_SUPPORT))
                        {
                            button.setText("OK");
                            button.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v)
                                {
                                    dialog.dismiss();
                                }});
                        }
                    }
                };
                responder.onStartLoading();
            }
         });
         return view;
     }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
        dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(DIALOG_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        return dialog;
    }
    
    public Dialog getDialog()
    {
        return dialog;
    }
}