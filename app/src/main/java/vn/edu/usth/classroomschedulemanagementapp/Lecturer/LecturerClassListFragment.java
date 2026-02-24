package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

// Màn hình 2: Danh sách LỚP HỌC của một môn, do giảng viên dạy
public class LecturerClassListFragment extends Fragment {

    private static final String TAG = "LecturerClassList";
    private RecyclerView rcvClasses;
    private TextView tvTitle, tvEmpty;
    private LecturerClassListAdapter adapter;
    private List<LecturerClass> classList = new ArrayList<>();
    private String subjectId = "";
    private String subjectName = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lecturer_class_list, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rcvClasses = view.findViewById(R.id.rcvClasses);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Nhận dữ liệu từ Bundle
        if (getArguments() != null) {
            subjectId = getArguments().getString("SUBJECT_ID", "");
            subjectName = getArguments().getString("SUBJECT_NAME", "");
        }
        tvTitle.setText(subjectName);

        rcvClasses.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khi click vào lớp → chuyển sang chi tiết lớp (danh sách sinh viên + tài liệu)
        adapter = new LecturerClassListAdapter(classList, lecturerClass -> {
            CourseDetailFragment fragment = new CourseDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("CLASS_ID", lecturerClass.getId());
            bundle.putString("CLASS_NAME", lecturerClass.getName());
            bundle.putString("SUBJECT_ID", subjectId);
            bundle.putString("SUBJECT_NAME", subjectName);
            fragment.setArguments(bundle);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rcvClasses.setAdapter(adapter);

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        loadClasses();

        return view;
    }

    private void loadClasses() {
        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");

        if (userId.isEmpty() || subjectId.isEmpty()) {
            Toast.makeText(getContext(), "Missing data", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading classes for subject: " + subjectId);

        RetrofitClient.getService().getLecturerSubjectClasses(userId, subjectId).enqueue(new Callback<List<LecturerClass>>() {
            @Override
            public void onResponse(Call<List<LecturerClass>> call, Response<List<LecturerClass>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    classList.clear();
                    classList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + classList.size() + " classes");

                    // Hiện/ẩn empty text
                    tvEmpty.setVisibility(classList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "Error: " + response.code());
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<LecturerClass>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
