package am.barcodemanager.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import am.barcodemanager.R;
import am.barcodemanager.model.History;

public class HistoryAdapter extends ArrayAdapter<History> {

    //storing all the names in the list
    private List<History> names;

    //context object
    private Context context;

    //constructor
    public HistoryAdapter(Context context, int resource, List<History> names) {
        super(context, resource, names);
        this.context = context;
        this.names = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview itmes
        View listViewItem = inflater.inflate(R.layout.history_list_data_text, null, true);
        TextView tv_date = (TextView) listViewItem.findViewById(R.id.tv_date);
        TextView tv_pallet = (TextView) listViewItem.findViewById(R.id.tv_pallet);
        TextView tv_rolls = (TextView) listViewItem.findViewById(R.id.tv_rolls);
        TextView tv_meters = (TextView) listViewItem.findViewById(R.id.tv_meters);
        ImageView iv_transfered = (ImageView) listViewItem.findViewById(R.id.iv_transfered);
        History name = names.get(position);

        //setting the name to textview
        tv_date.setText(name.getDate());
        tv_pallet.setText(name.getPallet_no());
        tv_rolls.setText(name.getTotal_rolls());
        tv_meters.setText(name.getTotal_meters());


        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        Log.e("transferes", name.getTransfered());
        if (name.getTransfered().equals("red"))
            iv_transfered.setImageResource(R.drawable.ic_baseline_cancel_24);
        else
            iv_transfered.setImageResource(R.drawable.ic_baseline_check_circle_24);
        return listViewItem;
    }
}



