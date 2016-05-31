package net.coscolla.highlight.view.list;

import android.content.Context;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import net.coscolla.highlight.R;
import net.coscolla.highlight.model.Highlight;
import java.util.ArrayList;

public class HighlightListAdapter extends BaseQuickAdapter<Highlight> {

  public HighlightListAdapter(Context context) {
    super(context, R.layout.fragment_list_item_highlight, new ArrayList<>());
  }

  @Override
  protected void convert(BaseViewHolder helper, Highlight highlight) {
    helper.setText(R.id.text, highlight.text())
        .setImageUrl(R.id.image, highlight.highlighted())
        .setOnClickListener(R.id.btn_change_text, new OnItemChildClickListener())
        .setOnClickListener(R.id.btn_see_image, new OnItemChildClickListener());
  }

}
