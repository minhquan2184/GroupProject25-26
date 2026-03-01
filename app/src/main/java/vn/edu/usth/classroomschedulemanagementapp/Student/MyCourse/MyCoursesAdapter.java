package vn.edu.usth.classroomschedulemanagementapp.Student.MyCourse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.Student.AllCourse.Subject;

public class MyCoursesAdapter extends RecyclerView.Adapter<MyCoursesAdapter.CourseViewHolder> {

    private final List<Subject> courseList;
    private final OnItemClickListener listener; // 1. Khai báo listener

    // 2. Tạo Interface để Fragment implements
    public interface OnItemClickListener {

        void onDetailClick(Subject subject);
    }

    // 3. Cập nhật Constructor để nhận listener
    public MyCoursesAdapter(List<Subject> courseList, OnItemClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_my_course_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Subject subject = courseList.get(position);

        holder.tvCourseName.setText(subject.getName());
        holder.tvCredits.setText("Credits: " + subject.getCredits());
        holder.tvProfessor.setText("Lecturer: " + subject.getLecturer());

        holder.btnAction.setText("Detail");

        // 4. Bắt sự kiện click và truyền ra ngoài
        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailClick(subject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView tvCourseName, tvProfessor, tvCredits;
        Button btnAction;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvProfessor = itemView.findViewById(R.id.tvProfessor);
            tvCredits = itemView.findViewById(R.id.tvCredits);
            btnAction = itemView.findViewById(R.id.btnDetail);
        }
    }
}
