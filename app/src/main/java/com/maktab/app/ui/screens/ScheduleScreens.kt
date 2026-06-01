package com.maktab.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.data.Lesson
import com.maktab.app.data.MockData
import com.maktab.app.ui.theme.*

private const val TODAY_IDX = 1
private val DAY_FULL  = listOf("Dushanba","Seshanba","Chorshanba","Payshanba","Juma")
private val DAY_SHORT = listOf("Du","Se","Ch","Pa","Ju")
private val DAY_DATE  = listOf("18","19","20","21","22")

fun subjectColor(subject: String): Pair<Color, Color> = when {
    subject.startsWith("Matematika")  -> Pair(Blue10,   BlueContainer)
    subject.startsWith("Ingliz")      -> Pair(Purple10,  PurpleContainer)
    subject.startsWith("Fizika")      -> Pair(Amber10,   AmberContainer)
    subject.startsWith("Biologiya")   -> Pair(Green10,   GreenContainer)
    subject.startsWith("O'zbek")      -> Pair(Teal10,    TealContainer)
    subject.startsWith("Tarix")       -> Pair(Red10,     RedContainer)
    subject.startsWith("Geografiya")  -> Pair(Amber10,   AmberContainer)
    subject.startsWith("Informatika") -> Pair(Purple10,  PurpleContainer)
    else -> Pair(Blue10, BlueContainer)
}

@Composable
private fun DaySelectorRow(selected: Int, onSelect: (Int) -> Unit, accent: Color) {
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=10.dp), horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        DAY_SHORT.forEachIndexed { i, short ->
            val isSel=i==selected; val isToday=i==TODAY_IDX
            Surface(onClick={onSelect(i)},modifier=Modifier.weight(1f),shape=RoundedCornerShape(10.dp),
                color=when{isSel->accent;isToday->accent.copy(0.12f);else->MaterialTheme.colorScheme.surfaceVariant},
                border=if(isToday&&!isSel)BorderStroke(1.dp,accent.copy(0.45f)) else null) {
                Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(vertical=8.dp)) {
                    Text(short,fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=when{isSel->Color.White;isToday->accent;else->MaterialTheme.colorScheme.onSurfaceVariant})
                    Spacer(Modifier.height(2.dp))
                    Text(DAY_DATE[i],fontSize=10.sp,color=when{isSel->Color.White.copy(0.85f);isToday->accent;else->MaterialTheme.colorScheme.onSurfaceVariant})
                    if(isToday){Spacer(Modifier.height(3.dp));Box(Modifier.size(4.dp).clip(CircleShape).background(if(isSel)Color.White else accent))}
                }
            }
        }
    }
}

@Composable
fun TeacherScheduleScreen() {
    var sel by remember{mutableStateOf(TODAY_IDX)}
    val lessons=MockData.teacherSchedule.find{it.dayIndex==sel}?.lessons?: emptyList()
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding=PaddingValues(bottom=24.dp)) {
        item{Row(Modifier.fillMaxWidth().padding(16.dp,14.dp,16.dp,4.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){WeekStatBox("Haftalik darslar","${MockData.teacherSchedule.sumOf{it.lessons.size}} ta",Teal10,TealContainer,Modifier.weight(1f));WeekStatBox("Bugungi darslar","${MockData.teacherSchedule.find{it.dayIndex==TODAY_IDX}?.lessons?.size?:0} ta",Blue10,BlueContainer,Modifier.weight(1f))}}
        item{Card(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=4.dp),colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp),border=BorderStroke(0.5.dp,Outline),elevation=CardDefaults.cardElevation(0.dp)){DaySelectorRow(sel,{sel=it},Teal10)}}
        item{Row(Modifier.fillMaxWidth().padding(start=16.dp,end=16.dp,top=12.dp,bottom=6.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text(DAY_FULL[sel],fontSize=17.sp,fontWeight=FontWeight.SemiBold);Text("${DAY_DATE[sel]} May 2026${if(sel==TODAY_IDX)" · Bugun" else ""}",fontSize=12.sp,color=if(sel==TODAY_IDX)Teal10 else MaterialTheme.colorScheme.onSurfaceVariant)};Box(Modifier.clip(RoundedCornerShape(8.dp)).background(TealContainer).padding(horizontal=12.dp,vertical=5.dp)){Text("${lessons.size} dars",fontSize=12.sp,fontWeight=FontWeight.Medium,color=Teal10)}}}
        if(lessons.isEmpty()){item{EmptyDay()}}
        else{itemsIndexed(lessons){_,lesson->val isActive=sel==TODAY_IDX&&lesson.period==2;val(color,container)=subjectColor(lesson.subject);LessonCard(lesson,isActive,color,container,isTeacher=true)}}
    }
}

@Composable
fun StudentScheduleScreen() {
    var sel by remember{mutableStateOf(TODAY_IDX)}
    val lessons=MockData.studentSchedule.find{it.dayIndex==sel}?.lessons?: emptyList()
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),contentPadding=PaddingValues(bottom=24.dp)){
        item{Card(Modifier.fillMaxWidth().padding(16.dp,14.dp,16.dp,4.dp),colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp),border=BorderStroke(0.5.dp,Outline),elevation=CardDefaults.cardElevation(0.dp)){Column{Row(Modifier.padding(14.dp,14.dp,14.dp,4.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).clip(CircleShape).background(BlueContainer),contentAlignment=Alignment.Center){Text("AK",fontSize=14.sp,fontWeight=FontWeight.Medium,color=Blue10)};Column{Text("Asilbek Karimov",fontSize=14.sp,fontWeight=FontWeight.SemiBold);Text("5-A sinf",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Spacer(Modifier.weight(1f));Box(Modifier.clip(RoundedCornerShape(8.dp)).background(BlueContainer).padding(horizontal=10.dp,vertical=4.dp)){Text("Bugun ${MockData.studentSchedule.find{it.dayIndex==TODAY_IDX}?.lessons?.size?:0} dars",fontSize=11.sp,color=Blue10,fontWeight=FontWeight.Medium)}};DaySelectorRow(sel,{sel=it},Blue10)}}}
        item{Row(Modifier.fillMaxWidth().padding(start=16.dp,end=16.dp,top=10.dp,bottom=6.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text(DAY_FULL[sel],fontSize=17.sp,fontWeight=FontWeight.SemiBold);Text("${DAY_DATE[sel]} May 2026${if(sel==TODAY_IDX)" · Bugun" else ""}",fontSize=12.sp,color=if(sel==TODAY_IDX)Blue10 else MaterialTheme.colorScheme.onSurfaceVariant)};Box(Modifier.clip(RoundedCornerShape(8.dp)).background(BlueContainer).padding(horizontal=12.dp,vertical=5.dp)){Text("${lessons.size} dars",fontSize=12.sp,fontWeight=FontWeight.Medium,color=Blue10)}}}
        if(lessons.isEmpty()){item{EmptyDay()}}
        else{itemsIndexed(lessons){_,lesson->val isActive=sel==TODAY_IDX&&lesson.period==3;val(color,container)=subjectColor(lesson.subject);LessonCard(lesson,isActive,color,container,isTeacher=false)}}
    }
}

@Composable
private fun LessonCard(lesson: Lesson, isActive: Boolean, accent: Color, container: Color, isTeacher: Boolean) {
    Card(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=5.dp),colors=CardDefaults.cardColors(containerColor=if(isActive)container.copy(0.55f) else Color.White),shape=RoundedCornerShape(14.dp),border=BorderStroke(if(isActive)1.5.dp else 0.5.dp,if(isActive)accent else Outline),elevation=CardDefaults.cardElevation(0.dp)){
        Row(Modifier.padding(14.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(if(isActive)accent else container),contentAlignment=Alignment.Center){Text("${lesson.period}",fontSize=18.sp,fontWeight=FontWeight.Bold,color=if(isActive)Color.White else accent)}
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(lesson.subject,fontSize=15.sp,fontWeight=FontWeight.SemiBold);if(isActive)Box(Modifier.clip(RoundedCornerShape(4.dp)).background(accent).padding(horizontal=7.dp,vertical=2.dp)){Text("Hozir",fontSize=10.sp,color=Color.White,fontWeight=FontWeight.Medium)}}
                InfoRow(Icons.Default.Schedule,lesson.time,MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
                    if(isTeacher)InfoRow(Icons.Default.Group,lesson.className,MaterialTheme.colorScheme.onSurfaceVariant)
                    else InfoRow(Icons.Default.Person,lesson.teacher,accent)
                    InfoRow(Icons.Default.MeetingRoom,lesson.room,MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, color: Color) {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){Icon(icon,null,Modifier.size(12.dp),tint=color);Text(label,fontSize=12.sp,color=color)}
}

@Composable
private fun WeekStatBox(label: String, value: String, color: Color, container: Color, modifier: Modifier=Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(container).padding(14.dp)){Column{Text(value,fontSize=22.sp,fontWeight=FontWeight.SemiBold,color=color);Text(label,fontSize=11.sp,color=color.copy(alpha=0.7f))}}
}

@Composable
private fun EmptyDay() {
    Column(Modifier.fillMaxWidth().padding(top=60.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.EventAvailable,null,tint=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.size(52.dp));Spacer(Modifier.height(12.dp));Text("Bu kunda dars yo'q",fontSize=15.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
}
