package net.coscolla.highlight.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import net.coscolla.highlight.R;

public class NothingHighlightedDialog extends DialogFragment {

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialogWrapper.Builder(this.getContext())
        .setTitle(R.string.error_title)
        .setMessage(R.string.no_highlight_message)
        .setNegativeButton(R.string.learn_more, (dialog, which) -> {
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=cxLG2wtE7TM")));
        })
        .setPositiveButton(R.string.OK, (dialog, which) -> {
          dialog.dismiss();
        }).show();

  }
}
