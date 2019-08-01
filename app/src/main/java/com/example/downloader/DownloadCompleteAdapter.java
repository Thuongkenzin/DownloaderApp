package com.example.downloader;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class DownloadCompleteAdapter extends ArrayAdapter<DownloadThread> {

   public DownloadCompleteAdapter(Context context, List<DownloadThread> list){
       super(context,R.layout.download_complete_item,list);
   }
   private static class ViewHolder{
       TextView textName;
       TextView textSize;
       ImageButton btnOption;
   }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DownloadThread downloadThread = getItem(position);

        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater= LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.download_complete_item,parent,false);
            viewHolder.textName = convertView.findViewById(R.id.tv_download_complete_item);
            viewHolder.textSize = convertView.findViewById(R.id.tv_file_size_download_complete);
            viewHolder.btnOption = convertView.findViewById(R.id.btn_option);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textName.setText(downloadThread.getDownloadFileName());
        viewHolder.textSize.setText(DownloadUtil.getStringSizeLengthFile(downloadThread.getFileSize()));
        viewHolder.btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showFilterPopup(v);
                PopupMenu popup = new PopupMenu(getContext(),v);
                popup.inflate(R.menu.download_complete_item);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.open_file:
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    MimeTypeMap map = MimeTypeMap.getSingleton();
                                    String ext = MimeTypeMap.getFileExtensionFromUrl(downloadThread.fileDownload.getName());
                                    String mime = map.getMimeTypeFromExtension(ext);
                                    intent.setDataAndType(Uri.fromFile(downloadThread.fileDownload),mime);
                                    getContext().startActivity(intent);

                                }catch(ActivityNotFoundException e){
                                    Toast.makeText(getContext(), "Can't support format file.", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.delete_item:

                                break;
                            case R.id.detail_item:
                                Toast.makeText(getContext(), "detail file", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return false;
                    }
                });

                popup.show();

            }
        });

        return convertView;
    }

    private void showFilterPopup(View v){
        PopupMenu popup = new PopupMenu(getContext(),v);
        popup.inflate(R.menu.download_complete_item);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.open_file:
                        Toast.makeText(getContext(), "Open file", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.delete_item:

                        break;
                    case R.id.detail_item:
                        Toast.makeText(getContext(), "detail file", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        popup.show();
    }
}
