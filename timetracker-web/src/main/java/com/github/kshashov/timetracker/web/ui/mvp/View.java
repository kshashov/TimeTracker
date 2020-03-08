package com.github.kshashov.timetracker.web.ui.mvp;

public interface View<P extends Presenter<? extends View<?>>> {
    default P getPresenter() {
        return null;
    }
}
