package com.example.finalcalcihide.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.RawRes;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.finalcalcihide.R;

import java.util.List;

public class AnimationManager {

    public interface AnimationCallback {
        void onProcessComplete(boolean success, List<String> selectedPaths);
    }

    private Context context;
    private FrameLayout animationContainer;
    private LottieAnimationView lottieHideUnhideAnimation;
    private LottieAnimationView lottieDeleteAnimation;
    private View customLayout;

    public AnimationManager(Context context, FrameLayout animationContainer) {
        this.context = context;
        this.animationContainer = animationContainer;
        initializeAnimations();
    }

    private void initializeAnimations() {
        LayoutInflater inflater = LayoutInflater.from(context);
        customLayout = inflater.inflate(R.layout.animation, null);
        lottieHideUnhideAnimation = customLayout.findViewById(R.id.ani_hide_unhide);
        lottieDeleteAnimation = customLayout.findViewById(R.id.ani_delete);
        animationContainer.addView(customLayout);
        hideAllAnimations();
    }

    private void hideAllAnimations() {
        lottieHideUnhideAnimation.setVisibility(View.GONE);
        lottieDeleteAnimation.setVisibility(View.GONE);
        animationContainer.setVisibility(View.GONE);
    }

    /**
     * Plays the Hide/Unhide animation.
     *
     * @param reverse If true, plays the animation in reverse.
     */
    public void playHideUnhideAnimation(boolean reverse) {
        animationContainer.setVisibility(View.VISIBLE);
        lottieHideUnhideAnimation.setVisibility(View.VISIBLE);
        lottieHideUnhideAnimation.setRepeatCount(LottieDrawable.INFINITE);
        lottieHideUnhideAnimation.setSpeed(reverse ? -1f : 1f);
        if (reverse) {
            lottieHideUnhideAnimation.setProgress(1f); // Start from the end for reverse
        }
        lottieHideUnhideAnimation.playAnimation();
    }

    /**
     * Plays the Delete animation.
     */
    public void playDeleteAnimation() {
        animationContainer.setVisibility(View.VISIBLE);
        lottieDeleteAnimation.setVisibility(View.VISIBLE);
        lottieDeleteAnimation.setRepeatCount(LottieDrawable.INFINITE);
        lottieDeleteAnimation.playAnimation();
    }

    /**
     * Stops all ongoing animations and hides the animation container.
     */
    public void stopAnimations() {
        if (lottieHideUnhideAnimation.isAnimating()) {
            lottieHideUnhideAnimation.cancelAnimation();
        }
        if (lottieDeleteAnimation.isAnimating()) {
            lottieDeleteAnimation.cancelAnimation();
        }
        hideAllAnimations();
    }

    /**
     * Handles the animation process by playing the appropriate animation,
     * executing the background task, and ensuring the animation runs for at least the minimum display time.
     *
     * @param animationType      The type of animation to play.
     * @param selectedPaths      The list of selected paths to process.
     * @param minimumDisplayTime The minimum time the animation should be displayed (in milliseconds).
     * @param processTask        The background task to execute.
     * @param callback           The callback to notify when the process is complete.
     */
    public void handleAnimationProcess(
            AnimationType animationType,
            List<String> selectedPaths,
            long minimumDisplayTime,
            Runnable processTask,
            AnimationCallback callback
    ) {
        // Play the appropriate animation based on the type
        switch (animationType) {
            case HIDE_UNHIDE:
                playHideUnhideAnimation(true); // You can parameterize reverse if needed
                break;
            case DELETE:
                playDeleteAnimation();
                break;
            // Add more cases here for additional animations
            default:
                throw new IllegalArgumentException("Unsupported Animation Type");
        }

        long animationStartTime = System.currentTimeMillis();

        // Run the process in a background thread
        new Thread(() -> {
            // Execute the background task
            processTask.run();

            // TODO: Replace the following line with actual success determination
            boolean processSuccess = true; // Replace with actual result

            // Calculate elapsed time
            long elapsedTime = System.currentTimeMillis() - animationStartTime;

            // Determine remaining time to meet minimum display time
            long remainingTime = minimumDisplayTime - elapsedTime;
            if (remainingTime > 0) {
                try {
                    Thread.sleep(remainingTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Post the callback to the main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                stopAnimations();
                callback.onProcessComplete(processSuccess, selectedPaths);
            });
        }).start();
    }

    /**
     * Enumeration for different types of animations.
     */
    public enum AnimationType {
        HIDE_UNHIDE,
        DELETE
        // Add more animation types here as needed
    }
}
