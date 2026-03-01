package vn.edu.usth.classroomschedulemanagementapp.Student.AllCourse;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.usth.classroomschedulemanagementapp.R;

public class AllCourseAdapter extends RecyclerView.Adapter<AllCourseAdapter.CourseViewHolder> {

    private Context context;
    private List<Subject> subjectList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {

        void onItemClick(Subject subject, int position);
    }

    public AllCourseAdapter(Context context, List<Subject> subjectList, OnItemClickListener listener) {
        this.context = context;
        this.subjectList = subjectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_all_course_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Subject subject = subjectList.get(position);

        holder.tvCourseName.setText(subject.getName());
        holder.tvProfessor.setText("Lecturer: " + (subject.getLecturer() != null ? subject.getLecturer() : "TBA"));
        holder.tvCredits.setText("Credits: " + subject.getCredits());

        // --- SỬA ĐỔI TẠI ĐÂY ---
        // Luôn hiển thị nút là Detail để người dùng bấm vào xem thông tin
        // Không kiểm tra isEnrolled ở đây để khóa nút nữa
        holder.btnAction.setText("Detail");
        holder.btnAction.setBackgroundColor(Color.parseColor("#0A2A57")); // Màu xanh
        holder.btnAction.setEnabled(true); // Luôn cho phép bấm

        // Sự kiện Click: Mở BottomSheet
        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onItemClick(subject, position);
            }
        };

        holder.btnAction.setOnClickListener(clickListener);
        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return subjectList != null ? subjectList.size() : 0;
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView tvCourseName, tvProfessor, tvCredits;
        Button btnAction;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvProfessor = itemView.findViewById(R.id.tvProfessor);
            tvCredits = itemView.findViewById(R.id.tvCredits);
            btnAction = itemView.findViewById(R.id.btnStudentCourseDetail);
        }
    }
}
