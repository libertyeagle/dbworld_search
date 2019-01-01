package com.libertyeagle;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InfoExtractor {
    private static final Locale LOCALE = Locale.US;
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", LOCALE);
    private static final String[] TOPIC_KEYWORDS = {"topic", "not limited to", "might cover"};
    private static final String[] TOPIC_FOLLOWING_KEYWORDS = {"submission procedure", "submission format",
            "submission guidelines", "submissions \n", "submissions\n",
            "editors-in-chief", "important dates", "organization"};

    public static void extract_conf_info() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("MST"));

        InfoGetter info_getter = new InfoGetter();
        info_getter.read_data_from_file();
        ArrayList<DBWorldMessage> dbworld_messages = info_getter.dbworld_info;
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        props.setProperty("ner.applyFineGrained", "false");
        props.setProperty("sutime.markTimeRanges", "true");
        props.setProperty("sutime.includeRange", "true");
        props.setProperty("threads", Integer.toString(Runtime.getRuntime().availableProcessors()));
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        long startTime = System.currentTimeMillis();

        for (DBWorldMessage message : dbworld_messages) {
            if (message.message_type.equals("conf. ann.")) {
                String text = message.description;
                CoreDocument document = new CoreDocument(text);
                pipeline.annotate(document);

                // extract conference date
                Date conference_start_date = new Date(0);
                Date conference_end_date = new Date(0);
                boolean flag = false;
                for (int index = 0; index < document.entityMentions().size(); index++) {
                    CoreEntityMention em = document.entityMentions().get(index);
                    if (em.entityType().equals("DATE")) {
                        CoreMap cm = em.coreMap();
                        String parsed_time = cm.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                        if (parsed_time.length() == 10
                                && !parsed_time.equals("FUTURE_REF")
                                && !parsed_time.startsWith("XXXX") && !parsed_time.startsWith("OFFSET")) {
                            flag = true;
                            try {
                                conference_start_date = DATE_FORMAT.parse(parsed_time);
                            } catch (ParseException pe) {
                                System.out.println("failed to parse conference date " + parsed_time + " in NLP for "
                                        + message.subject_title);
                                continue;
                            }
                            conference_end_date = conference_start_date;
                            break;
                        } else if (parsed_time.length() == 4 && index >= 2) {
                            CoreEntityMention em_day_range = document.entityMentions().get(index - 1);
                            CoreEntityMention em_month = document.entityMentions().get(index - 2);
                            if (em_day_range.entityType().equals("DATE") && em_month.entityType().equals("DATE")) {
                                CoreMap cm_day_range = em_day_range.coreMap();
                                CoreMap cm_month = em_month.coreMap();
                                String day_range =
                                        cm_day_range.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                                String month = cm_month.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                                if (day_range.length() == 17 && month.length() == 7) {
                                    String conf_month = month.substring(5);
                                    String conf_start_day = day_range.substring(6, 8);
                                    String conf_end_day = day_range.substring(15);
                                    // parse conference start date
                                    String conf_start = parsed_time + "-" + conf_month + "-" + conf_start_day;
                                    try {
                                        conference_start_date = DATE_FORMAT.parse(conf_start);
                                    } catch (ParseException pe) {
                                        System.out.println("failed to parse conference date " + conf_start + " in NLP for "
                                                + message.subject_title);
                                        continue;
                                    }
                                    // parse conference end date
                                    String conf_end = parsed_time + "-" + conf_month + "-" + conf_end_day;
                                    try {
                                        conference_end_date = DATE_FORMAT.parse(conf_end);
                                    } catch (ParseException pe) {
                                        System.out.println("failed to parse conference date " + conf_end + " in NLP for "
                                                + message.subject_title);
                                        continue;
                                    }
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                // if cannot resolve conference date, set to
                // January 1, 1970, 00:00:00 GMT
                if (!flag) {
                    conference_start_date = new Date(0);
                    conference_end_date = new Date(0);
                }

                // extract conference location
                String conference_location = "Unknown";
                flag = false;
                // location are generally consisted of two parts (or more)
                for (CoreSentence sentence : document.sentences()) {
                    for (int index = 0; index < sentence.entityMentions().size(); index++) {
                        CoreEntityMention em = sentence.entityMentions().get(index);
                        if (em.entityType().equals("LOCATION")) {
                            StringBuilder builder = new StringBuilder(em.text());
                            int next_index = index + 1;
                            CoreEntityMention em_following = null;
                            if (next_index < sentence.entityMentions().size())
                                em_following = sentence.entityMentions().get(next_index);
                            while (next_index < sentence.entityMentions().size()
                                    && em_following != null && em_following.entityType().equals("LOCATION")) {
                                builder.append(", ");
                                builder.append(em_following.text());
                                next_index++;
                                if (next_index < sentence.entityMentions().size())
                                    em_following = sentence.entityMentions().get(next_index);
                            }
                            if (next_index > index + 1) {
                                conference_location = builder.toString();
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (flag) break;
                }
                // if cannot found two consecutive locations, we fallback to single location
                if (!flag) {
                    for (CoreSentence sentence : document.sentences()) {
                        for (int index = 0; index < sentence.entityMentions().size(); index++) {
                            CoreEntityMention em = sentence.entityMentions().get(index);
                            if (em.entityType().equals("LOCATION")) {
                                conference_location = em.text();
                                flag = true;
                                break;
                            }
                        }
                        if (flag) break;
                    }
                }

                // extract topics
                StringBuilder topic_builder = new StringBuilder();
                boolean last_sentence_topic = false;
                for (CoreSentence sentence : document.sentences()) {
                    boolean topic_found = false;
                    String sentence_text = sentence.text().trim();
                    String sentence_text_lower = sentence_text.toLowerCase();
                    for (String keyword : TOPIC_KEYWORDS) {
                        if (sentence_text_lower.contains(keyword)) {
                            topic_found = true;
                            break;
                        }
                    }
                    if (!topic_found && (sentence_text.startsWith("---") ||
                            sentence_text.startsWith("___") || sentence_text.startsWith("===")))
                        last_sentence_topic = false;
                    if (!topic_found) {
                        for (String keyword : TOPIC_FOLLOWING_KEYWORDS) {
                            if (sentence_text_lower.startsWith(keyword)) {
                                last_sentence_topic = false;
                                break;
                            }
                        }
                    }
                    if (last_sentence_topic && (sentence_text.startsWith("-") || sentence_text.startsWith("*")
                            || sentence_text.startsWith("\u2022") || sentence_text.startsWith("\u00B7")))
                        topic_found = true;

                    if (topic_found) {
                        last_sentence_topic = true;
                        int topic_end_pos = -1;
                        for (String keyword : TOPIC_FOLLOWING_KEYWORDS) {
                            int last_pos = sentence_text_lower.lastIndexOf(keyword);
                            if (last_pos != -1) {
                                if (topic_end_pos == -1 || last_pos < topic_end_pos) {
                                    topic_end_pos = last_pos;
                                }
                            }
                        }
                        if (topic_end_pos != -1) {
                            sentence_text = sentence_text.substring(0, topic_end_pos);
                            int last_blank_line_pos = sentence_text.lastIndexOf("\n\n");
                            if (last_blank_line_pos != -1) {
                                topic_builder.append('\n');
                                topic_builder.append(sentence_text.substring(0, last_blank_line_pos).trim());
                                continue;
                            }
                        }
                        topic_builder.append('\n');
                        topic_builder.append(sentence_text);
                    }
                }
                String topic = (topic_builder.length() == 0) ? "Unknown" : topic_builder.toString();
                ConferenceInfo current_conf_info = new ConferenceInfo();
                current_conf_info.start_date = conference_start_date;
                current_conf_info.end_date = conference_end_date;
                current_conf_info.location = conference_location;
                current_conf_info.topics = topic;
                message.conf_info = current_conf_info;
                System.out.println("NLP processed completed for " + message.subject_title);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("running time for NLP: " + (endTime - startTime) / 1000 + "s");

        FileOutputStream file_out;
        try {
            file_out = new FileOutputStream("DBWorld_Data/CONF_PROCESSED_DATA.bin");
        } catch (FileNotFoundException fnfe) {
            System.out.println("file not found while create FileOutputStream");
            return;
        }
        try {
            ObjectOutputStream obj_out = new ObjectOutputStream(file_out);
            obj_out.writeObject(dbworld_messages);
            obj_out.close();
            file_out.close();
            System.out.println("data after information extraction for conference has been serialized.");
        } catch (IOException ioe) {
            System.out.println("I/O exception encountered in writing.");
        }
    }
}
