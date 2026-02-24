require('dotenv').config();
const express = require('express');
const { Client } = require('pg');
const cors = require('cors');

const app = express();
const port = process.env.PORT || 3000;


app.use(cors());
app.use(express.json());


const client = new Client({
  connectionString: process.env.DATABASE_URL,
  ssl: {
    rejectUnauthorized: false
  }
});

// --- Connection Status ---
client.connect()
  .then(() => console.log('Successful connection to Neon Database'))
  .catch(err => console.error('Connection error to Neon:', err.stack));

//                       API 

//Login API
app.post('/api/login', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ message: "Please enter Email and Password" });
  }

  try {
    const query = 'SELECT * FROM "User" WHERE email = $1';
    const values = [email];

    const result = await client.query(query, values);

    // Check if user exists
    if (result.rows.length === 0) {
      return res.status(401).json({ message: "Email does not exist!" });
    }

    const user = result.rows[0];

    // Check password (Simple comparison)
    if (user.password !== password) {
      return res.status(401).json({ message: "Incorrect password" });
    }

    // Login successful -> Return user info (excluding password)
    const { password: _, ...userWithoutPass } = user;

    console.log(`User ${email} logged in successfully!`);
    res.json(userWithoutPass);

  } catch (err) {
    console.error("Login Error:", err);
    res.status(500).json({ message: "Internal Server Error" });
  }
});

// Subjects API
app.get('/api/subjects', async (req, res) => {
  try {
    // Get all subjects, sorted by name
    const query = 'SELECT * FROM subject ORDER BY name ASC';
    const result = await client.query(query);

    res.json(result.rows);
  } catch (err) {
    console.error("Get Subjects Error:", err);
    res.status(500).json({ message: "Error fetching subject list" });
  }
});

// Enroll API (tự động thêm vào class luôn nên chỉ cần đăng ký môn)
app.post('/api/enroll-auto', async (req, res) => {
  const { userId, subjectId } = req.body;

  console.log("Processing enrollment for:", userId, subjectId);

  if (!userId || !subjectId) {
    return res.status(400).json({ message: "Missing User ID or Subject ID" });
  }

  try {
    // 1: tìm lớp xem tồn tại chưa
    const findClassQuery = 'SELECT id FROM class WHERE "subjectId" = $1 LIMIT 1';
    const classResult = await client.query(findClassQuery, [subjectId]);

    if (classResult.rows.length === 0) {
      return res.status(404).json({ message: "No open classes found for this subject!" });
    }

    const classId = classResult.rows[0].id;

    // 2: check xem đã đăng ký trước môn này chưa
    const checkQuery = 'SELECT * FROM enrollment WHERE "studentId" = $1 AND "classId" = $2';
    const checkResult = await client.query(checkQuery, [userId, classId]);

    if (checkResult.rows.length > 0) {
      return res.status(400).json({ message: "You have already enrolled in this course!" });
    }

    // 3: Insert vào enrolment table
    const insertQuery = 'INSERT INTO enrollment ("studentId", "classId") VALUES ($1, $2)';
    await client.query(insertQuery, [userId, classId]);

    console.log(`Enrollment success: User ${userId} -> Class ${classId}`);
    res.json({ message: "Enrollment successful!" });

  } catch (err) {
    console.error("Enroll Error:", err);
    res.status(500).json({ message: "Server Error: " + (err.detail || err.message) });
  }
});

// MyCourse API (Fetch enrolled courses)
app.get('/api/my-courses/:userId', async (req, res) => {
  const { userId } = req.params;
  console.log("Fetching your courses (MyCourses) for User ID:", userId);

  try {
    const query = `
      SELECT 
        c.id,                          
        c.name,                        
        COALESCE(s.credits, 3) AS credits,     
        COALESCE(up."fullName", 'Unknown Lecturer') AS lecturer

      FROM enrollment e
      JOIN class c ON e."classId" = c.id
      LEFT JOIN subject s ON c."subjectId" = s.id       
      LEFT JOIN user_profile up ON c."lecturerId" = up.id 
      WHERE e."studentId" = $1
    `;

    const result = await client.query(query, [userId]);

    console.log(`Found ${result.rows.length} enrolled courses.`);
    res.json(result.rows);

  } catch (err) {
    console.error("Get My Courses Error:", err);
    res.status(500).json({ message: "Error fetching enrolled courses" });
  }
});

// Lecturer Teaching Classes - LẤY DANH SÁCH LỚP GIẢNG VIÊN ĐANG DẠY
app.get('/api/lecturer-courses/:userId', async (req, res) => {
  const { userId } = req.params;
  console.log("Fetching teaching classes for Lecturer ID:", userId);

  try {
    const query = `
      SELECT 
        c.id,                          
        c.name,                        
        COALESCE(s.credits, 3) AS credits,     
        COALESCE(up."fullName", 'Unknown Lecturer') AS lecturer
      FROM class c
      LEFT JOIN subject s ON c."subjectId" = s.id       
      LEFT JOIN user_profile up ON c."lecturerId" = up.id 
      WHERE c."lecturerId" = $1
    `;

    const result = await client.query(query, [userId]);
    console.log(`Found ${result.rows.length} teaching classes.`);
    res.json(result.rows);

  } catch (err) {
    console.error("Get Lecturer Courses Error:", err);
    res.status(500).json({ message: "Error fetching teaching classes" });
  }
});

// Lecturer Subjects - LẤY DANH SÁCH MÔN HỌC GIẢNG VIÊN ĐANG DẠY
app.get('/api/lecturer-subjects/:lecturerId', async (req, res) => {
  const { lecturerId } = req.params;
  console.log("Fetching subjects for Lecturer ID:", lecturerId);

  try {
    const query = `
      SELECT DISTINCT s.id, s.name, COALESCE(s.credits, 3) AS credits
      FROM class c
      JOIN subject s ON c."subjectId" = s.id
      WHERE c."lecturerId" = $1
      ORDER BY s.name ASC
    `;
    const result = await client.query(query, [lecturerId]);
    console.log(`Found ${result.rows.length} subjects for lecturer.`);
    res.json(result.rows);
  } catch (err) {
    console.error("Get Lecturer Subjects Error:", err);
    res.status(500).json({ message: "Error fetching lecturer subjects" });
  }
});

// Lecturer Subject Classes - LẤY DANH SÁCH LỚP CỦA MÔN HỌC DO GIẢNG VIÊN DẠY
app.get('/api/lecturer-subject-classes/:lecturerId/:subjectId', async (req, res) => {
  const { lecturerId, subjectId } = req.params;
  console.log(`Fetching classes for Lecturer ${lecturerId}, Subject ${subjectId}`);

  try {
    const query = `
      SELECT c.id, c.name, COUNT(e."studentId")::int AS "studentCount"
      FROM class c
      LEFT JOIN enrollment e ON e."classId" = c.id
      WHERE c."lecturerId" = $1 AND c."subjectId" = $2
      GROUP BY c.id, c.name
      ORDER BY c.name ASC
    `;
    const result = await client.query(query, [lecturerId, subjectId]);
    console.log(`Found ${result.rows.length} classes.`);
    res.json(result.rows);
  } catch (err) {
    console.error("Get Lecturer Subject Classes Error:", err);
    res.status(500).json({ message: "Error fetching classes" });
  }
});

// Class Students - LẤY DANH SÁCH SINH VIÊN TRONG LỚP
app.get('/api/class-students/:classId', async (req, res) => {
  const { classId } = req.params;
  console.log("Fetching students for Class ID:", classId);

  try {
    const query = `
      SELECT up.id, up."fullName", up."studentCode"
      FROM enrollment e
      JOIN user_profile up ON e."studentId" = up.id
      WHERE e."classId" = $1
      ORDER BY up."fullName" ASC
    `;
    const result = await client.query(query, [classId]);
    console.log(`Found ${result.rows.length} students in class.`);
    res.json(result.rows);
  } catch (err) {
    console.error("Get Class Students Error:", err);
    res.status(500).json({ message: "Error fetching students" });
  }
});

// Schedule API (Lấy lịch học hoặc lịch dạy tùy theo Role)
app.get('/api/schedule/:userId', async (req, res) => {
  const { userId } = req.params;
  console.log("Fetching schedule for User ID:", userId);

  try {
    // 1. Kiểm tra role của user trước
    const roleQuery = 'SELECT role FROM "User" WHERE id = $1';
    const roleRes = await client.query(roleQuery, [userId]);

    if (roleRes.rows.length === 0) {
      return res.status(404).json({ message: "User not found" });
    }

    const role = roleRes.rows[0].role;
    let query = "";

    // 2. Chọn Query dựa trên Role
    // Sửa đổi: Dùng TO_CHAR để định dạng ngày tháng chuẩn ISO 8601 (có chữ T) để Android parse được.
    // Dùng LEFT JOIN cho room và user_profile để tránh mất dữ liệu nếu thông tin đó chưa có.
    if (role === 'LECTURER') {
      // Dành cho Giảng viên
      query = `
        SELECT 
          s.id AS "scheduleId",
          c.name AS "subjectName", 
          TO_CHAR(s."startTime", 'YYYY-MM-DD"T"HH24:MI:SS"Z"') AS "startTime", 
          TO_CHAR(s."endTime", 'YYYY-MM-DD"T"HH24:MI:SS"Z"') AS "endTime", 
          COALESCE(r.name, 'N/A') AS "roomName", 
          COALESCE(up."fullName", 'Lecturer') AS "lecturerName"
        FROM class c
        JOIN class_schedule s ON c.id = s."classId"
        LEFT JOIN room r ON s."roomId" = r.id
        LEFT JOIN user_profile up ON c."lecturerId" = up.id
        WHERE c."lecturerId" = $1
        ORDER BY s."startTime" ASC
      `;
    } else {
      // Dành cho Sinh viên
      query = `
        SELECT
          s.id AS "scheduleId",
          c.name AS "subjectName", 
          TO_CHAR(s."startTime", 'YYYY-MM-DD"T"HH24:MI:SS"Z"') AS "startTime", 
          TO_CHAR(s."endTime", 'YYYY-MM-DD"T"HH24:MI:SS"Z"') AS "endTime", 
          COALESCE(r.name, 'N/A') AS "roomName", 
          COALESCE(up."fullName", 'Lecturer') AS "lecturerName"
        FROM enrollment e
        JOIN class c ON e."classId" = c.id
        JOIN class_schedule s ON c.id = s."classId"
        LEFT JOIN room r ON s."roomId" = r.id
        LEFT JOIN user_profile up ON c."lecturerId" = up.id
        WHERE e."studentId" = $1
        ORDER BY s."startTime" ASC
      `;
    }

    const result = await client.query(query, [userId]);
    console.log(`Sent ${result.rows.length} schedule records to App.`);
    res.json(result.rows);

  } catch (err) {
    console.error("Get Schedule Error:", err);
    res.status(500).json({ message: "Error fetching schedule" });
  }
});

// Get Profile API
app.get('/api/profile/:userId', async (req, res) => {
  const { userId } = req.params;
  console.log("Fetching profile for User ID:", userId);

  try {
    // Truy vấn bảng user_profile (giả sử bảng này chứa studentCode và major)
    // Cần đảm bảo tên cột trong DB khớp với tên biến ở đây
    // Dùng dấu " " nếu tên cột trong Postgres có chữ hoa
    const query = `
      SELECT 
        id, 
        "fullName", 
        "studentCode", 
        major,
        department
      FROM user_profile 
      WHERE id = $1
    `;

    const result = await client.query(query, [userId]);

    if (result.rows.length === 0) {
      // Nếu không tìm thấy trong user_profile, thử tìm trong bảng User cơ bản
      const userQuery = 'SELECT id, "fullName", role FROM "User" WHERE id = $1';
      const userResult = await client.query(userQuery, [userId]);

      if (userResult.rows.length === 0) {
        return res.status(404).json({ message: "User not found" });
      }
      return res.json(userResult.rows[0]);
    }

    // Trả về kết quả - dùng id làm staffCode nếu studentCode là null (cho Lecturer)
    const profile = result.rows[0];
    if (!profile.studentCode) {
      profile.studentCode = profile.id;
    }
    res.json(profile);

  } catch (err) {
    console.error("Get Profile Error:", err);
    res.status(500).json({ message: "Error fetching profile" });
  }
});

// ==========================================
//             API XEM ĐIỂM (GRADES)
// ==========================================
app.get('/api/grades/:studentId', async (req, res) => {
  const { studentId } = req.params;
  console.log("Fetching grades for student:", studentId);

  try {
    const query = `
      SELECT 
        s.name AS "subjectName",
        gi.name AS "gradeItemName",
        gr.score,
        gi.weight
      FROM grade_records gr
      JOIN enrollment e ON gr."enrollmentId" = e.id
      JOIN grade_items gi ON gr."itemId" = gi."itemId"
      JOIN class c ON e."classId" = c.id
      JOIN subject s ON c."subjectId" = s.id
      WHERE e."studentId" = $1
      ORDER BY s.name, gi.name
    `;
    const result = await client.query(query, [studentId]);
    console.log(`Found ${result.rows.length} grade records.`);
    res.json(result.rows);
  } catch (err) {
    console.error("Get Grades Error:", err);
    res.status(500).json({ message: "Error fetching grades" });
  }
});

// ==========================================
//             API QUẢN LÝ TÀI LIỆU
// ==========================================

app.post('/api/document', async (req, res) => {
  const { courseName, title, url } = req.body; // Dữ liệu từ App gửi lên

  if (!courseName || !title || !url) {
    return res.status(400).json({ message: "Vui lòng cung cấp đầy đủ thông tin" });
  }

  try {
    // INSERT khớp với cấu trúc bảng thực tế trong ảnh của bạn
    // Map 'url' vào cột 'description', 'courseName' vào 'classid' (hoặc subjectid tùy logic của bạn)
    const query = `
      INSERT INTO document (
        classid, 
        uploadedby, 
        title, 
        description, 
        filedata, 
        filename, 
        mimetype, 
        filesize, 
        subjectid
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) 
      RETURNING *
    `;

    // Lưu ý: uploadedby và classid cần ID thực tế (VARCHAR). Ở đây tạm dùng courseName/Dummy.
    const values = [
      courseName,             // classid
      'lecturer_system',      // uploadedby (ID người dùng)
      title,                  // title
      url,                    // description (Lưu link tài liệu vào đây)
      Buffer.from(''),        // filedata (bytea không được để null theo ảnh)
      'link_document.txt',    // filename
      'text/plain',           // mimetype
      0,                      // filesize
      courseName              // subjectid
    ];

    const result = await client.query(query, values);
    console.log("Đã lưu vào Database Neon thành công!");
    res.status(201).json(result.rows[0]);

  } catch (err) {
    console.error("Lỗi Database:", err.message);
    res.status(500).json({ message: "Lỗi tương thích bảng: " + err.message });
  }
});
// ==========================================
//      FIXED ATTENDANCE MANAGEMENT API
// ==========================================

// Student Attendance History - XEM LỊCH SỬ ĐIỂM DANH CỦA TỪNG SINH VIÊN
// ⚠️ PHẢI đặt TRƯỚC route /api/attendance/:scheduleId để Express route đúng
app.get('/api/attendance', async (req, res) => {
  const { classId, studentId } = req.query;
  console.log(`📋 Fetching attendance history for student "${studentId}" in class "${classId}"`);

  if (!classId || !studentId) {
    return res.status(400).json({ message: "Missing classId or studentId" });
  }

  try {
    // Debug: Kiểm tra có schedule nào thuộc class này không
    const debugSchedules = await client.query(
      'SELECT id, "classId" FROM class_schedule WHERE "classId" = $1', [classId]
    );
    console.log(`🔍 Found ${debugSchedules.rows.length} schedules for class "${classId}"`);

    // Debug: Kiểm tra có attendance nào cho student này không
    const debugAttendance = await client.query(
      'SELECT schedule_id, student_id, status FROM attendances WHERE student_id = $1', [studentId]
    );
    console.log(`🔍 Found ${debugAttendance.rows.length} total attendance records for student "${studentId}"`);

    const query = `
      SELECT 
        a.status,
        TO_CHAR(a.check_in_time, 'YYYY-MM-DD"T"HH24:MI:SS"Z"') AS "checkInTime",
        TO_CHAR(cs."startTime"::date, 'YYYY-MM-DD') AS "scheduleDate",
        TO_CHAR(cs."startTime", 'HH24:MI') AS "startTime",
        TO_CHAR(cs."endTime", 'HH24:MI') AS "endTime"
      FROM attendances a
      JOIN class_schedule cs ON a.schedule_id = cs.id
      WHERE cs."classId" = $1 AND a.student_id = $2
      ORDER BY cs."startTime" ASC
    `;
    const result = await client.query(query, [classId, studentId]);
    console.log(`✅ Final result: ${result.rows.length} attendance records for student "${studentId}" in class "${classId}"`);
    res.json(result.rows);
  } catch (err) {
    console.error("Get Student Attendance Error:", err);
    res.status(500).json({ message: "Error fetching attendance history" });
  }
});

// Submit Attendance - LƯU ĐÚNG NGÀY HỌC
app.post('/api/attendance/submit', async (req, res) => {
  const { scheduleId, records } = req.body;
  console.log(`💾 Saving attendance for schedule: ${scheduleId}, Records: ${records.length}`);

  if (!scheduleId || !records || !Array.isArray(records)) {
    return res.status(400).json({ message: "Invalid request data" });
  }

  try {
    await client.query('BEGIN');

    // ✅ BƯỚC 1: Lấy startTime từ class_schedule
    const scheduleQuery = `
      SELECT "startTime" 
      FROM class_schedule 
      WHERE id = $1
    `;
    const scheduleResult = await client.query(scheduleQuery, [scheduleId]);

    if (scheduleResult.rows.length === 0) {
      await client.query('ROLLBACK');
      console.error(`❌ Schedule ${scheduleId} not found`);
      return res.status(404).json({ message: "Schedule not found" });
    }

    const scheduleStartTime = scheduleResult.rows[0].startTime;
    console.log(`📅 Schedule date: ${scheduleStartTime}`);

    // ✅ BƯỚC 2: Insert/Update với ĐÚNG NGÀY HỌC
    for (const record of records) {
      if (!record.studentId || !record.status) {
        console.warn('⚠️ Skipping invalid record:', record);
        continue;
      }

      const query = `
        INSERT INTO attendances (id, schedule_id, student_id, status, check_in_time)
        VALUES (gen_random_uuid(), $1, $2, $3, $4)
        ON CONFLICT (schedule_id, student_id) 
        DO UPDATE SET 
          status = EXCLUDED.status, 
          check_in_time = EXCLUDED.check_in_time
      `;

      // ✅ SỬ DỤNG scheduleStartTime (ngày học) thay vì CURRENT_TIMESTAMP
      const values = [scheduleId, record.studentId, record.status, scheduleStartTime];
      await client.query(query, values);
    }

    await client.query('COMMIT');
    console.log(`✅ Saved ${records.length} records with date: ${scheduleStartTime}`);
    res.json({ message: "Attendance saved successfully!" });

  } catch (err) {
    await client.query('ROLLBACK');
    console.error("❌ Save attendance error:", err.message);
    res.status(500).json({ message: "Database error: " + err.message });
  }
});

// Add Student to Attendance - LƯU ĐÚNG NGÀY HỌC
app.post('/api/attendance/add-student', async (req, res) => {
  const { scheduleId, studentId } = req.body;
  console.log(`➕ Adding student ${studentId} to schedule ${scheduleId}`);

  if (!scheduleId || !studentId) {
    return res.status(400).json({ message: "Missing scheduleId or studentId" });
  }

  try {
    // ✅ BƯỚC 1: Lấy startTime từ class_schedule
    const scheduleQuery = `
      SELECT "startTime" 
      FROM class_schedule 
      WHERE id = $1
    `;
    const scheduleResult = await client.query(scheduleQuery, [scheduleId]);

    if (scheduleResult.rows.length === 0) {
      console.error(`❌ Schedule ${scheduleId} not found`);
      return res.status(404).json({ message: "Schedule not found" });
    }

    const scheduleStartTime = scheduleResult.rows[0].startTime;
    console.log(`📅 Using schedule date: ${scheduleStartTime}`);

    // Check if student exists
    const studentCheck = `SELECT id, "fullName" FROM user_profile WHERE id = $1`;
    const studentResult = await client.query(studentCheck, [studentId]);

    if (studentResult.rows.length === 0) {
      console.error(`❌ Student ${studentId} not found`);
      return res.status(404).json({ message: "Student not found" });
    }

    // Check if already in attendance
    const checkQuery = `
      SELECT id FROM attendances 
      WHERE schedule_id = $1 AND student_id = $2
    `;
    const checkResult = await client.query(checkQuery, [scheduleId, studentId]);

    if (checkResult.rows.length > 0) {
      console.warn(`⚠️ Student already in attendance`);
      return res.status(400).json({ message: "Student already in attendance list" });
    }

    // ✅ Insert với ĐÚNG NGÀY HỌC
    const insertQuery = `
      INSERT INTO attendances (id, schedule_id, student_id, status, check_in_time)
      VALUES (gen_random_uuid(), $1, $2, 'Present', $3)
      RETURNING id
    `;
    const result = await client.query(insertQuery, [scheduleId, studentId, scheduleStartTime]);

    console.log(`✅ Added student with date: ${scheduleStartTime}`);
    res.json({
      message: "Student added successfully",
      attendanceId: result.rows[0].id
    });

  } catch (err) {
    console.error("❌ Add student error:", err.message);
    res.status(500).json({ message: "Database error: " + err.message });
  }
});
// Get Attendance Records - TỰ ĐỘNG TẠO từ enrollment nếu chưa có
app.get('/api/attendance/:scheduleId', async (req, res) => {
  const { scheduleId } = req.params;
  console.log(`📋 Fetching attendance for schedule: ${scheduleId}`);

  try {
    // ✅ BƯỚC 1: Kiểm tra schedule tồn tại và lấy thông tin
    const scheduleCheck = `
      SELECT cs.id, cs."classId", cs."startTime"
      FROM class_schedule cs
      WHERE cs.id = $1
    `;
    const scheduleResult = await client.query(scheduleCheck, [scheduleId]);

    if (scheduleResult.rows.length === 0) {
      console.error(`❌ Schedule ${scheduleId} not found in class_schedule`);
      return res.status(404).json({ message: "Schedule not found" });
    }

    const classId = scheduleResult.rows[0].classId;
    const startTime = scheduleResult.rows[0].startTime;
    console.log(`📚 Class ID: ${classId}, Start Time: ${startTime}`);

    // ✅ BƯỚC 2: Lấy attendance records hiện có
    const attendanceQuery = `
      SELECT 
        a.id as "attendanceId",
        up.id as "studentId",
        up."fullName", 
        up."studentCode", 
        a.status
      FROM attendances a
      JOIN user_profile up ON a.student_id = up.id
      WHERE a.schedule_id = $1
      ORDER BY up."fullName" ASC
    `;
    const attendanceResult = await client.query(attendanceQuery, [scheduleId]);

    // ✅ BƯỚC 3: Nếu đã có records, trả về luôn
    if (attendanceResult.rows.length > 0) {
      console.log(`✅ Found ${attendanceResult.rows.length} existing attendance records`);
      return res.json(attendanceResult.rows);
    }

    // ✅ BƯỚC 4: Nếu CHƯA có, tự động tạo từ enrollment
    console.log(`📝 No attendance records found, auto-creating from enrollment...`);

    const enrollmentQuery = `
      SELECT 
        up.id as "studentId",
        up."fullName",
        up."studentCode"
      FROM enrollment e
      JOIN user_profile up ON e."studentId" = up.id
      WHERE e."classId" = $1
      ORDER BY up."fullName" ASC
    `;
    const enrollmentResult = await client.query(enrollmentQuery, [classId]);

    if (enrollmentResult.rows.length === 0) {
      console.log(`⚠️ No students enrolled in this class yet`);
      return res.json([]); // Trả về mảng rỗng
    }

    // ✅ BƯỚC 5: Tạo attendance records cho tất cả sinh viên đã đăng ký
    await client.query('BEGIN');

    for (const student of enrollmentResult.rows) {
      const insertQuery = `
        INSERT INTO attendances (id, schedule_id, student_id, status, check_in_time)
        VALUES (gen_random_uuid(), $1, $2, 'Present', $3)
        ON CONFLICT (schedule_id, student_id) DO NOTHING
      `;
      await client.query(insertQuery, [scheduleId, student.studentId, startTime]);
    }

    await client.query('COMMIT');
    console.log(`✅ Auto-created ${enrollmentResult.rows.length} attendance records`);

    // ✅ BƯỚC 6: Lấy lại data vừa tạo để trả về
    const newRecordsResult = await client.query(attendanceQuery, [scheduleId]);
    console.log(`📤 Returning ${newRecordsResult.rows.length} records to app`);
    res.json(newRecordsResult.rows);

  } catch (err) {
    await client.query('ROLLBACK');
    console.error("❌ Get attendance error:", err.message);
    console.error("Full error:", err);
    res.status(500).json({ message: "Error fetching attendance: " + err.message });
  }
});

// Remove Students from Attendance (GIỮ NGUYÊN)
app.post('/api/attendance/remove-students', async (req, res) => {
  const { scheduleId, studentIds } = req.body;
  console.log(`🗑️ Removing ${studentIds?.length || 0} students from schedule ${scheduleId}`);

  if (!scheduleId || !studentIds || !Array.isArray(studentIds)) {
    return res.status(400).json({ message: "Invalid request data" });
  }

  if (studentIds.length === 0) {
    return res.json({ message: "No students to remove", deletedCount: 0 });
  }

  try {
    await client.query('BEGIN');

    const deleteQuery = `
      DELETE FROM attendances 
      WHERE schedule_id = $1 AND student_id = ANY($2)
      RETURNING student_id
    `;
    const result = await client.query(deleteQuery, [scheduleId, studentIds]);

    await client.query('COMMIT');

    console.log(`✅ Successfully removed ${result.rowCount} attendance records`);
    res.json({
      message: "Students removed successfully",
      deletedCount: result.rowCount
    });

  } catch (err) {
    await client.query('ROLLBACK');
    console.error("❌ Delete attendance error:", err.message);
    res.status(500).json({ message: "Database error: " + err.message });
  }
});


// Search Students (GIỮ NGUYÊN)
app.get('/api/students/search', async (req, res) => {
  const { name } = req.query;
  console.log(`🔍 Searching for students: "${name}"`);

  if (!name || name.trim().length < 2) {
    return res.json([]);
  }

  try {
    const query = `
      SELECT id, "fullName", "studentCode" 
      FROM user_profile 
      WHERE "fullName" ILIKE $1 OR "studentCode" ILIKE $1
      ORDER BY "fullName" ASC
      LIMIT 10
    `;
    const result = await client.query(query, [`%${name}%`]);
    console.log(`✅ Found ${result.rows.length} students`);
    res.json(result.rows);
  } catch (err) {
    console.error("❌ Search Error:", err);
    res.status(500).json({ message: err.message });
  }
});
// --- Start Server ---
const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running at http://0.0.0.0:${PORT}`);
});