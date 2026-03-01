package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.usth.classroomschedulemanagementapp.R;

public class LecturerClassAdapter extends RecyclerView.Adapter<LecturerClassAdapter.ViewHolder> {

    private List<ClassModel> classList;

    public LecturerClassAdapter(List<ClassModel> classList) {
        this.classList = classList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lecturer_class_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassModel item = classList.get(position);
        if (item != null) {
            holder.tvClassName.setText(item.getName() != null ? item.getName() : "N/A");
            String roomDisplay = item.getRoomName() != null ? "Room " + item.getRoomName() : "No Room";
            holder.tvRoom.setText(roomDisplay);
            holder.tvTime.setText(item.getTimeRange());

            holder.btnDetails.setOnClickListener(v -> {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                CourseDetailFragment detailFragment = new CourseDetailFragment();

                // Truyền dữ liệu sang Fragment chi tiết
                Bundle bundle = new Bundle();
                bundle.putString("COURSE_NAME", item.getName());
                detailFragment.setArguments(bundle);

                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    @Override
    public int getItemCount() {
        return classList != null ? classList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvClassName, tvRoom, tvTime;
        Button btnDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
