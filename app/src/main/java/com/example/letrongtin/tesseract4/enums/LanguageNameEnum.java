package com.example.letrongtin.tesseract4.enums;

public enum LanguageNameEnum {

    AFRIKAANS("afr"),
    ALBANIAN("sqi"),
    ARABIC("ara"),
    AZERI("aze"),
    BASQUE("eus"),
    BELARUSIAN("bel"),
    BENGALI("ben"),
    BULGARIAN("bul"),
    CATALAN("cat"),
    CHINESE_SIMPLIFIED("chi_sim"),
    CHINESET_TRADITIONAL("chi_tra"),
    CROATIAN("hrv"),
    CZECH("ces"),
    DANISH("dan"),
    DUTCH("nld"),
    ENGLISH("eng"),
    ESTONIAN("est"),
    FINNISH("fin"),
    FRENCH("fra"),
    GALICIAN("glg"),
    GERMAN("deu"),
    GREEK("ell"),
    HEBREW("heb"),
    HINDI("hin"),
    HUNGARIAN("hun"),
    ICELANDIC("isl"),
    INDONESIAN("ind"),
    ITALIAN("ita"),
    JAPANESE("jpn"),
    KANNADA("kan"),
    KOREAN("kor"),
    LATVIAN("lav"),
    LITHUANIAN("lit"),
    MACEDONIAN("mkd"),
    MALAY("msa"),
    MALAYALAM("mal"),
    MALTESE("mlt"),
    NORWEGIAN("nor"),
    POLISH("pol"),
    PORTUGUESE("por"),
    ROMANIAN("ron"),
    RUSSIAN("rus"),
    SERBIAN("srp"),
    SLOVAK("slk"),
    SLOVENIAN("slv"),
    SPANISH("spa"),
    SWAHILI("swa"),
    SWEDISH("swe"),
    TAGALOG("tgl"),
    TAMIL("tam"),
    TELUGU("tel"),
    THAI("tha"),
    TURKISH("tur"),
    UKRAINIAN("ukr"),
    VIETNAMESE("vie");

    public String name;

    LanguageNameEnum(String name) {
        this.name = name;
    }

    public static LanguageNameEnum getLanguageNameEnum(String name) {
        for (LanguageNameEnum e : LanguageNameEnum.values()) {
            if (e.name.equals(name))
                return e;
        }
        return ENGLISH;
    }

}
