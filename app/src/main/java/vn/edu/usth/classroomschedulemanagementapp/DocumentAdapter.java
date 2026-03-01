package vn.edu.usth.classroomschedulemanagementapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private List<ApiService.DocumentResponse> documentList;
    private Context context;

    public DocumentAdapter(Context context, List<ApiService.DocumentResponse> documentList) {
        this.context = context;
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApiService.DocumentResponse document = documentList.get(position);
        holder.tvTitle.setText(document.title != null ? document.title : "Untitled Document");
        holder.tvUrl.setText(document.description != null ? document.description : "No URL provided");

        holder.itemView.setOnClickListener(v -> {
            if (document.description != null && !document.description.isEmpty()) {
                try {
                    String url = document.description;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "No URL available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentList != null ? documentList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvUrl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDocTitle);
            tvUrl = itemView.findViewById(R.id.tvDocUrl);
        }
    }
}
