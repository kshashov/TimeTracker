package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.layout.size.Bottom;
import com.github.kshashov.timetracker.web.ui.layout.size.Uniform;
import com.github.kshashov.timetracker.web.ui.util.css.BorderRadius;
import com.github.kshashov.timetracker.web.ui.util.css.Shadow;

public class LargeWidget extends Widget {
    public LargeWidget() {
        super();

        initLayout();
    }

    protected void initLayout() {
        super.initLayout();
        setBorderRadius(BorderRadius.L);
        setShadow(Shadow.M);
    }

    protected void initContent(FlexBoxLayout content) {
        super.initContent(content);
        content.setPadding(Bottom.RESPONSIVE_L);

        content.setVisible(false);
    }

    protected void initHeader(FlexBoxLayout header) {
        super.initHeader(header);
        header.setPadding(Uniform.M);
        header.getStyle().set("border-top-left-radius", BorderRadius.L.getValue());
        header.getStyle().set("border-top-right-radius", BorderRadius.L.getValue());
    }
}
