package com.eitankri.tanachyomi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class ParekYomiCalculator {
    private static final String[] sfarim = {"יהושע", "שופטים", "שמואל", "מלכים", "ישעיהו", "ירמיהו", "יחזקאל", "תרי עשר", "תהילים"
            , "משלי", "איוב", "שיר השירים", "רות", "איכה", "קהלת", "אסתר", "דניאל", "עזרא ונחמיה", "דברי הימים", "דברי הימים"};
    private static final int[] howMuchinSefer = {14, 14, 34, 35, 26, 31, 29, 21, 19
            , 8, 8, 1, 1, 1, 4, 5, 7, 10, 25};
    ArrayList<String> holidays = new ArrayList<>(
            Arrays.asList("פסח", "שבועות", "תשעה באב",
                    "ראש השנה", "סוכות", "שמיני עצרת", "יום כיפור", "פורים", "יום העצמאות"));


    public String getParekYomi(JewishCalendar todayDate) {
        boolean isStudent = MainActivity.getIfStudent();
        //כדי לקבל הכל בעברית
        HebrewDateFormatter hebrewDateFormatter = new HebrewDateFormatter();
        hebrewDateFormatter.setHebrewFormat(true);
        //כאן מכניס את כל הפרקים שיש בשנה כולל השינויים
        ArrayList<String> allPrakim = new ArrayList<>();
        int countDays = 0;//סופר את הימים שקוראים בהם


            //אם היום הנוכחי הוא חודש שלא קוראים בו אז אני מחזיר כבר ערך
            if (holidays.contains(hebrewDateFormatter.formatYomTov(todayDate))) {
                return "היום " + hebrewDateFormatter.formatYomTov(todayDate) + " לכן לא קוראים היום";
            }


            int extraDays;//הימים הנוספים ללא הימים שנוספים מהחודש מעובר
            //בודק אם מדובר לפני כג תשרי ואז מראה שמדובר בסבב של שנה קודמת
            if (todayDate.getJewishMonth() == 7 && todayDate.getJewishDayOfMonth() < 23) {
                todayDate.setJewishYear(todayDate.getJewishYear() - 1);
            }
            //מקבל 0-4 תלוי כמה ימים נופלים על שבתות
            extraDays = (isStudent)?0:getExtraDays(todayDate);

            //רץ על כל הספרים מספר הפרקים שבו
            for (int i = 0; i < sfarim.length - 1; i++) {
                for (int j = 0; j < howMuchinSefer[i]; j++) {
                    //לפי מספר הימים שצריך להוסיף את מחכה לפרק ואז מוסיף מה שמוסיף
                    //בכוונה גדול אחד מהשני כי כך יכול לרוץ על כמה
                    if (sfarim[i].equals("רות") && extraDays > 0) {
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1) + "1");
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1) + "2");
                    } else if (sfarim[i].equals("שיר השירים") && extraDays > 1) {
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1) + "1");
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1) + "2");

                    } else if (sfarim[i].equals("ירמיהו") && extraDays > 2 && j == 8) {
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1) + "1");
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1) + "2");

                    } else if (sfarim[i].equals("יהושע") && extraDays > 3 && j == 3) {
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1) + "1");
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1) + "2");

                    } else {
                        allPrakim.add("" + sfarim[i] + " " + hebrewDateFormatter.formatHebrewNumber(j + 1));
                    }
                }
            }


        if (!isStudent) {

            //אם שנה מעוברת אז מוסיף עוד דברי הימים
            if (JewishCalendar.isJewishLeapYear(todayDate.getJewishYear())) {
                for (int i = 0; i < howMuchinSefer[howMuchinSefer.length - 1]; i++) {
                    allPrakim.add("" + sfarim[howMuchinSefer.length - 1] + " " + hebrewDateFormatter.formatHebrewNumber(i + 1) + "*");
                }
            }

            //תחילת הסבב כג תשרי
            JewishCalendar beginningsDate = new JewishCalendar();
            beginningsDate.setJewishDate(todayDate.getJewishYear(), 7, 23, 0, 0, 0);


            while (!(beginningsDate.getJewishMonth() == todayDate.getJewishMonth()
                    && beginningsDate.getJewishDayOfMonth() == todayDate.getJewishDayOfMonth())) {
                if (beginningsDate.getDayOfWeek() == Calendar.SATURDAY) {
                } else if (holidays.contains(hebrewDateFormatter.formatYomTov(beginningsDate))) {
                } else {
                    countDays++;
                }
                beginningsDate.forward(Calendar.DAY_OF_MONTH, 1);
            }
        }else {
            Calendar beginningsDate = Calendar.getInstance();
            beginningsDate.set((beginningsDate.get(Calendar.MONTH) < 9) ? beginningsDate.get(Calendar.YEAR) - 1 : beginningsDate.get(Calendar.YEAR), 9, 1);

            while (!(beginningsDate.get(Calendar.MONTH) == todayDate.getGregorianCalendar().get(Calendar.MONTH)
                    && beginningsDate.get(Calendar.DAY_OF_MONTH) == todayDate.getGregorianCalendar().get(Calendar.DAY_OF_MONTH))) {

                if (!holidays.contains(hebrewDateFormatter.formatYomTov(new JewishCalendar(beginningsDate)))) {
                    countDays++;
                }
                beginningsDate.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        //מחזיר את הפרק לפי היום הימים שצריך לקרוא שספרנו
        if (countDays>allPrakim.size()){
            return "";

        }
        return "סדר הקריאה היומי הוא: " + allPrakim.get(countDays);
    }

    //מקבל מספר ימים נוספים שנוצרים מכך שחג נופל על שבת
    private int getExtraDays(JewishCalendar jewishCalendar) {
        //הנוסחה היא האם שנה מעוברת -> באיזה יום חל ראש השנה של שנה הבאה
        //האם השנה מלאה = גם חשוון וגם כסלו שלמים
        //כסדרה = חשוון קצר כסלו מלא
        //חסרה = שניהם חסרים
        boolean full = (jewishCalendar.isCheshvanLong() && !jewishCalendar.isKislevShort());
        boolean regular = (!jewishCalendar.isCheshvanLong() && !jewishCalendar.isKislevShort());
        boolean half = (!jewishCalendar.isCheshvanLong() && jewishCalendar.isKislevShort());

        if (JewishCalendar.isJewishLeapYear(jewishCalendar.getJewishYear())) {
            //מקבל את יום של ראש השנה
            //מוסיף אחד בגלל שצריך לקבל את שנה הבאה
            //מוסיף עוד אחד כי מקבל ערכים של 0-6 וצריך 1-7
            //מחלק ב 7 כדי לקבל יום בשבוע
            switch ((JewishDate.getJewishCalendarElapsedDays(jewishCalendar.getJewishYear() + 1) + 1) % 7) {
                case 2:
                    if (full) {
                        return 2;
                    }
                    if (regular) {
                        return 1;
                    }
                    if (half) {
                        return 1;
                    }

                case 3:
                    return 0;
                case 5:
                    if (full) {
                        return 2;

                    }
                    if (half) {
                        return 1;

                    }

                case 0:
                    if (full) {
                        return 4;
                    }
                    if (half) {
                        return 2;
                    }
            }
        } else {
            switch ((JewishDate.getJewishCalendarElapsedDays(jewishCalendar.getJewishYear() + 1) + 1) % 7) {
                case 2:
                    return 0;
                case 3:
                    if (full) {
                        return 1;

                    }
                    if (half) {
                        return 0;

                    }
                case 5:
                    if (full) {
                        return 2;

                    }
                    if (regular) {
                        return 0;

                    }
                    if (half) {
                        return 0;

                    }
                case 0:
                    if (full) {
                        return 3;

                    }
                    if (regular) {
                        return 2;

                    }
            }
        }


        return -1;
    }
}
