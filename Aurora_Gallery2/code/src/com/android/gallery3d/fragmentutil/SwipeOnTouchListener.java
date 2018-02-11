

package com.android.gallery3d.fragmentutil;

import android.view.View;

public interface SwipeOnTouchListener extends View.OnTouchListener {
    /**
     * @return True if the user is currently swiping a list-item horizontally.
     */
    public boolean isSwiping();
}
