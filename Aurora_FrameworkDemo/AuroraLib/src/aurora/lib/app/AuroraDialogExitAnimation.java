package aurora.lib.app;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class AuroraDialogExitAnimation extends Animation {
    private View mTargetView;

    public AuroraDialogExitAnimation(View target) {
        mTargetView = target;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        final Matrix matrix = t.getMatrix();
        t.setAlpha(interpolatedTime);
        mTargetView.setScaleX(interpolatedTime);
        mTargetView.setScaleY(interpolatedTime);
    }
    
    

}
