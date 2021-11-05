package ru.inncreator.vezdekod.fragments.task1

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.inncreator.vezdekod.utils.FFmpegRepo
import ru.inncreator.vezdekod.R
import ru.inncreator.vezdekod.databinding.FragmentTaskOneBinding
import timber.log.Timber
import java.io.IOException


class TaskOneFragment : Fragment() {

    private val ffmpegRepo: FFmpegRepo by inject()

    private lateinit var videoView: SurfaceView
    private lateinit var selectViewContainer: RelativeLayout
    private lateinit var editViewContainer: RelativeLayout


    private val mediaPlayer = MediaPlayer()

    private lateinit var seekBar: SeekBar


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentTaskOneBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_task_one, container, false)

        binding.openVideo.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "video/*"
            startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO)

        }

        videoView = binding.videoView

        editViewContainer = binding.editVideo
        selectViewContainer = binding.selectVideo

        seekBar = binding.seekbar

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        mediaPlayer.seekTo((p1*100).toLong(),MediaPlayer.SEEK_CLOSEST)
                    } else mediaPlayer.seekTo(p1*100)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        videoView.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder) {
                mediaPlayer.setDisplay(holder)
                Timber.i("surface Created")
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                Timber.d("surface changed")

            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                Timber.i("surface destroyed")

            }

        })

        var isPlay = false
        binding.play.setOnClickListener {
            if (!isPlay) {
                mediaPlayer.start()
                it.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.baseline_pause_circle_outline_24
                )
            } else {
                it.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_play_button_56);
                mediaPlayer.pause()
                isPlay.not()
            }
            isPlay = !isPlay
        }


        binding.back.setOnClickListener {
            invertContainer()
        }

        binding.backToMain.setOnClickListener {
            findNavController().popBackStack()
        }

        GlobalScope.launch {
            while (isActive) {
                if (isPlay){
                    val currentPos = mediaPlayer.currentPosition / 100
                    seekBar.progress = currentPos
                    delay(100)
                }

            }


        }

        binding.done.setOnClickListener {
            ffmpegRepo.saveVideo()
        }

        return binding.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)

        when (requestCode) {
            REQUEST_SELECT_PHOTO -> if (resultCode === RESULT_OK) {
                if (imageReturnedIntent == null) {
                    Timber.e("Ochko")
                    return
                }
                val selectedVideo: Uri = imageReturnedIntent.data!!
//                val imageStream: InputStream? = requireContext().contentResolver.openInputStream(selectedVideo)
//                val yourSelectedVideo = BitmapFactory.decodeStream(imageStream)
                Timber.i("Hhh $selectedVideo")
                openVideo(selectedVideo)
            }
        }

    }

    private fun openVideo(selectedVideo: Uri) {
        try {
            mediaPlayer.setDataSource(requireContext(), selectedVideo)
            mediaPlayer.prepare()
            ffmpegRepo.selectVideo(requireContext(),selectedVideo)
            seekBar.max = mediaPlayer.duration/100
            Timber.i("Seekbar max = ${seekBar.max}")
            invertContainer()
            Timber.i("Video started")
        } catch (e: IOException) {
            Timber.e(e, "MediaPlayer is not done")
        }

    }


    private fun invertContainer() {
        if (editViewContainer.visibility == View.VISIBLE) {
            editViewContainer.visibility = View.GONE
            selectViewContainer.visibility = View.VISIBLE
        } else {
            editViewContainer.visibility = View.VISIBLE
            selectViewContainer.visibility = View.GONE
        }
    }

    companion object {
        private const val REQUEST_SELECT_PHOTO = 100
    }
}