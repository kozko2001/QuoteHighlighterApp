package net.coscolla.highlight.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import me.panavtec.drawableview.DrawableView;

public class CustomDrawableView extends DrawableView {
  public CustomDrawableView(Context context) {
    super(context);
  }

  public CustomDrawableView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomDrawableView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public CustomDrawableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public void onScaleChange(float scaleFactor) {
    // HACK!!!
    if(scaleFactor != 33.333f) {
      super.onScaleChange(scaleFactor);
    }
  }
}
