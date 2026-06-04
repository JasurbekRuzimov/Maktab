package com.maktab.app.network.repositories

import com.maktab.app.network.*
import com.maktab.app.network.models.JournalCellRequest

class TeacherRepository {
    private val api = RetrofitClient.teacherService

    // Haftalik jadval — api/teacher/scheduled-lessons/board
    suspend fun getSchedule(fromDate: String, toDate: String) = safeApiCall {
        api.getLessonsBoard(fromDate = fromDate, toDate = toDate)
    }

    // Jurnal options — api/teacher/journal/options
    suspend fun getJournalOptions() = safeApiCall {
        api.getJournalOptions()
    }

    // Jurnal — api/teacher/journal
    suspend fun getJournal(
        classId: String,
        quarterId: String,
        subjectId: String,
        weekStart: String? = null
    ) = safeApiCall {
        api.getJournal(classId, quarterId, subjectId, weekStart)
    }

    // Jurnal katakchasi yangilash — api/teacher/journal/cells
    suspend fun updateJournalCell(request: JournalCellRequest) = safeApiCall {
        api.updateJournalCell(request)
    }

    // Davomat — api/teacher/attendance/lessons
    suspend fun getAttendance(
        classId: String? = null,
        from: String? = null,
        to: String? = null
    ) = safeApiCall {
        api.getAttendanceLessons(classId = classId, from = from, to = to)
    }

    // Sinflar — api/teacher/classes
    suspend fun getClasses() = safeApiCall {
        api.getClasses()
    }

    // Uy vazifalari — api/teacher/homework
    suspend fun getHomework(
        classId: String? = null,
        subjectId: String? = null
    ) = safeApiCall {
        api.getHomework(classId = classId, subjectId = subjectId)
    }

    // Baholar — api/teacher/grade-records
    suspend fun getGradeRecords(studentId: String? = null) = safeApiCall {
        api.getGradeRecords(studentId = studentId)
    }

    // Xulq — api/teacher/behavior-cases
    suspend fun getBehaviorCases(classId: String? = null) = safeApiCall {
        api.getBehaviorCases(classId = classId)
    }

    // Fanlar — api/teacher/subjects
    suspend fun getSubjects() = safeApiCall {
        api.getSubjects()
    }

    // Choraklar — api/teacher/academic-quarters
    suspend fun getAcademicQuarters(academicYearId: String? = null) = safeApiCall {
        api.getAcademicQuarters(academicYearId = academicYearId)
    }
}