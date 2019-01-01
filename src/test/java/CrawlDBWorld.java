import com.libertyeagle.InfoExtractor;
import com.libertyeagle.InfoGetter;

public class CrawlDBWorld {
    public static void main(String[] args) {
        InfoGetter info_getter = new InfoGetter();
        info_getter.get_dbworld_messages();
        InfoExtractor.extract_conf_info();
    }
}
