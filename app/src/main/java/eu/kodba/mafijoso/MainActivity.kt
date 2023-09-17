package eu.kodba.mafijoso

import android.R.attr.textAlignment
import android.R.attr.value
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.kodba.mafijoso.ui.theme.MafijosoTheme
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    var textToSpeech: TextToSpeech? = null

    private val viewModel: MyViewModel by viewModels()
    lateinit var resources2:Resources

    fun changeLocale(){
        val config = resources.configuration
        //var lang:Boolean = PreferenceManager.getDefaultSharedPreferences(c).getBoolean(MySettingsFragment.UI_LANGUAGE_TOGGLE, false)
        val langstr = if (viewModel.lang) "hr" else "en"
        val locale = Locale(langstr)
        Locale.setDefault(locale)
        textToSpeech?.language = locale
        config.setLocale(locale)

        createConfigurationContext(config)
        resources2 = Resources(assets, resources.displayMetrics, config);
        resources.updateConfiguration(config, resources.displayMetrics)


        viewModel.translatedMaleDeaths = resources2.getStringArray(R.array.deaths_male).toList()
        viewModel.translatedFemaleDeaths = resources2.getStringArray(R.array.deaths_female).toList()
        viewModel.translatedMaleCures = resources2.getStringArray(R.array.cures_male).toList()
        viewModel.translatedFemaleCures = resources2.getStringArray(R.array.cures_female).toList()

        viewModel.translatedSpeaks = mapOf(
            SpeakTranslations.EVERYONE_WAKEUP to resources2.getString(R.string.everyone_wakeup),
            SpeakTranslations.NOBODY_KILLED to resources2.getString(R.string.nobody_killed),
            SpeakTranslations.EVERYONE_SLEEP to resources2.getString(R.string.everyone_sleep),
            SpeakTranslations.X_WAKEUP to resources2.getString(R.string.x_wakeup),
            SpeakTranslations.X_SLEEP to resources2.getString(R.string.x_sleep),
            SpeakTranslations.MAFIA to resources2.getString(R.string.mafia),
            SpeakTranslations.INVESTIGATOR to resources2.getString(R.string.investigator),
            SpeakTranslations.DOCTOR to resources2.getString(R.string.doctor),
            SpeakTranslations.VOTED_DIES to resources2.getString(R.string.voted_dies),
            SpeakTranslations.MAFIA_WINS to resources2.getString(R.string.mafia_wins),
            SpeakTranslations.VILLAGERS_WIN to resources2.getString(R.string.villagers_win),
            SpeakTranslations.NOBODY_VOTED_OUT to resources2.getString(R.string.nobody_voted_out),
        )
    }

    fun changeLanguage(isChecked: Boolean){
        viewModel.changeLanguage(isChecked)
        changeLocale()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        changeLocale()

        // create an object textToSpeech and adding features into it
        // create an object textToSpeech and adding features into it

        textToSpeech = TextToSpeech(applicationContext) { i ->
            // if No error is found then only it will run
            if (i != TextToSpeech.ERROR) {
                // To Choose language of speech
                //textToSpeech!!.language = Locale.ROOT//Locale("hr-HR")
                //textToSpeech!!.setSpeechRate(0.95f)

            }
        }

        viewModel.speakText.observe(this, Observer {
            textToSpeech?.speak(it as CharSequence,TextToSpeech.QUEUE_FLUSH,null,
                1.toString()
            );
        })



        setContent {
            MafijosoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Game(viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        textToSpeech?.stop()
    }

    override fun onBackPressed() {
        if(viewModel.uiState.value.stage != GameStage.CHOOSE_NAMES) {
            viewModel.togglePause()
            textToSpeech?.stop()
        } else {
            super.onBackPressed()
        }
    }

    @Composable
    private fun FinalScoreDialog(
        onPlayAgain: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val activity = (LocalContext.current as Activity)

        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onCloseRequest.
            },
            title = { Text(text = resources2.getString(R.string.pauza)) },
            text = { Text(text = resources2.getString(R.string.igra_je_pauzirana)) },
            modifier = modifier,
            dismissButton = {
                TextButton(
                    onClick = {
                        onPlayAgain()
                    }
                ) {
                    Text(text = resources2.getString(R.string.restartaj_igru))
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.pause(false) }) {
                    Text(text = resources2.getString(R.string.nastavi))
                }
            }
        )
    }


    @OptIn(ExperimentalUnitApi::class)
    @Composable
    fun Conversation(gameUiState: DiceUiState, onUserClick: (User) -> Unit) {
        for(user in gameUiState.users) {
            if(user.role == Role.MURDERER && gameUiState.stage == GameStage.MURDER) continue
            if(user.role == Role.INVESTIGATOR && gameUiState.stage == GameStage.INVESTIGATE) continue
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(onClick = { onUserClick(user) }, modifier = Modifier.fillMaxSize()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        /*Text(text = "${user.name} ${user.role} ${if(user.tmpDead) 1 else 0 }", modifier= Modifier
                        .padding(10.dp),
                        color = if(user.dead) Color(android.graphics.Color.RED) else androidx.compose.ui.graphics.Color.Unspecified, fontSize = TextUnit(20f, TextUnitType.Sp),
                    )*/
                        userText(user = user, displayRole = gameUiState.stage != GameStage.CHOOSE_NAMES && gameUiState.displayRoles)
                        if (user.selected) {
                            if (gameUiState.stage == GameStage.INVESTIGATE) {
                                Text(if (user.role == Role.MURDERER) "M" else "V", fontWeight = FontWeight.Bold,
                                    fontSize = TextUnit(20f, TextUnitType.Sp))
                                /*Icon(
                                    painter = painterResource(id = if (user.role == Role.MURDERER) R.drawable.person_alert_24px else R.drawable.how_to_reg_24px),
                                    contentDescription = if (user.role == Role.MURDERER) "mafia" else "not mafia"
                                )*/
                            } else {
                                Icon(Icons.Rounded.Check, contentDescription = "checked")
                            }
                        }
                        if (gameUiState.stage == GameStage.CHOOSE_NAMES) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Remove",
                                tint = Color.Red
                            )
                        }
                        if(gameUiState.stage == GameStage.CURE && user.cured) {
                            Icon(
                                painter = painterResource(id = R.drawable.syringe_24px),
                                contentDescription = "Already cured"
                            )
                        }
                        if (gameUiState.stage == GameStage.VOTE) {
                            Text(user.votes.toString(), fontWeight = FontWeight.Bold,
                                    fontSize = TextUnit(20f, TextUnitType.Sp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun EnterNames(gameViewModel: MyViewModel, gameUiState: DiceUiState){
        val mediumPadding = dimensionResource(R.dimen.padding_medium)
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            OutlinedTextField(
                value = gameViewModel.userGuess,
                onValueChange = { gameViewModel.onUserGuessChanged(it) },
                label = { Text(resources2.getString(R.string.players_name)) },
                singleLine = true,
                modifier= Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words
                ),
                keyboardActions = KeyboardActions(
                    onDone = { gameViewModel.addUser(gameViewModel.userGuess) }
                )
            )
            Conversation(gameUiState) {gameViewModel.removeUser(it)}
            OutlinedTextField(
                value = gameViewModel.timeDelay,
                onValueChange = { gameViewModel.onTimeDelayChanged(it) },
                label = { Text(resources2.getString(R.string.vrijeme_izme_u_igra_a)) },
                singleLine = true,
                modifier= Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
            )
            OutlinedTextField(
                value = gameViewModel.votingDuration,
                onValueChange = { gameViewModel.onVotingDurationChanged(it) },
                label = { Text(resources2.getString(R.string.discussion_time)) },
                singleLine = true,
                modifier= Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = gameUiState.dvijeMafije,
                    onCheckedChange = { isChecked -> gameViewModel.checkDvijeMafije(isChecked) }
                )
                Text(resources2.getString(R.string.two_mafias))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = gameUiState.displayRoles,
                    onCheckedChange = { isChecked -> gameViewModel.setDisplayRoles(isChecked) }
                )
                Text(resources2.getString(R.string.display_roles))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = gameUiState.language,
                    onCheckedChange = { isChecked -> changeLanguage(isChecked) }
                )
                Text(stringResource(R.string.croatian))
            }
            BigButton(resources2.getString(R.string.start_game), gameUiState.users.size >= 4){ gameViewModel.nextStage() }
        }
    }

    @Composable
    fun BigButton(text: String, enabled: Boolean, onClick: () -> Unit){
        Button(
            modifier = Modifier.fillMaxWidth(),onClick = {
                onClick()
            },
            enabled = enabled
        ) {
            Text(text, fontSize = 16.sp, modifier = Modifier.padding(15.dp))
        }
    }

    @Composable
    fun Investigate(gameViewModel: MyViewModel, gameUiState: DiceUiState){
        ChoosePlayers(gameViewModel = gameViewModel, gameUiState = gameUiState){gameViewModel.select(it)}
        val mediumPadding = dimensionResource(R.dimen.padding_medium)
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            modifier = Modifier
                .padding(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BigButton(
                resources2.getString(R.string.next),
                gameUiState.investigationOver
            ) { gameViewModel.nextStage() }
        }
    }

    @Composable
    fun ChoosePlayers(gameViewModel: MyViewModel, gameUiState: DiceUiState, onUserClick: (User) -> Unit){
        val mediumPadding = dimensionResource(R.dimen.padding_medium)
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Conversation(gameUiState){onUserClick(it)}
        }
    }

    @Composable
    fun Vote(gameViewModel: MyViewModel, gameUiState: DiceUiState, onUserClick: (User) -> Unit) {
        val mediumPadding = dimensionResource(R.dimen.padding_medium)
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gameUiState.voteText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            ChoosePlayers(gameViewModel = gameViewModel, gameUiState = gameUiState){onUserClick(it)}
            Text(
                text = formatDurationTime(gameUiState.voteCountdown.toLong()),
                style = MaterialTheme.typography.displayMedium
            )
            BigButton(text = resources2.getString(R.string.skip_voting), enabled = true) {
                gameViewModel.voteEnd()
            }
        }
    }

    fun formatDurationTime(durationSeconds: Long) =
        durationSeconds.seconds.toComponents { hours, minutes, seconds, _ ->
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds,
            )
        }

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    fun userText(user: User, displayRole: Boolean){
        Text(
            text = "${user.name} ${if (displayRole) resources2.getString(MyViewModel.roleTranslations[user.role]!!) else ""}",// ${if (user.tmpDead) 1 else 0} ${if (user.selected) 1 else 0} ${if (user.dead) 1 else 0}
            modifier = Modifier
                .padding(10.dp),
            color = if (user.dead) Color(android.graphics.Color.RED) else androidx.compose.ui.graphics.Color.Unspecified,
            fontSize = TextUnit(20f, TextUnitType.Sp),
        )
    }

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    fun VoteResults(gameViewModel: MyViewModel, gameUiState: DiceUiState, onUserClick: (User) -> Unit) {
        val mediumPadding = dimensionResource(R.dimen.padding_medium)
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gameUiState.voteResultsText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            for(user in gameUiState.users) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        userText(user = user, displayRole = gameUiState.displayRoles)
                        Text(user.votes.toString(), modifier = Modifier
                            .padding(10.dp),
                            fontSize = TextUnit(20f, TextUnitType.Sp),
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(), onClick = {
                    gameViewModel.nextStage()
                },
            ) {
                Text(resources2.getString(R.string.sljede_a_runda), fontSize = 16.sp, modifier = Modifier.padding(15.dp))
            }
        }
    }

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    fun GameOver(gameViewModel: MyViewModel, gameUiState: DiceUiState){
        Text(
            text = gameUiState.gameOver.toString(),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        for(user in gameUiState.users) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    userText(user = user, displayRole = true)
                    Text(user.votes.toString(), modifier = Modifier
                        .padding(10.dp),
                        fontSize = TextUnit(20f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(), onClick = {
                gameViewModel.reset()
            },
        ) {
            Text(resources2.getString(R.string.igraj_ponovno), fontSize = 16.sp, modifier = Modifier.padding(15.dp))
        }
    }

    @Composable
    fun ReadRoles(gameViewModel: MyViewModel, gameUiState: DiceUiState){
        val configuration = LocalConfiguration.current

        val screenHeight = configuration.screenHeightDp.dp
        val screenWidth = configuration.screenWidthDp.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .height(screenHeight-150.dp)
        ) {
            Text(
                gameUiState.currentUser,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall
            )
            if(gameUiState.currentRole != null) {
                Text(
                    text = resources2.getString(gameUiState.currentRole),
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
        loadingTrack(gameViewModel, gameUiState)
    }


    @Composable
    fun Game(gameViewModel: MyViewModel = viewModel()){
        val gameUiState by gameViewModel.uiState.collectAsState()
        gameViewModel.scroll = rememberScrollState()
        val mediumPadding = dimensionResource(R.dimen.padding_medium)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(mediumPadding)
                .verticalScroll(gameViewModel.scroll!!),
            verticalArrangement = Arrangement.spacedBy(mediumPadding)
        ){
            if(gameUiState.stage != GameStage.READ_ROLES) {
                val picker = gameViewModel.pickers[gameUiState.stage]
                if(picker != null) {
                    Text(
                        text = resources2.getString(picker),
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                    )
                }
                val pickerHelper = gameViewModel.pickerHelper[gameUiState.stage]
                if(pickerHelper != null) {
                    Text(
                        resources2.getString(pickerHelper),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            when(gameUiState.stage){
                GameStage.CHOOSE_NAMES -> EnterNames(gameViewModel = gameViewModel, gameUiState = gameUiState)
                GameStage.READ_ROLES -> ReadRoles(gameViewModel = gameViewModel, gameUiState = gameUiState)
                GameStage.MURDER -> ChoosePlayers(gameViewModel = gameViewModel, gameUiState = gameUiState){gameViewModel.select(it)}
                GameStage.CURE -> ChoosePlayers(gameViewModel = gameViewModel, gameUiState = gameUiState){gameViewModel.select(it)}
                GameStage.INVESTIGATE -> Investigate(gameViewModel = gameViewModel, gameUiState = gameUiState)
                GameStage.VOTE -> Vote(gameViewModel = gameViewModel, gameUiState = gameUiState){gameViewModel.vote(it)}
                GameStage.VOTE_RESULTS -> VoteResults(gameViewModel = gameViewModel, gameUiState = gameUiState){}
                GameStage.GAME_OVER -> GameOver(gameViewModel = gameViewModel, gameUiState = gameUiState)
                else -> {}
            }
            if(gameUiState.pause) FinalScoreDialog(onPlayAgain = {viewModel.reset()})
            //Text(text=gameUiState.toString())
        }

    }

    @Composable
    fun loadingTrack(gameViewModel: MyViewModel, gameUiState: DiceUiState){
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val screenWidth = configuration.screenWidthDp.dp

        val transition = updateTransition(gameViewModel.openClosed, label = "box state")
        val percentage by transition.animateFloat(label = "rectangle", transitionSpec = {
            when {
                BoxState.Expanded isTransitioningTo BoxState.Collapsed ->
                    tween(durationMillis = 1000)
                else ->
                    tween(durationMillis = gameViewModel.animationDuration, easing = LinearEasing)
            }})
        { state ->
            when (state) {
                BoxState.Collapsed -> 0f
                BoxState.Expanded -> 1f
            }
        }
        Box(modifier = Modifier
            .width(screenWidth * percentage)
            .height(50.dp)
            .background(if(gameUiState.currentRole == R.string.mafia) Color.Red else MaterialTheme.colorScheme.primary))
    }

}


enum class BoxState {
    Collapsed,
    Expanded
}