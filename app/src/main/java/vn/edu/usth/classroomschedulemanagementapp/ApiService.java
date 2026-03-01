package vn.edu.usth.classroomschedulemanagementapp;

import retrofit2.Call;
import java.util.List;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.edu.usth.classroomschedulemanagementapp.Calendar.ScheduleResponse;
import vn.edu.usth.classroomschedulemanagementapp.Login.LoginRequest;
import vn.edu.usth.classroomschedulemanagementapp.Student.AllCourse.Subject;
import vn.edu.usth.classroomschedulemanagementapp.Student.MyCourse.CourseDetail.Attendance;
import vn.edu.usth.classroomschedulemanagementapp.Student.Account.UserProfile;
import vn.edu.usth.classroomschedulemanagementapp.Student.Account.StudentGrade;
import vn.edu.usth.classroomschedulemanagementapp.Lecturer.LecturerSubject;
import vn.edu.usth.classroomschedulemanagementapp.Lecturer.LecturerClass;
import vn.edu.usth.classroomschedulemanagementapp.Lecturer.StudentInfo;

public interface ApiService {

    @POST("/api/login")
    Call<User> login(@Body LoginRequest request);

    @GET("/api/profile/{userId}")
    Call<UserProfile> getProfile(@Path("userId") String userId);

    @GET("/api/grades/{studentId}")
    Call<List<StudentGrade>> getGrades(@Path("studentId") String studentId);

    @GET("/api/subjects")
    Call<List<Subject>> getSubjects(@Query("userId") String userId);

    @POST("/api/enroll-auto")
    Call<Void> enrollAuto(@Body EnrollRequest request);

    @GET("/api/my-courses/{userId}")
    Call<List<Subject>> getMyCourses(@Path("userId") String userId);

    @GET("/api/lecturer-courses/{userId}")
    Call<List<Subject>> getLecturerCourses(@Path("userId") String userId);

    @GET("/api/lecturer-subjects/{lecturerId}")
    Call<List<LecturerSubject>> getLecturerSubjects(@Path("lecturerId") String lecturerId);

    @GET("/api/lecturer-subject-classes/{lecturerId}/{subjectId}")
    Call<List<LecturerClass>> getLecturerSubjectClasses(@Path("lecturerId") String lecturerId, @Path("subjectId") String subjectId);

    @GET("/api/class-students/{classId}")
    Call<List<StudentInfo>> getClassStudents(@Path("classId") String classId);

    @GET("/api/schedule/{userId}")
    Call<List<ScheduleResponse>> getStudentSchedule(@Path("userId") String userId);

    @GET("/api/attendance")
    Call<List<Attendance>> getAttendance(@Query("classId") String classId, @Query("studentId") String studentId);

    @GET("/api/students/search")
    Call<List<StudentSearchResponse>> searchStudents(@Query("name") String name);

    @GET("/api/attendance/{scheduleId}")
    Call<List<StudentAttendanceInfo>> getAttendanceRecords(@Path("scheduleId") String scheduleId);

    @POST("/api/attendance/submit")
    Call<Void> submitAttendance(@Body AttendanceSubmission submission);

    @POST("/api/attendance/remove-students")
    Call<Void> removeStudents(@Body DeleteRequest request);

    @POST("/api/attendance/add-student")
    Call<Void> addStudentToClass(@Body AddStudentRequest request);

    @POST("/api/document")
    Call<Void> uploadDocument(@Body DocumentRequest request);

    @GET("/api/documents/{classId}")
    Call<List<DocumentResponse>> getDocuments(@Path("classId") String classId);

    class DocumentResponse {

        public String id;
        public String title;
        public String description; // URL is stored in description
        public String createdat;
    }

    class DocumentRequest {

        String classId;
        String uploaderId;
        String subjectId;
        String title;
        String url;

        public DocumentRequest(String classId, String uploaderId, String subjectId, String title, String url) {
            this.classId = classId;
            this.uploaderId = uploaderId;
            this.subjectId = subjectId;
            this.title = title;
            this.url = url;
        }
    }

    class StudentSearchResponse {

        public String id;
        public String fullName;
        public String studentCode;
    }

    class StudentAttendanceInfo {

        public String attendanceId;  // Added missing field
        public String studentId;
        public String id;
        public String fullName;
        public String studentCode;
        public String status = "Present";
    }

    class AttendanceSubmission {

        public String scheduleId;
        public List<AttendanceRecordJson> records;

        public AttendanceSubmission(String scheduleId, List<AttendanceRecordJson> records) {
            this.scheduleId = scheduleId;
            this.records = records;
        }
    }

    class AttendanceRecordJson {

        String studentId;
        String status;

        public AttendanceRecordJson(String studentId, String status) {
            this.studentId = studentId;
            this.status = status;
        }
    }

    class DeleteRequest {

        public String scheduleId;
        public List<String> studentIds;

        public DeleteRequest(String scheduleId, List<String> studentIds) {
            this.scheduleId = scheduleId;
            this.studentIds = studentIds;
        }
    }

    class AddStudentRequest {

        public String scheduleId;
        public String studentId;

        public AddStudentRequest(String scheduleId, String studentId) {
            this.scheduleId = scheduleId;
            this.studentId = studentId;
        }
    }
}
