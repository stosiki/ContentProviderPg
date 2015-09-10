package study.stosiki.com.contentproviderpg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;


/**
 * Created by User on 08/08/2015.
 *
 * Dialog to enter string property of an event
 */

public class EventStringPropertyDialogFragment extends DialogFragment {
    private static final int DEFAULT_TYPE_POSITION = 0;

    public interface DialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    private View view;
    private DialogListener listener;

    private EditText dataEntry;

    public EventStringPropertyDialogFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (DialogListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogNegativeClick(EventStringPropertyDialogFragment.this);
            }
        });
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.string_property_dialog, null);
        dialogBuilder.setView(view);

        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog dialog = (AlertDialog)getDialog();
        if(dialog != null) {
            Button positiveButton = (Button)dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(verifyInput()) {
                        listener.onDialogPositiveClick(EventStringPropertyDialogFragment.this);
                        dialog.dismiss();
                    } else {
                        dataEntry.setHintTextColor(
                                getResources().getColor(android.R.color.holo_blue_bright));
                    }
                }
            });
        }
    }

    private boolean verifyInput() {
        return dataEntry.getText().toString().isEmpty() == false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dataEntry = (EditText)view.findViewById(R.id.event_string_entry);
    }

    public String getData() {
        return dataEntry.getText().toString();
    }
}
