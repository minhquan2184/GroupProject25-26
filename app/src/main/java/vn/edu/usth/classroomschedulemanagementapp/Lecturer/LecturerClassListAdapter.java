package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.usth.classroomschedulemanagementapp.R;

// Adapter hiển thị danh sách LỚP HỌC của một môn
public class LecturerClassListAdapter extends RecyclerView.Adapter<LecturerClassListAdapter.ViewHolder> {

    private final List<LecturerClass> classList;
    private final OnClassClickListener listener;

    // Interface để bắt sự kiện click
    public interface OnClassClickListener {

        void onClassClick(LecturerClass lecturerClass);
    }

    public LecturerClassListAdapter(List<LecturerClass> classList, OnClassClickListener listener) {
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lecturer_class_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LecturerClass item = classList.get(position);
        holder.tvClassName.setText(item.getName());
        holder.tvStudentCount.setText("Students: " + item.getStudentCount());

        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClassClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classList != null ? classList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvClassName, tvStudentCount;
        Button btnDetail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}
