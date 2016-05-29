package net.coscolla.highlight.recognition;

import rx.Observable;

public interface  Recognition {

  Observable<String> recognition(String filePath);

}
