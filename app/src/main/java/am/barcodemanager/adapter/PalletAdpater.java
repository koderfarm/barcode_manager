package am.barcodemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import am.barcodemanager.R;
import am.barcodemanager.model.Pallet;

public class PalletAdpater extends ArrayAdapter<Pallet> {

    //storing all the names in the list
    private List<Pallet> names;

    //context object
    private Context context;

    //constructor
    public PalletAdpater(Context context, int resource, List<Pallet> names) {
        super(context, resource, names);
        this.context = context;
        this.names = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview itmes
        View listViewItem = inflater.inflate(R.layout.pallet_data_list_text, null, true);
        TextView tv_lot_no = (TextView) listViewItem.findViewById(R.id.tv_lot_no);
        TextView tv_roll_no = (TextView) listViewItem.findViewById(R.id.tv_roll_no);
        TextView tv_qty_no = (TextView) listViewItem.findViewById(R.id.tv_qty_no);
        TextView tv_article_no = (TextView) listViewItem.findViewById(R.id.tv_article_no);
//        TextView imageViewStatus = (TextView) listViewItem.findViewById(R.id.imageViewStatus);

        //getting the current name
        Pallet name = names.get(position);
        //setting the name to textview
        tv_lot_no.setText(name.getLot_no());
        tv_roll_no.setText(name.getRoll_no());
        tv_qty_no.setText(name.getProd_qty());
        tv_article_no.setText(name.getArticle_no());


        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
      /*  if (name.getStatus() == 0)
            imageViewStatus.setText("Pending");
        else
            imageViewStatus.setText("Uploaded");*/
        return listViewItem;
    }
}



