package com.github.kshashov.timetracker.web.ui.mvp;

public interface Presenter<V extends View<? extends Presenter<?>>> {
    default void onAttach(V view) {
        // do nothing
    }

    default void onDetach() {
        // do nothing
    }
}
