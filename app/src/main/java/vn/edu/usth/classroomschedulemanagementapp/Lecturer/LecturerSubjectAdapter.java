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

// Adapter hiển thị danh sách môn học cho giảng viên
public class LecturerSubjectAdapter extends RecyclerView.Adapter<LecturerSubjectAdapter.ViewHolder> {

    private final List<LecturerSubject> subjectList;
    private final OnSubjectClickListener listener;

    // Interface để bắt sự kiện click
    public interface OnSubjectClickListener {

        void onSubjectClick(LecturerSubject subject);
    }

    public LecturerSubjectAdapter(List<LecturerSubject> subjectList, OnSubjectClickListener listener) {
        this.subjectList = subjectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lecturer_subject_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LecturerSubject subject = subjectList.get(position);
        holder.tvSubjectName.setText(subject.getName());
        holder.tvCredits.setText("Credits: " + subject.getCredits());

        holder.btnViewClasses.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubjectClick(subject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectList != null ? subjectList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSubjectName, tvCredits;
        Button btnViewClasses;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvCredits = itemView.findViewById(R.id.tvCredits);
            btnViewClasses = itemView.findViewById(R.id.btnViewClasses);
        }
    }
}
