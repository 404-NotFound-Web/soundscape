package com.example.kkgroup.soundscape_v2.fragment


import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kkgroup.soundscape_v2.R
import com.example.kkgroup.soundscape_v2.Tools.Tools
import kotlinx.android.synthetic.main.fragment_recording.*
import java.io.File
import java.io.IOException
import java.util.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
class FragmentRecording : Fragment() {
    private var permissionToRecordAccepted = false
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var audioFile:File? = null
    private var mStartRecording = true

    companion object {
        fun newInstance(): FragmentRecording {
            return FragmentRecording()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recording, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS), REQUEST_RECORD_AUDIO_PERMISSION)
        }

        recordingBtn.setOnClickListener {
            onRecord(mStartRecording)
            if (mStartRecording) {
                recordingBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
            } else {
                recordingBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_mic))
            }

            mStartRecording = !mStartRecording
        }
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
        storageTV.text = audioFile?.absolutePath
    }

    private fun startRecording() {
        audioFile = File(Tools.getMyRecordingPath() + "${Date().time}.3gp")

        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setAudioEncodingBitRate(16)
            setAudioSamplingRate(44100)
            setOutputFile(audioFile?.absolutePath)

            try {
                prepare()
            } catch (e: IOException) {
                Tools.log_e("prepare() failed")
            }
            start()
        }
    }

    private fun stopRecording() {
        mRecorder?.apply {
            stop()
            release()
        }
        mRecorder = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) {
            Tools.toastShow(context!!, " Permission Denied")
        }
    }

    override fun onStop() {
        super.onStop()
        mRecorder?.release()
        mRecorder = null
        mPlayer?.release()
        mPlayer = null
    }

}
