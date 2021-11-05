package ru.inncreator.vezdekod.fragments.task2

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import ru.inncreator.vezdekod.utils.FFmpegRepo
import ru.inncreator.vezdekod.R
import ru.inncreator.vezdekod.utils.SelectedScaleView
import ru.inncreator.vezdekod.databinding.FragmentTaskTwoBinding
import ru.inncreator.vezdekod.utils.FeatureMenu
import timber.log.Timber


class TaskTwoFragment : Fragment() {

    private lateinit var videoView: SurfaceView
    private lateinit var selectViewContainer: RelativeLayout
    private lateinit var editViewContainer: RelativeLayout

    private val ffmpegRepo: FFmpegRepo by inject()

    private lateinit var menu: FeatureMenu


    private val mediaPlayer = MediaPlayer()

//    private lateinit var seekBar: SeekBar


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentTaskTwoBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_task_two, container, false)

        binding.openVideo.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "video/*"
            startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO)

        }

        videoView = binding.videoView

        editViewContainer = binding.editVideo
        selectViewContainer = binding.selectVideo

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

        val progressBar = binding.progressBar

        binding.done.setOnClickListener {
            GlobalScope.launch {
                Timber.d("Start save btn")
                withContext(Dispatchers.Main){
                    it.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                }

                ffmpegRepo.saveAndUpdateVideo()
                //ochko
                withContext(Dispatchers.Main){
                    openVideo(ffmpegRepo.getCurrentVideo())
                    it.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
                Timber.d("End save btn")
            }

        }

        val scale = SelectedScaleView(videoView,binding.videoCard)
        scale.isSelected = true

        menu = FeatureMenu(binding.featureMenu as RelativeLayout)

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
                ffmpegRepo.selectVideo(requireContext(),selectedVideo)
                invertContainer()
            }
        }

    }

    private fun openVideo(selectedVideo: Uri) {
        try {
            if (mediaPlayer.isPlaying){
                mediaPlayer.pause()
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(requireContext(), selectedVideo)
            mediaPlayer.setOnPreparedListener { it.start() }
            mediaPlayer.prepareAsync()
            mediaPlayer.isLooping = true
            menu.initDuration(mediaPlayer)
            Timber.i("Video started")
        } catch (e: Throwable) {
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