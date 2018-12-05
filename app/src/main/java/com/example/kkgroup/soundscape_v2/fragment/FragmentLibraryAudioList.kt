package com.example.kkgroup.soundscape_v2.fragment

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.kkgroup.soundscape_v2.R
import com.example.kkgroup.soundscape_v2.Tools.Tools
import com.example.kkgroup.soundscape_v2.activity.PlayActivity
import com.example.kkgroup.soundscape_v2.adapter.AudioItemAdapter
import com.example.kkgroup.soundscape_v2.widget.ItemAnimation
import kotlinx.android.synthetic.main.fragment_library_audio_list.*
import org.jetbrains.anko.support.v4.startActivity
import java.io.File



class FragmentLibraryAudioList : Fragment() {

    private val LOADING_DURATION = 1000
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAudioItemAdapter: AudioItemAdapter
    private lateinit var categoryName: String

    companion object {
        fun newInstance(): FragmentLibraryAudioList {
            return FragmentLibraryAudioList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_library_audio_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initListeners()
        loadingAndDisplayContent(view)
    }

    private fun loadingAndDisplayContent(view: View) {

        categoryName = arguments!!.getString("obj")

        recyclerView = view.findViewById(R.id.recyclerView)
        val lytProgress = view.findViewById<LinearLayout>(R.id.lyt_progress)
        lytProgress.visibility = View.VISIBLE
        lytProgress.alpha = 1.0f
        recyclerView.visibility = View.GONE

        Handler().postDelayed({ Tools.viewFadeOut(lytProgress) }, LOADING_DURATION.toLong())

        Handler().postDelayed({ initComponents(view) }, (LOADING_DURATION + 400).toLong())
    }

    private fun initComponents(view: View) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        val audioFilesByCategory = Tools.getRemoteAudioFiles(
                Tools.getDownloadedAudioByCategoryPath(categoryName))

        //set data and list adapter
        mAudioItemAdapter = AudioItemAdapter(context!!, audioFilesByCategory, ItemAnimation.FADE_IN)
        recyclerView.adapter = mAudioItemAdapter

        // on item list clicked
        mAudioItemAdapter.setOnItemClickListener(object : AudioItemAdapter.OnItemClickListener {
            override fun onItemClick(view: View, file: File, position: Int) {
                startActivity<PlayActivity>("obj" to file)
            }
        })

        mAudioItemAdapter.setOnMoreButtonClickListener(object : AudioItemAdapter.OnMoreButtonClickListener {
            override fun onItemClick(view: View, file: File, menuItem: MenuItem) {
                when(menuItem.itemId) {
                    R.id.action_play -> {
                        startActivity<PlayActivity>("obj" to file)
                    }

                    R.id.action_add_to_track -> {
                        Tools.toastShow(context!!, " add to track")
                    }

                    R.id.action_delete -> {
                        Tools.toastShow(context!!, "still working on it ")
                    }
                }
            }
        })


    }

    private fun initListeners() {

        fabBack.setOnClickListener {
            val trans = fragmentManager!!.beginTransaction()
            trans.replace(R.id.root_frame, FragmentLibraryCategory.newInstance())
            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            trans.addToBackStack(null)
            trans.commit()
        }

    }

}