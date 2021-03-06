package com.github.kshashov.timetracker.data.service.admin.users;

import com.github.kshashov.timetracker.data.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private String name;
    private DayOfWeek weekStart;

    public UserInfo(User user) {
        setWeekStart(user.getWeekStart());
        setName(user.getName());
    }
}
