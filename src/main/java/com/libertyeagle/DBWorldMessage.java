package com.libertyeagle;

import java.io.Serializable;
import java.util.Date;

class DBWorldMessage implements Serializable {
    Date date_sent;
    String sender;
    String message_type;
    String subject_title;
    String detail_url;
    Date deadline;
    String web_page_url;
    String description;
    ConferenceInfo conf_info;
}
