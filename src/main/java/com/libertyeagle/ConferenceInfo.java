package com.libertyeagle;

import java.io.Serializable;
import java.util.Date;

class ConferenceInfo implements Serializable {
    Date start_date;
    Date end_date;
    String location;
    String topics;
}
