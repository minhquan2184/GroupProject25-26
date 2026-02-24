package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

// Màn hình 1: Danh sách MÔN HỌC giảng viên đang dạy
public class LecturerDashboardFragment extends Fragment {

    private static final String TAG = "LecturerDashboard";
    RecyclerView recyclerView;
    LecturerSubjectAdapter adapter;
    List<LecturerSubject> subjectList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lecturer_dashboard_classes, container, false);

        recyclerView = view.findViewById(R.id.recyclerItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khi click vào môn học → chuyển sang danh sách lớp
        adapter = new LecturerSubjectAdapter(subjectList, subject -> {
            LecturerClassListFragment fragment = new LecturerClassListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("SUBJECT_ID", subject.getId());
            bundle.putString("SUBJECT_NAME", subject.getName());
            fragment.setArguments(bundle);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        loadSubjects();

        return view;
    }

    private void loadSubjects() {
        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");

        if (userId.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading subjects for lecturer: " + userId);

        RetrofitClient.getService().getLecturerSubjects(userId).enqueue(new Callback<List<LecturerSubject>>() {
            @Override
            public void onResponse(Call<List<LecturerSubject>> call, Response<List<LecturerSubject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjectList.clear();
                    subjectList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + subjectList.size() + " subjects");
                } else {
                    Log.e(TAG, "Error: " + response.code());
                    Toast.makeText(getContext(), "Không tìm thấy môn học", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LecturerSubject>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
