package com.maktab.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.data.MockData
import com.maktab.app.network.ApiResult
import com.maktab.app.viewmodel.TeacherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maktab.app.ui.components.*
import com.maktab.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OquvRejaScreen() {
    val expandedIds = remember { mutableStateListOf<Int>() }
    val typeColor = mapOf("Dars" to Pair(Blue10,BlueContainer),"Test" to Pair(Red10,RedContainer),"Quiz" to Pair(Amber10,AmberContainer),"Imtixon" to Pair(Purple10,PurpleContainer))
    LazyColumn(modifier=Modifier.fillMaxSize(), contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("O'quv reja") { Button(onClick={}, colors=ButtonDefaults.buttonColors(containerColor=Teal10), contentPadding=PaddingValues(horizontal=14.dp,vertical=6.dp), modifier=Modifier.height(34.dp)) { Icon(Icons.Default.Add,null,modifier=Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Yangi fan",fontSize=12.sp) } }; Spacer(Modifier.height(4.dp)) }
        items(MockData.subjects) { sub ->
            val isExp = sub.id in expandedIds
            val sc = when(sub.id){1->Pair(Blue10,BlueContainer);2->Pair(Purple10,PurpleContainer);else->Pair(Amber10,AmberContainer)}
            Card(modifier=Modifier.fillMaxWidth(), colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(12.dp), border=BorderStroke(0.5.dp,Outline), elevation=CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier=Modifier.fillMaxWidth().clickable{if(isExp)expandedIds.remove(sub.id) else expandedIds.add(sub.id)}.padding(14.dp), horizontalArrangement=Arrangement.spacedBy(12.dp), verticalAlignment=Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(sc.second),contentAlignment=Alignment.Center){Icon(Icons.Default.MenuBook,null,tint=sc.first,modifier=Modifier.size(22.dp))}
                        Column(Modifier.weight(1f)){Text(sub.name,fontWeight=FontWeight.Medium,fontSize=15.sp);Spacer(Modifier.height(3.dp));Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Text("${sub.lessons} dars",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${sub.tests} test",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${sub.quizzes} quiz",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
                        Icon(if(isExp)Icons.Default.ExpandLess else Icons.Default.ExpandMore,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    AnimatedVisibility(isExp,enter=expandVertically(),exit=shrinkVertically()) {
                        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp)) {
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.padding(bottom=10.dp)){listOf("Dars","Test","Quiz").forEach{tp->OutlinedButton(onClick={},modifier=Modifier.weight(1f).height(32.dp),contentPadding=PaddingValues(0.dp),shape=RoundedCornerShape(8.dp)){Icon(Icons.Default.Add,null,modifier=Modifier.size(13.dp));Spacer(Modifier.width(2.dp));Text(tp,fontSize=11.sp)}}}
                            sub.items.forEachIndexed{idx,item->val tc=typeColor[item.type]?:Pair(Teal10,TealContainer);Row(Modifier.fillMaxWidth().padding(vertical=7.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){Icon(Icons.Default.Description,null,tint=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.size(16.dp));Text(item.title,fontSize=13.sp,modifier=Modifier.weight(1f));StatusChip(item.type,tc.first,tc.second)};if(idx<sub.items.size-1)HorizontalDivider(color=Outline,thickness=0.5.dp)}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DavomatScreen(vm: TeacherViewModel = viewModel()) {
    val attendanceState by vm.attendanceState.collectAsState()

    // Davomat yuklash
    LaunchedEffect(Unit) { vm.loadAttendance() }

    val att = remember { mutableStateMapOf<Int,MutableList<Boolean>>().apply{MockData.initialAttendance.forEach{(k,v)->put(k,v.toMutableList())}} }
    val todayPresent by remember(att){derivedStateOf{att.values.count{it[1]==true}}}
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item{SectionHeader("Davomat — 5-A sinf"){Text("19 May 2026",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal=10.dp,vertical=4.dp))}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Jami","${MockData.students.size}",Blue10,Modifier.weight(1f));StatCard("Keldi","$todayPresent",Teal10,Modifier.weight(1f));StatCard("Kelmadi","${MockData.students.size-todayPresent}",Red10,Modifier.weight(1f))}}
        item{Card(modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(12.dp),border=BorderStroke(0.5.dp,Outline),elevation=CardDefaults.cardElevation(0.dp)){Column{Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal=14.dp,vertical=8.dp).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){Text("O'quvchi",fontSize=11.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.weight(1f));MockData.weekDays.forEach{d->Text(d,fontSize=11.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.width(36.dp),textAlign=TextAlign.Center)}}
            MockData.students.forEachIndexed{idx,student->val sa=att[student.id]?:mutableListOf();if(idx>0)HorizontalDivider(color=Outline,thickness=0.5.dp);Row(Modifier.padding(horizontal=14.dp,vertical=9.dp).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(8.dp)){AvatarCircle(student.initials,Teal10,28.dp);Text(student.name.split(" ")[0],fontSize=13.sp)};sa.forEachIndexed{di,isP->Box(Modifier.width(36.dp),contentAlignment=Alignment.Center){AttendanceDot(isP){val n=sa.toMutableList();n[di]=!isP;att[student.id]=n}}}}}}}}
    }
}

@Composable
fun OylikHisobiScreen() {
    val months=listOf("Yan","Fev","Mar","Apr","May")
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionHeader("Oylik hisob"){StatusChip("300 000 UZS / oy",Teal10,TealContainer)}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Yig'ildi","4.5M UZS",Teal10,Modifier.weight(1f));StatCard("Qoldiq","1.5M UZS",Amber10,Modifier.weight(1f))}}
        item{Card(modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(12.dp),border=BorderStroke(0.5.dp,Outline),elevation=CardDefaults.cardElevation(0.dp)){Column{Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal=14.dp,vertical=8.dp).fillMaxWidth()){Text("O'quvchi",fontSize=11.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.weight(1f));months.forEach{m->Text(m,fontSize=11.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.width(32.dp),textAlign=TextAlign.Center)};Text("Jami",fontSize=11.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.width(44.dp),textAlign=TextAlign.End)}
            MockData.students.forEachIndexed{idx,student->val paid=months.mapIndexed{mi,_->mi<(5-idx%3)};val pc=paid.count{it};if(idx>0)HorizontalDivider(color=Outline,thickness=0.5.dp);Row(Modifier.padding(horizontal=14.dp,vertical=10.dp).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(7.dp)){AvatarCircle(student.initials,Teal10,26.dp);Text(student.name.split(" ")[0],fontSize=12.sp)};paid.forEach{p->Box(Modifier.width(32.dp),contentAlignment=Alignment.Center){Box(Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if(p)TealContainer else RedContainer),contentAlignment=Alignment.Center){Text(if(p)"✓" else "✗",color=if(p)Teal10 else Red10,fontSize=11.sp)}}};Text("${pc*300}k",fontSize=12.sp,fontWeight=FontWeight.Medium,color=if(pc==5)Teal10 else Amber10,modifier=Modifier.width(44.dp),textAlign=TextAlign.End)}}}}}
    }
}

@Composable
fun UygaVazifaScreen() {
    val scope=rememberCoroutineScope();var checkingId by remember{mutableStateOf<Int?>(null)};val results=remember{mutableStateMapOf<Int,Pair<Int,String>>()}
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionHeader("Uyga vazifa tekshirish"){StatusChip("${MockData.homeworkTasks.count{!results.containsKey(it.id)}} kutilmoqda",Amber10,AmberContainer)}}
        items(MockData.homeworkTasks){hw->val result=results[hw.id];val isChecking=checkingId==hw.id
            AppCard{Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){AvatarCircle(hw.initials,Teal10,40.dp);Column(Modifier.weight(1f)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(hw.studentName,fontWeight=FontWeight.Medium,fontSize=14.sp);Text(hw.submittedTime,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Spacer(Modifier.height(2.dp));Text("${hw.subject} — ${hw.task}",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(4.dp));Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){Icon(Icons.Default.AttachFile,null,Modifier.size(13.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant);Text(hw.fileName,fontSize=12.sp,color=Blue10)};Spacer(Modifier.height(8.dp))
                if(result!=null){Surface(color=TealContainer,shape=RoundedCornerShape(8.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp)){Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.SmartToy,null,tint=Teal10,modifier=Modifier.size(15.dp));Text("AI baho: ${result.first}/100",fontSize=13.sp,fontWeight=FontWeight.Medium,color=Teal10)};Spacer(Modifier.height(5.dp));Text(result.second,fontSize=12.sp,color=Green10,lineHeight=17.sp)}}}
                else{Button(onClick={scope.launch{checkingId=hw.id;delay(2000);results[hw.id]=MockData.hwFeedbacks[hw.id]?:Pair(80,"Tekshirildi.");checkingId=null}},enabled=!isChecking,colors=ButtonDefaults.buttonColors(containerColor=TealContainer,contentColor=Teal10,disabledContainerColor=MaterialTheme.colorScheme.surfaceVariant),contentPadding=PaddingValues(horizontal=12.dp,vertical=6.dp),modifier=Modifier.height(32.dp),shape=RoundedCornerShape(8.dp)){Icon(if(isChecking)Icons.Default.HourglassEmpty else Icons.Default.SmartToy,null,Modifier.size(14.dp));Spacer(Modifier.width(4.dp));Text(if(isChecking)"AI tekshirmoqda..." else "AI bilan tekshir",fontSize=12.sp)}}}}}
        }
    }
}

@Composable
fun ImtixonScreen() {
    val scope=rememberCoroutineScope();var gradingId by remember{mutableStateOf<Int?>(null)};val grades=remember{mutableStateMapOf<Int,Triple<Int,Int,String>>()}
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionHeader("Imtixon tekshirish"){StatusChip("${MockData.examTasks.count{!grades.containsKey(it.id)}} tekshirilmagan",Purple10,PurpleContainer)}}
        items(MockData.examTasks){exam->val grade=grades[exam.id];val isGrading=gradingId==exam.id
            AppCard{Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){AvatarCircle(exam.initials,Purple10,40.dp);Column(Modifier.weight(1f)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(exam.studentName,fontWeight=FontWeight.Medium,fontSize=14.sp);Text(exam.date,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Spacer(Modifier.height(2.dp));Text("${exam.subject} — ${exam.title}",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${exam.questionCount} ta savol",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(8.dp))
                if(grade!=null){Surface(color=PurpleContainer,shape=RoundedCornerShape(8.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp)){Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.SmartToy,null,tint=Purple10,modifier=Modifier.size(15.dp));Text("AI: ${grade.second}/${exam.questionCount} — ${grade.first}%",fontSize=13.sp,fontWeight=FontWeight.Medium,color=Purple10)};Spacer(Modifier.height(5.dp));Text(grade.third,fontSize=12.sp,color=Purple10,lineHeight=17.sp)}}}
                else{Button(onClick={scope.launch{gradingId=exam.id;delay(2500);grades[exam.id]=MockData.examFeedbacks[exam.id]?:Triple(80,16,"Tekshirildi.");gradingId=null}},enabled=!isGrading,colors=ButtonDefaults.buttonColors(containerColor=PurpleContainer,contentColor=Purple10,disabledContainerColor=MaterialTheme.colorScheme.surfaceVariant),contentPadding=PaddingValues(horizontal=12.dp,vertical=6.dp),modifier=Modifier.height(32.dp),shape=RoundedCornerShape(8.dp)){Icon(if(isGrading)Icons.Default.HourglassEmpty else Icons.Default.SmartToy,null,Modifier.size(14.dp));Spacer(Modifier.width(4.dp));Text(if(isGrading)"AI baholayapti..." else "AI bilan baholash",fontSize=12.sp)}}}}}
        }
    }
}

@Composable
fun TeacherSorovnomaScreen() {
    val answered=remember{mutableStateMapOf<Int,String>()}
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{SectionHeader("So'rovnomalar")}
        items(MockData.teacherSurveys){sv->AppCard{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Top){Text(sv.title,fontWeight=FontWeight.Medium,fontSize=14.sp,modifier=Modifier.weight(1f));StatusChip("Muddat: ${sv.deadline}",Amber10,AmberContainer)};Spacer(Modifier.height(4.dp));Text(sv.question,fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(10.dp))
            val ans=answered[sv.id];if(ans==null){Column(verticalArrangement=Arrangement.spacedBy(7.dp)){sv.options.forEach{opt->OutlinedButton(onClick={answered[sv.id]=opt},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(8.dp),contentPadding=PaddingValues(12.dp)){Text(opt,fontSize=13.sp,modifier=Modifier.fillMaxWidth())}}}}
            else{Surface(color=TealContainer,shape=RoundedCornerShape(8.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(10.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.CheckCircle,null,tint=Teal10,modifier=Modifier.size(18.dp));Text("Javob: $ans",fontSize=13.sp,color=Teal10)}}};Spacer(Modifier.height(10.dp));LinearProgressIndicator(progress={sv.answered.toFloat()/sv.total},modifier=Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),color=Teal10,trackColor=MaterialTheme.colorScheme.surfaceVariant)}}
    }
}