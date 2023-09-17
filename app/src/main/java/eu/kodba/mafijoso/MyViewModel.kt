package eu.kodba.mafijoso

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.kodba.mafijoso.DeathsAndCures.Companion.cures
import eu.kodba.mafijoso.DeathsAndCures.Companion.deaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MyViewModel : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    var userGuess by mutableStateOf("")
        private set

    var timeDelay by mutableStateOf("3")
        private set

    var votingDuration by mutableStateOf("90")
        private set

    var lang: Boolean = false

    var maxKillPerRound = 1
    var maxCurePerRound = 1
    var maxInvestigatePerRound = 1

    var countdownCoroutine: Job? = null
    var speechCoroutine: Job? = null

    var speakText = MutableLiveData<String>()

    var skipAnnounce = false

    var scroll: ScrollState? = null
    var lastDeathReason = ""
    var openClosed: BoxState = BoxState.Collapsed
    var animationDuration: Int = 1000

    var readNext: String = ""

    var translatedMaleDeaths: List<String> = listOf()
    var translatedFemaleDeaths: List<String> = listOf()
    var translatedMaleCures: List<String> = listOf()
    var translatedFemaleCures: List<String> = listOf()

    var translatedSpeaks: Map<SpeakTranslations, String> = mapOf()

    fun assignRoles(){
        val roles = mutableListOf(Role.VILLAGER, Role.MURDERER, Role.INVESTIGATOR,
            Role.DOCTOR
        ).apply {
            if(uiState.value.dvijeMafije) add(Role.MURDERER)
            repeat(uiState.value.users.size-this.size){add(Role.VILLAGER)}
            shuffle()
        }
        _uiState.update { currentState ->
            currentState.copy(
                users = currentState.users.mapIndexed { index, user -> user.copy(role = roles[index]) }
            )
        }
    }

    // Handle business logic
    fun addUser(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                users = currentState.users.plus(User(name))
            )
        }
        userGuess = ""
    }

    fun select(user: User){
        if(user.dead) return
        if(uiState.value.stage == GameStage.CURE && user.cured) return
        if(uiState.value.stage == GameStage.INVESTIGATE){
            if(uiState.value.investigationOver) return
            _uiState.update { currentState ->
                currentState.copy(
                    users = currentState.users.toMutableList().apply {
                        set(
                            currentState.users.indexOfFirst { it.name == user.name },
                            user.copy(selected = true)
                        )
                    },
                    investigationOver = uiState.value.users.count{it.selected}+1

                            == maxInvestigatePerRound
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    users = currentState.users.toMutableList().apply {
                        set(
                            currentState.users.indexOfFirst { it.name == user.name },
                            user.copy(selected = !user.selected)
                        )
                    }
                )
            }
            checkSelect()
        }
    }

    fun voteEnd(){
        countdownCoroutine?.cancel()
        val itWillSpeak = StringBuilder()
        // svi su glasali
        val bestVotes = uiState.value.users.sortedByDescending { it.votes }.take(2)
        if(bestVotes[0].votes == bestVotes[1].votes){
            // izjednačeno
            _uiState.update { currentState ->
                currentState.copy(
                    userVotedOut = null
                )
            }
            itWillSpeak.append(translatedSpeaks[SpeakTranslations.NOBODY_VOTED_OUT])
            //itWillSpeak.append("Nobody was voted out.")
        } else {
            // kill that one

            // provjeri ako je igra završena
            if(uiState.value.users.count{ !it.dead && it.role == Role.MURDERER } == 1 && bestVotes[0].role == Role.MURDERER){
                _uiState.update { currentState ->
                    currentState.copy(
                        //gameOver = "Villagers win!"
                        gameOver = translatedSpeaks[SpeakTranslations.VILLAGERS_WIN],
                        users = currentState.users.toMutableList().map{ user -> user.copy(dead = (user.dead || user == bestVotes[0]))},
                        userVotedOut = bestVotes[0]
                    )
                }
                itWillSpeak.append(translatedSpeaks[SpeakTranslations.VOTED_DIES]?.replace(";", bestVotes[0].name) + translatedSpeaks[SpeakTranslations.VILLAGERS_WIN])
                //itWillSpeak.append("${bestVotes[0].name} was voted out. Villagers win!")
            } else if(uiState.value.users.count{ !it.dead && it.role != Role.MURDERER && it.name != bestVotes[0].name} <= uiState.value.users.count{ !it.dead && it.role == Role.MURDERER && it.name != bestVotes[0].name}){
                _uiState.update { currentState ->
                    currentState.copy(
                        gameOver = translatedSpeaks[SpeakTranslations.MAFIA_WINS],
                        //gameOver = "Mafia wins!"
                        users = currentState.users.toMutableList().map{ user -> user.copy(dead = (user.dead || user == bestVotes[0]))},
                        userVotedOut = bestVotes[0]
                    )
                }
                itWillSpeak.append(translatedSpeaks[SpeakTranslations.VOTED_DIES]?.replace(";", bestVotes[0].name) + translatedSpeaks[SpeakTranslations.MAFIA_WINS])
                //itWillSpeak.append("${bestVotes[0].name} was voted out. Mafia wins!")
            } else {
                itWillSpeak.append(translatedSpeaks[SpeakTranslations.VOTED_DIES]?.replace(";", bestVotes[0].name))
                //itWillSpeak.append("Izglasano je da ${bestVotes[0].name} umire.")
                //itWillSpeak.append("${bestVotes[0].name} was voted out.")
                _uiState.update { currentState ->
                    currentState.copy(
                        users = currentState.users.toMutableList().map{ user -> user.copy(dead = (user.dead || user == bestVotes[0]))},
                        userVotedOut = bestVotes[0]
                    )
                }
            }
        }
        speakText.value = itWillSpeak.toString()
        _uiState.update { currentState ->
            currentState.copy(
                voteResultsText = itWillSpeak.toString()
            )
        }
        nextStage()
    }

    fun pause(yesno: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                pause = yesno
            )
        }
    }

    fun togglePause(){
        _uiState.update { currentState ->
            currentState.copy(
                pause = !currentState.pause
            )
        }
    }

    fun reset(){
        setStage(GameStage.CHOOSE_NAMES)
        speechCoroutine?.cancel()
        countdownCoroutine?.cancel()
        openClosed = BoxState.Collapsed
        _uiState.update { currentState ->
            DiceUiState().copy(
                users = currentState.users.toMutableList().map{
                    it.copy(dead = false, tmpDead = false, selected = false, votes = 0, cured = false, tmpCured = false, deadHow = null, curedHow = null) },
                language = currentState.language
            )
        }
    }

    fun vote(user: User){
        _uiState.update { currentState ->
            currentState.copy(
                users = currentState.users.toMutableList().apply{set(currentState.users.indexOfFirst { it.name == user.name }, user.copy(votes = user.votes+1))}
            )
        }
        if(uiState.value.users.sumOf {it.votes } == uiState.value.users.count{!it.dead}){
            voteEnd()
        }
    }

    fun checkSelect(){
        // provjeri je li odabrano dovoljno ljudi (posebno za ubiti, liječiti, inspektirati)
        when(uiState.value.stage){
            GameStage.MURDER -> {
                if(uiState.value.users.count{it.selected} == maxKillPerRound){
                    _uiState.update { currentState ->
                        currentState.copy(
                            users = currentState.users.toMutableList().map{ if(it.selected) it.copy(selected = false, tmpDead = true) else it}
                        )
                    }
                    nextStage()
                }
            }
            GameStage.CURE -> {
                if(uiState.value.users.count{it.selected} == maxCurePerRound){
                    _uiState.update { currentState ->
                        currentState.copy(
                            users = currentState.users.toMutableList().map{
                                if(it.selected) {
                                    it.copy(selected = false, tmpCured = true)//tmpDead = false, cured = true
                                } else it
                            }
                        )
                    }
                    nextStage()
                }
            }

            else -> {}
        }
    }

    fun removeUser(user: User){
        _uiState.update { currentState ->
            currentState.copy(
                users = currentState.users.filter { it.name != user.name }
            )
        }
    }

    fun onUserGuessChanged(text: String){
        userGuess = text
    }

    fun onTimeDelayChanged(text: String){
        timeDelay = text
    }

    fun onVotingDurationChanged(text: String){
        votingDuration = text
    }

    fun setStage(stage: GameStage){
        _uiState.update { currentState ->
            currentState.copy(
                stage = stage
            )
        }
    }

    fun checkDvijeMafije(isChecked: Boolean){
        if(uiState.value.users.size <= 4){
            //Toast.makeText(this, "Too few players", Toast.LENGTH_SHORT).show()
            return
        }
        _uiState.update { currentState ->
            currentState.copy(
                dvijeMafije = isChecked
            )
        }
    }

    fun changeLanguage(isChecked: Boolean){
        lang = isChecked
        _uiState.update { currentState ->
            currentState.copy(
                language = isChecked
            )
        }
    }

    fun setDisplayRoles(isChecked: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                displayRoles = isChecked
            )
        }
    }
    
    suspend fun myDelay(timeMillis: Long){
        delay(timeMillis)
        while(uiState.value.pause){
            myDelay(500)
        }
    }

    fun nextStage(){
        viewModelScope.launch {
            scroll?.scrollTo(0)
        }
        when(uiState.value.stage){
            GameStage.CHOOSE_NAMES -> {
                assignRoles()
                setStage(GameStage.READ_ROLES)
                speechCoroutine = viewModelScope.launch {
                    // Coroutine that will be canceled when the ViewModel is cleared.
                    speakText.value = translatedSpeaks[SpeakTranslations.EVERYONE_SLEEP]
                    if(!skipAnnounce && !uiState.value.displayRoles) {
                        myDelay(2000)
                        for (user in uiState.value.users) {

                            speakText.value = user.name
                            myDelay(1500)
                            animationDuration = 4000
                            openClosed = BoxState.Expanded
                            withContext(Dispatchers.Main) {

                                _uiState.update { currentState ->
                                    currentState.copy(
                                        currentUser = user.name,
                                        currentRole = roleTranslations.get(user.role),
                                        openClosed = BoxState.Expanded
                                    )
                                }
                            }
                            myDelay(4000)
                            openClosed = BoxState.Collapsed
                            withContext(Dispatchers.Main) {
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        currentUser = "",
                                        currentRole = null,
                                        openClosed = BoxState.Collapsed
                                    )
                                }


                            }
                            myDelay(2000)
                        }
                    }
                    withContext(Dispatchers.Main){
                        nextStage()
                    }
                }
            }
            GameStage.READ_ROLES -> {
                speechCoroutine?.cancel()
                setStage(GameStage.BLANK)
                speechCoroutine = viewModelScope.launch {
                    //myDelay(2000)
                    speakText.value = translatedSpeaks[SpeakTranslations.MAFIA] + ", " + translatedSpeaks[SpeakTranslations.X_WAKEUP]
                    withContext(Dispatchers.Main){
                        setStage(GameStage.MURDER)
                    }
                }
            }
            GameStage.MURDER -> {
                setStage(GameStage.BLANK)
                animationDuration = timeDelay.toInt()*1000
                openClosed = BoxState.Expanded
                speakText.value = translatedSpeaks[SpeakTranslations.MAFIA] + ", " + translatedSpeaks[SpeakTranslations.X_SLEEP]
                speechCoroutine?.cancel()
                speechCoroutine = viewModelScope.launch {
                    if(uiState.value.users.count{it.role == Role.DOCTOR && !it.dead} > 0) {
                        myDelay(timeDelay.toLong()*1000)
                        openClosed = BoxState.Collapsed
                        withContext(Dispatchers.Main) {
                            setStage(GameStage.CURE)
                        }
                        speakText.value = translatedSpeaks[SpeakTranslations.DOCTOR] + ", " + translatedSpeaks[SpeakTranslations.X_WAKEUP]
                    } else if(uiState.value.users.count{it.role == Role.INVESTIGATOR && !it.dead} > 0) {
                        myDelay(timeDelay.toLong()*1000)
                        openClosed = BoxState.Collapsed
                        withContext(Dispatchers.Main) {
                            setStage(GameStage.INVESTIGATE)
                        }
                        speakText.value = translatedSpeaks[SpeakTranslations.INVESTIGATOR] + ", " + translatedSpeaks[SpeakTranslations.X_WAKEUP]
                    } else {
                        startVoting()
                    }
                }
            }
            GameStage.CURE -> {
                setStage(GameStage.BLANK)
                animationDuration = timeDelay.toInt()*1000
                openClosed = BoxState.Expanded
                speechCoroutine?.cancel()
                speechCoroutine = viewModelScope.launch {
                    speakText.value = translatedSpeaks[SpeakTranslations.DOCTOR] + ", " + translatedSpeaks[SpeakTranslations.X_SLEEP]
                    if(uiState.value.users.count{it.role == Role.INVESTIGATOR && !it.dead} > 0) {
                        myDelay(timeDelay.toLong()*1000)
                        openClosed = BoxState.Collapsed
                        withContext(Dispatchers.Main) {
                            setStage(GameStage.INVESTIGATE)
                        }
                        speakText.value = translatedSpeaks[SpeakTranslations.INVESTIGATOR] + ", " + translatedSpeaks[SpeakTranslations.X_WAKEUP]
                    } else {
                        startVoting()
                    }
                }
            }
            GameStage.INVESTIGATE -> {
                speakText.value = translatedSpeaks[SpeakTranslations.INVESTIGATOR] + ", " + translatedSpeaks[SpeakTranslations.X_SLEEP]
                startVoting()
            }
            GameStage.VOTE -> {
                if(uiState.value.gameOver == null) {
                    setStage(GameStage.VOTE_RESULTS)
                } else {
                    setStage(GameStage.GAME_OVER)
                }
            }
            GameStage.VOTE_RESULTS -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        users = currentState.users.toMutableList().map{ user -> user.copy(votes = 0)},
                    )
                }
                setStage(GameStage.BLANK)
                speechCoroutine = viewModelScope.launch {
                    speakText.value = translatedSpeaks[SpeakTranslations.EVERYONE_SLEEP]//"Everyone, go to sleep."
                    myDelay(timeDelay.toLong()*1000)
                    speakText.value = translatedSpeaks[SpeakTranslations.MAFIA] + ", " + translatedSpeaks[SpeakTranslations.X_WAKEUP]
                    withContext(Dispatchers.Main){
                        setStage(GameStage.MURDER)
                    }
                }

            }
            else -> {}
        }
    }

    fun startVoting(){
        speechCoroutine?.cancel()
        setStage(GameStage.BLANK)
        animationDuration = timeDelay.toInt()*1000
        openClosed = BoxState.Expanded
        viewModelScope.launch {
            myDelay(timeDelay.toLong()*1000)
            openClosed = BoxState.Collapsed
            val newlyDead = uiState.value.users.filter{it.tmpDead}
            val newlyCured = newlyDead.filter{it.tmpCured}

            val randomDeathIndexes = newlyDead.map { Random.nextInt(translatedMaleDeaths.size) }
            Log.d("ingo", "newlyDead $newlyDead $newlyCured")
            val itWillSpeak = StringBuilder(translatedSpeaks[SpeakTranslations.EVERYONE_WAKEUP])//"Everyone wake up and vote! "
            if(newlyDead.isNotEmpty()) {
                for ((index, newlyDeadGuy) in newlyDead.withIndex()) {
                    itWillSpeak.append(getDeath(randomDeathIndexes[index], newlyDead[index].name))
                    if (newlyCured.contains(newlyDeadGuy)) {
                        itWillSpeak.append(" ${getCure(randomDeathIndexes[index], newlyDead[index].name)}")
                    } else {
                        itWillSpeak.append("")
                    }
                }
            } else {
                itWillSpeak.append(translatedSpeaks[SpeakTranslations.NOBODY_KILLED])
                //itWillSpeak.append("Nobody was killed.")
            }

            speakText.value = itWillSpeak.toString()
            withContext(Dispatchers.Main){
                _uiState.update { currentState ->
                    currentState.copy(
                        users = currentState.users.toMutableList().map{
                            if(it.tmpDead && !it.tmpCured) {
                                it.copy(selected = false, dead = true, tmpDead = false)
                            } else if(it.tmpCured) {
                                it.copy(selected = false, tmpCured = false, cured = true, tmpDead = false)
                            } else it.copy(selected = false)
                        },
                        investigationOver = false,
                        voteCountdown = votingDuration.toInt(),
                        newlyKilled = newlyDead,
                        voteText = itWillSpeak.toString()
                    )
                }
                countdownCoroutine = viewModelScope.launch {
                    myDelay(1000)
                    // Coroutine that will be canceled when the ViewModel is cleared.
                    while(uiState.value.voteCountdown > 0){
                        withContext(Dispatchers.Main){
                            _uiState.update { currentState ->
                                currentState.copy(
                                    voteCountdown = currentState.voteCountdown-1
                                )
                            }
                        }
                        if(uiState.value.voteCountdown <= 5){
                            speakText.value = uiState.value.voteCountdown.toString()
                        }
                        myDelay(1000)
                    }
                    withContext(Dispatchers.Main){
                        voteEnd()
                    }
                }
                setStage(GameStage.VOTE)
            }
        }
    }

    fun getDeath(id: Int, name: String): String{
        if(name.endsWith("a") || name.endsWith("i")){
            return translatedFemaleDeaths[id].replace(";", name)
        } else {
            return translatedMaleDeaths[id].replace(";", name)
        }
    }

    fun getCure(id: Int, name: String): String{
        if(name.endsWith("a") || name.endsWith("i")){
            return translatedFemaleCures[id].replace(";", name)
        } else {
            return translatedMaleCures[id].replace(";", name)
        }
    }

    val pickerHelper = mapOf(GameStage.CHOOSE_NAMES to R.string.add_players,
    GameStage.MURDER to R.string.who_kill,
    GameStage.CURE to R.string.who_cure,
    GameStage.INVESTIGATE to R.string.investigate)

    val pickers = mapOf(GameStage.CHOOSE_NAMES to R.string.app_name,
        GameStage.MURDER to R.string.mafia,
        GameStage.CURE to R.string.doctor,
        GameStage.INVESTIGATE to R.string.investigator,
        GameStage.VOTE to R.string.discuss_and_vote,
        GameStage.VOTE_RESULTS to R.string.vote_results,
        GameStage.GAME_OVER to R.string.game_over)

    companion object{
        val roleTranslations = mapOf(Role.MURDERER to R.string.mafia, Role.DOCTOR to R.string.doctor, Role.VILLAGER to R.string.villager, Role.INVESTIGATOR to R.string.investigator)
    }
}

data class Speech(
    val english: String = "",
    val croatian_male: String = "",
    val croatian_female: String = "",
)

data class User(
    val name: String = "",
    val dead: Boolean = false,
    val tmpDead: Boolean = false,
    val role: Role = Role.VILLAGER,
    val selected: Boolean = false,
    val votes: Int = 0,
    val tmpCured: Boolean = false,
    val cured: Boolean = false,
    val deadHow: String? = null,
    val curedHow: String? = null,
)

data class DiceUiState(
    val users: List<User> = listOf(),//listOf(User("Ivica"), User("Stanko"), User("Marija"), User("Nataša")),
    val stage: GameStage = GameStage.CHOOSE_NAMES,
    val picker: String = "",
    val pickerHelp: String = "",
    val voteCountdown: Int = 0,
    val userVotedOut: User? = null,
    val gameOver: String? = null,
    val investigationOver: Boolean = false,
    val investigationDisableButton: Boolean = false,
    val currentUser: String = "",
    val currentRole: Int? = null,
    val newlyKilled: List<User> = listOf(),
    val dvijeMafije: Boolean = false,
    val displayRoles: Boolean = false,
    val openClosed: BoxState = BoxState.Collapsed,
    val voteResultsText: String = "",
    val voteText: String = "",
    val language: Boolean = false,
    val pause: Boolean = false,
)

enum class GameStage {
    CHOOSE_NAMES, READ_ROLES, MURDER, INVESTIGATE, CURE, VOTE, VOTE_RESULTS, GAME_OVER, BLANK
}

enum class SpeakTranslations {
    EVERYONE_WAKEUP, NOBODY_KILLED, EVERYONE_SLEEP, X_WAKEUP, X_SLEEP, MAFIA, INVESTIGATOR, DOCTOR, VOTED_DIES, MAFIA_WINS, VILLAGERS_WIN, NOBODY_VOTED_OUT
}

enum class Role {
    VILLAGER, MURDERER, INVESTIGATOR, DOCTOR
}