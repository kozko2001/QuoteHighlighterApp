package net.coscolla.highlight.view.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import net.coscolla.highlight.model.Highlight;

public class ChangeTextDialog extends DialogFragment {

  public static final int CHANGE_TEXT_DIALOG_RESULT_CODE = 3423;
  public static final String ARG_MODEL = "model";

  public static DialogFragment newInstance(Highlight model) {
    ChangeTextDialog fragment = new ChangeTextDialog();
    Bundle args = new Bundle();
    args.putParcelable(ARG_MODEL, model);
    fragment.setArguments(args);
    return fragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Highlight model = getArguments().getParcelable(ARG_MODEL);
    return new MaterialDialog.Builder(this.getContext())
        .title("Change current text")
        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        .input("", model.text(), (dialog, input) -> {
          Intent intent = getActivity().getIntent();
          intent.putExtra("text", input.toString());
          intent.putExtra("model", model);

          getTargetFragment().onActivityResult(getTargetRequestCode(), CHANGE_TEXT_DIALOG_RESULT_CODE, intent);
        }).build();
  }
}
