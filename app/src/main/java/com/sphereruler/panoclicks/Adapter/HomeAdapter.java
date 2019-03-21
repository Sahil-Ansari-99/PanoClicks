package com.sphereruler.panoclicks.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sphereruler.panoclicks.Model.FileModel;
import com.sphereruler.panoclicks.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeViewHolder>{

ArrayList<FileModel> fileList;
Context context;
LayoutInflater layoutInflater;

    public HomeAdapter(ArrayList<FileModel> fileList, Context context) {
        this.fileList = fileList;
        this.context = context;
        layoutInflater=LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView=layoutInflater.inflate(R.layout.home_card,viewGroup,false);
        return new HomeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder homeViewHolder, int i) {

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(fileList.get(i).getDate()*1000);
        String date= DateFormat.format("dd-MM-yyyy hh:mm:ss",cal).toString();

        homeViewHolder.title.setText(fileList.get(i).getTitle());
        homeViewHolder.pubDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }
}

class HomeViewHolder extends RecyclerView.ViewHolder{
    TextView title, pubDate;
    ImageView imageView;

    public HomeViewHolder(@NonNull View itemView) {
        super(itemView);

        title=itemView.findViewById(R.id.home_card_tv_name);
        pubDate=itemView.findViewById(R.id.home_card_tv_time);
        imageView=itemView.findViewById(R.id.home_card_image);


    }
}
