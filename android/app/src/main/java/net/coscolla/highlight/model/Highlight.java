package net.coscolla.highlight.model;


import com.google.auto.value.AutoValue;


@AutoValue
public abstract class Highlight implements HighlightSQLModel {
  public static final Mapper<Highlight> MAPPER = new Mapper<>(AutoValue_Highlight::new);

  public static final class Marshal extends HighlightSQLMarshal<Marshal> { }
}
