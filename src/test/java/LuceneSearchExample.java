import com.libertyeagle.DBWorldMessage;
import com.libertyeagle.QueryProcessor;

import java.util.ArrayList;

public class LuceneSearchExample {
    public static void main(String[] args) {
        QueryProcessor query_processor = new QueryProcessor();
        query_processor.init();
        ArrayList<DBWorldMessage> results =
                query_processor.search_conference_default("multimedia");
        for (DBWorldMessage message : results) {
            System.out.println(message.subject_title);
            System.out.println(message.detail_url);
            System.out.println("---------------------");
        }
    }
}
