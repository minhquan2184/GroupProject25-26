package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import com.google.gson.annotations.SerializedName;

// Model cho lớp học thuộc một môn
public class LecturerClass {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("studentCount")
    private int studentCount;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStudentCount() {
        return studentCount;
    }
}
