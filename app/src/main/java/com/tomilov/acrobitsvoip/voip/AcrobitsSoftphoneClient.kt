package com.tomilov.acrobitsvoip.voip

import android.app.Application
import com.tomilov.acrobitsvoip.BuildConfig
import cz.acrobits.ali.Xml
import cz.acrobits.libsoftphone.Instance
import cz.acrobits.libsoftphone.account.AccountXml
import cz.acrobits.libsoftphone.data.Account
import cz.acrobits.libsoftphone.data.Call
import cz.acrobits.libsoftphone.data.DialAction
import cz.acrobits.libsoftphone.data.RegistrationState
import cz.acrobits.libsoftphone.event.CallEvent
import cz.acrobits.libsoftphone.event.Event
import cz.acrobits.libsoftphone.event.StreamParty
import cz.acrobits.libsoftphone.mergeable.MergeableNodeAttributes
import cz.acrobits.libsoftphone.support.Listeners
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AcrobitsSoftphoneClient(
    private val application: Application
) : SoftphoneClient,
    Listeners.OnRegistrationStateChanged,
    Listeners.OnRegistrationErrorMessage,
    Listeners.OnNewCall,
    Listeners.OnCallStateChanged,
    Listeners.OnCallHoldStateChanged {

    private val listeners = object : Listeners() {
        override fun getRingtone(callEvent: CallEvent): Any? = null
    }
    private val accountId = "Test Account"
    private var currentCall: CallEvent? = null
    private var currentCallStartedAtMillis: Long? = null

    private val _registrationStatus = MutableStateFlow(RegistrationStatus.NotConfigured)
    override val registrationStatus: StateFlow<RegistrationStatus> = _registrationStatus

    private val _activeCall = MutableStateFlow<CallSession?>(null)
    override val activeCall: StateFlow<CallSession?> = _activeCall

    init {
        Instance.loadLibrary(application)
        val provisioning = Xml.parse(
            """
            <?xml version="1.1"?>
            <provisioning>
                <saas>
                    <identifier>${BuildConfig.ACROBITS_LICENSE_KEY}</identifier>
                </saas>
            </provisioning>
            """.trimIndent()
        )

        Instance.init(application, provisioning, SoftphonePreferences::class.java)
        Instance.preferences.trafficLogging.set(BuildConfig.DEBUG)

        listeners.register(this)
        Instance.setObserver(listeners)
    }

    override fun configureAccount(credentials: SipCredentials) {
        val account = Xml("account").apply {
            setAttribute(Account.Attributes.ID, accountId)
            setChildValue(Account.USERNAME, credentials.username)
            setChildValue(Account.PASSWORD, credentials.password)
            setChildValue(Account.ICM, Account.ICM_MODE_STANDARD)
            setChildValue(Account.HOST, BuildConfig.SIP_HOST)

            // TLS avoids common SIP ALG issues on consumer routers.
            setChildValue("transport", "tls+sip:")
        }

        Instance.Registration.saveAccount(AccountXml(account, MergeableNodeAttributes.gui()))
        Instance.Registration.updateAll()
    }

    override fun dial(number: String): CallPlacementResult {
        currentCall?.let(Instance.Calls::close)

        val call = CallEvent(StreamParty(number).match(accountId).toRemoteUser()).apply {
            transients.put(Event.Transients.DIAL_ACTION, DialAction.VOICE_CALL.id)
        }

        val result = Instance.Events.post(call)
        if (result != Instance.Events.PostResult.SUCCESS) {
            currentCall = null
            currentCallStartedAtMillis = null
            _activeCall.value = null
            return CallPlacementResult.Failure("Call failed: $result")
        }

        currentCall = call
        currentCallStartedAtMillis = null
        val session = call.toSession(Call.State.Trying)
        _activeCall.value = session
        return CallPlacementResult.Success(session)
    }

    override fun hangUp() {
        currentCall?.let { Instance.Calls.hangup(it, null) }
    }

    override fun setMuted(muted: Boolean) {
        Instance.Audio.setMuted(muted)
        updateCurrentCallSession()
    }

    override fun setHeld(held: Boolean) {
        currentCall?.let { Instance.Calls.setHeld(it, held) }
        updateCurrentCallSession()
    }

    override fun onRegistrationStateChanged(accountId: String?, state: RegistrationState) {
        _registrationStatus.value = RegistrationStatus(
            label = state.label,
            isRegistered = state == RegistrationState.Registered
        )
    }

    override fun onRegistrationErrorMessage(accountId: String?, message: String) {
        if (message.isNotBlank()) {
            _registrationStatus.value = RegistrationStatus(
                label = message,
                isRegistered = false
            )
        }
    }

    override fun onNewCall(callEvent: CallEvent) {
        if (currentCall != null) {
            Instance.Calls.close(callEvent)
            return
        }

        currentCall = callEvent
        currentCallStartedAtMillis = null
        updateCurrentCallSession()
    }

    override fun onCallStateChanged(callEvent: CallEvent, callState: Call.State) {
        if (callEvent != currentCall) return

        if (callState == Call.State.Established && currentCallStartedAtMillis == null) {
            currentCallStartedAtMillis = System.currentTimeMillis()
        }

        if (callState.isTerminal) {
            Instance.Calls.close(callEvent)
            currentCall = null
            currentCallStartedAtMillis = null
            _activeCall.value = null
            return
        }

        _activeCall.value = callEvent.toSession(callState)
    }

    override fun onCallHoldStateChanged(callEvent: CallEvent, states: Call.HoldStates) {
        if (callEvent != currentCall) return
        updateCurrentCallSession()
    }

    private fun updateCurrentCallSession() {
        val call = currentCall ?: run {
            _activeCall.value = null
            return
        }
        _activeCall.value = call.toSession(Instance.Calls.getState(call))
    }

    private fun CallEvent.toSession(state: Call.State): CallSession {
        val remoteUser = remoteUser
        return CallSession(
            displayName = remoteUser?.displayName.orEmpty(),
            number = remoteUser?.genericUri.orEmpty(),
            stateLabel = state.label,
            startedAtMillis = currentCallStartedAtMillis,
            isMuted = Instance.Audio.isMuted(),
            isHeld = Instance.Calls.isHeld(this).local == Call.HoldState.Held
        )
    }
}
