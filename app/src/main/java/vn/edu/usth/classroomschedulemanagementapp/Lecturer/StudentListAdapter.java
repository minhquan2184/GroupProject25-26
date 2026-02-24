package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.usth.classroomschedulemanagementapp.R;

// Adapter hiển thị danh sách sinh viên trong lớp
public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.ViewHolder> {

    private final List<StudentInfo> studentList;

    public StudentListAdapter(List<StudentInfo> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentInfo student = studentList.get(position);
        holder.tvStudentName.setText(student.getFullName());
        String code = student.getStudentCode();
        holder.tvStudentCode.setText(code != null ? code : "N/A");
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvStudentName, tvStudentCode;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
        }
    }
}
