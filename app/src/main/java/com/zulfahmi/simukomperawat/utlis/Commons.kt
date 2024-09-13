package com.zulfahmi.simukomperawat.utlis

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.zulfahmi.simukomperawat.R
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*


object Commons {
    fun setFullscreenLayout(activity: Activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE)
        activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun setIntentTransition(activity: Activity): Bundle? {
        return ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
    }

    fun setBoldAtSpanString(text: String, startIndex: Int, endIndex: Int): SpannableString{
        val spannableString = SpannableString(text)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return spannableString
    }

    fun getResourceId(resName: String, c: Class<*>): Int {
        return try {
            val idField: Field = c.getDeclaredField(resName)
            idField.getInt(idField)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    fun setTextViewAttributes(textView: TextView, params: LinearLayout.LayoutParams, font: Typeface, str: String): TextView {
        return textView.apply{
            layoutParams = params
            textSize = 18F
            typeface = font
            setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
            text = str
        }
    }

    fun showAlertDialog(context: Context, message: String, haveNegativeButton: Boolean = false, listener: ((Any) -> Unit)) {
        val alertDialog = AlertDialog.Builder(context).setMessage(message).setCancelable(haveNegativeButton)

        if (haveNegativeButton) {
            alertDialog.setPositiveButton("OK") { dialogInterface, _ ->
                listener(dialogInterface)
            }.create()
            alertDialog.setNegativeButton("BATAL"){ dialogInterface, _ ->
                dialogInterface.cancel()
            }.create()
        }else {
            alertDialog.setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create()
        }
        alertDialog.show()
    }

    fun showAlertDialog(context: Context, message: SpannableString) {
        AlertDialog.Builder(context).setMessage(message).setCancelable(false)
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create().show()
    }

    fun isEmailValid(Email: String): Boolean {
        return Email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(Email).matches()
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        return SimpleDateFormat("dd MMM yyyy , HH.mm").format(Date())
    }

    fun zoomImageFromThumb(viewGroup: ViewGroup, thumbView: View, imageResId: Int, animationDuration: Int) {
        var currentAnimator: Animator? = null
        
        val imgExpanded = viewGroup.findViewById<ImageView>(R.id.img_expanded)
        val imgBgDarken = viewGroup.findViewById<ImageView>(R.id.img_bg_darken)

        currentAnimator?.cancel()

        imgExpanded.setImageResource(imageResId)
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        thumbView.getGlobalVisibleRect(startBoundsInt)
        viewGroup.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        thumbView.alpha = 0f
        imgExpanded.visibility = View.VISIBLE
        imgBgDarken.visibility = View.VISIBLE
        imgExpanded.pivotX = 0f
        imgExpanded.pivotY = 0f

        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    imgExpanded,
                    View.X,
                    startBounds.left,
                    finalBounds.left)
            ).apply {
                with(ObjectAnimator.ofFloat(imgExpanded, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(imgExpanded, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(imgExpanded, View.SCALE_Y, startScale, 1f))
            }
            duration = animationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        imgExpanded.setOnClickListener {
            currentAnimator?.cancel()

            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(imgExpanded, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(imgExpanded, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(imgExpanded, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(imgExpanded, View.SCALE_Y, startScale))
                }
                duration = animationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        imgExpanded.visibility = View.GONE
                        imgBgDarken.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        imgExpanded.visibility = View.GONE
                        imgBgDarken.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }


    fun countFilledArray(array: Any) : Int{
        var totalFilled = 0
        when (array) {
            is IntArray -> {
                for (answer in array) {
                    if (answer != 0)
                        totalFilled++
                }
            }
            is Array<*> -> {
                for (answer in array) {
                    if (answer != "")
                        totalFilled++
                }
            }
            else -> throw IllegalArgumentException("type is not assigned yet")
        }
        return totalFilled
    }

    fun showSelector(
        context: Context,
        title: String,
        items: Array<String>,
        onClick: (DialogInterface, Int) -> Unit
    ) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setTitle(title)
        alertBuilder.setItems(items) { dialog, which ->
            onClick(dialog, which)
        }.show()
    }
}