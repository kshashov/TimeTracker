package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Right;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.LumoStyles;
import com.github.kshashov.timetracker.web.ui.util.css.BoxSizing;

public class FullScreenButtonsWidget extends FullScreenWidget {

    @Override
    protected void initFooter(FlexBoxLayout footer) {
        super.initFooter(footer);

        footer.setBackgroundColor(LumoStyles.Color.Contrast._5);
        footer.setBoxSizing(BoxSizing.BORDER_BOX);
        footer.setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
        footer.setSpacing(Right.S);
        footer.setWidthFull();
    }
}
