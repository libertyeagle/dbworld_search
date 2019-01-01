package com.libertyeagle;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CreateIndex {
    private static final String INDEX_DIR = "DBWorld_Data/lucene_index";

    public static void create_lucene_index() {
        Directory directory;
        try {
             directory = FSDirectory.open(Paths.get(INDEX_DIR));
        } catch (IOException ioe) {
            System.out.println("cannot open lucene index directory.");
            return;
        }
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter index_writer;
        try {
            index_writer = new IndexWriter(directory, config);
        } catch (IOException ioe) {
            System.out.println("failed to create IndexWriter.");
            return;
        }
        InfoGetter info_getter = new InfoGetter();
        info_getter.read_processed_data_from_file();
        ArrayList<DBWorldMessage> dbworld_data = info_getter.dbworld_info;
        int total = 0;
        for (DBWorldMessage message : dbworld_data) {
            Document document = new Document();
            TextField body = new TextField("body", message.description, Field.Store.YES);
            StringField sender = new StringField("sender", message.sender, Field.Store.YES);
            StringField message_type = new StringField("type", message.message_type, Field.Store.YES);
            TextField subject_title = new TextField("title", message.subject_title, Field.Store.YES);
            StringField detail_url = new StringField("url", message.detail_url, Field.Store.YES);
            StringField web_page_url = null;
            if (message.web_page_url != null) {
                web_page_url = new StringField("web_page", message.web_page_url, Field.Store.YES);
            }
            String date_sent_converted = DateTools.dateToString(message.date_sent, DateTools.Resolution.DAY);
            String deadline_converted = DateTools.dateToString(message.deadline, DateTools.Resolution.DAY);
            StringField date_sent = new StringField("date_sent", date_sent_converted, Field.Store.YES);
            StringField deadline = new StringField("deadline", deadline_converted, Field.Store.YES);
            document.add(body);
            document.add(sender);
            document.add(message_type);
            document.add(subject_title);
            document.add(deadline);
            if (web_page_url != null) {
                document.add(web_page_url);
            }
            document.add(detail_url);
            document.add(date_sent);
            if (message.message_type.equals("conf. ann.")) {
                // conference, index conference date, location, and topics
                TextField conf_topics = new TextField("conf_topics", message.conf_info.topics, Field.Store.YES);
                String conf_start_date_converted =
                        DateTools.dateToString(message.conf_info.start_date, DateTools.Resolution.DAY);
                String conf_end_date_converted =
                        DateTools.dateToString(message.conf_info.end_date, DateTools.Resolution.DAY);
                StringField conf_start_date =
                        new StringField("conf_start_date", conf_start_date_converted, Field.Store.YES);
                StringField conf_end_date =
                        new StringField("conf_end_date", conf_end_date_converted, Field.Store.YES);
                TextField conf_location =
                        new TextField("conf_location", message.conf_info.location, Field.Store.YES);
                document.add(conf_topics);
                document.add(conf_start_date);
                document.add(conf_end_date);
                document.add(conf_location);
            }
            try {
                index_writer.addDocument(document);
            } catch (IOException ioe) {
                System.out.println("failed to create index for document " + message.subject_title);
                return;
            }
            total++;
        }
        try {
            index_writer.close();
        } catch (IOException ioe) {
            System.out.println("failed to close IndexWriter");
            return;
        }
        System.out.println("processed " + Integer.toString(total) + " items" );
    }
}
