package com.example.downloader;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.downloader.Database.DownloadDatabaseHelper;
import com.example.downloader.Database.FileDownload;

import java.io.File;
import java.util.List;

public class DownloadCompleteAdapter extends ArrayAdapter<FileDownload> {

    public DownloadCompleteAdapter(Context context, List<FileDownload> list) {
        super(context, R.layout.download_complete_item, list);
    }

    private static class ViewHolder {
        TextView textName;
        TextView textSize;
        ImageButton btnOption;
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
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //get file have been saved in SD
        final File file = new File(downloadThread.getUriFileDir());
        long size = file.length();

        viewHolder.textName.setText(downloadThread.getFileName());
        viewHolder.textSize.setText(DownloadUtil.getStringSizeLengthFile(size));
        viewHolder.btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showFilterPopup(v);
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.inflate(R.menu.download_complete_item);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.open_file:
                                try {
                                    if (file.exists()) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        MimeTypeMap map = MimeTypeMap.getSingleton();
                                        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
                                        String mime = map.getMimeTypeFromExtension(ext);
                                        intent.setDataAndType(Uri.fromFile(file), mime);
                                        getContext().startActivity(intent);
                                    } else {
                                        DownloadDatabaseHelper.getInstance(getContext()).deleteFileDownload(downloadThread.get_id());
                                        DownloadManager.getInstance().getCompleteListDownload().remove(downloadThread);
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
                                        DownloadDatabaseHelper.getInstance(getContext()).deleteFileDownload(downloadThread.get_id());
                                        DownloadManager.getInstance().getCompleteListDownload().remove(downloadThread);
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


}
