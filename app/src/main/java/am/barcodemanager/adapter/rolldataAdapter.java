package am.barcodemanager.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import am.barcodemanager.R;
import am.barcodemanager.model.RollInfo;


public class rolldataAdapter extends ArrayAdapter<RollInfo> {

    //storing all the names in the list
    private List<RollInfo> names;

    //context object
    private Context context;

    //constructor
    public rolldataAdapter(Context context, int resource, List<RollInfo> names) {
        super(context, resource, names);
        this.context = context;
        this.names = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview itmes
        View listViewItem = inflater.inflate(R.layout.roll_info_list_text, null, true);
        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        TextView tv_roll = (TextView) listViewItem.findViewById(R.id.tv_roll);
        TextView imageViewStatus = (TextView) listViewItem.findViewById(R.id.imageViewStatus);

        //getting the current name
        RollInfo name = names.get(position);
        //setting the name to textview
        textViewName.setText(name.getPalletNumber());
        tv_roll.setText(name.getRollNumber());


        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        if (name.getStatus() == 0)
            imageViewStatus.setText("Pending");
        else
            imageViewStatus.setText("Uploaded");
        return listViewItem;
    }
}



