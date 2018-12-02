package com.example.kkgroup.soundscape_v2.activity

import android.content.Context
import android.os.Build
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity

import android.os.Bundle
import android.os.Vibrator
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.kkgroup.soundscape_v2.Model.AudioCardModel
import com.example.kkgroup.soundscape_v2.R
import com.example.kkgroup.soundscape_v2.Tools.ConstantValue
import com.example.kkgroup.soundscape_v2.Tools.LocaleManager
import com.example.kkgroup.soundscape_v2.Tools.Tools
import com.example.kkgroup.soundscape_v2.widget.MyLinearLayout
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.jaygoo.widget.VerticalRangeSeekBar
import kotlinx.android.synthetic.main.activity_new_soundscape.*
import org.jetbrains.anko.startActivity

private const val AUDIO_CARD_HEIGHT = 360
class NewSoundscapeActivity : AppCompatActivity(), View.OnLongClickListener, MyLinearLayout.VerticalPositionDetectListener {

    override fun handleViewVerticalPostion(view: View) {
       // Tools.log_e("view.tag: ${view.tag} --> yPosition: $yPosition")
       Tools.log_e("view.tag: ${view.tag} --> top: ${view.top} --> bottom: ${view.bottom}")

        val audioCardModel = view.tag as AudioCardModel

        Tools.log_e("contains: ${audioCardModelList.contains(audioCardModel)}")
        if (audioCardModelList.contains(audioCardModel)) {
            audioCardModel.topPosition = view.top
            audioCardModel.bottomPosition = view.bottom
            getOrderOfAudioCards()
        }
    }

    private lateinit var seekBar: VerticalRangeSeekBar
    private lateinit var audioTrack01: MyLinearLayout
    private lateinit var audioTrack02: MyLinearLayout
    private var audioCardViewListForTrack01 = mutableListOf<View>()
    private var audioCardViewListForTrack02 = mutableListOf<View>()
    private var audioCardViewList = mutableListOf<View>()

    val audioCardModelList = mutableListOf<AudioCardModel>()
    private var mVibrator: Vibrator? = null
    private var isPlaying = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager(this).getLocale()
        setContentView(R.layout.activity_new_soundscape)

        initToolbar()
        initComponents()
        initListeners()

        generateAudioCard(1)
        generateAudioCard(1)
        generateAudioCard(2)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {

            for (audioCardView in audioCardViewListForTrack01) {
                val audioCardModel = AudioCardModel(1, audioCardView.top, audioCardView.bottom)
                audioCardView.tag = audioCardModel
                audioCardModelList.add(audioCardModel)
                // Tools.log_e("audioCardView Track 01: ${audioCardView.top} --- ${audioCardView.bottom}")
            }

            for (audioCardView in audioCardViewListForTrack02) {
                val audioCardModel = AudioCardModel(2, audioCardView.top, audioCardView.bottom)
                audioCardView.tag = audioCardModel
                audioCardModelList.add(audioCardModel)
                // Tools.log_e("audioCardView Track 02: ${audioCardView.top} --- ${audioCardView.bottom}")
            }
        }
    }

    private fun getOrderOfAudioCards() {

        audioCardModelList.sortBy { it.topPosition }
        audioCardModelList.forEach {
            Tools.log_e(it.toString())
        }
    }


    /**
     * flag -> 1 : add audio card to track 01
     *         2 : add audio card to track 02
     */
    private fun generateAudioCard(flag: Int) {

        if (flag == 1) {
            val audioCard = LayoutInflater.from(this)
                    .inflate(R.layout.audio_file_small_item, audioTrack01, false)

            audioTrack01.addView(audioCard)
            audioCardViewListForTrack01.add(audioCard)
            audioCardViewList.add(audioCard)

            audioCard.setOnClickListener {
                showBottomSheetDialog("childView 1")
            }
            audioCard.setOnLongClickListener(this)


        } else {
            val audioCard = LayoutInflater.from(this)
                    .inflate(R.layout.audio_file_small_item, audioTrack02, false)

            audioTrack02.addView(audioCard)
            audioCardViewListForTrack02.add(audioCard)
            audioCardViewList.add(audioCard)

            audioCard.setOnClickListener {
                showBottomSheetDialog("childView 2")
            }
            audioCard.setOnLongClickListener(this)
        }

    }

    /**
     * The following method requires SDK >= 26, Our Target SDK is 21
     * We are lazy to do SDK check, so we just use deprecated method , let me go this time :)
     */
    override fun onLongClick(view: View?): Boolean {
        mVibrator?.let {
            // if (it.hasVibrator()) it.vibrate(VibrationEffect.createOneShot(ConstantValue.vibrationTime, -1))
            if (it.hasVibrator()) it.vibrate(ConstantValue.vibrationTime)
            return true
        }
        return true
    }

    private fun initComponents() {
        seekBar = findViewById(R.id.myVerticalSeekbar)
        seekBar.setValue(30f)

        // make it work!
        seekBar.invalidate()

        audioTrack01 = findViewById(R.id.audio_track_one)
        audioTrack02 = findViewById(R.id.audio_track_two)
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        bottom_sheet = findViewById(R.id.bottom_sheet)
        mBehavior = BottomSheetBehavior.from(bottom_sheet)
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Soundscape V2"
        Tools.setSystemBarColor(this, R.color.colorPrimary)
    }

    private fun initListeners() {

        audioTrack01.setMyVerticalPositionDetectListener(this)
        audioTrack02.setMyVerticalPositionDetectListener(this)

        ib_play.setOnClickListener {

            getOrderOfAudioCards()

            if (isPlaying) {
                ib_play.setImageResource(R.drawable.ic_play_arrow)
            } else {
                ib_play.setImageResource(R.drawable.ic_pause)
            }
            isPlaying = !isPlaying
        }

        seekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {

                // Tools.log_e("leftValue: $leftValue --> rightValue: $rightValue")
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

        })

        // make it work!
        seekBar.invalidate()
    }

    private lateinit var mBehavior: BottomSheetBehavior<View>
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var bottom_sheet: View? = null
    private fun showBottomSheetDialog(message: String) {
        if (mBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val view = layoutInflater.inflate(R.layout.sheet_audio_card_floating, null)
        (view.findViewById(R.id.name) as TextView).text = message
        (view.findViewById(R.id.brief) as TextView).text = "From human category"
        view.findViewById<ImageButton>(R.id.bt_close).setOnClickListener {
            mBottomSheetDialog?.hide()
        }

        view.findViewById<AppCompatButton>(R.id.deleteFromTrack).setOnClickListener {
            Toast.makeText(applicationContext, "Delete From Track", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<AppCompatButton>(R.id.previewAudioCard).setOnClickListener {
            Toast.makeText(applicationContext, "Preview Audio Card", Toast.LENGTH_SHORT).show()
        }

        mBottomSheetDialog = BottomSheetDialog(this)
        mBottomSheetDialog?.setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog?.getWindow()!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        // set background transparent
        (view.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        mBottomSheetDialog?.show()
        mBottomSheetDialog?.setOnDismissListener {
            mBottomSheetDialog = null
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add) {
            startActivity<SelectAudioActivity>()
        }
        return super.onOptionsItemSelected(item)
    }
}

