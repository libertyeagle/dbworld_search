package com.libertyeagle;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class QueryProcessor {
    private static final String INDEX_DIR = "/Users/libertyeagle/dbworld_search/DBWorld_Data/lucene_index";
    private static final Locale LOCALE = Locale.US;
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", LOCALE);
    private IndexReader index_reader;
    private IndexSearcher index_searcher;
    private ArrayList<DBWorldMessage> dbworld_data;


    public void init() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("MST"));
        Directory directory;
        try {
            directory = FSDirectory.open(Paths.get(INDEX_DIR));
        } catch (IOException ioe) {
            System.out.println("cannot open lucene index directory.");
            return;
        }
        try {
            index_reader = DirectoryReader.open(directory);
        } catch (IOException ioe) {
            System.out.println(INDEX_DIR);
            System.out.println("cannot create IndexReader.");
            return;
        }
        index_searcher = new IndexSearcher(index_reader);
    }

    public ArrayList<DBWorldMessage> search_conference_default(String query_text) {
        ArrayList<DBWorldMessage> query_results = new ArrayList<DBWorldMessage>();

        String[] fields = {"title", "body", "conf_topics"};
        MultiFieldQueryParser query_parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
        Query conf_type_query = new TermQuery(new Term("type", "conf. ann."));

        BooleanQuery query;
        try {
            query = new BooleanQuery.Builder()
                    .add(query_parser.parse(query_text), BooleanClause.Occur.MUST)
                    .add(conf_type_query, BooleanClause.Occur.MUST)
                    .build();
        }
        catch (ParseException pe) {
            System.out.println("failed to parse query: " + query_text);
            return query_results;
        }
        try {
            TopDocs top_docs = index_searcher.search(query, 1000);
            for (ScoreDoc score_doc : top_docs.scoreDocs) {
                Document document = index_searcher.doc(score_doc.doc);
                DBWorldMessage current_result = new DBWorldMessage();
                current_result.date_sent = DateTools.stringToDate(document.get("date_sent"));
                current_result.message_type = "conf. ann.";
                current_result.sender = document.get("sender");
                current_result.subject_title =
                        get_highlighted_result(query, "title", document.get("title"));
                if (current_result.subject_title == null)
                    current_result.subject_title = document.get("title");
                current_result.description =
                        get_highlighted_result(query, "body", document.get("body"));
                if (current_result.description == null)
                     current_result.description = document.get("body");
                current_result.deadline = DateTools.stringToDate(document.get("deadline"));
                current_result.detail_url = document.get("url");
                current_result.web_page_url = document.get("web_page");
                current_result.conf_info = new ConferenceInfo();
                current_result.conf_info.start_date = DateTools.stringToDate(document.get("conf_start_date"));
                current_result.conf_info.end_date = DateTools.stringToDate(document.get("conf_end_date"));
                current_result.conf_info.topics =
                        get_highlighted_result(query, "conf_topics", document.get("conf_topics"));
                if (current_result.conf_info.topics == null)
                    current_result.conf_info.topics = document.get("conf_topics");
                current_result.conf_info.location = document.get("conf_location");
                query_results.add(current_result);
            }
        }
        catch (IOException ioe) {
            System.out.println("search failed! query: " + query_text);
            return query_results;
        }
        catch (java.text.ParseException pe){
            System.out.println("failed parsing date when retrieving document.");
            return query_results;
        }
        catch (InvalidTokenOffsetsException itoe) {
            System.out.println("failed to highlight search results.");
            return query_results;
        }
        return query_results;
    }

    public ArrayList<DBWorldMessage> search_conf_by_conf_date(String query_text) {
        ArrayList<DBWorldMessage> query_results = new ArrayList<DBWorldMessage>();

        Query start_date_query, end_date_query;
        try {
            Date query_date = DATE_FORMAT.parse(query_text);
            start_date_query = new TermQuery
                    (new Term("conf_start_date", DateTools.dateToString(query_date, DateTools.Resolution.DAY)));
            end_date_query = new TermQuery
                    (new Term("conf_end_date", DateTools.dateToString(query_date, DateTools.Resolution.DAY)));
        }
        catch (java.text.ParseException pe) {
            System.out.println("failed to parse query: " + query_text);
            return query_results;
        }
        Query conf_type_query = new TermQuery(new Term("type", "conf. ann."));

        BooleanQuery date_query = new BooleanQuery.Builder()
                .add(start_date_query, BooleanClause.Occur.SHOULD)
                .add(end_date_query, BooleanClause.Occur.SHOULD)
                .build();
        BooleanQuery query = new BooleanQuery.Builder()
                .add(date_query, BooleanClause.Occur.MUST)
                .add(conf_type_query, BooleanClause.Occur.MUST)
                .build();
        try {
            TopDocs top_docs = index_searcher.search(query, 1000);
            for (ScoreDoc score_doc : top_docs.scoreDocs) {
                Document document = index_searcher.doc(score_doc.doc);
                DBWorldMessage current_result = new DBWorldMessage();
                current_result.date_sent = DateTools.stringToDate(document.get("date_sent"));
                current_result.message_type = "conf. ann.";
                current_result.sender = document.get("sender");
                current_result.subject_title = document.get("title");
                current_result.description = document.get("body");
                current_result.deadline = DateTools.stringToDate(document.get("deadline"));
                current_result.detail_url = document.get("url");
                current_result.web_page_url = document.get("web_page");
                current_result.conf_info = new ConferenceInfo();
                current_result.conf_info.start_date = DateTools.stringToDate(document.get("conf_start_date"));
                current_result.conf_info.end_date = DateTools.stringToDate(document.get("conf_end_date"));
                current_result.conf_info.topics = document.get("conf_topics");
                current_result.conf_info.location = document.get("conf_location");
                query_results.add(current_result);
            }
        }
        catch (IOException ioe) {
            System.out.println("search failed! query: " + query_text);
            return query_results;
        }
        catch (java.text.ParseException pe){
            System.out.println("failed parsing date when retrieving document.");
            return query_results;
        }
        return query_results;
    }

    public ArrayList<DBWorldMessage> search_conf_by_deadline(String query_text) {
        ArrayList<DBWorldMessage> query_results = new ArrayList<DBWorldMessage>();

        Query deadline_query;
        try {
            Date query_date = DATE_FORMAT.parse(query_text);
            deadline_query = new TermQuery
                    (new Term("deadline", DateTools.dateToString(query_date, DateTools.Resolution.DAY)));
        }
        catch (java.text.ParseException pe) {
            System.out.println("failed to parse query: " + query_text);
            return query_results;
        }
        Query conf_type_query = new TermQuery(new Term("type", "conf. ann."));
        BooleanQuery query = new BooleanQuery.Builder()
                .add(deadline_query, BooleanClause.Occur.MUST)
                .add(conf_type_query, BooleanClause.Occur.MUST)
                .build();
        try {
            TopDocs top_docs = index_searcher.search(query, 1000);
            for (ScoreDoc score_doc : top_docs.scoreDocs) {
                Document document = index_searcher.doc(score_doc.doc);
                DBWorldMessage current_result = new DBWorldMessage();
                current_result.date_sent = DateTools.stringToDate(document.get("date_sent"));
                current_result.message_type = "conf. ann.";
                current_result.sender = document.get("sender");
                current_result.subject_title = document.get("title");
                current_result.description = document.get("body");
                current_result.deadline = DateTools.stringToDate(document.get("deadline"));
                current_result.detail_url = document.get("url");
                current_result.web_page_url = document.get("web_page");
                current_result.conf_info = new ConferenceInfo();
                current_result.conf_info.start_date = DateTools.stringToDate(document.get("conf_start_date"));
                current_result.conf_info.end_date = DateTools.stringToDate(document.get("conf_end_date"));
                current_result.conf_info.topics = document.get("conf_topics");
                current_result.conf_info.location = document.get("conf_location");
                query_results.add(current_result);
            }
        }
        catch (IOException ioe) {
            System.out.println("search failed! query: " + query_text);
            return query_results;
        }
        catch (java.text.ParseException pe){
            System.out.println("failed parsing date when retrieving document.");
            return query_results;
        }
        return query_results;
    }

    public ArrayList<DBWorldMessage> search_conf_by_location(String query_text) {
        ArrayList<DBWorldMessage> query_results = new ArrayList<DBWorldMessage>();

        QueryParser query_parser = new QueryParser("conf_location", new StandardAnalyzer());
        Query conf_type_query = new TermQuery(new Term("type", "conf. ann."));

        BooleanQuery query;
        try {
            query = new BooleanQuery.Builder()
                    .add(query_parser.parse(query_text), BooleanClause.Occur.MUST)
                    .add(conf_type_query, BooleanClause.Occur.MUST)
                    .build();
        }
        catch (ParseException pe) {
            System.out.println("failed to parse query: " + query_text);
            return query_results;
        }
        try {
            TopDocs top_docs = index_searcher.search(query, 1000);
            for (ScoreDoc score_doc : top_docs.scoreDocs) {
                Document document = index_searcher.doc(score_doc.doc);
                DBWorldMessage current_result = new DBWorldMessage();
                current_result.date_sent = DateTools.stringToDate(document.get("date_sent"));
                current_result.message_type = "conf. ann.";
                current_result.sender = document.get("sender");
                current_result.subject_title = document.get("title");
                current_result.description = document.get("body");
                current_result.deadline = DateTools.stringToDate(document.get("deadline"));
                current_result.detail_url = document.get("url");
                current_result.web_page_url = document.get("web_page");
                current_result.conf_info = new ConferenceInfo();
                current_result.conf_info.start_date = DateTools.stringToDate(document.get("conf_start_date"));
                current_result.conf_info.end_date = DateTools.stringToDate(document.get("conf_end_date"));
                current_result.conf_info.topics = document.get("conf_topics");
                current_result.conf_info.location =
                        get_highlighted_result(query, "conf_location", document.get("conf_location"));
                if (current_result.conf_info.location == null)
                    current_result.conf_info.location = document.get("conf_location");
                query_results.add(current_result);
            }
        }
        catch (IOException ioe) {
            System.out.println("search failed! query: " + query_text);
            return query_results;
        }
        catch (java.text.ParseException pe) {
            System.out.println("failed parsing date when retrieving document.");
            return query_results;
        }
        catch (InvalidTokenOffsetsException itoe) {
            System.out.println("failed to highlight search results.");
            return query_results;
        }
        return query_results;
    }

    public ArrayList<DBWorldMessage> search_conf_by_topics(String query_text) {
        ArrayList<DBWorldMessage> query_results = new ArrayList<DBWorldMessage>();

        QueryParser query_parser = new QueryParser("conf_topics", new StandardAnalyzer());
        Query conf_type_query = new TermQuery(new Term("type", "conf. ann."));

        BooleanQuery query;
        try {
            query = new BooleanQuery.Builder()
                    .add(query_parser.parse(query_text), BooleanClause.Occur.MUST)
                    .add(conf_type_query, BooleanClause.Occur.MUST)
                    .build();
        }
        catch (ParseException pe) {
            System.out.println("failed to parse query: " + query_text);
            return query_results;
        }
        try {
            TopDocs top_docs = index_searcher.search(query, 1000);
            for (ScoreDoc score_doc : top_docs.scoreDocs) {
                Document document = index_searcher.doc(score_doc.doc);
                DBWorldMessage current_result = new DBWorldMessage();
                current_result.date_sent = DateTools.stringToDate(document.get("date_sent"));
                current_result.message_type = "conf. ann.";
                current_result.sender = document.get("sender");
                current_result.subject_title = document.get("title");
                current_result.description = document.get("body");
                current_result.deadline = DateTools.stringToDate(document.get("deadline"));
                current_result.detail_url = document.get("url");
                current_result.web_page_url = document.get("web_page");
                current_result.conf_info = new ConferenceInfo();
                current_result.conf_info.start_date = DateTools.stringToDate(document.get("conf_start_date"));
                current_result.conf_info.end_date = DateTools.stringToDate(document.get("conf_end_date"));
                current_result.conf_info.topics =
                        get_highlighted_result(query, "conf_topics", document.get("conf_topics"));
                if (current_result.conf_info.topics == null)
                    current_result.conf_info.topics = document.get("conf_topics");
                current_result.conf_info.location = document.get("conf_location");
                query_results.add(current_result);
            }
        }
        catch (IOException ioe) {
            System.out.println("search failed! query: " + query_text);
            return query_results;
        }
        catch (java.text.ParseException pe){
            System.out.println("failed parsing date when retrieving document.");
            return query_results;
        }
        catch (InvalidTokenOffsetsException itoe) {
            System.out.println("failed to highlight search results.");
            return query_results;
        }
        return query_results;
    }

    public ArrayList<DBWorldMessage> search_job_default(String query_text) {
        ArrayList<DBWorldMessage> query_results = new ArrayList<DBWorldMessage>();

        String[] fields = {"title", "body"};
        MultiFieldQueryParser query_parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
        Query job_type_query = new TermQuery(new Term("type", "job ann."));

        BooleanQuery query;
        try {
            query = new BooleanQuery.Builder()
                    .add(query_parser.parse(query_text), BooleanClause.Occur.MUST)
                    .add(job_type_query, BooleanClause.Occur.MUST)
                    .build();
        }
        catch (ParseException pe) {
            System.out.println("failed to parse query: " + query_text);
            return query_results;
        }
        try {
            TopDocs top_docs = index_searcher.search(query, 1000);
            for (ScoreDoc score_doc : top_docs.scoreDocs) {
                Document document = index_searcher.doc(score_doc.doc);
                DBWorldMessage current_result = new DBWorldMessage();
                current_result.date_sent = DateTools.stringToDate(document.get("date_sent"));
                current_result.message_type = "job ann.";
                current_result.sender = document.get("sender");
                current_result.subject_title =
                        get_highlighted_result(query, "title", document.get("title"));
                if (current_result.subject_title == null)
                    current_result.subject_title = document.get("title");
                current_result.description =
                        get_highlighted_result(query, "body", document.get("body"));
                if (current_result.description == null)
                    current_result.description = document.get("body");
                current_result.deadline = DateTools.stringToDate(document.get("deadline"));
                current_result.detail_url = document.get("url");
                current_result.web_page_url = document.get("web_page");
                current_result.conf_info = null;
                query_results.add(current_result);
            }
        }
        catch (IOException ioe) {
            System.out.println("search failed! query: " + query_text);
            return query_results;
        }
        catch (java.text.ParseException pe){
            System.out.println("failed parsing date when retrieving document.");
            return query_results;
        }
        catch (InvalidTokenOffsetsException itoe) {
            System.out.println("failed to highlight search results.");
            return query_results;
        }
        return query_results;
    }

    public ArrayList<DBWorldMessage> search_journal_default(String query_text) {
        ArrayList<DBWorldMessage> query_results = new ArrayList<DBWorldMessage>();

        String[] fields = {"title", "body"};
        MultiFieldQueryParser query_parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
        Query journal_ann_type_query = new TermQuery(new Term("type", "journal ann."));
        Query journal_cfp_type_query = new TermQuery(new Term("type", "journal CFP"));
        BooleanQuery journal_query = new BooleanQuery.Builder()
                .add(journal_ann_type_query, BooleanClause.Occur.SHOULD)
                .add(journal_cfp_type_query, BooleanClause.Occur.SHOULD)
                .build();

        BooleanQuery query;
        try {
            query = new BooleanQuery.Builder()
                    .add(query_parser.parse(query_text), BooleanClause.Occur.MUST)
                    .add(journal_query, BooleanClause.Occur.MUST)
                    .build();
        }
        catch (ParseException pe) {
            System.out.println("failed to parse query: " + query_text);
            return query_results;
        }
        try {
            TopDocs top_docs = index_searcher.search(query, 1000);
            for (ScoreDoc score_doc : top_docs.scoreDocs) {
                Document document = index_searcher.doc(score_doc.doc);
                DBWorldMessage current_result = new DBWorldMessage();
                current_result.date_sent = DateTools.stringToDate(document.get("date_sent"));
                current_result.message_type = document.get("type");
                current_result.sender = document.get("sender");
                current_result.subject_title =
                        get_highlighted_result(query, "title", document.get("title"));
                if (current_result.subject_title == null)
                    current_result.subject_title = document.get("title");
                current_result.description =
                        get_highlighted_result(query, "body", document.get("body"));
                if (current_result.description == null)
                    current_result.description = document.get("body");
                current_result.deadline = DateTools.stringToDate(document.get("deadline"));
                current_result.detail_url = document.get("url");
                current_result.web_page_url = document.get("web_page");
                current_result.conf_info = null;
                query_results.add(current_result);
            }
        }
        catch (IOException ioe) {
            System.out.println("search failed! query: " + query_text);
            return query_results;
        }
        catch (java.text.ParseException pe){
            System.out.println("failed parsing date when retrieving document.");
            return query_results;
        }
        catch (InvalidTokenOffsetsException itoe) {
            System.out.println("failed to highlight search results.");
            return query_results;
        }
        return query_results;
    }

    public ArrayList<DBWorldMessage> search_others_default(String query_text) {
        ArrayList<DBWorldMessage> query_results = new ArrayList<DBWorldMessage>();

        String[] fields = {"title", "body"};
        MultiFieldQueryParser query_parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
        Query journal_ann_type_query = new TermQuery(new Term("type", "journal ann."));
        Query journal_cfp_type_query = new TermQuery(new Term("type", "journal CFP"));
        Query conf_type_query = new TermQuery(new Term("type", "conf. ann."));
        Query job_type_query = new TermQuery(new Term("type", "job ann."));
        BooleanQuery others_type_query = new BooleanQuery.Builder()
                .add(conf_type_query, BooleanClause.Occur.MUST_NOT)
                .add(job_type_query, BooleanClause.Occur.MUST_NOT)
                .add(journal_cfp_type_query, BooleanClause.Occur.MUST_NOT)
                .add(journal_ann_type_query, BooleanClause.Occur.MUST_NOT)
                .build();

        BooleanQuery query;
        try {
            query = new BooleanQuery.Builder()
                    .add(query_parser.parse(query_text), BooleanClause.Occur.MUST)
                    .add(others_type_query, BooleanClause.Occur.MUST)
                    .build();
        }
        catch (ParseException pe) {
            System.out.println("failed to parse query: " + query_text);
            return query_results;
        }
        try {
            TopDocs top_docs = index_searcher.search(query, 1000);
            for (ScoreDoc score_doc : top_docs.scoreDocs) {
                Document document = index_searcher.doc(score_doc.doc);
                DBWorldMessage current_result = new DBWorldMessage();
                current_result.date_sent = DateTools.stringToDate(document.get("date_sent"));
                current_result.message_type = "journal ann.";
                current_result.sender = document.get("sender");
                current_result.subject_title =
                        get_highlighted_result(query, "title", document.get("title"));
                if (current_result.subject_title == null)
                    current_result.subject_title = document.get("title");
                current_result.description =
                        get_highlighted_result(query, "body", document.get("body"));
                if (current_result.description == null)
                    current_result.description = document.get("body");
                current_result.deadline = DateTools.stringToDate(document.get("deadline"));
                current_result.detail_url = document.get("url");
                current_result.web_page_url = document.get("web_page");
                current_result.conf_info = null;
                query_results.add(current_result);
            }
        }
        catch (IOException ioe) {
            System.out.println("search failed! query: " + query_text);
            return query_results;
        }
        catch (java.text.ParseException pe){
            System.out.println("failed parsing date when retrieving document.");
            return query_results;
        }
        catch (InvalidTokenOffsetsException itoe) {
            System.out.println("failed to highlight search results.");
            return query_results;
        }
        return query_results;
    }

    private String get_highlighted_result(Query query, String field_name, String field_value)
            throws IOException, InvalidTokenOffsetsException {
        Formatter formatter =
                new SimpleHTMLFormatter("<span style='color:red;font-weight:bold'>", "</span>");
        QueryScorer query_scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, query_scorer);
        highlighter.setTextFragmenter(new SimpleSpanFragmenter(query_scorer, Integer.MAX_VALUE));
        highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);
        return highlighter.getBestFragment(new StandardAnalyzer(), field_name, field_value);
    }
}
