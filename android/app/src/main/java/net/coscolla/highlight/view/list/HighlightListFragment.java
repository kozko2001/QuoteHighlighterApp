package net.coscolla.highlight.view.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import net.coscolla.highlight.HighlightApplication;
import net.coscolla.highlight.R;
import net.coscolla.highlight.model.Highlight;
import net.coscolla.highlight.model.HighlightRepository;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static rx.schedulers.Schedulers.io;

public class HighlightListFragment extends Fragment {

  private RecyclerView list;
  private HighlightListAdapter adapter;
  private HighlightRepository repository;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_list_highlight, container, false);
    list = (RecyclerView) view.findViewById(R.id.list);
    adapter = new HighlightListAdapter(getContext());
    repository = new HighlightRepository(HighlightApplication.getDb());

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    setupList();
  }

  @Override
  public void onResume() {
    super.onResume();
    getData();
  }

  private void setupList() {
    list.setLayoutManager(new LinearLayoutManager(this.getContext()));
    list.setHasFixedSize(true);
    list.setItemAnimator(new DefaultItemAnimator());
    list.setAdapter(adapter);
    adapter.setOnRecyclerViewItemClickListener((view, position) -> {
      onHighlightSelected(position);
    });
  }

  private void onHighlightSelected(int position) {

  }

  private void getData() {
    repository.filter("")
        .toList()
        .subscribeOn(io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe((highlights) -> adapter.setNewData(highlights),
            (e) -> {
              Timber.d("ERROR " + e );
            });
  }

  public static HighlightListFragment create() {
    HighlightListFragment f = new HighlightListFragment();
    Bundle args = new Bundle();
    f.setArguments(args);
    return f;
  }

  public void newHighlightAdded(Highlight newHighlightAdded) {
    adapter.add(0, newHighlightAdded);
  }
}
