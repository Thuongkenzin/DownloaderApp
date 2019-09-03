package com.example.downloader.Adapter;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.downloader.Database.FileDownload;
import com.example.downloader.DownloadManager;
import com.example.downloader.Utilities.DownloadUtil;
import com.example.downloader.R;

import java.io.File;
import java.util.Date;
import java.util.List;

public class DownloadCompleteAdapter extends ArrayAdapter<FileDownload> {

    public DownloadCompleteAdapter(Context context, List<FileDownload> list) {
        super(context, R.layout.download_complete_item, list);
    }

    private static class ViewHolder {
        TextView textName;
        ImageView imageTypeView;
        TextView textSize;
        ImageView btnOption;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FileDownload downloadThread = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.download_complete_item, parent, false);
            viewHolder.textName = convertView.findViewById(R.id.tv_download_complete_item);
            viewHolder.textSize = convertView.findViewById(R.id.tv_file_size_download_complete);
            viewHolder.btnOption = convertView.findViewById(R.id.btn_option);
            viewHolder.imageTypeView = convertView.findViewById(R.id.image_view_type);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //get file have been saved in SD
        final File file = new File(downloadThread.getUriFileDir());
        Date lastModDate = new Date(file.lastModified());

        Log.v("DownloadAdapter:", "lastModDate:" + lastModDate);
        String mime = DownloadUtil.getMimeTypeFile(file);

        try {
            if (mime.contains("image")) {
                viewHolder.imageTypeView.setImageResource(R.drawable.image_icon);
            } else if (mime.contains("video")) {
                viewHolder.imageTypeView.setImageResource(R.drawable.video_icon);
            } else if (mime.contains("audio")) {
                viewHolder.imageTypeView.setImageResource(R.drawable.audio_icon);
            } else if (mime.contains("pdf")) {
                viewHolder.imageTypeView.setImageResource(R.drawable.pdf_icon);
            }else if(mime.contains("text")){
                viewHolder.imageTypeView.setImageResource(R.drawable.text_file_icon);
            } else {
                viewHolder.imageTypeView.setImageResource(R.drawable.undefine_icon);
            }
        }catch (Exception e){
            e.printStackTrace();
            viewHolder.imageTypeView.setImageResource(R.drawable.undefine_icon);
        }
        viewHolder.textName.setText(downloadThread.getFileName());
        viewHolder.textSize.setText(DownloadUtil.getStringSizeLengthFile(downloadThread.getFileLength())
        + "\n" + DownloadUtil.getDayMonthYear(lastModDate));
        viewHolder.btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showFilterPopup(v);
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.inflate(R.menu.download_complete_item);
                final DownloadManager downloadManager = DownloadManager.getInstance();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.open_file:
                                try {
                                    if (file.exists()) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        String mimeTypeFile = DownloadUtil.getMimeTypeFile(file);
                                        intent.setDataAndType(Uri.fromFile(file), mimeTypeFile);
                                        getContext().startActivity(intent);
                                    } else {
                                        //DownloadDatabaseHelper.getInstance(getContext()).deleteFileDownload(downloadThread.get_id());
                                        downloadManager.deleteFileFromDatabase(getContext(),downloadThread.get_id());
                                        downloadManager.getCompleteListDownload().remove(downloadThread);
                                        notifyDataSetChanged();
                                        Toast.makeText(getContext(), "Cannot find this file!", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(getContext(), "Cannot support format file!", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.delete_item:
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Do you want to delete this file?");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (file != null) {
                                            file.delete();
                                        }
                                        downloadManager.deleteFileFromDatabase(getContext(),downloadThread.get_id());
                                        downloadManager.getCompleteListDownload().remove(downloadThread);
                                        notifyDataSetChanged();
                                    }
                                });
                                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.show();

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




}
