package com.maktab.app.data

data class Student(val id: Int, val name: String, val initials: String, val score: Int)
data class SubjectItem(val title: String, val type: String)
data class Subject(val id: Int, val name: String, val lessons: Int, val tests: Int, val quizzes: Int, val items: List<SubjectItem>)
data class HomeworkTask(val id: Int, val studentName: String, val initials: String, val subject: String, val task: String, val submittedTime: String, val fileName: String)
data class ExamTask(val id: Int, val studentName: String, val initials: String, val subject: String, val title: String, val date: String, val questionCount: Int)
data class Survey(val id: Int, val title: String, val question: String, val options: List<String>, val answered: Int, val total: Int, val deadline: String)
data class GradeRecord(val subject: String, val scores: List<Int>, val avg: Int)
data class HomeworkStatus(val id: Int, val subject: String, val task: String, val deadline: String, val isDone: Boolean)
data class BehaviorRecord(val date: String, val type: String, val title: String, val description: String, val teacher: String)
data class CalendarDay(val day: Int, val status: String)
data class Lesson(val period: Int, val time: String, val subject: String, val className: String, val teacher: String, val room: String)
data class DaySchedule(val dayIndex: Int, val lessons: List<Lesson>)

// ── JURNAL ────────────────────────────────────────────────────────────────
data class JournalSession(val id: Int, val date: String, val shortDate: String, val type: String, val lessonNum: Int)
data class JournalEntry(val studentId: Int, val sessionId: Int, val grade: Int?, val attendance: String, val homework: String, val behavior: Int?)

// ── DARS KONTENTI ─────────────────────────────────────────────────────────
data class LessonContent(val id: Int, val title: String, val type: String, val status: String, val className: String, val subject: String, val journalDate: String, val materials: Int, val blocks: Int)

// ── SINFLARIM ────────────────────────────────────────────────────────────
data class SchoolClass(val id: Int, val name: String, val code: String, val year: String, val studentCount: Int)
data class ClassStudent(val id: Int, val classId: Int, val name: String, val studentId: String)

// ── BAHOLASH ─────────────────────────────────────────────────────────────
data class HomeworkGroup(val id: Int, val title: String, val subject: String, val date: String, val type: String, val totalStudents: Int, val submitted: Int, val graded: Int, val submissions: List<HomeworkSubmission>)
data class HomeworkSubmission(val studentName: String, val status: String) // "submitted", "not_submitted", "graded"

object MockData {
    val students = listOf(
        Student(1,"Asilbek Karimov","AK",94), Student(2,"Nodira Hamidova","NH",91),
        Student(3,"Dilnoza Yusupova","DY",88), Student(4,"Zulfiya Nazarova","ZN",82),
        Student(5,"Jasur Toshmatov","JT",76), Student(6,"Sanjar Mirzayev","SM",65),
    )
    val myChild = students[0]
    val weekDays = listOf("Du","Se","Ch","Pa","Ju")
    val initialAttendance: Map<Int,List<Boolean>> = students.associate { s -> s.id to listOf(true,true,true,s.id!=5,true) }
    val subjects = listOf(
        Subject(1,"Matematika",12,3,5,listOf(SubjectItem("§1. Kasrlar","Dars"),SubjectItem("§2. Ko'paytmalar","Dars"),SubjectItem("1-bob testi","Test"),SubjectItem("Tengsizliklar quiz","Quiz"),SubjectItem("Oraliq nazorat","Imtixon"))),
        Subject(2,"Ingliz tili",10,2,8,listOf(SubjectItem("Present Simple","Dars"),SubjectItem("Unit 5 Quiz","Quiz"),SubjectItem("Past Continuous","Dars"),SubjectItem("Grammar nazorat","Test"))),
        Subject(3,"Fizika",8,4,3,listOf(SubjectItem("Mexanika asoslari","Dars"),SubjectItem("Newton qonunlari","Dars"),SubjectItem("1-bob testi","Test"))),
    )
    val homeworkTasks = listOf(
        HomeworkTask(1,"Asilbek Karimov","AK","Matematika","§12. Kasrlar (1-15 mashq)","Bugun 09:15","vazifa_12.pdf"),
        HomeworkTask(2,"Dilnoza Yusupova","DY","Matematika","§12. Kasrlar (1-15 mashq)","Bugun 08:42","dilnoza.jpg"),
        HomeworkTask(3,"Jasur Toshmatov","JT","Ingliz tili","Unit 5 Ex. B","Kecha 22:10","jasur_eng.pdf"),
        HomeworkTask(4,"Nodira Hamidova","NH","Matematika","§12. Kasrlar (1-15 mashq)","Kecha 20:30","nodira.pdf"),
    )
    val hwFeedbacks = mapOf(
        1 to Pair(92,"A'lo! 14/15 mashq to'g'ri. 9-masalada kichik xato."),
        2 to Pair(78,"Yaxshi! 5,11,13-mashqlarda xatolar. Kasrlarni soddalashtirish qoidasini takrorlang."),
        3 to Pair(88,"Good work! Grammar correct. Vocabulary could be richer in sentences 4 and 7."),
        4 to Pair(96,"Mukammal ish! Barcha mashqlar to'g'ri va tartibli bajarilgan.")
    )
    val examTasks = listOf(
        ExamTask(1,"Asilbek Karimov","AK","Matematika","1-chorak yakuniy imtihon","18 May",20),
        ExamTask(2,"Nodira Hamidova","NH","Matematika","1-chorak yakuniy imtihon","18 May",20),
        ExamTask(3,"Dilnoza Yusupova","DY","Ingliz tili","Grammar nazorat ishi","17 May",30),
        ExamTask(4,"Jasur Toshmatov","JT","Ingliz tili","Grammar nazorat ishi","17 May",30),
    )
    val examFeedbacks = mapOf(
        1 to Triple(87,17,"Algebra bo'limida kuchli. Geometriyada 3 xato. Daraja: yaxshi."),
        2 to Triple(95,19,"Ajoyib natija! Faqat 1 hisoblash xatosi. Daraja: a'lo."),
        3 to Triple(73,22,"Tense ishlatishda qiynalyapti. Articles va prepositions mustahkamlanishi kerak."),
        4 to Triple(80,24,"Yaxshi natija. Irregular verbs bo'limida kichik xatolar.")
    )
    val teacherSurveys = listOf(
        Survey(1,"O'qituvchilar ish sharoiti","Maktabdagi ish sharoitidan qanchalik mamnunsiz?",listOf("Juda mamnun","Mamnun","O'rtacha","Mamnun emas"),18,25,"25 May"),
        Survey(2,"Darslik sifati baholash","Joriy darsliklar sifatini baholang",listOf("A'lo","Yaxshi","Qoniqarli","Yomon"),22,25,"30 May"),
    )
    val grades = listOf(
        GradeRecord("Matematika",listOf(92,88,95,90),91),GradeRecord("Ingliz tili",listOf(85,90,92,88),89),
        GradeRecord("Fizika",listOf(78,82,80,85),81),GradeRecord("Biologiya",listOf(95,93,97,92),94),
    )
    val childHomework = listOf(
        HomeworkStatus(1,"Matematika","§12. Kasrlar (1-15 mashq)","Bugun, 23:59",true),
        HomeworkStatus(2,"Ingliz tili","Unit 5 Exercise B — Grammar","Ertaga, 08:00",false),
        HomeworkStatus(3,"Fizika","§8. Newton qonunlari 1-5 masala","22 May",false),
    )
    val behaviorRecords = listOf(
        BehaviorRecord("19 May","positive","Darsda faol ishtirok","Matematika darsida barcha savollarga javob berdi","Karimova N."),
        BehaviorRecord("16 May","neutral","Kechikish","2-darsga 5 daqiqa kechikib keldi","Tursunov B."),
        BehaviorRecord("14 May","positive","Olimpiada g'olibi","Maktab olimpiadasida 1-o'rinni egalladi","Yo'ldosheva M."),
        BehaviorRecord("10 May","negative","Uy vazifasi bajarilmagan","Ingliz tili uy vazifasini 2 marta bajarmasdi","Alimova F."),
    )
    val parentSurveys = listOf(
        Survey(1,"Maktab ovqatlanish sifati","Oshxona ovqatidan qanchalik mamnunsiz?",listOf("Juda yaxshi","Yaxshi","O'rtacha","Yomon"),45,60,"25 May"),
        Survey(2,"O'qituvchi muloqoti","O'qituvchilar ota-onalar bilan muloqoti qanday?",listOf("A'lo","Yaxshi","O'rtacha","Yaxshilanishi kerak"),52,60,"30 May"),
    )
    val calendarDays = listOf(
        CalendarDay(1,"present"),CalendarDay(2,"present"),CalendarDay(3,"present"),CalendarDay(4,"present"),CalendarDay(5,"absent"),
        CalendarDay(6,"weekend"),CalendarDay(7,"weekend"),CalendarDay(8,"present"),CalendarDay(9,"present"),CalendarDay(10,"present"),
        CalendarDay(11,"present"),CalendarDay(12,"present"),CalendarDay(13,"weekend"),CalendarDay(14,"weekend"),CalendarDay(15,"present"),
        CalendarDay(16,"present"),CalendarDay(17,"present"),CalendarDay(18,"absent"),CalendarDay(19,"today"),
    )
    val teacherSchedule = listOf(
        DaySchedule(0,listOf(Lesson(1,"08:00–08:45","Matematika","5-A sinf","","201-xona"),Lesson(2,"09:00–09:45","Matematika","6-B sinf","","201-xona"),Lesson(4,"11:00–11:45","Matematika","7-A sinf","","201-xona"),Lesson(5,"12:00–12:45","Matematika","9-B sinf","","201-xona"))),
        DaySchedule(1,listOf(Lesson(1,"08:00–08:45","Matematika","5-B sinf","","201-xona"),Lesson(3,"10:00–10:45","Matematika","8-A sinf","","201-xona"),Lesson(5,"12:00–12:45","Matematika","6-A sinf","","201-xona"))),
        DaySchedule(2,listOf(Lesson(1,"08:00–08:45","Matematika","5-A sinf","","201-xona"),Lesson(2,"09:00–09:45","Matematika","6-A sinf","","201-xona"),Lesson(4,"11:00–11:45","Matematika","7-B sinf","","201-xona"))),
        DaySchedule(3,listOf(Lesson(2,"09:00–09:45","Matematika","5-A sinf","","201-xona"),Lesson(3,"10:00–10:45","Matematika","6-B sinf","","201-xona"),Lesson(5,"12:00–12:45","Matematika","8-B sinf","","201-xona"),Lesson(6,"13:00–13:45","Matematika","9-A sinf","","201-xona"))),
        DaySchedule(4,listOf(Lesson(1,"08:00–08:45","Matematika","5-A sinf","","201-xona"),Lesson(3,"10:00–10:45","Matematika","7-A sinf","","201-xona"),Lesson(4,"11:00–11:45","Matematika","8-A sinf","","201-xona"))),
    )
    val studentSchedule = listOf(
        DaySchedule(0,listOf(Lesson(1,"08:00–08:45","Matematika","","Karimova N.","201-xona"),Lesson(2,"09:00–09:45","Ingliz tili","","Alimova F.","305-xona"),Lesson(3,"10:00–10:45","Fizika","","Tursunov B.","115-xona"),Lesson(4,"11:00–11:45","Biologiya","","Yo'ldosheva M.","302-xona"),Lesson(5,"12:00–12:45","Geografiya","","Nazarov A.","210-xona"))),
        DaySchedule(1,listOf(Lesson(1,"08:00–08:45","Matematika","","Karimova N.","201-xona"),Lesson(2,"09:00–09:45","O'zbek tili","","Rashidova G.","104-xona"),Lesson(3,"10:00–10:45","Tarix","","Mirzayev S.","208-xona"),Lesson(4,"11:00–11:45","Ingliz tili","","Alimova F.","305-xona"),Lesson(5,"12:00–12:45","Informatika","","Yusupov R.","105-xona"))),
        DaySchedule(2,listOf(Lesson(1,"08:00–08:45","Fizika","","Tursunov B.","115-xona"),Lesson(2,"09:00–09:45","Matematika","","Karimova N.","201-xona"),Lesson(3,"10:00–10:45","Biologiya","","Yo'ldosheva M.","302-xona"),Lesson(4,"11:00–11:45","O'zbek tili","","Rashidova G.","104-xona"),Lesson(5,"12:00–12:45","Informatika","","Yusupov R.","105-xona"))),
        DaySchedule(3,listOf(Lesson(1,"08:00–08:45","Tarix","","Mirzayev S.","208-xona"),Lesson(2,"09:00–09:45","Matematika","","Karimova N.","201-xona"),Lesson(3,"10:00–10:45","Ingliz tili","","Alimova F.","305-xona"),Lesson(4,"11:00–11:45","Geografiya","","Nazarov A.","210-xona"),Lesson(5,"12:00–12:45","Fizika","","Tursunov B.","115-xona"))),
        DaySchedule(4,listOf(Lesson(1,"08:00–08:45","Matematika","","Karimova N.","201-xona"),Lesson(2,"09:00–09:45","Fizika","","Tursunov B.","115-xona"),Lesson(3,"10:00–10:45","O'zbek tili","","Rashidova G.","104-xona"),Lesson(4,"11:00–11:45","Biologiya","","Yo'ldosheva M.","302-xona"),Lesson(5,"12:00–12:45","Ingliz tili","","Alimova F.","305-xona"))),
    )

    // ─── JURNAL mock data ───────────────────────────────────────────────────
    val journalSessions = listOf(
        JournalSession(1,"20.05.2026","20.05","Dars",1),
        JournalSession(2,"21.05.2026","21.05","Dars",2),
        JournalSession(3,"22.05.2026","22.05","Test",3),
        JournalSession(4,"23.05.2026","23.05","Dars",4),
        JournalSession(5,"26.05.2026","26.05","Quiz",5),
    )
    val journalEntries = listOf(
        JournalEntry(1,1,5,"present","done",5), JournalEntry(1,2,4,"present","done",5), JournalEntry(1,3,5,"present","done",5), JournalEntry(1,4,4,"present","not_done",4), JournalEntry(1,5,null,"present","-",null),
        JournalEntry(2,1,4,"present","done",4), JournalEntry(2,2,5,"present","done",5), JournalEntry(2,3,4,"present","done",4), JournalEntry(2,4,3,"absent","not_done",4), JournalEntry(2,5,null,"present","-",null),
        JournalEntry(3,1,3,"present","done",4), JournalEntry(3,2,4,"present","done",4), JournalEntry(3,3,3,"present","not_done",4), JournalEntry(3,4,4,"present","done",3), JournalEntry(3,5,null,"present","-",null),
        JournalEntry(4,1,4,"present","done",4), JournalEntry(4,2,3,"present","done",3), JournalEntry(4,3,4,"absent","not_done",3), JournalEntry(4,4,3,"present","done",4), JournalEntry(4,5,null,"present","-",null),
        JournalEntry(5,1,2,"present","not_done",3), JournalEntry(5,2,3,"absent","not_done",3), JournalEntry(5,3,2,"present","not_done",3), JournalEntry(5,4,3,"present","done",3), JournalEntry(5,5,null,"absent","-",null),
        JournalEntry(6,1,1,"present","not_done",3), JournalEntry(6,2,2,"present","not_done",2), JournalEntry(6,3,2,"absent","not_done",2), JournalEntry(6,4,2,"present","not_done",3), JournalEntry(6,5,null,"present","-",null),
    )
    fun getEntry(studentId: Int, sessionId: Int) = journalEntries.find { it.studentId == studentId && it.sessionId == sessionId }

    // ─── DARS KONTENTI ──────────────────────────────────────────────────────
    val lessonContents = listOf(
        LessonContent(1,"1-dars","Dars","Bog'langan","2-A","Alifbe","5/21/2026 · 09:45 AM · 2-dars",0,3),
        LessonContent(2,"Browser test dars","Test","Bog'langan","2-A","Alifbe","5/22/2026 · 09:45 AM · 1-dars",2,2),
        LessonContent(3,"Harflar va tovushlar","Dars","Bog'langan","2-A","Alifbe","5/23/2026 · 09:45 AM · 1-dars",1,4),
        LessonContent(4,"So'z tuzilishi","Dars","Belgilanmagan","5-A","Matematika","-",0,0),
    )

    // ─── SINFLARIM ──────────────────────────────────────────────────────────
    val schoolClasses = listOf(
        SchoolClass(1,"2-A","2-A","2025/2026",6),
        SchoolClass(2,"5-A","5-A","2025/2026",6),
        SchoolClass(3,"BILIMDON","BILIMDON","2025/2026",4),
    )
    val classStudents = listOf(
        ClassStudent(1,1,"Aliqulov Abdujabbor Alisherovich","2026-000137"),
        ClassStudent(2,1,"Azamova Jasmina Jahongirovna","2026-000140"),
        ClassStudent(3,1,"Ergashev Biloliddin Abdubakir o'g'li","2026-000023"),
        ClassStudent(4,1,"Juraqulova Zuxra Shaxobidinova","2026-000134"),
        ClassStudent(5,1,"Karimjonov Nodirbek Xusanovich","2026-000139"),
        ClassStudent(6,1,"Qaxxorov Abduvali Abdumalikovich","2026-000145"),
        ClassStudent(7,2,"Asilbek Karimov","2026-000001"),
        ClassStudent(8,2,"Nodira Hamidova","2026-000002"),
        ClassStudent(9,2,"Dilnoza Yusupova","2026-000003"),
        ClassStudent(10,3,"Jasur Toshmatov","2026-000004"),
        ClassStudent(11,3,"Sanjar Mirzayev","2026-000005"),
    )

    // ─── BAHOLASH ────────────────────────────────────────────────────────────
    val homeworkGroups = listOf(
        HomeworkGroup(1,"Uyga vazifa: Hikoya davomi","Alifbe","23.05.2026","Faqat online",6,0,0,
            listOf(HomeworkSubmission("Aliqulov Abdujabbor","not_submitted"),HomeworkSubmission("Azamova Jasmina","not_submitted"),HomeworkSubmission("Ergashev Biloliddin","not_submitted"),HomeworkSubmission("Juraqulova Zuxra","not_submitted"),HomeworkSubmission("Karimjonov Nodirbek","not_submitted"),HomeworkSubmission("Qaxxorov Abduvali","not_submitted"))),
        HomeworkGroup(2,"Mashq bajarish","Alifbe","22.05.2026","Faqat online",13,7,7,
            listOf(HomeworkSubmission("Aliqulov Abdujabbor","submitted"),HomeworkSubmission("Azamova Jasmina","submitted"),HomeworkSubmission("Ergashev Biloliddin","not_submitted"),HomeworkSubmission("Juraqulova Zuxra","submitted"),HomeworkSubmission("Karimjonov Nodirbek","submitted"),HomeworkSubmission("Qaxxorov Abduvali","not_submitted"))),
        HomeworkGroup(3,"Uyga vazifa","Alifbe","21.05.2026","Faqat online",6,0,0,
            listOf(HomeworkSubmission("Aliqulov Abdujabbor","submitted"),HomeworkSubmission("Azamova Jasmina","not_submitted"),HomeworkSubmission("Ergashev Biloliddin","submitted"),HomeworkSubmission("Juraqulova Zuxra","not_submitted"),HomeworkSubmission("Karimjonov Nodirbek","submitted"),HomeworkSubmission("Qaxxorov Abduvali","not_submitted"))),
    )
}
