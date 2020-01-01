package com.github.kshashov.timetracker.core.i18n;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageSourceBasedTranslator implements Translator {
    private final MessageSource messages;

    @Autowired
    public MessageSourceBasedTranslator(@Qualifier("timetracker.MessageSource") MessageSource messageSource) {
        this.messages = messageSource;
    }

    public String toLocale(@NonNull String key, Locale locale) {
        return messages.getMessage(key, null, locale);
    }
}
