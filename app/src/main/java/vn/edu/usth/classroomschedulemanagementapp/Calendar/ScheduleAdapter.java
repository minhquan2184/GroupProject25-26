package vn.edu.usth.classroomschedulemanagementapp.Calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.Lecturer.Attendance.AttendanceActivity;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<Schedule> scheduleList;

    public ScheduleAdapter(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public void updateData(List<Schedule> newList) {
        this.scheduleList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Schedule item = scheduleList.get(position);
        holder.tvSubject.setText(item.subject);
        holder.tvTime.setText(item.time);
        holder.tvRoom.setText(item.room);
        holder.tvLecturer.setText("Lecturer: " + item.lecturer);

        // Hiển thị nhãn EXAM nếu category là EXAM
        if ("EXAM".equalsIgnoreCase(item.getCategory())) {
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        // Kiểm tra role từ SharedPreferences
        Context context = holder.itemView.getContext();
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "");
        boolean isLecturer = "LECTURER".equalsIgnoreCase(role);

        if (isLecturer) {
            // Chỉ Lecturer mới thấy nút View Attendance
            holder.btnViewAttendance.setVisibility(View.VISIBLE);
            holder.btnViewAttendance.setOnClickListener(v -> {
                Intent intent = new Intent(context, AttendanceActivity.class);

                // Truyền ID buổi học và tên môn sang màn hình điểm danh
                intent.putExtra("SCHEDULE_ID", item.id);
                intent.putExtra("SUBJECT_NAME", item.subject);
                intent.putExtra("SCHEDULE_DATE", item.date);

                context.startActivity(intent);
            });
        } else {
            holder.btnViewAttendance.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList != null ? scheduleList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSubject, tvTime, tvRoom, tvLecturer, tvCategory;
        TextView btnViewAttendance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubjectName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvLecturer = itemView.findViewById(R.id.tvLecturer);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnViewAttendance = itemView.findViewById(R.id.btnViewAttendance);
        }
    }
}
