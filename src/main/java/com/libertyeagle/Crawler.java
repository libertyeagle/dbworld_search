package com.libertyeagle;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Crawler {
    private static final Locale LOCALE = Locale.US;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy", LOCALE);
    private static final String INFO_SITE = "https://research.cs.wisc.edu/dbworld/browse.html";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2)"
            + "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0.2 Safari/605.1.15";
    CopyOnWriteArrayList<DBWorldMessage> dbworld_messages = new CopyOnWriteArrayList<DBWorldMessage>();

    boolean crawl(String first_item_subject) {
        long startTime = System.currentTimeMillis();

        Connection connection;
        Document document;
        try {
            connection = Jsoup.connect(INFO_SITE).userAgent(USER_AGENT);
            document = connection.get();
            if (!connection.response().contentType().contains("text/html")) {
                System.out.println("retrieved something other than html.");
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("failed to retrieve target web page.");
            return false;
        }
        // change the document charset to ISO-LATIN-1;
        document.charset(Charset.forName("ISO-8859-1"));
        Elements table_items = document.getElementsByTag("TBODY");
        boolean first_one = true;
        for (Element item : table_items) {
            Elements contents = item.getElementsByTag("TD");
            Element date_sent_elem = contents.get(0);
            Element message_type_elem = contents.get(1);
            Element sender_elem = contents.get(2);
            Element detail_href_elem = contents.get(3).getElementsByTag("A").first();
            Element deadline_elem = contents.get(4);
            Element web_page_elem = contents.get(5).getElementsByTag("A").first();

            String detail_url = detail_href_elem.attr("HREF");
            String subject_title = detail_href_elem.text();
            if (first_one) {
                first_one = false;
                if (subject_title.equals(first_item_subject)) {
                    System.out.println("no messages updated since last time.");
                    System.out.println("quitting...");
                    return false;
                }
            }

            Date date_sent;
            try {
                date_sent = DATE_FORMAT.parse(date_sent_elem.text());
            } catch (ParseException pe) {
                System.out.println("failed to parse / no sent date " + subject_title);
                date_sent = new Date(0);
            }
            String message_type = message_type_elem.text();
            String sender = sender_elem.text();
            Date deadline;
            try {
                deadline = DATE_FORMAT.parse(deadline_elem.text());
            } catch (ParseException pe) {
                System.out.println("failed to parse / no deadline on " + subject_title);
                deadline = new Date(0);
            }

            String web_page_url = web_page_elem == null ? null : web_page_elem.attr("HREF");
            DBWorldMessage current_message = new DBWorldMessage();
            current_message.date_sent = date_sent;
            current_message.message_type = message_type;
            current_message.sender = sender;
            current_message.subject_title = subject_title;
            current_message.detail_url = detail_url;
            current_message.deadline = deadline;
            current_message.web_page_url = web_page_url;
            current_message.description = null;
            current_message.conf_info = null;
            dbworld_messages.add(current_message);
        }
        ExecutorService thread_pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int index = 0; index < dbworld_messages.size(); index++) {
            thread_pool.execute(new DetailPageCrawlThread(dbworld_messages.get(index).detail_url, index));
        }

        thread_pool.shutdown();
        boolean is_wait = true;
        while (is_wait) {
            try {
                is_wait = !thread_pool.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                System.out.println("interrupted while awaiting completion of threads.");
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("running time for crawler: " + (endTime - startTime) / 1000 + "s");
        return true;
    }

    private class DetailPageCrawlThread implements Runnable {
        private String url;
        private int index;

        DetailPageCrawlThread(String url, int index) {
            this.url = url;
            this.index = index;
        }

        public void run() {
            Connection connection;
            Document document;
            try {
                connection = Jsoup.connect(url).userAgent(USER_AGENT);
                document = connection.get();
                if (!connection.response().contentType().contains("text/html")) {
                    System.out.println("retrieved something other than html in retrieving " + url);
                    return;
                }
            } catch (IOException ioe) {
                System.out.println("failed to retrieve target web page in retrieving " + url);
                return;
            }
            document.charset(Charset.forName("ISO-8859-1"));
            // get text from body
            Element body = document.body();
            dbworld_messages.get(index).description = body.text();
        }
    }
}
