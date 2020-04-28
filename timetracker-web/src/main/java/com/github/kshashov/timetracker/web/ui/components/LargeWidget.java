package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.layout.size.Bottom;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Uniform;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.LumoStyles;
import com.github.kshashov.timetracker.web.ui.util.css.BorderRadius;
import com.github.kshashov.timetracker.web.ui.util.css.Shadow;

public class LargeWidget extends Widget {
    public LargeWidget() {
        super();

        initLayout();
    }

    protected void initLayout() {
        super.initLayout();

        setMargin(Uniform.L);
        setBorderRadius(BorderRadius.L);
        setShadow(Shadow.XL);
    }

    protected void initContent(FlexBoxLayout content) {
        super.initContent(content);
        content.setPadding(Bottom.L);
        content.getStyle().set("border-bottom-left-radius", BorderRadius.L.getValue());
        content.getStyle().set("border-bottom-right-radius", BorderRadius.L.getValue());

        content.setVisible(false);
    }

    protected void initHeader(FlexBoxLayout header) {
        super.initHeader(header);
        header.setBackgroundColor(LumoStyles.Color.Contrast._5);
        header.setPadding(Horizontal.RESPONSIVE_L);
        header.setPadding(Vertical.S);
        header.getStyle().set("border-top-left-radius", BorderRadius.L.getValue());
        header.getStyle().set("border-top-right-radius", BorderRadius.L.getValue());
    }
}
