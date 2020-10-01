package com.thefidebox.fidebox.utils;

import java.util.Calendar;
import java.util.Date;

public class DateDisplay {

    public static String format(Date date) {

        //minute hour day
        Date currentTime = Calendar.getInstance().getTime();
        long timeDifference = (currentTime.getTime() - date.getTime());

        String timeShownOnCV = null;

        if(timeDifference<0){
            timeShownOnCV="0s";
        } else if (timeDifference < 1000 * 60 /* 1min */) {
            timeShownOnCV = (Math.round(timeDifference / 1000)) + "s"/* in min eg 54s */;
        } else if (timeDifference < 1000 * 60 * 60 /* 1hr */) {
            timeShownOnCV = (Math.round(timeDifference / 1000 / 60)) + "m"/* in min eg 54min */;
        } else if (timeDifference < 1000 * 60 * 60 * 24 /* 24hr */) {
            timeShownOnCV = Math.round(timeDifference / 1000 / 60 / 60) + "h"; /* in hr eg 7h*/
        } else {

            timeShownOnCV = Math.round(timeDifference / 1000 / 60 / 60 / 24) + "d"; /* in day eg 7day*/
        }

        return timeShownOnCV;

    }

}
