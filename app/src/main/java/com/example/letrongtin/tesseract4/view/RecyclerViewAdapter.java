package com.example.letrongtin.tesseract4.view;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.letrongtin.tesseract4.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter {

    ArrayList mText;
    ArrayList mImage;
    Context mContext;
    RecyclerViewClickListener listener;

    public RecyclerViewAdapter(ArrayList mText, ArrayList mImage, Context mContext, RecyclerViewClickListener listener) {
        this.mText = mText;
        this.mImage = mImage;
        this.mContext = mContext;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView textView;
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.recyclerViewListClicked(view, this.getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_view_item, viewGroup, false);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = (size.y - 300) / 2;
        view.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.textView.setText(mText.get(i).toString());
        holder.imageView.setImageResource(Integer.parseInt(mImage.get(i).toString()));
    }

    @Override
    public int getItemCount() {
        return mText.size();
    }
}
