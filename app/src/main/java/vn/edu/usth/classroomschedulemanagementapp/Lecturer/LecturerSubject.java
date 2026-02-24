package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import com.google.gson.annotations.SerializedName;

// Model cho môn học của giảng viên
public class LecturerSubject {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("credits")
    private int credits;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCredits() {
        return credits;
    }
}
