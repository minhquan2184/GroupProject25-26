package vn.edu.usth.classroomschedulemanagementapp.Lecturer.Attendance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.ApiService;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;

public class AttendanceActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceActivity";
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();
    private AttendanceCheckAdapter adapter;
    private String currentScheduleId;
    private TextView tvSelectionCount;
    private ExtendedFloatingActionButton btnDeleteSelected;
    private ImageButton btnToggleEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        // Get intent data with validation
        currentScheduleId = getIntent().getStringExtra("SCHEDULE_ID");
        String rawDate = getIntent().getStringExtra("SCHEDULE_DATE");
        Log.d(TAG, "📋 Received SCHEDULE_ID: " + currentScheduleId);
        Log.d(TAG, "📅 Received SCHEDULE_DATE: " + rawDate);

        // CRITICAL: Validate scheduleId
        if (currentScheduleId == null || currentScheduleId.isEmpty()) {
            Log.e(TAG, "❌ CRITICAL ERROR: SCHEDULE_ID is null or empty!");
            Toast.makeText(this, "Error: Missing schedule ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "📋 Initialized with Schedule ID: " + currentScheduleId);
        Log.d(TAG, "📅 Schedule Date: " + rawDate);

        formatAndDisplayDate(rawDate);
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        loadAttendanceData();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackAction();
            }
        });
    }

    private void formatAndDisplayDate(String rawDate) {
        TextView tvDate = findViewById(R.id.tvAttendanceDate);
        if (tvDate == null || rawDate == null) {
            return;
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(rawDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
            tvDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            Log.e(TAG, "Date parsing error: " + e.getMessage());
            tvDate.setText(rawDate);
        }
    }

    private void setupRecyclerView() {
        RecyclerView rcv = findViewById(R.id.rcvAttendanceCheck);
        adapter = new AttendanceCheckAdapter(attendanceList, new AttendanceCheckAdapter.SelectionListener() {
            @Override
            public void onSelectionChanged(int count) {
                updateSelectionUI(count);
            }
        });
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adapter);
    }

    private void setupButtons() {
        tvSelectionCount = findViewById(R.id.tvSelectionCount);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnToggleEditMode = findViewById(R.id.btnToggleEditMode);

        findViewById(R.id.btnAddStudent).setOnClickListener(v -> showAddStudentBottomSheet());
        findViewById(R.id.btnSaveAttendance).setOnClickListener(v -> saveAttendanceData());
        btnDeleteSelected.setOnClickListener(v -> confirmDeleteSelected());
        btnToggleEditMode.setOnClickListener(v -> toggleEditMode());
    }

    private void toggleEditMode() {
        boolean newMode = !adapter.isEditMode();
        adapter.setEditMode(newMode);

        btnDeleteSelected.setVisibility(newMode && adapter.getSelectedCount() > 0 ? View.VISIBLE : View.GONE);
        tvSelectionCount.setVisibility(newMode ? View.VISIBLE : View.GONE);

        Toast.makeText(this,
                newMode ? "Selection mode enabled" : "Selection mode disabled",
                Toast.LENGTH_SHORT).show();
    }

    private void updateSelectionUI(int count) {
        tvSelectionCount.setText(count + " selected");
        btnDeleteSelected.setVisibility(count > 0 && adapter.isEditMode() ? View.VISIBLE : View.GONE);
    }

    private void loadAttendanceData() {
        Log.d(TAG, "🔄 Loading attendance for schedule: " + currentScheduleId);

        RetrofitClient.getService().getAttendanceRecords(currentScheduleId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<ApiService.StudentAttendanceInfo>> call,
                    @NonNull Response<List<ApiService.StudentAttendanceInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    attendanceList.clear();
                    Log.d(TAG, "✅ Received " + response.body().size() + " attendance records");

                    for (ApiService.StudentAttendanceInfo info : response.body()) {
                        Log.d(TAG, String.format("Student: %s, ID: %s, Code: %s, Status: %s",
                                info.fullName, info.studentId, info.studentCode, info.status));

                        attendanceList.add(new AttendanceRecord(
                                info.attendanceId,
                                currentScheduleId,
                                info.studentId,
                                info.studentCode,
                                info.fullName,
                                info.status,
                                System.currentTimeMillis()
                        ));
                    }
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "✅ Adapter updated with " + attendanceList.size() + " records");
                } else {
                    Log.e(TAG, "❌ Response failed: " + response.code());
                    Toast.makeText(AttendanceActivity.this,
                            "Failed to load attendance: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ApiService.StudentAttendanceInfo>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Network Error: " + t.getMessage());
                Toast.makeText(AttendanceActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteSelected() {
        int count = adapter.getSelectedCount();
        if (count == 0) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Students")
                .setMessage("Delete " + count + " student(s) from attendance?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSelectedStudents())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSelectedStudents() {
        List<AttendanceRecord> toDelete = new ArrayList<>();
        List<Integer> positions = adapter.getSelectedPositions();

        for (int pos : positions) {
            toDelete.add(attendanceList.get(pos));
        }

        List<String> studentIds = toDelete.stream()
                .map(AttendanceRecord::getStudentId)
                .collect(Collectors.toList());

        Log.d(TAG, "🗑️ Deleting students: " + studentIds);

        ApiService.DeleteRequest request = new ApiService.DeleteRequest(currentScheduleId, studentIds);

        RetrofitClient.getService().removeStudents(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    positions.sort(Comparator.reverseOrder());
                    for (int pos : positions) {
                        attendanceList.remove(pos);
                    }
                    adapter.notifyDataSetChanged();
                    adapter.clearSelections();

                    Snackbar.make(findViewById(R.id.rcvAttendanceCheck),
                            "Deleted " + positions.size() + " student(s)",
                            Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> loadAttendanceData())
                            .show();

                    Log.d(TAG, "✅ Deleted " + positions.size() + " students");
                } else {
                    Log.e(TAG, "❌ Delete failed: " + response.code());
                    Toast.makeText(AttendanceActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Delete error: " + t.getMessage());
                Toast.makeText(AttendanceActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAttendanceData() {
        Log.d(TAG, "💾 Saving attendance for " + attendanceList.size() + " students");
        Log.d(TAG, "💾 Schedule ID: " + currentScheduleId);

        List<ApiService.AttendanceRecordJson> records = new ArrayList<>();
        for (AttendanceRecord item : attendanceList) {
            Log.d(TAG, "  📝 Student: " + item.getStudentId() + " → " + item.getStatus());
            records.add(new ApiService.AttendanceRecordJson(item.getStudentId(), item.getStatus()));
        }

        ApiService.AttendanceSubmission sub = new ApiService.AttendanceSubmission(currentScheduleId, records);
        RetrofitClient.getService().submitAttendance(sub).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Attendance saved successfully");
                    Toast.makeText(AttendanceActivity.this, "Attendance saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Save failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body");
                    }
                    Log.e(TAG, "❌ " + errorMsg);
                    Toast.makeText(AttendanceActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Save error: " + t.getMessage());
                Toast.makeText(AttendanceActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupToolbar() {
        findViewById(R.id.btnBack).setOnClickListener(v -> handleBackAction());
    }

    private void handleBackAction() {
        if (adapter != null && adapter.isEditMode()) {
            adapter.setEditMode(false);
            updateSelectionUI(0);
        } else {
            finish();
        }
    }

    private void showAddStudentBottomSheet() {
        // CRITICAL: Check scheduleId before showing dialog
        if (currentScheduleId == null || currentScheduleId.isEmpty()) {
            Toast.makeText(this, "Error: Invalid schedule ID", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Cannot add student: scheduleId is null/empty");
            return;
        }

        Log.d(TAG, "📝 Opening add student dialog for schedule: " + currentScheduleId);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_add_student_bottom_sheet, null);
        dialog.setContentView(view);

        EditText etName = view.findViewById(R.id.etStudentName);
        Button btnConfirm = view.findViewById(R.id.btnConfirmAdd);
        final ApiService.StudentSearchResponse[] foundStudent = {null};

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    String searchQuery = s.toString().trim();
                    Log.d(TAG, "🔍 Searching for: " + searchQuery);

                    RetrofitClient.getService().searchStudents(searchQuery).enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<List<ApiService.StudentSearchResponse>> call,
                                @NonNull Response<List<ApiService.StudentSearchResponse>> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                foundStudent[0] = response.body().get(0);
                                Log.d(TAG, "✅ Found student: " + foundStudent[0].fullName + " (ID: " + foundStudent[0].id + ")");
                                etName.setError("Found: " + foundStudent[0].studentCode);
                            } else {
                                foundStudent[0] = null;
                                Log.d(TAG, "⚠️ No students found");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<ApiService.StudentSearchResponse>> call, @NonNull Throwable t) {
                            Log.e(TAG, "❌ Search failed: " + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (foundStudent[0] == null) {
                Toast.makeText(this, "Please search and select a student first", Toast.LENGTH_SHORT).show();
                return;
            }

            // CRITICAL: Double-check scheduleId before making request
            if (currentScheduleId == null || currentScheduleId.isEmpty()) {
                Toast.makeText(this, "Error: Invalid schedule ID", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "❌ CRITICAL: scheduleId is null when adding student!");
                return;
            }

            Log.d(TAG, "➕ Adding student " + foundStudent[0].id + " to schedule " + currentScheduleId);

            ApiService.AddStudentRequest req = new ApiService.AddStudentRequest(
                    currentScheduleId,
                    foundStudent[0].id
            );

            RetrofitClient.getService()
                    .addStudentToClass(req)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "✅ Student added successfully");
                                loadAttendanceData();
                                dialog.dismiss();
                                Toast.makeText(
                                        AttendanceActivity.this,
                                        "Student added successfully",
                                        Toast.LENGTH_SHORT
                                ).show();
                            } else {
                                Log.e(TAG, "❌ Add student failed: " + response.code());
                                String errorMsg = "Add student failed";
                                try {
                                    if (response.errorBody() != null) {
                                        errorMsg += ": " + response.errorBody().string();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error reading error body: " + e.getMessage());
                                }
                                Toast.makeText(
                                        AttendanceActivity.this,
                                        errorMsg,
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            Log.e(TAG, "❌ Network error: " + t.getMessage());
                            Toast.makeText(
                                    AttendanceActivity.this,
                                    "Network error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        dialog.show();
    }
}
