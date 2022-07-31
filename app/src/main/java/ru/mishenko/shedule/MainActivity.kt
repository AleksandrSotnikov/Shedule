package ru.mishenko.shedule

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.mishenko.shedule.model.*
import ru.mishenko.shedule.ui.theme.*

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val database = Firebase.database
            val mainShedRef = database.getReference("DataList")
            val reShedRef = database.getReference("TempAlterTable")
            val mainSheduleString = remember { mutableStateOf("") }
            val reSheduleString = remember { mutableStateOf("") }
            val trigerSwitch = remember { mutableStateOf(true) }
            val trigerWeek = remember {
                mutableStateOf(1)
            }
            val trigerDay = remember {
                mutableStateOf(1)
            }
            val selectedSort = remember { mutableStateOf(0) }
            val selectedContent = remember { mutableStateOf("") }
            val selectedDate = remember { mutableStateOf("") }
            var rememberCoroutineScope = rememberCoroutineScope()
            var rememberBottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberBottomSheetState(
                    initialValue = BottomSheetValue.Collapsed
                )
            )
            var listMainShed = emptyList<MainShedule>()

            mainShedRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mainSheduleString.value = snapshot.getValue<String>().toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
            reShedRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reSheduleString.value = snapshot.getValue<String>().toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })

            val MainShed = if (mainSheduleString.value != "") Gson().fromJson(
                mainSheduleString.value,
                MainSheduleResult::class.java
            ) else null
            val ReShed = if (reSheduleString.value != "") Gson().fromJson(
                reSheduleString.value,
                ListReShedule::class.java
            ) else null

            SheduleTheme {
                Scaffold(
                    bottomBar = {
                        BottomBar(selectedIndex = selectedSort.value) {
                            selectedSort.value = it
                        }
                    }
                ) { it ->
                    it.calculateBottomPadding()
                    BottomSheetScaffold(
                        sheetContent = {
                            if (trigerSwitch.value) RowRaspes(
                                getFilter(
                                    selectedSort.value,
                                    MainShed,
                                    selectedContent.value,
                                    trigerWeek.value,
                                    trigerDay.value
                                )
                            ) else {
                                if (ReShed != null && !selectedDate.value.isNullOrEmpty()) {
                                    RowRaspes2(
                                        getFilter2(
                                            selectedSort.value,
                                            ReShed.list.filter { n -> n.date == selectedDate.value }[0].results,
                                            selectedContent.value,
                                            selectedDate.value
                                        ), selectedDate.value, MainShed
                                    )
                                }
                            }
                        },
                        scaffoldState = rememberBottomSheetScaffoldState
                    ) {
                        Column {
                            SheduleSwitch(trigerSwitch.value) {
                                trigerSwitch.value = !trigerSwitch.value
                            }

                            if (trigerSwitch.value) Week(trigerWeek.value) {
                                trigerWeek.value = it
                            } else RowDates(ReShed, { selectedDate.value = it }, selectedDate.value)

                            if (trigerSwitch.value) Day(trigerDay.value) {
                                trigerDay.value = it
                            } else Spacer(Modifier)

                            if (trigerSwitch.value && MainShed != null)
                                ContentList(
                                    selectedIndex = selectedSort.value,
                                    MainShed,
                                    selectedContent.value,
                                    rememberCoroutineScope,
                                    rememberBottomSheetScaffoldState,
                                    trigerDay.value,
                                    trigerWeek.value
                                ) { selectedContent.value = it }
                            else if (!trigerSwitch.value && ReShed != null && !selectedDate.value.isNullOrEmpty()) ContentList2(
                                selectedIndex = selectedSort.value,
                                ReShed,
                                selectedContent.value,
                                selectedDate = selectedDate.value,
                                rememberCoroutineScope,
                                rememberBottomSheetScaffoldState
                            ) {
                                selectedContent.value = it
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowDates(ReShed: ListReShedule?, function: (str: String) -> Unit, dates: String) {
    Row(modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp)) {
        LazyRow() {
            items(1) {
                ReShed?.list?.forEach { date ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                color = if (dates == date.date) colorSelected else colorUnselected,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, colorBorder, RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        date.date?.let { it1 ->
                            Text(
                                text = it1, modifier = Modifier
                                    .clickable {
                                        date.date?.let { it1 -> function(it1) }
                                    },
                                color = if (dates == date.date) colorTextSelected else colorTextUnSelected
                            )
                        }
                    }
                    Spacer(Modifier.padding(16.dp))
                }
            }
        }
    }
}


@Composable()
fun SheduleSwitch(trigerSwitch: Boolean, function: () -> Unit) {
    SheduleTheme {
        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Button(
                onClick = function, modifier = Modifier.align(Alignment.Center),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (trigerSwitch) colorSelected else colorSelected,
                    contentColor = if (trigerSwitch) colorTextSelected else colorTextSelected
                ),
                border = BorderStroke(1.dp, colorBorder)
            ) {
                Text(if (trigerSwitch) "Основное расписание" else "Измененное расписание")
            }
        }
    }
}

@Composable()
fun Week(week: Int, function: (week: Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
        ) {
            Button(
                onClick = { function(1) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (week == 1) colorSelected else colorUnselected,
                    contentColor = if (week == 1) colorTextSelected else colorTextUnSelected
                ), modifier = Modifier.align(Alignment.Center),
                border = BorderStroke(1.dp, colorBorder)
            ) {
                Text("Неделя 1")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = { function(2) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (week == 2) colorSelected else colorUnselected,
                    contentColor = if (week == 2) colorTextSelected else colorTextUnSelected
                ), modifier = Modifier.align(Alignment.Center),
                border = BorderStroke(1.dp, colorBorder)
            ) {
                Text("Неделя 2")
            }
        }
    }
}

//Чек
@Composable
fun Day(Day: Int, function: (day: Int) -> Unit) {
    LazyRow(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 8.dp)
    ) {
        items(6) {
            Spacer(modifier = Modifier.padding(2.dp))
            Box() {
                Button(
                    onClick = { function(it + 1) }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (Day == it + 1) colorSelected else colorUnselected,
                        contentColor = if (Day == it + 1) colorTextSelected else colorTextUnSelected
                    ), modifier = Modifier.align(Alignment.Center),
                    border = BorderStroke(1.dp, colorBorder)
                ) {
                    Text(text = DayConverter(it + 1))
                }
            }
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentList(
    selectedIndex: Int,
    shedule: MainSheduleResult,
    selectedContent: String,
    rememberCoroutineScope: CoroutineScope,
    rememberBottomSheetScaffoldState: BottomSheetScaffoldState,
    day: Int,
    week: Int,
    function: (content: String) -> Unit,
) {

    var content = when (selectedIndex) {
        0 -> listGroup(shedule.results.filter { n -> n.dow?.toInt() == day.toInt() && n.week?.toInt() == week.toInt() }).toList()
            .sorted()
        1 -> listTeacher(shedule.results.filter { n -> n.dow?.toInt() == day.toInt() && n.week?.toInt() == week.toInt() }).toList()
            .sorted()
        2 -> listAud(shedule.results.filter { n -> n.dow?.toInt() == day.toInt() && n.week?.toInt() == week.toInt() }).toList()
            .sorted()
        else -> {
            emptyList<String>()
        }
    }

    LazyColumn(
        Modifier
            .padding(start = 16.dp)
            .fillMaxHeight()
    ) {
        items(1) {
            FlowRow() {
                repeat(content.size) { index ->
                    Box(
                        Modifier
                            .padding(bottom = 8.dp, start = 4.dp)
                            .background(
                                color = if (content.toList()[index] == selectedContent) colorSelected else colorUnselected,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, colorBorder, RoundedCornerShape(16.dp))
                    ) {
                        Text(content.toList()[index],
                            color = if (content.toList()[index] == selectedContent) colorTextSelected else colorTextUnSelected,
                            modifier = Modifier
                                .padding(vertical = 2.dp, horizontal = 8.dp)
                                .clickable {
                                    function(content.toList()[index])
                                    rememberCoroutineScope.launch {
                                        rememberBottomSheetScaffoldState.bottomSheetState.expand()
                                    }
                                })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentList2(
    selectedIndex: Int,
    shedule: ListReShedule?,
    selectedContent: String,
    selectedDate: String,
    rememberCoroutineScope: CoroutineScope,
    rememberBottomSheetScaffoldState: BottomSheetScaffoldState,
    function: (content: String) -> Unit
) {
    var content = emptyList<String>()
    var list = emptyList<ReSheduleResult>()
    if (selectedDate.isNotEmpty()) {
        list = shedule?.list?.filter { n -> n.date.toString() == selectedDate.toString() }!!
        Log.e("qqa", shedule.list.toString().plus(selectedDate))
        content = when (selectedIndex) {
            0 -> listGroup2(shedule.list.filter { n -> n.date == selectedDate }).toList().sorted()
            1 -> listTeacher2(shedule.list.filter { n -> n.date == selectedDate }).toList().sorted()
            2 -> listAud2(shedule.list.filter { n -> n.date == selectedDate }).toList().sorted()
            else -> {
                emptyList<String>()
            }
        }
    }

    LazyColumn(
        Modifier
            .padding(start = 16.dp)
            .fillMaxHeight()
    ) {
        items(1) {
            FlowRow() {
                repeat(content.size) { index ->
                    Box(
                        Modifier
                            .padding(bottom = 8.dp, start = 4.dp)
                            .background(
                                color = if (content.toList()[index] == selectedContent) colorSelected else colorUnselected,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, colorBorder, RoundedCornerShape(16.dp))
                    ) {
                        Text(content.toList()[index],
                            color = if (content.toList()[index] == selectedContent) colorTextSelected else colorTextUnSelected,
                            modifier = Modifier
                                .padding(vertical = 2.dp, horizontal = 8.dp)
                                .clickable {
                                    function(content.toList()[index])
                                    rememberCoroutineScope.launch {
                                        rememberBottomSheetScaffoldState.bottomSheetState.expand()
                                    }
                                })
                    }
                }
            }
        }
    }
}

private fun listGroup(json: List<MainShedule>): Set<String> {
    var list = emptySet<String>()
    json.forEach {
        if (it.group?.isNotEmpty() == true)
            list = list.plus(it.group!!)
    }
    return list;
}

private fun listGroup2(json: List<ReSheduleResult>?): Set<String> {
    var list = emptySet<String>()
    json!![0].results.forEachIndexed { id, it ->
        if (it.group?.isNotEmpty()!!)
            list = list.plus(it.group.toString())
    }
    return list;
}

private fun listTeacher(json: List<MainShedule>): Set<String> {
    var list = emptySet<String>()

    json.forEach {
        if (it.teacher?.isNotEmpty() == true)
            list = list.plus(it.teacher!!)
    }
    return list
}

private fun listTeacher2(json: List<ReSheduleResult>?): Set<String> {
    var list = emptySet<String>()

    json!![0].results.forEachIndexed { id, it ->
        if (it.teacher?.isNotEmpty()!!)
            list = list.plus(it.teacher.toString())
    }
    return list;
}

private fun listAud(json: List<MainShedule>): Set<String> {
    var list = emptySet<String>()
    json.forEach {
        if (it.audience?.isNotEmpty() == true)
            list = list.plus(it.audience!!)
    }
    return list;
}

private fun listAud2(json: List<ReSheduleResult>?): Set<String> {
    var list = emptySet<String>()

    json!![0].results.forEachIndexed { id, it ->
        if (it.audience?.isNotEmpty()!!)
            list = list.plus(it.audience.toString())
    }
    return list;
}

fun DayConverter(day: Int): String {
    when (day) {
        1 -> return "Пн"
        2 -> return "Вт"
        3 -> return "Ср"
        4 -> return "Чт"
        5 -> return "Пт"
        6 -> return "Сб"
    }
    return ""
}

@Composable//+++
fun BottomBar(selectedIndex: Int, function: (Sort: Int) -> Unit) {

    BottomNavigation(modifier = Modifier.height(68.dp), backgroundColor = colorBottom) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .padding(16.dp, 0.dp)
                    .fillMaxWidth()
                    .height(0.dp)
                    .background(colorTintBottom)
            ) {}
            Row(Modifier.fillMaxWidth()) {
                BottomNavigationItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.teacher),
                            "",
                            //contentScale = ContentScale.FillBounds,
                            modifier = Modifier.size(40.dp),
                            tint = colorTintBottom

                        )
                    },
                    label = { Text(text = "Преподаватель", color = colorTintBottom) },
                    selected = (selectedIndex == 1),
                    onClick = { function(1) })
                Box(
                    Modifier
                        .padding(0.dp, 8.dp, 0.dp, 8.dp)
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(colorTintBottom)
                )
                BottomNavigationItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.group),
                            "",
                            //contentScale = ContentScale.FillBounds,
                            modifier = Modifier.size(40.dp),
                            tint = colorTintBottom
                        )
                    },

                    label = { Text(text = "Группа", color = colorTintBottom) },
                    selected = (selectedIndex == 0),
                    onClick = { function(0) })
                Box(
                    Modifier
                        .padding(0.dp, 8.dp, 0.dp, 8.dp)
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(colorTintBottom)
                )
                BottomNavigationItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.audience),
                            "",
                            //contentScale = ContentScale.FillBounds,
                            modifier = Modifier.size(40.dp),
                            tint = colorTintBottom
                        )
                    },
                    label = { Text(text = "Аудитория", color = colorTintBottom) },
                    selected = (selectedIndex == 2),
                    onClick = {
                        function(2)
                    })
            }
        }
    }
}

fun getFilter2(
    selectedSort: Int,
    MainShed: List<ReShedule>?,
    value: String,
    value1: String,
): List<ReShedule> {

    return if (MainShed != null) {
        when (selectedSort) {
            0 -> MainShed.filter { q -> q.group == value }
            1 -> MainShed.filter { q -> q.teacher == value }
            2 -> MainShed.filter { q -> q.audience == value }
            else -> {
                emptyList<ReShedule>()
            }
        }
    } else {
        emptyList<ReShedule>()
    }
}

fun getFilter(
    selectedSort: Int,
    MainShed: MainSheduleResult?,
    value: String,
    value1: Int,
    value2: Int
): List<MainShedule> {

    return if (MainShed != null) {
        when (selectedSort) {
            0 -> MainShed.results.filter { n -> n.group == value && n.week.toString() == value1.toString() && n.dow.toString() == value2.toString() }
            1 -> MainShed.results.filter { n -> n.teacher == value && n.week.toString() == value1.toString() && n.dow.toString() == value2.toString() }
            2 -> MainShed.results.filter { n -> n.audience == value && n.week.toString() == value1.toString() && n.dow.toString() == value2.toString() }
            else -> {
                emptyList<MainShedule>()
            }
        }
    } else {
        emptyList<MainShedule>()
    }
}

@Composable
fun RowRaspes(state: List<MainShedule>) {
    val scroll = rememberScrollState()
    val items = state.sortedBy { it.para }
    Box(
        Modifier
            .padding(bottom = 64.dp)
            .background(colorBottomSheet)
            .verticalScroll(scroll)
    ) {
        for (item in items) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(
                        top = 128.dp * (item.para
                            ?.toInt()
                            ?.minus(1)!!)
                    )
                    .height(128.dp)
                    .fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        item.para!!, color = colorBottomSheetText, modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(start = 16.dp), textAlign = TextAlign.Left
                    )
                    Text(
                        paraToTimeConvert(item.para!!),
                        color = colorBottomSheetText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        textAlign = TextAlign.Right
                    )
                }
                Row(Modifier.fillMaxWidth()) {
                    if (item.pg!!.toInt() == 0) {
                        pg0(item)
                    }
                    if (item.pg!!.toInt() == 1) {
                        pg1(item)
                    }
                    if (item.pg!!.toInt() == 2) {
                        pg2(item)
                    }
                }
            }
        }
    }
}

@Composable
fun RowRaspes2(state: List<ReShedule>, date: String, MainShed: MainSheduleResult?) {
    val scroll = rememberScrollState()
    val items = state.sortedBy { it.para }

    Box(
        Modifier
            .padding(bottom = 64.dp)
            .background(colorBottomSheet)
            .verticalScroll(scroll)
    ) {
        for (item in items) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(
                        top = 128.dp * (item.para
                            ?.toInt()
                            ?.minus(1)!!)
                    )
                    .height(128.dp)
                    .fillMaxWidth()
            ) {
                if (MainShed?.results?.contains(
                        MainShedule(
                            week = date[date.length - 2].toString(),
                            dow = DateToStringConvert(date.split(" ")[3]),
                            audience = item.audience,
                            discipline = item.discipline,
                            group = item.group,
                            para = item.para,
                            pg = item.pg,
                            teacher = item.teacher
                        )
                    )!!
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            item.para!!, color = colorBottomSheetText, modifier = Modifier.background(color = Color.Blue, RoundedCornerShape(128.dp))
                                .fillMaxWidth(0.1f)
                                .padding(start = 16.dp), textAlign = TextAlign.Left
                        )
                        Text(
                            paraToTimeConvert(item.para!!),
                            color = colorBottomSheetText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp),

                            textAlign = TextAlign.Right
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        if (item.pg!!.toInt() == 0) {
                            pg0(item)
                        }
                        if (item.pg!!.toInt() == 1) {
                            pg1(item)
                        }
                        if (item.pg!!.toInt() == 2) {
                            pg2(item)
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            item.para!!, color = colorBottomSheetText, modifier = Modifier.background(color = Color.Red, RoundedCornerShape(128.dp))
                                .fillMaxWidth(0.1f)
                                .padding(start = 16.dp), textAlign = TextAlign.Left
                        )
                        Text(
                            paraToTimeConvert(item.para!!),
                            color = colorBottomSheetText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                                ,
                            textAlign = TextAlign.Right
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        if (item.pg!!.toInt() == 0) {
                            pg0(item)
                        }
                        if (item.pg!!.toInt() == 1) {
                            pg1(item)
                        }
                        if (item.pg!!.toInt() == 2) {
                            pg2(item)
                        }
                    }
                }
            }
        }
    }
}

fun paraToTimeConvert(para: String): String {
    return when (para) {
        "1" -> "8:00 - 9:35"
        "2" -> "9:45 - 11:20"
        "3" -> "11:55 - 13:30"
        "4" -> "13:45 - 15:20"
        "5" -> "15:40 - 17:15"
        "6" -> "17:25 - 19:00"
        else -> "Unknown"
    }
}

fun DateToStringConvert(para: String): String {
    Log.e("ff",para)
    return when (para) {
        "(понедельник)" -> "1"
        "(вторник)" -> "2"
        "(среда)" -> "3"
        "(четверг)" -> "4"
        "(пятница)" -> "5"
        "(суббота)" -> "6"
        else -> "Unknown"
    }
}

@Composable
fun pg0(item: MainShedule) {
    Column(Modifier.fillMaxWidth()) {
        item.discipline?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item.teacher?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item.audience?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item.group?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun pg1(item: MainShedule) {
    Column() {
        Row() {
            item.discipline?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth())
        }
        Row() {
            item.teacher?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth())
        }
        Row() {
            item.audience?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth())
        }
        item.group?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun pg2(item: MainShedule) {
    Column() {
        Row() {
            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
            item.discipline?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                )
            }
        }
        Row() {
            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
            item.teacher?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                )
            }
        }
        Row() {
            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
            item.audience?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                )
            }
        }
        item.group?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun pg0(item: ReShedule) {
    Column(Modifier.fillMaxWidth()) {
        item.discipline?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item.teacher?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item.audience?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item.group?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun pg1(item: ReShedule) {
    Column() {
        Row() {
            item.discipline?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth())
        }
        Row() {
            item.teacher?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth())
        }
        Row() {
            item.audience?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth())
        }
        item.group?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun pg2(item: ReShedule) {
    Column() {
        Row() {
            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
            item.discipline?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                )
            }
        }
        Row() {
            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
            item.teacher?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                )
            }
        }
        Row() {
            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
            item.audience?.let {
                Text(
                    it,
                    color = colorBottomSheetText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                )
            }
        }
        item.group?.let {
            Text(
                it,
                color = colorBottomSheetText,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            )
        }
    }
}


