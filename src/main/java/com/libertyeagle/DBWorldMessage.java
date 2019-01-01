package com.libertyeagle;

import java.io.Serializable;
import java.util.Date;

public class DBWorldMessage implements Serializable {
    public Date date_sent;
    public String sender;
    public String message_type;
    public String subject_title;
    public String detail_url;
    public Date deadline;
    public String web_page_url;
    public String description;
    public ConferenceInfo conf_info;
}
