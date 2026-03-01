package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.app.AlertDialog;
import android.graphics.Color;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import vn.edu.usth.classroomschedulemanagementapp.ApiService;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;

// Màn hình 3: Chi tiết lớp học — Danh sách sinh viên + Tài liệu (dùng chung theo môn)
public class CourseDetailFragment extends Fragment {

    private static final String TAG = "LecturerCourseDetail";

    private TextView tvDetailTitle, tvNoDocs, tvAttTitle;
    private ImageButton btnBack;
    private LinearLayout btnAddDoc;
    private RecyclerView rcvDocuments, rcvStudents;

    private StudentListAdapter studentAdapter;
    private List<StudentInfo> studentList = new ArrayList<>();

    private vn.edu.usth.classroomschedulemanagementapp.DocumentAdapter documentAdapter;
    private List<ApiService.DocumentResponse> documentList = new ArrayList<>();

    private String classId = "";
    private String className = "";
    private String subjectId = "";
    private String subjectName = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_detail, container, false);

        btnBack = view.findViewById(R.id.btnBack);
        tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        btnAddDoc = view.findViewById(R.id.btnAddDoc);
        rcvDocuments = view.findViewById(R.id.rcvDocuments);
        tvNoDocs = view.findViewById(R.id.tvNoDocs);
        tvAttTitle = view.findViewById(R.id.tvAttTitle);
        rcvStudents = view.findViewById(R.id.rcvAttendance); // Dùng lại RecyclerView attendance cho student list

        // Nhận dữ liệu từ Bundle
        if (getArguments() != null) {
            classId = getArguments().getString("CLASS_ID", "");
            className = getArguments().getString("CLASS_NAME", "");
            subjectId = getArguments().getString("SUBJECT_ID", "");
            subjectName = getArguments().getString("SUBJECT_NAME", "");

            // Hỗ trợ cả cách cũ (COURSE_NAME)
            if (className.isEmpty()) {
                className = getArguments().getString("COURSE_NAME", "");
            }
        }
        tvDetailTitle.setText(className);

        // Đổi tiêu đề "Attendance History" thành "Student List"
        tvAttTitle.setText("Student List");

        rcvDocuments.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup student adapter
        studentAdapter = new StudentListAdapter(studentList);
        rcvStudents.setAdapter(studentAdapter);

        // Setup document adapter
        rcvDocuments.setLayoutManager(new LinearLayoutManager(getContext()));
        documentAdapter = new vn.edu.usth.classroomschedulemanagementapp.DocumentAdapter(getContext(), documentList);
        rcvDocuments.setAdapter(documentAdapter);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnAddDoc.setOnClickListener(v -> showAddDocumentDialog());

        // Tải danh sách sinh viên
        if (!classId.isEmpty()) {
            loadStudents();
            loadDocuments();
        }

        return view;
    }

    // Tải danh sách sinh viên trong lớp
    private void loadStudents() {
        Log.d(TAG, "Loading students for class: " + classId);

        RetrofitClient.getService().getClassStudents(classId).enqueue(new Callback<List<StudentInfo>>() {
            @Override
            public void onResponse(Call<List<StudentInfo>> call, Response<List<StudentInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    studentList.clear();
                    studentList.addAll(response.body());
                    studentAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + studentList.size() + " students");
                } else {
                    Log.e(TAG, "Error loading students: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<StudentInfo>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Error loading students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Dialog thêm tài liệu (giữ nguyên logic cũ)
    private void showAddDocumentDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_document, null);
        EditText edtTitle = dialogView.findViewById(R.id.edtDocTitle);
        EditText edtUrl = dialogView.findViewById(R.id.edtDocUrl);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("UPLOAD", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnPositive.setTextColor(Color.WHITE);
        btnPositive.setBackgroundColor(Color.parseColor("#14345E"));
        btnPositive.setPadding(30, 0, 30, 0);

        btnPositive.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String url = edtUrl.getText().toString().trim();

            if (!title.isEmpty() && !url.isEmpty()) {
                handleUploadDocument(title, url, dialog);
            } else {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDocuments() {
        RetrofitClient.getService().getDocuments(classId).enqueue(new Callback<List<ApiService.DocumentResponse>>() {
            @Override
            public void onResponse(Call<List<ApiService.DocumentResponse>> call, Response<List<ApiService.DocumentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    documentList.clear();
                    documentList.addAll(response.body());
                    documentAdapter.notifyDataSetChanged();

                    if (documentList.isEmpty()) {
                        tvNoDocs.setVisibility(View.VISIBLE);
                        rcvDocuments.setVisibility(View.GONE);
                    } else {
                        tvNoDocs.setVisibility(View.GONE);
                        rcvDocuments.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Error fetching documents");
                    tvNoDocs.setVisibility(View.VISIBLE);
                    rcvDocuments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.DocumentResponse>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                tvNoDocs.setVisibility(View.VISIBLE);
                rcvDocuments.setVisibility(View.GONE);
            }
        });
    }

    // Upload tài liệu
    private void handleUploadDocument(String title, String url, AlertDialog dialog) {
        // Lấy uploaderId từ SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String uploaderId = prefs.getString("USER_ID", "");

        if (uploaderId.isEmpty() || classId.isEmpty() || subjectId.isEmpty()) {
            Toast.makeText(getContext(), "Missing user, class, or subject info", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.DocumentRequest request = new ApiService.DocumentRequest(classId, uploaderId, subjectId, title, url);

        RetrofitClient.getService().uploadDocument(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Upload Successful!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadDocuments(); // Refresh the list
                } else {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
