package com.zenodotus.ringer

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import com.zenodotus.ringer.data.UserPreferences
import com.zenodotus.ringer.database.AppDatabase
import com.zenodotus.ringer.database.EnumTrophy
import com.zenodotus.ringer.database.Task
import com.zenodotus.ringer.database.TrophyWithTaskName
import com.zenodotus.ringer.database.User
import com.zenodotus.ringer.database.dao.TaskDao
import com.zenodotus.ringer.database.repositories.TaskRepository
import com.zenodotus.ringer.ui.theme.RingerTheme
import com.zenodotus.ringer.viewmodels.ColorSettingsViewModel
import com.zenodotus.ringer.viewmodels.LoginViewModel
import com.zenodotus.ringer.viewmodels.MedalViewModel
import com.zenodotus.ringer.viewmodels.PermissionsViewModel
import com.zenodotus.ringer.viewmodels.TaskViewModel
import com.zenodotus.ringer.viewmodels.TrophyViewModel
import com.zenodotus.ringer.viewmodels.UserSettingsViewModel
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskViewModel
    private lateinit var permissionsViewModel: PermissionsViewModel

    private val authViewModel: LoginViewModel by lazy {
        (application as MyApp).authViewModel
    }


    private val medalViewModel: MedalViewModel by lazy {
        (application as MyApp).medalViewModel
    }

    private val trophyViewModel: TrophyViewModel by lazy {
        (application as MyApp).trophyViewModel
    }

    private val masterSettingsViewModel: UserSettingsViewModel by lazy {
        (application as MyApp).masterSettingsViewModel
    }

    private lateinit var settingsViewModel: ColorSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = AppDatabase.getDatabase(this).taskDao()
        val medalDao = AppDatabase.getDatabase(this).medalDao()
        alarmScheduler = AlarmScheduler(this)
        repository = TaskRepository(dao, alarmScheduler)
        viewModel = TaskViewModel(application, repository)
        permissionsViewModel = PermissionsViewModel(application)
        settingsViewModel = ColorSettingsViewModel(application)



        enableEdgeToEdge()


        setContent {
            RingerTheme {
                AppNavHost(
                    authViewModel = authViewModel,
                    taskViewModel = viewModel,
                    permissionsViewModel = permissionsViewModel,
                    settingsViewModel = settingsViewModel,
                    medalViewModel = medalViewModel,
                    trophyViewModel = trophyViewModel,
                    masterSettingsViewModel = masterSettingsViewModel,
                    onRequestNotification = { handleNotificationPermission() },
                    onRequestBattery = { requestIgnoreBatteryOptimizations() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionsViewModel.refresh()
    }

    // ---------- PERMISSIONS ----------

    private val notificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun handleNotificationPermission() {
        openNotificationSettings(this)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // pre-Android 13: altijd toegestaan
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        startActivity(intent)
    }

    private fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }


    override fun onStart() {
        super.onStart()
        AppVisibilityTracker.isForeground = true
    }

    override fun onStop() {
        super.onStop()
        AppVisibilityTracker.isForeground = false
    }
}


@Composable
fun AppNavHost(
    authViewModel: LoginViewModel,
    taskViewModel: TaskViewModel,
    permissionsViewModel: PermissionsViewModel,
    settingsViewModel: ColorSettingsViewModel,
    medalViewModel: MedalViewModel,
    trophyViewModel: TrophyViewModel,
    masterSettingsViewModel: UserSettingsViewModel,
    onRequestNotification: () -> Unit,
    onRequestBattery: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext as Application
    val db = AppDatabase.getDatabase(context)
    val taskDao = db.taskDao()
    val dao = AppDatabase.getDatabase(context).taskDao()

    NavHost(navController, startDestination = "choose") {

        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        composable("main") {
            MainScreen(
                navController = navController,
                viewModel = taskViewModel,
                settingsViewModel = settingsViewModel,
                medalViewModel = medalViewModel,
                trophyViewModel = trophyViewModel,
                permissionsViewModel = permissionsViewModel,
                masterSettingsViewModel = masterSettingsViewModel,
                onRequestNotification = onRequestNotification,
                onRequestBattery = onRequestBattery
            )
        }

        composable("choose") {
            ChooseScreen(
                Modifier,
                navController,
                taskDao,
                authViewModel,
                taskViewModel,
                permissionsViewModel,
                settingsViewModel,
                medalViewModel,
                trophyViewModel,
                masterSettingsViewModel,
                onRequestNotification,
                onRequestBattery
            )
        }


    }
}


@Composable
fun ChooseScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    taskDao: TaskDao,
    authViewModel: LoginViewModel,
    viewModel: TaskViewModel,
    permissionsViewModel: PermissionsViewModel,
    settingsViewModel: ColorSettingsViewModel,
    medalViewModel: MedalViewModel,
    trophyViewModel: TrophyViewModel,
    masterSettingsViewModel: UserSettingsViewModel,
    onRequestNotification: () -> Unit,
    onRequestBattery: () -> Unit
) {

    val userPrefs = UserPreferences(LocalContext.current)
    var isLoggedIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isLoggedIn = userPrefs.isLoggedIn()
    }
    if (isLoggedIn) {
        MainScreen(
            navController = navController,
            viewModel = viewModel,
            settingsViewModel = settingsViewModel,
            medalViewModel = medalViewModel,
            trophyViewModel = trophyViewModel,
            permissionsViewModel = permissionsViewModel,
            masterSettingsViewModel = masterSettingsViewModel,
            onRequestNotification = onRequestNotification,
            onRequestBattery = onRequestBattery
        )
    } else {
        LoginScreen(navController, authViewModel)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    ViewModel: TaskViewModel,
    medalViewModel: MedalViewModel,
    trophyViewModel: TrophyViewModel,
    onMenuClick: () -> Unit
) {
    val database = AppDatabase.getDatabase(LocalContext.current)
    val userPrefs = UserPreferences(LocalContext.current)
    var user by remember { mutableStateOf<User?>(null) }
    var avatar by remember { mutableStateOf("neutral.png") }
    LaunchedEffect(avatar) {
        user = database.userDao().getUser(userPrefs.getUsername() as String)

    }
    val trophies by trophyViewModel.trophies.collectAsState()
    var isDialogOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dayMedals by medalViewModel.dayMedals.collectAsState()
    val refreshTrigger by medalViewModel.refreshTrigger.collectAsState()
    val userName = user?.userName
    if (userName != null) {
        LaunchedEffect(refreshTrigger) {
            medalViewModel.medalsToday(userName, LocalDate.now())
        }
    }
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gebruiker icoon
                avatar = user?.avatar ?: "neutral.png"
                Image(
                    painter = rememberAsyncImagePainter(
                        if (avatar.startsWith("content://"))
                            avatar
                        else
                            "file:///android_asset/avatars/${avatar}"
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)  // dezelfde grootte als het icoon
                        .clickable { isDialogOpen = true }
                        .clip(CircleShape), // optioneel: rond maken

                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Gebruikersnaam
                Text(text = user?.userName ?: "Geen gebruikersnaam")
                Spacer(modifier = Modifier.width(8.dp))
                val cumulativeTrophy = trophies
                    .firstOrNull { it.trophyType == "cumulative" }
                /*Log.d("myapp", "cumulativeTrophy: $cumulativeTrophy")
                Log.d("myapp", "badge: ${cumulativeTrophy!!.trophyName}")*/
                cumulativeTrophy?.let { trophy ->
                    val badge = rememberAsyncImagePainter(
                        "file:///android_asset/badges/${trophy.trophyName.lowercase()}.png"
                    )
                    Image(
                        painter = badge,
                        contentDescription = "Medal",
                        modifier = Modifier
                            .size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                val medalPainter = rememberAsyncImagePainter("file:///android_asset/medal.svg")
                if (dayMedals <= 5) {
                    repeat(dayMedals) {
                        Image(
                            painter = medalPainter,
                            contentDescription = "Medal",
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = medalPainter,
                            contentDescription = "Medal",
                            modifier = Modifier.size(24.dp) // pas grootte naar wens aan
                        )
                        Text(
                            text = "$dayMedals x",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }


            }

        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    )

    if (isDialogOpen) {
        AvatarPickerScreen(
            onDismiss = { isDialogOpen = false },
            onAvatarChosen = { uri ->
                avatar = uri.toString()
                user!!.userName.let { username ->
                    scope.launch {
                        database.userDao().changeAvatar(username, avatar)
                    }
                }
                isDialogOpen = false
            }
        )
    }
}


@Composable
fun MainScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    settingsViewModel: ColorSettingsViewModel,
    permissionsViewModel: PermissionsViewModel,
    medalViewModel: MedalViewModel,
    trophyViewModel: TrophyViewModel,
    masterSettingsViewModel: UserSettingsViewModel,
    archived: Boolean = false,
    onRequestNotification: () -> Unit,
    onRequestBattery: () -> Unit,
    modifier: Modifier = Modifier

) {
    val database = AppDatabase.getDatabase(LocalContext.current)
    var showAddTask by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val userPrefs = UserPreferences(LocalContext.current)
    var user by remember { mutableStateOf<User?>(null) }
    val argon2 = Argon2Kt()
    val salt = "somesalt".toByteArray()
    LaunchedEffect(user) {
        user = database.userDao().getUser(userPrefs.getUsername() as String)

    }
    var showSetMasterPassDialog by remember { mutableStateOf(false) }
    var showEnterMasterPassDialog by remember { mutableStateOf(false) }
    var enteredPass by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.75f),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Hoofdscherm") },
                    label = { Text("Hoofdscherm") },
                    selected = false,
                    onClick = {
                        innerNavController.navigate("home")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Instellingen") },
                    label = { Text("Instellingen") },
                    selected = false,
                    onClick = {
                        innerNavController.navigate("settings")
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Trofeeën") },
                    label = { Text("Trofeeën") },
                    selected = false,
                    onClick = {
                        innerNavController.navigate("trophies")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Activaties") },
                    label = { Text("Activaties") },
                    selected = false,
                    onClick = {
                        innerNavController.navigate("Activations")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Archive, contentDescription = "Archief") },
                    label = { Text("Archief") },
                    selected = false,
                    onClick = {
                        innerNavController.navigate("archive")
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Master activaties") },
                    label = { Text("Master activaties") },
                    selected = false,
                    onClick = {
                        when {
                            user == null -> {
                                // eventueel naar login navigeren
                            }

                            user!!.masterPass == null || user!!.masterPass == "" -> {
                                showSetMasterPassDialog = true
                            }

                            else -> {
                                showEnterMasterPassDialog = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    viewModel,
                    medalViewModel,
                    trophyViewModel,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            },
            floatingActionButton = {
                if (currentRoute == "home") {
                    FloatingActionButton(
                        onClick = { showAddTask = true },
                        containerColor = settingsViewModel.getColor("add_color"),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Voeg toe"
                        )
                    }
                }
            }

        ) { innerPadding ->
            NavHost(
                innerNavController, startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(route = "home") {
                    HomeScreen(
                        viewModel,
                        settingsViewModel,
                        medalViewModel,
                        trophyViewModel,
                        false
                    )
                }
                composable("activations") {
                    PermissionsScreen(
                        navController = navController,
                        viewModel = permissionsViewModel,
                        onRequestNotification = onRequestNotification,
                        onRequestBattery = onRequestBattery
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        navController = navController,
                        viewModel = settingsViewModel,
                        userSettingsViewModel = masterSettingsViewModel
                    )
                }

                composable(route = "trophies") {
                    TrophiesScreen(
                        navController = navController,
                        viewModel = trophyViewModel,
                        medalViewModel = medalViewModel,
                        taskViewModel = viewModel
                    )
                }

                composable(route = "master") {
                    MasterSettingsScreen(
                        navController = navController,
                        viewModel = masterSettingsViewModel,
                        colorViewModel = settingsViewModel


                    )
                }
                composable(route = "archive") {
                    HomeScreen(
                        viewModel,
                        settingsViewModel,
                        medalViewModel,
                        trophyViewModel,
                        true
                    )
                }
            }

        }


    }
    if (showAddTask) {
        AddTask(null, viewModel, trophyViewModel, { showAddTask = false })
        //PictoSelectorModal(onSelect = {}, onDismiss = { showAddTask = false })
    }

    if (showSetMasterPassDialog) {
        AlertDialog(
            onDismissRequest = { showSetMasterPassDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("MasterPass instellen") },
            text = {
                Column {
                    OutlinedTextField(
                        value = enteredPass,
                        onValueChange = { enteredPass = it },
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Nieuwe MasterPass") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (enteredPass.isNotEmpty()) {
                        user?.masterPass = enteredPass  // simpele demo, normaal via ViewModel
                        val passwordHash = argon2.hash(
                            Argon2Mode.ARGON2_I,
                            enteredPass.toByteArray(),
                            salt,
                            5,
                            65536
                        )
                        scope.launch {
                            database.userDao().setMasterPassword(
                                user!!.userName,
                                passwordHash.encodedOutputAsString()
                            )
                        }
                        enteredPass = ""
                        showSetMasterPassDialog = false
                    }
                }) {
                    Text("Opslaan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSetMasterPassDialog = false
                }) {
                    Text("Annuleren")
                }
            }
        )
    }

    // Dialog om masterPass in te voeren
    if (showEnterMasterPassDialog) {
        AlertDialog(
            onDismissRequest = {
                showEnterMasterPassDialog = false
                errorMessage = ""
                enteredPass = ""
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("MasterPass invoeren") },
            text = {
                Column {
                    OutlinedTextField(
                        value = enteredPass,
                        onValueChange = { enteredPass = it },
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("MasterPass") }
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val isValid = argon2.verify(
                        Argon2Mode.ARGON2_I,
                        user!!.passwordHash,
                        enteredPass.toByteArray()
                    )
                    if (isValid) {
                        showEnterMasterPassDialog = false
                        enteredPass = ""
                        errorMessage = ""

                        innerNavController.navigate("master")
                        scope.launch { drawerState.close() }
                    } else {
                        errorMessage = "Verkeerd wachtwoord"
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEnterMasterPassDialog = false
                    enteredPass = ""
                    errorMessage = ""
                }) {
                    Text("Annuleren")
                }
            }
        )
    }

}

@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    settingsViewModel: ColorSettingsViewModel,
    medalViewModel: MedalViewModel,
    trophyViewModel: TrophyViewModel,
    archived: Boolean
) {
    val userPrefs = UserPreferences(LocalContext.current)
    LaunchedEffect(Unit) {
        val userName = userPrefs.getUsername() // Zorg dat je hier bij kunt
        viewModel.loadTasks(userName)
    }
    val taskList = viewModel.tasks
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {// Gebruik nu 'taskList' (de gewone lijst) in plaats van 'viewModel.tasks'
        items(taskList) { task ->
            if (!archived) {
                if (!task.archived) {
                    TaskItem(
                        task,
                        viewModel,
                        medalViewModel,
                        settingsViewModel,
                        trophyViewModel
                    )
                }
            } else {
                if (task.archived) {
                    TaskItem(
                        task,
                        viewModel,
                        medalViewModel,
                        settingsViewModel,
                        trophyViewModel,
                        true
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task, viewModel: TaskViewModel,
    medalViewModel: MedalViewModel,
    settingsViewModel: ColorSettingsViewModel,
    trophyViewModel: TrophyViewModel,
    archived: Boolean = false
) {
    val context = LocalContext.current
    var showEditTask by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    val trophies by trophyViewModel.trophies.collectAsState()
    val trophy = trophies.firstOrNull { it.taskId == task.taskId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                showStatistics = true
            },
        colors = CardDefaults.cardColors(
            containerColor = settingsViewModel.getColor("background_color")
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // toon picto als er één is
            if (!task.picto.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter("file:///android_asset/pictos/${task.picto}"),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(60.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                TaskWithBadge(task, trophy)
                Text(
                    text = "${dayNumberToName(task.day)}, ${
                        task.time.format(
                            DateTimeFormatter.ofPattern(
                                "HH:mm"
                            )
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Frequentie: ${task.frequency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Toegewezen aan: ${task.assignedToUserName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(12.dp))


            IconButton(
                onClick = {
                    showEditTask = true

                }) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Bewerken",

                    )
            }
            IconButton(
                onClick = {
                    viewModel.deleteTask(task.taskId)
                    trophyViewModel.deleteTrophy(task.taskId)

                }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Verwijder",

                    )
            }

            IconButton(
                onClick = {
                    val updatedTask = task.copy(
                        archived = !archived
                    )
                    viewModel.updateTask(updatedTask)


                }) {
                Icon(
                    imageVector = if (archived) Icons.Default.Unarchive else Icons.Default.Archive,
                    contentDescription = "Archiveer",

                    )
            }

        }
    }
    if (showEditTask) {
        AddTask(task, viewModel, trophyViewModel, onDismiss = { showEditTask = false })
        //PictoSelectorModal(onSelect = {}, onDismiss = { showAddTask = false })
    }
    if (showStatistics) {
        ModalStatisticsTask(
            task.taskId,
            viewModel,
            medalViewModel,
            trophyViewModel,
            onDismiss = { showStatistics = false })
    }
}

@Composable
fun TaskWithBadge(task: Task, trophy: TrophyWithTaskName?) {

    val enumTrophy = trophy?.let { t ->
        EnumTrophy.entries.firstOrNull { it.name == t.trophyName }
    }

    if (enumTrophy == null || enumTrophy.typeName == "INIT_STREAK") {
        // Geen badge → gewoon tekst
        Text(
            text = task.task,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        return
    }

    val inlineContentId = "badge"

    val annotatedText = buildAnnotatedString {
        append(task.task)
        append(" ") // kleine spacing
        appendInlineContent(inlineContentId, "[badge]")
    }

    val inlineContent = mapOf(
        inlineContentId to InlineTextContent(
            Placeholder(
                width = 14.sp,
                height = 14.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            val painter = rememberAsyncImagePainter(
                "file:///android_asset/badges/${enumTrophy.assetPath}"
            )

            Image(
                painter = painter,
                contentDescription = enumTrophy.title,
                modifier = Modifier.size(14.dp)
            )
        }
    )

    Text(
        text = annotatedText,
        inlineContent = inlineContent,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

fun dayNumberToName(day: Int): String =
    when (day) {
        1 -> "Maandag"
        2 -> "Dinsdag"
        3 -> "Woensdag"
        4 -> "Donderdag"
        5 -> "Vrijdag"
        6 -> "Zaterdag"
        7 -> "Zondag"
        else -> "Onbekend"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTask(
    taskToEdit: Task? = null,
    viewModel: TaskViewModel,
    trophyViewModel: TrophyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var task by remember { mutableStateOf(taskToEdit?.task ?: "") }
    var selectedFrequency by remember { mutableStateOf(taskToEdit?.frequency ?: "Dagelijks") }
    var frequencyValue by remember { mutableStateOf(taskToEdit?.frequencyValue ?: 0) }
    var selectedDay by remember {
        mutableStateOf(
            taskToEdit?.day ?: LocalDate.now().dayOfWeek.value
        )
    }
    var timeText by remember { mutableStateOf(taskToEdit?.time ?: LocalTime.now()) }
    var expanded by remember { mutableStateOf(false) }
    var expandedDay by remember { mutableStateOf(false) }
    var showPicto by remember { mutableStateOf(false) }
    var selectedPicto by remember { mutableStateOf<String?>(taskToEdit?.picto ?: null) }
    val userPrefs = UserPreferences(LocalContext.current)
    //val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    //val ringtone: Ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
    val dayNames = listOf(
        "Maandag",
        "Dinsdag",
        "Woensdag",
        "Donderdag",
        "Vrijdag",
        "Zaterdag",
        "Zondag"
    )
    val selectedDayText = dayNames[selectedDay - 1]

    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Vul het formulier in", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = task,
                    onValueChange = { task = it },
                    label = { Text("Naam") },
                    trailingIcon = {
                        IconButton(onClick = { showPicto = true })
                        {
                            val context = LocalContext.current

                            val bitmap = remember(selectedPicto) {
                                if (selectedPicto != null) {
                                    try {
                                        val stream = context.assets.open("pictos/${selectedPicto}")
                                        BitmapFactory.decodeStream(stream)
                                    } catch (e: IOException) {
                                        null
                                    }
                                } else null
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(Icons.Default.Face, contentDescription = null)
                            }
                        }
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDay,
                    onExpandedChange = { expandedDay = it },
                    modifier = Modifier.background(Color.Transparent)
                ) {
                    TextField(
                        value = selectedDayText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dag") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDay,
                        onDismissRequest = { expandedDay = false }
                    ) {
                        dayNames.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedDay = index + 1
                                    expandedDay = false
                                },
                                text = { Text(name) })
                        }
                    }
                }
                TimePickerField(
                    tijd = timeText,
                    onTimeChange = { newTime -> timeText = newTime }
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },

                        ) {
                        TextField(
                            value = selectedFrequency,
                            onValueChange = { selectedFrequency = it },
                            readOnly = true,
                            label = { Text("Frequentie") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf(
                                "Eenmalig",
                                "Dagelijks",
                                "Wekelijks",
                                "Maandelijks",
                                "Jaarlijks",
                                "Aangepast"
                            ).forEach { option ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedFrequency = option
                                        expanded = false
                                    },
                                    text = { Text(option) }
                                )
                            }
                        }
                    }

                    if (selectedFrequency == "Aangepast") {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = if (frequencyValue > 0) frequencyValue.toString() else "",
                            onValueChange = { input ->
                                frequencyValue = input.toIntOrNull()?.coerceAtLeast(1) ?: 0
                            },
                            label = { Text("Aantal dagen") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }


                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Annuleer")
                    }
                    if (taskToEdit == null) {
                        Button(onClick = {
                            scope.launch {
                                val newTask = Task(
                                    task = task,
                                    picto = selectedPicto,
                                    day = selectedDay,
                                    time = timeText,
                                    frequency = selectedFrequency,
                                    frequencyValue = if (selectedFrequency == "Aangepast") frequencyValue else 0,
                                    assignedToUserName = userPrefs.getUsername()!!
                                )
                                val taskWithId = viewModel.addTask(newTask)
                                if (taskWithId.frequency != "Eenmalig" && (taskWithId.frequencyValue == 0 || taskWithId.frequencyValue > 1)) {
                                    trophyViewModel.awardTrophy(
                                        userPrefs.getUsername()!!,
                                        taskWithId.taskId,
                                        "streak",
                                        "INIT_STREAK",
                                        0
                                    )

                                }

                                //ringtone.play()
                                onDismiss()
                            }
                        }) {
                            Text("Opslaan")
                        }
                    } else {
                        Button(onClick = {
                            scope.launch {
                                val updatedTask = taskToEdit.copy(
                                    task = task,
                                    picto = selectedPicto,
                                    day = selectedDay,
                                    time = timeText,
                                    frequency = selectedFrequency,
                                    frequencyValue = if (selectedFrequency == "Aangepast") frequencyValue else 0,
                                    assignedToUserName = userPrefs.getUsername()!!
                                )
                                viewModel.updateTask(updatedTask)
                                //ringtone.play()
                                onDismiss()
                            }
                        }) {
                            Text("Aanpassen")
                        }
                    }
                }
            }
        }
    }
    if (showPicto) {
        PictoSelectorModal(
            onSelect = { fileName ->
                selectedPicto = fileName

                showPicto = false
            }, onDismiss = { showPicto = false })
    }
}

@Composable
fun TimePickerField(
    tijd: LocalTime,
    onTimeChange: (LocalTime) -> Unit
) {
    var timeText by remember { mutableStateOf(tijd.format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var showDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = timeText,
        onValueChange = { timeText = it },
        label = { Text("Tijd") },
        readOnly = true,
        modifier = Modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    showDialog = true
                    focusManager.clearFocus()
                }
            }
    )

    if (showDialog) {
        TimePickerModal(
            onConfirm = { hour, minute ->
                val newTime = LocalTime.of(hour, minute)
                timeText = newTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                onTimeChange(newTime)  // <-- geef de nieuwe tijd terug
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = LocalTime.now()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surface,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,

                        selectorColor = MaterialTheme.colorScheme.primary,

                        containerColor = MaterialTheme.colorScheme.surface,

                        periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,

                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onDismiss) {
                        Text("Annuleer")
                    }
                    Button(onClick = {
                        onConfirm(timePickerState.hour, timePickerState.minute)
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictoSelectorModal(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val assetManager = context.assets
    var searchText by remember { mutableStateOf("") }
    // volledige lijst van bestandsnamen, omgezet naar List zodat drop/take werken
    val allFiles = remember { assetManager.list("pictos")?.sorted()?.toList() ?: emptyList() }
    val filteredFiles = remember(searchText, allFiles) {
        allFiles.filter {
            it.contains(searchText, ignoreCase = true)
        }
    }

    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 9

    // alleen decoderen van de huidige pagina, gecached zolang de pagina niet verandert
    val pageItemsWithBitmaps = remember(filteredFiles, currentPage) {
        filteredFiles
            .drop(currentPage * pageSize)
            .take(pageSize)
            .associateWith { fileName ->
                assetManager.open("pictos/$fileName").use { BitmapFactory.decodeStream(it) }
            }
    }

    val pictosDisplayNames =
        pageItemsWithBitmaps.keys.associateWith { it.substringBeforeLast(".").replace("_", " ") }
    LaunchedEffect(searchText) {
        currentPage = 0
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Zoeken") },
                    readOnly = false,

                    )

                Spacer(modifier = Modifier.height(16.dp))

                // de grid van de huidige pagina
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pageItemsWithBitmaps.entries.toList()) { (fileName, bitmap) ->
                        val displayName = pictosDisplayNames[fileName]!!

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onSelect(fileName) }
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = displayName,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayName,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // navigatieknopjes onderaan
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) {
                        Text("Vorige")
                    }
                    Button(
                        onClick = { if ((currentPage + 1) * pageSize < allFiles.size) currentPage++ },
                        enabled = (currentPage + 1) * pageSize < allFiles.size
                    ) {
                        Text("Volgende")
                    }
                }
            }
        }
    }
}


/*@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RingerTheme {
        MainScreen()
    }
}*/