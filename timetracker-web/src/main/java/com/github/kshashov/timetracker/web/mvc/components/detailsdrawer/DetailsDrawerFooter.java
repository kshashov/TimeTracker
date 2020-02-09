package com.github.kshashov.timetracker.web.mvc.components.detailsdrawer;

import com.github.kshashov.timetracker.web.mvc.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.mvc.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.mvc.layout.size.Right;
import com.github.kshashov.timetracker.web.mvc.layout.size.Vertical;
import com.github.kshashov.timetracker.web.mvc.util.LumoStyles;
import com.github.kshashov.timetracker.web.mvc.util.UIUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

@Getter
public class DetailsDrawerFooter extends FlexBoxLayout {

    private Button save;
    private Button cancel;

    public DetailsDrawerFooter() {
        setBackgroundColor(LumoStyles.Color.Contrast._5);
        setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
        setSpacing(Right.S);
        setWidthFull();

        save = UIUtils.createPrimaryButton("Save");
        cancel = UIUtils.createTertiaryButton("Cancel");
        add(save, cancel);
    }

    public Registration addSaveListener(
            ComponentEventListener<ClickEvent<Button>> listener) {
        return save.addClickListener(listener);
    }

    public Registration addCancelListener(
            ComponentEventListener<ClickEvent<Button>> listener) {
        return cancel.addClickListener(listener);
    }

}
