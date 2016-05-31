package net.coscolla.highlight.view.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import net.coscolla.highlight.R;

public class RecognizingProgressDialog extends DialogFragment {

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new MaterialDialog.Builder(this.getContext())
        .title(R.string.processing)
        .content(R.string.extracting_from_image)
        .progress(true, 0)
        .cancelable(false)
        .build();
  }
}
