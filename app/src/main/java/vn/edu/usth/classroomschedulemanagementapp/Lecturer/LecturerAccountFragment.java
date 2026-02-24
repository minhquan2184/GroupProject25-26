package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.Login.LoginActivity;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;
import vn.edu.usth.classroomschedulemanagementapp.Student.Account.UserProfile;
import vn.edu.usth.classroomschedulemanagementapp.Student.AllCourse.Subject;
import vn.edu.usth.classroomschedulemanagementapp.Student.MyCourse.MyCoursesAdapter;

public class LecturerAccountFragment extends Fragment {

    private TextView tvName, tvStaffId, tvDept;
    private Button btnLogout;
    private RecyclerView rcvCourses;
    private MyCoursesAdapter adapter;
    private List<Subject> courseList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lecturer_account, container, false);

        tvName = view.findViewById(R.id.tvLecturerName);
        tvStaffId = view.findViewById(R.id.tvStaffId);
        tvDept = view.findViewById(R.id.tvDepartment);
        btnLogout = view.findViewById(R.id.btnLogoutLecturer);
        rcvCourses = view.findViewById(R.id.rcvLecturerCourses);

        rcvCourses.setLayoutManager(new LinearLayoutManager(getContext()));

        // SỬA LỖI: Truyền đúng tham số cho MyCoursesAdapter
        // (List môn học và một ClickListener trống để tránh lỗi runtime)
        adapter = new MyCoursesAdapter(courseList, subject -> {
            // Có thể xử lý khi bấm vào môn học ở đây nếu cần
        });
        rcvCourses.setAdapter(adapter);

        loadLecturerData();

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadLecturerData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");

        if (userId.isEmpty()) {
            return;
        }

        // Lấy thông tin profile giảng viên
        RetrofitClient.getService().getProfile(userId).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    tvName.setText(profile.getFullName());
                    // staffCode sẽ là profile.id (vd: "lec-02") vì backend trả id khi studentCode null
                    String staffCode = profile.getStudentCode();
                    tvStaffId.setText("Staff ID: " + (staffCode != null ? staffCode : userId));
                    String dept = profile.getMajor();
                    tvDept.setText("Department: " + (dept != null && !dept.isEmpty() ? dept : "-"));
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
            }
        });

        // Lấy danh sách lớp giảng viên đang dạy (KHÔNG PHẢI getMyCourses cho student)
        RetrofitClient.getService().getLecturerCourses(userId).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courseList.clear();
                    courseList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading classes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
