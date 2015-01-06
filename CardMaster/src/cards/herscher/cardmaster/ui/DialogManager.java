package cards.herscher.cardmaster.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogManager
{
    private final Context context;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private String alertTitle;
    private String alertMessage;

    public DialogManager(Context context)
    {
        if (context == null)
        {
            throw new IllegalStateException();
        }

        this.context = context;
    }
    
    public void showProgress(String title, String message)
    {
        if (title == null || message == null)
        {
            throw new IllegalArgumentException();
        }
        
        closeProgress();
        
        progressDialog = ProgressDialog.show(context, title, message);
    }

    public void showAlert(String title, String message)
    {
        if (title == null || message == null)
        {
            throw new IllegalArgumentException();
        }

        // If an alert dialog is already open and its text is different than what should be shown
        // close it
        if (alertDialog != null)
        {
            if (!alertTitle.equals(title) || !alertMessage.equals(message))
            {
                alertDialog.dismiss();
            }
        }

        AlertDialog newDialog = new AlertDialog.Builder(context).create();
        newDialog.setTitle(title);
        newDialog.setMessage(message);
        newDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        closeAlert();
                    }
                });

        alertDialog = newDialog;
        alertDialog.show();
    }
    
    public void closeAlert()
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
    
    public void closeProgress()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
