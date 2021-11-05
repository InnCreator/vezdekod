package ru.inncreator.vezdekod.utils

import android.media.MediaPlayer
import android.view.View
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.slider.RangeSlider
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.inncreator.vezdekod.R
import timber.log.Timber

class FeatureMenu(private val root: RelativeLayout): KoinComponent {

    private val ffmpegRepo: FFmpegRepo by inject()


//    private lateinit var durationContainer: RelativeLayout

    init {
        val menu: BottomNavigationView = root.findViewById(R.id.bottom_bar)

        val durationContainer: RelativeLayout = root.findViewById(R.id.duration_container)
        val magicContainer : LinearLayout= root.findViewById(R.id.magic_container)

        menu.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.duration -> {
                    durationContainer.visibility = invertVisibility(durationContainer.visibility)
//                    if (durationContainer.visibility == View.GONE)
//                        jobDuration.cancel()
                }
                R.id.magic -> {

                }

                else -> null
            }

            return@setOnItemSelectedListener true
        }
    }

    private var jobDuration = Job() + GlobalScope.coroutineContext

    fun initDuration(mediaPlayer: MediaPlayer) {
        if (jobDuration.isActive)
            jobDuration.cancel()

        val seekBar = root.findViewById<SeekBar>(R.id.seekbar)

        val rangeSlider = root.findViewById<RangeSlider>(R.id.rangeSlider)

        val trimText = root.findViewById<TextView>(R.id.trim_result_text)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        mediaPlayer.seekTo((p1 * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
                    } else mediaPlayer.seekTo(p1 * 1000)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        rangeSlider.addOnChangeListener(RangeSlider.OnChangeListener { slider, value, fromUser ->
            if(fromUser){
                Timber.i("Values = ${slider.values}")
                trimText.text = "${(mediaPlayer.duration/1000).toInt()} Секунд       >       ${(slider.values[1] - slider.values[0]).toInt()} Секунд"
                ffmpegRepo.setTrim(slider.values[0],slider.values[1])
            }
        })

        rangeSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
            override fun onStartTrackingTouch(slider: RangeSlider) {
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                Timber.i("Start = ${slider.values}; end = ${slider.valueTo}")
            }

        })


        seekBar.max = mediaPlayer.duration / 1000
//        rangeSlider.va
        val mpDur = mediaPlayer.duration /1000.toFloat()
        rangeSlider.valueTo = if (mpDur <1f) 10f else mpDur
        rangeSlider.valueFrom = 0f
        val myValue = rangeSlider.valueTo - rangeSlider.valueFrom %40
        rangeSlider.setValues(0f,myValue)
        Timber.d("Start = ${rangeSlider.valueFrom}, firstPoint = ${rangeSlider.values[0]} , secondPoint = ${rangeSlider.values[1]}, End = ${rangeSlider.valueTo} , my Value = $myValue ")
        Timber.i("Seekbar max = ${seekBar.max}")

        jobDuration = GlobalScope.launch {
            while (isActive) {
                val currentPos = mediaPlayer.currentPosition / 1000
                seekBar.progress = currentPos
                delay(1000)

            }
        }
    }


    fun initMagic(){
        val loop = root.findViewById<RadioButton>(R.id.loop)
        val boomerang = root.findViewById<RadioButton>(R.id.boomerang)

        loop.setOnClickListener {
            ffmpegRepo.isLoop = (it as RadioButton).isChecked
        }
        boomerang.setOnClickListener {
            ffmpegRepo.isLoop = (it as RadioButton).isChecked
        }
    }

    private fun invertVisibility(visibility: Int) =
        if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

}