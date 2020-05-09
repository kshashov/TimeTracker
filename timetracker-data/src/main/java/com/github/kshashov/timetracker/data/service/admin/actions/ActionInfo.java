package com.github.kshashov.timetracker.data.service.admin.actions;

import com.github.kshashov.timetracker.data.entity.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActionInfo {
    String title;

    public ActionInfo(Action bean) {
        setTitle(bean.getTitle());
    }
}
