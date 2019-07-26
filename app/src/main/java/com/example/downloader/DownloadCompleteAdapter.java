package com.example.downloader;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DownloadCompleteAdapter extends ArrayAdapter<DownloadThread> {

   public DownloadCompleteAdapter(Context context, List<DownloadThread> list){
       super(context,R.layout.download_complete_item,list);
   }
   private static class ViewHolder{
       TextView textName;
       TextView textSize;
   }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DownloadThread downloadThread = getItem(position);

        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater= LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.download_complete_item,parent,false);
            viewHolder.textName = convertView.findViewById(R.id.tv_download_complete_item);
            viewHolder.textSize = convertView.findViewById(R.id.tv_file_size_download_complete);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textName.setText(downloadThread.getDownloadFileName());
        viewHolder.textSize.setText(DownloadUtil.getStringSizeLengthFile(downloadThread.getFileSize()));

        return convertView;
    }
}
