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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mike on 7/26/2015.
 */
public class CreateEventLineDialogFragment extends DialogFragment {
    private static final int DEFAULT_TYPE_POSITION = 0;

    private static final int INPUT_OK = 0;
    private static final int INPUT_EMPTY = 1;
    private static final int INPUT_DUPLICATE_NAME = 2;

    public interface DialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    private View view;
    private DialogListener listener;

    private ListView eventLineTypeSelector;
    private EditText eventLineTitleEntry;
    private TextView errorMessage;

    private ArrayList<String> lineNames;

    public CreateEventLineDialogFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (DialogListener)activity;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        lineNames = getArguments().getStringArrayList("line_names");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogNegativeClick(CreateEventLineDialogFragment.this);
            }
        });
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.create_event_line_dialog, null);
        dialogBuilder.setView(view);

        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                    switch (verifyInput()) {
                        case INPUT_OK:
                            listener.onDialogPositiveClick(CreateEventLineDialogFragment.this);
                            dialog.dismiss();
                            break;
                        case INPUT_EMPTY:
                            eventLineTitleEntry.setHintTextColor(
                                getResources().getColor(android.R.color.holo_blue_bright));
                            break;
                        case INPUT_DUPLICATE_NAME:
//                            errorMessage.setText(R.string.duplicate_eventline_name_error_msg);
                            errorMessage.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            });
        }
    }

    private int verifyInput() {
        String name = eventLineTitleEntry.getText().toString();
        if(name.isEmpty()) {
            return INPUT_EMPTY;
        } else if(used(name)) {
            return INPUT_DUPLICATE_NAME;
        } else {
            return INPUT_OK;
        }
    }

    private boolean used(String name) {
        for(String lineName : lineNames) {
            if(lineName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] eventLineTypes = new String[]{"Basic", "Number", "Comment"};
        ArrayAdapter<String> eventTypesAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.line_type_selector_item, eventLineTypes);
        eventLineTypeSelector = (ListView)view.findViewById(R.id.event_line_type_selector);
        eventLineTypeSelector.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        eventLineTypeSelector.getSelectedView();

        eventLineTypeSelector.setAdapter(eventTypesAdapter);
        setSelectedItem(DEFAULT_TYPE_POSITION);
        eventLineTitleEntry = (EditText)view.findViewById(R.id.event_line_title_entry);
        errorMessage = (TextView)view.findViewById(R.id.error_msg_text);
    }

    private void setSelectedItem(final int position) {
        eventLineTypeSelector.setItemChecked(position, true);
    }

    public int getSelectedType() {
        return eventLineTypeSelector.getCheckedItemPosition();
    }

    public String getTitle() {
        return eventLineTitleEntry.getText().toString();
    }
}
