package vn.edu.usth.classroomschedulemanagementapp.Student.AllCourse;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.EnrollRequest;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;

public class AllCoursesFragment extends Fragment {

    private RecyclerView rvCourses;
    private AllCourseAdapter adapter;
    private List<Subject> subjectList;
    private String userId;

    public AllCoursesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_all_course, container, false);

        rvCourses = view.findViewById(R.id.rvCourses);
        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        subjectList = new ArrayList<>();

        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("USER_ID", "");

        adapter = new AllCourseAdapter(getContext(), subjectList, (subject, position) -> {
            // Khi bấm nút Detail ở list -> Mở BottomSheet
            showCourseDetailBottomSheet(subject, position);
        });

        rvCourses.setAdapter(adapter);
        fetchSubjects();

        return view;
    }

    // === QUAN TRỌNG: LOGIC KHÓA NÚT Ở ĐÂY ===
    private void showCourseDetailBottomSheet(Subject subject, int position) {
        if (getContext() == null) {
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.student_all_course_detail, null);
        dialog.setContentView(sheetView);

        TextView tvName = sheetView.findViewById(R.id.tvSheetCourseName);
        TextView tvLecturer = sheetView.findViewById(R.id.tvSheetLecturer);
        TextView tvDesc = sheetView.findViewById(R.id.tvSheetDescription);
        Button btnEnroll = sheetView.findViewById(R.id.btnSheetEnroll);

        // Hiển thị thông tin chi tiết
        tvName.setText(subject.getName());
        tvLecturer.setText("Lecturer: " + (subject.getLecturer() != null ? subject.getLecturer() : "TBA"));

        if (subject.getDescription() != null && !subject.getDescription().isEmpty() && !subject.getDescription().equals("null")) {
            tvDesc.setText(subject.getDescription());
        } else {
            tvDesc.setText("No description available.");
        }

        // --- CHECK LOGIC ENROLL TẠI ĐÂY ---
        if (subject.isEnrolled()) {
            // Nếu đã đăng ký: Đổi nút thành "Enrolled", màu xám, không bấm được
            btnEnroll.setText("Enrolled");
            btnEnroll.setBackgroundColor(Color.GRAY);
            btnEnroll.setEnabled(false);
        } else {
            // Nếu chưa đăng ký: Nút Enroll Now, màu xanh, bấm để đăng ký
            btnEnroll.setText("Enroll Now");
            btnEnroll.setBackgroundColor(Color.parseColor("#0A2A57"));
            btnEnroll.setEnabled(true);

            btnEnroll.setOnClickListener(v -> {
                performEnroll(subject, position, btnEnroll, dialog);
            });
        }

        dialog.show();
    }

    private void performEnroll(Subject subject, int position, Button btnEnroll, BottomSheetDialog dialog) {
        if (userId.isEmpty()) {
            Toast.makeText(getContext(), "Please login first!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnroll.setText("Processing...");
        btnEnroll.setEnabled(false);

        EnrollRequest request = new EnrollRequest(userId, subject.getId());
        RetrofitClient.getService().enrollAuto(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Enroll Success!", Toast.LENGTH_SHORT).show();

                    // Cập nhật trạng thái trong object Subject
                    subject.setEnrolled(true);

                    // Cập nhật UI ngay lập tức trên BottomSheet (biến thành nút Enrolled)
                    btnEnroll.setText("Enrolled");
                    btnEnroll.setBackgroundColor(Color.GRAY);
                    btnEnroll.setEnabled(false);

                    // Không cần đóng dialog ngay, để người dùng thấy trạng thái đã đổi
                    // dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    // Reset lại nút nếu lỗi
                    btnEnroll.setText("Enroll Now");
                    btnEnroll.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                btnEnroll.setText("Enroll Now");
                btnEnroll.setEnabled(true);
            }
        });
    }

    private void fetchSubjects() {
        if (userId.isEmpty()) {
            return;
        }
        RetrofitClient.getService().getSubjects(userId).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjectList.clear();
                    subjectList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Log.e("AllCourses", "Error: " + t.getMessage());
            }
        });
    }
}
