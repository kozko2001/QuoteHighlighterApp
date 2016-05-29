package net.coscolla.highlight.model;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;


@AutoValue
public abstract class Highlight implements HighlightSQLModel, Parcelable {
  public static final Mapper<Highlight> MAPPER = new Mapper<>(AutoValue_Highlight::new);

  public static final class Marshal extends HighlightSQLMarshal<Marshal> { }
}
