package com.unitconverter.app.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Greetings for the home header.
 *
 * On launch the header cycles through hellos from around the world, starting
 * with "Namaste", and settles on the viewer's own: Namaste for India/Hindi,
 * otherwise the greeting for the device language (English "Hello" if unknown).
 */
public final class Greetings {

    private Greetings() {
    }

    /** {language code, greeting}; romanised so the display face can set them. */
    private static final String[][] WORLD = {
            {"hi", "Namaste"},
            {"es", "Hola"},
            {"fr", "Bonjour"},
            {"ja", "Konnichiwa"},
            {"ta", "Vanakkam"},
            {"de", "Hallo"},
            {"ar", "Marhaba"},
            {"pt", "Olá"},
            {"zh", "Nǐ hǎo"},
            {"bn", "Nomoshkar"},
            {"it", "Ciao"},
            {"ko", "Annyeong"},
            {"ru", "Privet"},
            {"pa", "Sat Sri Akal"},
            {"tr", "Merhaba"},
            {"sw", "Jambo"},
            {"en", "Hello"},
    };

    /** The sequence to cycle through, ending on {@link #home(boolean)}. */
    public static List<String> intro(boolean indian) {
        List<String> out = new ArrayList<>();
        out.add("Namaste");
        out.add("Hola");
        out.add("Bonjour");
        out.add("Konnichiwa");
        out.add("Vanakkam");
        out.add("Hello");
        String home = home(indian);
        if (!out.get(out.size() - 1).equals(home)) out.add(home);
        return out;
    }

    /** The greeting to settle on. */
    public static String home(boolean indian) {
        if (indian) return "Namaste";
        String lang = Locale.getDefault().getLanguage();
        for (String[] g : WORLD) {
            if (g[0].equals(lang)) return g[1];
        }
        return "Hello";
    }

    public enum DayPart { MORNING, AFTERNOON, EVENING, NIGHT }

    public static DayPart dayPart() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (h >= 5 && h < 12) return DayPart.MORNING;
        if (h >= 12 && h < 17) return DayPart.AFTERNOON;
        if (h >= 17 && h < 21) return DayPart.EVENING;
        return DayPart.NIGHT;
    }

    public static String english(DayPart part) {
        switch (part) {
            case MORNING: return "Good morning";
            case AFTERNOON: return "Good afternoon";
            case EVENING: return "Good evening";
            default: return "Good night";
        }
    }

    /** Hindi in Devanagari, shown when Hindi touches are on. */
    public static String hindi(DayPart part) {
        switch (part) {
            case MORNING: return "सुप्रभात";
            case AFTERNOON: return "नमस्ते";
            case EVENING: return "शुभ संध्या";
            default: return "शुभ रात्रि";
        }
    }
}
