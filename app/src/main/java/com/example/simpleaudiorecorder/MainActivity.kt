package com.example.simpleaudiorecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val soundVisualizerView: SoundVisualizerView by lazy{
        findViewById(R.id.soundVisualizerView)
    }

    private val resetButton: Button by lazy{
        findViewById(R.id.resetButton)
    }

    private val recordButton: RecordButton by lazy{
        findViewById(R.id.recordButton)
    }

    private val recordTimeTextView: CountUpView by lazy {
        findViewById(R.id.recordTimeTextView)
    }

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val recordingFilePath: String by lazy{
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var state = State.BEFORE_RECORDING   // 초기 state : BEFORE_RECORDING
        set(value) {   // set 해줄 때마다 호출
            field = value
            // 녹음 후나 재생 중일 때만 reset 가능
            resetButton.isEnabled = (value == State.AFTER_RECORDING)
                    || (value == State.ON_PLAYING)
            // state에 따라 recordButton UI 변경
            recordButton.updateIconWithState(value)
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermission()
        initViews()
        bindViews()
        initVariables()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestAudioPermission() {
        // 권한 요청
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    // 요청한 권한 요청에 대한 결과를 바탕으로 작업
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 권한이 혀용되었는지 확인(허용되었으면 true)
        val audioRecordPermissionGranted: Boolean =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                    grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if(!audioRecordPermissionGranted) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                // "다시 묻지 않기" 상태로 거부되었을 경우
                Toast.makeText(this, "권한을 허용한 뒤에 다시 앱을 실행해주세요", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:$packageName"))    // 앱 설정 화면으로 이동(직접 권한 허용하도록)
                finish()
                startActivity(intent)
            } else{
                finish()    // 권한 거절할 경우 앱 종료
            }

        }
    }

    private fun initViews(){
        // 녹음 상태에 따라 버튼 UI 변경
        recordButton.updateIconWithState(state)
    }

    private fun bindViews(){
        // soundVisualizerView
        soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude ?: 0
        }

        // 녹음 버튼
        recordButton.setOnClickListener {
            // 현재 state에 따라 취하는 행위가 다름
            when(state){
                State.BEFORE_RECORDING -> {     // 녹음 전 -> 녹음 시작
                    startRecording()
                }
                State.ON_RECORDING -> {         // 녹음 중 -> 녹음 끝
                    stopRecording()
                }
                State.AFTER_RECORDING -> {      // 녹음 후 -> 재생 시작
                    startPlaying()
                }
                State.ON_PLAYING -> {           // 재생 중 -> 재생 멈춤
                    stopPlaying()
                }
            }
        }

        // 리셋
        resetButton.setOnClickListener {
            stopPlaying()   // 재생 멈춤
            soundVisualizerView.clearVisualization()
            recordTimeTextView.clearCountTime()
            state = State.BEFORE_RECORDING  // 상태 녹음 전으로 바꿈
        }
    }

    private fun initVariables(){
        // 처음 한 번만 set 호출해서 resetButton -> disable
        state = State.BEFORE_RECORDING
    }

    private fun startRecording(){
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)
            prepare()
        }
        recorder?.start()

        soundVisualizerView.startVisualizing(false)

        recordTimeTextView.startCountUp()

        state = State.ON_RECORDING
    }

    private fun stopRecording(){
        recorder?.run{
            stop()
            release()
        }
        recorder = null

        soundVisualizerView.stopVisualizing()

        recordTimeTextView.stopCountUp()

        state = State.AFTER_RECORDING
    }

    private fun startPlaying(){
        player = MediaPlayer().apply{
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECORDING
        }
        player?.start()

        soundVisualizerView.startVisualizing(true)

        recordTimeTextView.startCountUp()

        state = State.ON_PLAYING
    }

    private fun stopPlaying(){
        player?.release()
        player = null

        soundVisualizerView.stopVisualizing()

        recordTimeTextView.stopCountUp()

        state = State.AFTER_RECORDING
    }

    companion object{
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
}