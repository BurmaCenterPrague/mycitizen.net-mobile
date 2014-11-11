package net.mycitizen.mcn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MultiSpinner extends Spinner implements OnMultiChoiceClickListener, OnCancelListener {

    private List<String> items;
    private List<Integer> items_identificators;
    private int[] ids;
    private boolean[] selected;
    private String defaultText;
    private MultiSpinnerListener listener;


    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        selected[which] = isChecked;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        // refresh text on spinner
        StringBuffer spinnerBuffer = new StringBuffer();
        boolean someUnselected = false;
        for (int i = 0; i < items.size(); i++) {
            if (selected[i]) {
                spinnerBuffer.append(items.get(i));
                spinnerBuffer.append(", ");
            } else {
                someUnselected = true;
            }
        }
        String spinnerText;
        if (someUnselected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
        } else {
            spinnerText = defaultText;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{spinnerText});
        setAdapter(adapter);

        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<Integer, Boolean>();

        for (int i = 0; i < selected.length; i++) {
            result.put(items_identificators.get(i), selected[i]);
        }
        listener.onItemsSelected(result);
    }

    @Override
    public boolean performClick() {
        if (items != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMultiChoiceItems(
                    items.toArray(new CharSequence[items.size()]), selected, this);
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            builder.setOnCancelListener(this);
            builder.show();
        }
        return true;
    }

    public void setAll(boolean stat) {
        List<Boolean> defs = new ArrayList<Boolean>();

        for (int i = 0; i < selected.length; i++) {
            defs.add(stat);
        }

        this.setItems(this.items_identificators, this.items, defs, this.defaultText);
    }

    public void setItems(List<Integer> identificators, List<String> items, List<Boolean> defaults, String allText) {
        this.items_identificators = identificators;
        this.items = items;

        this.defaultText = allText;


        // all selected by default
        selected = new boolean[defaults.size()];
        boolean someUnselected = false;
        StringBuffer spinnerBuffer = new StringBuffer();
        int tr = 0;
        int fa = 0;
        for (int i = 0; i < selected.length; i++) {
            if (!defaults.get(i)) {
                someUnselected = true;
                fa++;
            } else {
                spinnerBuffer.append(items.get(i));
                spinnerBuffer.append(", ");
                tr++;
            }
            selected[i] = defaults.get(i);
        }
        // all text on the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{allText});
        setAdapter(adapter);
        adapter.notifyDataSetChanged();


        String spinnerText;
        if (someUnselected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
        } else {
            spinnerText = defaultText;
        }
        Log.d(Config.DEBUG_TAG, "TR FA: " + tr + " " + fa + " " + selected.length);
        if (tr == selected.length || fa == selected.length) {
            spinnerText = defaultText;
        }

        ArrayAdapter<String> adapter_2 = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{spinnerText});
        setAdapter(adapter_2);
        adapter_2.notifyDataSetChanged();
    }

    public void setMultiselectListener(MultiSpinnerListener lis) {
        this.listener = lis;
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(LinkedHashMap<Integer, Boolean> res);

    }
}
