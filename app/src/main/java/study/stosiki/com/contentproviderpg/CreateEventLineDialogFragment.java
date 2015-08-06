package study.stosiki.com.contentproviderpg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Created by mike on 7/26/2015.
 */
public class CreateEventLineDialogFragment extends DialogFragment {
    private static final int DEFAULT_TYPE_POSITION = 0;

    public interface CreateEventLineDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    private View view;
    private CreateEventLineDialogListener listener;

    private ListView eventLineTypeSelector;
    private EditText eventLineTitleEntry;

    public CreateEventLineDialogFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (CreateEventLineDialogListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogPositiveClick(CreateEventLineDialogFragment.this);
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

        return dialogBuilder.create();
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
//        eventLineTypeSelector.setSelection(DEFAULT_TYPE_POSITION);

        eventLineTypeSelector.setAdapter(eventTypesAdapter);
        /*
        eventLineTypeSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setSelectedItem(position);
            }
        });
        */
        setSelectedItem(DEFAULT_TYPE_POSITION);
        eventLineTitleEntry = (EditText)view.findViewById(R.id.event_line_title_entry);
    }

    private void setSelectedItem(final int position) {
        eventLineTypeSelector.setItemChecked(position, true);
/*
        eventLineTypeSelector.clearFocus();
        eventLineTypeSelector.post(new Runnable() {
            public void run() {
                eventLineTypeSelector.setSelection(position);
            }
        });
*/
//        View v = (View)eventLineTypeSelector.se.getItemAtPosition(position);
//        v.setSelected(true);
    }

    public int getSelectedType() {
        return eventLineTypeSelector.getCheckedItemPosition();
    }

    public String getTitle() {
        return eventLineTitleEntry.getText().toString();
    }
}
