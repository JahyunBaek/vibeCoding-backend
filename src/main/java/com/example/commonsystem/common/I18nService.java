package com.example.commonsystem.common;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

/**
 * MessageSource 기반 다국어 메시지 조회 서비스.
 * locale 문자열("ko", "en")을 받아 해당 언어의 메시지를 반환한다.
 */
@Service
public class I18nService {

    private final MessageSource messageSource;

    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public Locale toLocale(String locale) {
        if (locale == null || locale.isBlank()) return Locale.KOREAN;
        return Locale.forLanguageTag(locale);
    }

    public String getMessage(String code, String locale, Object... args) {
        return messageSource.getMessage(code, args, code, toLocale(locale));
    }

    public String getMessage(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, code, locale);
    }
}
