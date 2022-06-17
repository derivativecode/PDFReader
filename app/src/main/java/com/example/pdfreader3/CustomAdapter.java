package com.example.pdfreader3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.DocumentViewHolder> {

    private Context context;
    private List<Document> documents;
    private ItemClickListener mClickListener;

    String TAG = CustomAdapter.class.getSimpleName();

    /*
    Constructor
     */

    public CustomAdapter(Context context, List<Document> documents) {
        this.context = context;
        this.documents = documents;
    }




    public class DocumentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tv_Filename;
        private TextView tv_Date;

        public DocumentViewHolder(View view) {
            super(view);
            tv_Filename = (TextView) view.findViewById(R.id.textViewList);
            tv_Date = (TextView) view.findViewById(R.id.tv_date);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAbsoluteAdapterPosition());
        }

        public TextView getTextView() {
            return tv_Filename;
        }

        public TextView getDateView() {
            return tv_Date;
        }
    }


    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.text_row_item, viewGroup, false);
        return new DocumentViewHolder(view);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DocumentViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(documents.get(position).getFilename());
        viewHolder.getDateView().setText(getSimpleDate(documents.get(position).getUpdatedAt()));
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + documents.size() + ", " + documents.toString());
        return documents.size();
    }


    /*
    Intercept click events
     */

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // convert Date to simpler format
    public String getSimpleDate(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormatter.format(date);
    }
}
