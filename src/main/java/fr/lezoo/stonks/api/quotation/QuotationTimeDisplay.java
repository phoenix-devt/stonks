package fr.lezoo.stonks.api.quotation;

public enum QuotationTimeDisplay {
    QUARTERHOUR, HOUR, DAY, WEEK, MONTH, YEAR;


    /**
     * @return the time in millisecond corresponding to each term of the enum
     * BE CAREFUL OVERFLOW WITH INTEGER
     */
    public long getTime() {
        switch (this) {
            case QUARTERHOUR:
                return 1000L*15L * 60L;
            case HOUR:
                return 1000L*60L * 60L;
            case DAY:
                return 1000L*24L * 60L * 60L;
            case WEEK:
                return 1000L* 7L * 24L * 60L * 60L;
            case MONTH:
                return 1000L* 30L * 24L * 60L * 60L;
            case YEAR:
                return 1000L*365L * 24L * 60L * 60L;
        }
        return 0;

    }

    /**
     * Check if the string corresponds to the enum
     */
    public static boolean checkQuotationTimeDisplay(String s) {
        for (QuotationTimeDisplay quot : QuotationTimeDisplay.values()) {
            if (quot.toString().equals(s))
                return true;

        }
        return false;
    }

}
