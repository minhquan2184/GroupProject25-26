package vn.edu.usth.classroomschedulemanagementapp.Student.MyCourse.CourseDetail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;

public class CourseDetailFragment extends Fragment {

    private RecyclerView rcvAttendance;
    private AttendanceAdapter adapter;
    private List<Attendance> attendanceList;
    private String classId;
    private String courseName;

    // Hàm tạo static để truyền dữ liệu vào Fragment an toàn
    public static CourseDetailFragment newInstance(String classId, String courseName) {
        CourseDetailFragment fragment = new CourseDetailFragment();
        Bundle args = new Bundle();
        args.putString("CLASS_ID", classId);
        args.putString("COURSE_NAME", courseName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            classId = getArguments().getString("CLASS_ID");
            courseName = getArguments().getString("COURSE_NAME");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_detail, container, false);

        // Setup UI
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        tvTitle.setText(courseName != null ? courseName : "Course Detail");

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        // Nút Back: Quay lại Fragment trước đó trong ngăn xếp (Back Stack)
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Ẩn nút "Add Document" vì Student không được phép thêm tài liệu
        View btnAddDoc = view.findViewById(R.id.btnAddDoc);
        if (btnAddDoc != null) {
            btnAddDoc.setVisibility(View.GONE);
        }

        // Setup RecyclerView Attendance
        rcvAttendance = view.findViewById(R.id.rcvAttendance);
        rcvAttendance.setLayoutManager(new LinearLayoutManager(getContext()));
        attendanceList = new ArrayList<>();
        adapter = new AttendanceAdapter(attendanceList);
        rcvAttendance.setAdapter(adapter);

        fetchAttendance();

        return view;
    }

    private void fetchAttendance() {
        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String studentId = prefs.getString("USER_ID", "");

        android.util.Log.d("ATTENDANCE_DEBUG", "classId=" + classId + ", studentId=" + studentId);

        if (studentId.isEmpty() || classId == null) {
            android.util.Log.e("ATTENDANCE_DEBUG", "Missing data! studentId=" + studentId + ", classId=" + classId);
            return;
        }

        RetrofitClient.getService().getAttendance(classId, studentId).enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                android.util.Log.d("ATTENDANCE_DEBUG", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("ATTENDANCE_DEBUG", "Records received: " + response.body().size());
                    attendanceList.clear();
                    attendanceList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    android.util.Log.e("ATTENDANCE_DEBUG", "Response failed or empty. Code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        android.util.Log.e("ATTENDANCE_DEBUG", "Error body: " + errorBody);
                    } catch (Exception e) {
                        android.util.Log.e("ATTENDANCE_DEBUG", "Could not read error body");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Attendance>> call, Throwable t) {
                android.util.Log.e("ATTENDANCE_DEBUG", "Network failure: " + t.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading attendance", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
