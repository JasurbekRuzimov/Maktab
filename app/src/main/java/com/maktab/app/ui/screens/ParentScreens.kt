package com.maktab.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.data.MockData
import com.maktab.app.ui.components.*
import com.maktab.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ParentDavomatScreen() {
    val child=MockData.myChild;val presentCount=MockData.calendarDays.count{it.status=="present"};val absentCount=MockData.calendarDays.count{it.status=="absent"}
    val dayH=listOf("D","S","Ch","P","J","Sh","Y")
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{SectionHeader("${child.name} — Davomat")}
        item{AppCard{Row(horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){AvatarCircle(child.initials,Teal10,52.dp);Column{Text(child.name,fontWeight=FontWeight.SemiBold,fontSize=15.sp);Text("5-A sinf · O'quvchi",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(6.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){StatusChip("$presentCount keldi",Teal10,TealContainer);StatusChip("$absentCount kelmadi",Red10,RedContainer)}}}}}
        item{AppCard{Text("May 2026",fontSize=13.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth()){dayH.forEach{d->Text(d,modifier=Modifier.weight(1f),textAlign=TextAlign.Center,fontSize=11.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant)}};Spacer(Modifier.height(4.dp))
            val offset=4
            LazyVerticalGrid(columns=GridCells.Fixed(7),modifier=Modifier.height(200.dp),horizontalArrangement=Arrangement.spacedBy(4.dp),verticalArrangement=Arrangement.spacedBy(4.dp),userScrollEnabled=false){items(35){idx->val di=idx-offset;val cd=if(di>=0&&di<MockData.calendarDays.size)MockData.calendarDays[di] else null;if(cd!=null){val(bg,tc)=when(cd.status){"present"->Pair(TealContainer,Teal10);"absent"->Pair(RedContainer,Red10);"today"->Pair(AmberContainer,Amber10);else->Pair(Color.Transparent,MaterialTheme.colorScheme.onSurfaceVariant)};Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(6.dp)).background(bg),contentAlignment=Alignment.Center){Text("${cd.day}",fontSize=12.sp,color=tc,fontWeight=if(cd.status=="today")FontWeight.Bold else FontWeight.Normal)}}else{Box(Modifier.aspectRatio(1f))}}}
            Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){listOf(Triple("Keldi",Teal10,TealContainer),Triple("Kelmadi",Red10,RedContainer),Triple("Bugun",Amber10,AmberContainer)).forEach{(l,c,bg)->Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(bg).border(0.5.dp,c,RoundedCornerShape(3.dp)));Text(l,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}}}
    }
}

@Composable
fun BaholarScreen() {
    val gc=listOf(Pair(Blue10,BlueContainer),Pair(Purple10,PurpleContainer),Pair(Amber10,AmberContainer),Pair(Green10,GreenContainer))
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionHeader("Baholar va Reyting")}
        itemsIndexed(MockData.grades){idx,g->val(color,container)=gc[idx];AppCard{Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(container),contentAlignment=Alignment.Center){Icon(Icons.Default.MenuBook,null,tint=color,modifier=Modifier.size(20.dp))};Column(Modifier.weight(1f)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(g.subject,fontWeight=FontWeight.Medium,fontSize=15.sp);Text("${g.avg}",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=color)};Spacer(Modifier.height(6.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){g.scores.forEach{sc->Box(Modifier.weight(1f).clip(RoundedCornerShape(5.dp)).background(container).padding(vertical=3.dp),contentAlignment=Alignment.Center){Text("$sc",fontSize=12.sp,color=color,fontWeight=FontWeight.Medium)}}}}}}}
        item{Spacer(Modifier.height(4.dp));Text("Sinf reytingi",fontSize=14.sp,fontWeight=FontWeight.Medium)}
        item{Card(modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(12.dp),border=BorderStroke(0.5.dp,Outline),elevation=CardDefaults.cardElevation(0.dp)){Column{MockData.students.forEachIndexed{idx,student->val isMe=student.id==MockData.myChild.id;if(idx>0)HorizontalDivider(color=Outline,thickness=0.5.dp);Row(Modifier.fillMaxWidth().background(if(isMe)TealContainer.copy(alpha=0.4f) else Color.Transparent).padding(horizontal=14.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){Box(Modifier.size(26.dp).clip(CircleShape).background(if(idx<3)AmberContainer else MaterialTheme.colorScheme.surfaceVariant),contentAlignment=Alignment.Center){Text("${idx+1}",fontSize=12.sp,fontWeight=FontWeight.Medium,color=if(idx<3)Amber10 else MaterialTheme.colorScheme.onSurfaceVariant)};AvatarCircle(student.initials,if(isMe)Teal10 else Blue10,30.dp);Text(student.name,fontSize=13.sp,fontWeight=if(isMe)FontWeight.SemiBold else FontWeight.Normal,modifier=Modifier.weight(1f));if(isMe)StatusChip("Siz",Teal10,TealContainer);Text("${student.score}",fontSize=15.sp,fontWeight=FontWeight.SemiBold,color=if(isMe)Teal10 else MaterialTheme.colorScheme.onSurface)}}}}}
    }
}

@Composable
fun ParentUygaVazifaScreen() {
    val scope=rememberCoroutineScope();val submitted=remember{mutableStateListOf<Int>()};var submitting by remember{mutableStateOf<Int?>(null)};val hwC=listOf(Pair(Blue10,BlueContainer),Pair(Purple10,PurpleContainer),Pair(Amber10,AmberContainer))
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionHeader("Uyga vazifa")}
        itemsIndexed(MockData.childHomework){idx,hw->val done=hw.isDone||hw.id in submitted;val(color,container)=hwC[idx]
            AppCard{Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(container),contentAlignment=Alignment.Center){Icon(Icons.Default.MenuBook,null,tint=color,modifier=Modifier.size(20.dp))};Column(Modifier.weight(1f)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(hw.subject,fontWeight=FontWeight.SemiBold,fontSize=15.sp);StatusChip(if(done)"Bajarildi" else "Kutilmoqda",if(done)Teal10 else Amber10,if(done)TealContainer else AmberContainer)};Spacer(Modifier.height(3.dp));Text(hw.task,fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(3.dp));Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){Icon(Icons.Default.Schedule,null,Modifier.size(12.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant);Text("Muddat: ${hw.deadline}",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                if(!done){Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={},modifier=Modifier.weight(1f).height(34.dp),shape=RoundedCornerShape(8.dp),contentPadding=PaddingValues(0.dp)){Icon(Icons.Default.Upload,null,Modifier.size(14.dp));Spacer(Modifier.width(4.dp));Text("Fayl",fontSize=12.sp)};Button(onClick={scope.launch{submitting=hw.id;delay(1500);submitted.add(hw.id);submitting=null}},enabled=submitting!=hw.id,modifier=Modifier.weight(1f).height(34.dp),colors=ButtonDefaults.buttonColors(containerColor=color),shape=RoundedCornerShape(8.dp),contentPadding=PaddingValues(0.dp)){Icon(if(submitting==hw.id)Icons.Default.HourglassEmpty else Icons.Default.Send,null,Modifier.size(14.dp));Spacer(Modifier.width(4.dp));Text(if(submitting==hw.id)"Yuborilmoqda..." else "Topshirish",fontSize=12.sp)}}}}}}
        }
    }
}

@Composable
fun XulqScreen() {
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionHeader("Xulq va xatti-harakat")}
        item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Ijobiy","2",Teal10,Modifier.weight(1f));StatCard("Neytral","1",Amber10,Modifier.weight(1f));StatCard("Salbiy","1",Red10,Modifier.weight(1f))}}
        items(MockData.behaviorRecords){rec->val(icon,color,container)=when(rec.type){"positive"->Triple(Icons.Default.ThumbUp,Teal10,TealContainer);"negative"->Triple(Icons.Default.Warning,Red10,RedContainer);else->Triple(Icons.Default.Remove,Amber10,AmberContainer)}
            AppCard{Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(container),contentAlignment=Alignment.Center){Icon(icon,null,tint=color,modifier=Modifier.size(20.dp))};Column(Modifier.weight(1f)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(rec.title,fontWeight=FontWeight.Medium,fontSize=14.sp);Text(rec.date,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Spacer(Modifier.height(3.dp));Text(rec.description,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(4.dp));Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){Icon(Icons.Default.Person,null,Modifier.size(11.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant);Text(rec.teacher,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}}
        }
    }
}

@Composable
fun ShikoyatScreen() {
    var selCat by remember{mutableStateOf("")};var rating by remember{mutableStateOf(0)};var text by remember{mutableStateOf("")};var submitted by remember{mutableStateOf(false)}
    val cats=listOf("O'qituvchi munosabati","Infratuzilma","O'quv jarayoni","Xavfsizlik","Boshqa")
    val rl=listOf("Juda yomon","Yomon","O'rtacha","Yaxshi","A'lo")
    if(submitted){Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Box(Modifier.size(72.dp).clip(CircleShape).background(TealContainer),contentAlignment=Alignment.Center){Icon(Icons.Default.Check,null,tint=Teal10,modifier=Modifier.size(36.dp))};Spacer(Modifier.height(16.dp));Text("Murojaatingiz qabul qilindi",fontSize=18.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center);Spacer(Modifier.height(8.dp));Text("Maktab ma'muriyati 2 ish kuni ichida ko'rib chiqadi",fontSize=14.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center);Spacer(Modifier.height(24.dp));OutlinedButton(onClick={submitted=false;selCat="";rating=0;text=""},shape=RoundedCornerShape(10.dp)){Text("Yangi murojaat")}};return}
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{SectionHeader("Shikoyat / Baholash")}
        item{AppCard{Text("Murojaat turi",fontSize=12.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(8.dp));cats.chunked(2).forEach{chunk->Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.padding(bottom=8.dp)){chunk.forEach{cat->val isSel=selCat==cat;Surface(onClick={selCat=cat},modifier=Modifier.weight(1f),shape=RoundedCornerShape(20.dp),color=if(isSel)TealContainer else Color.Transparent,border=BorderStroke(0.5.dp,if(isSel)Teal10 else Outline)){Text(cat,modifier=Modifier.padding(horizontal=10.dp,vertical=6.dp),fontSize=12.sp,color=if(isSel)Teal10 else MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center,fontWeight=if(isSel)FontWeight.Medium else FontWeight.Normal)}};if(chunk.size==1)Spacer(Modifier.weight(1f))}}}}
        item{AppCard{Text("Maktabni baholang",fontSize=12.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(8.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){(1..5).forEach{r->Surface(onClick={rating=r},modifier=Modifier.weight(1f).aspectRatio(1f),shape=RoundedCornerShape(8.dp),color=if(rating>=r)AmberContainer else MaterialTheme.colorScheme.surfaceVariant,border=BorderStroke(0.5.dp,if(rating>=r)Amber10 else Outline)){Box(contentAlignment=Alignment.Center){Text("★",fontSize=22.sp,color=if(rating>=r)Amber10 else MaterialTheme.colorScheme.onSurfaceVariant)}}}};if(rating>0){Spacer(Modifier.height(6.dp));Text(rl[rating-1],fontSize=12.sp,color=Amber10,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())}}}
        item{AppCard{Text("Murojaat matni",fontSize=12.sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(8.dp));OutlinedTextField(value=text,onValueChange={text=it},placeholder={Text("Muammoni batafsil yozing...",fontSize=13.sp)},modifier=Modifier.fillMaxWidth().heightIn(min=100.dp),shape=RoundedCornerShape(10.dp),minLines=4,colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Teal10))}}
        item{Button(onClick={if(selCat.isNotEmpty()&&text.isNotEmpty())submitted=true},modifier=Modifier.fillMaxWidth().height(50.dp),enabled=selCat.isNotEmpty()&&text.isNotEmpty(),colors=ButtonDefaults.buttonColors(containerColor=Teal10),shape=RoundedCornerShape(12.dp)){Icon(Icons.Default.Send,null,Modifier.size(16.dp));Spacer(Modifier.width(8.dp));Text("Yuborish",fontSize=15.sp)};Spacer(Modifier.height(16.dp))}
    }
}
