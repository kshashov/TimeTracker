package com.github.kshashov.timetracker.core.i18n;

import lombok.NonNull;

import java.util.Locale;

public interface Translator {
    String toLocale(@NonNull String key, Locale locale);
}
