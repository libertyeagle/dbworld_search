package com.libertyeagle;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class InfoGetter {
    ArrayList<DBWorldMessage> dbworld_info = new ArrayList<DBWorldMessage>();

    public void get_dbworld_messages() {
        Crawler dbworld_crawler = new Crawler();
        boolean flag;
        if (dbworld_info.size() != 0) {
            flag = dbworld_crawler.crawl(dbworld_info.get(0).subject_title);
        } else {
            flag = dbworld_crawler.crawl(null);
        }
        if (flag) {
            CopyOnWriteArrayList<DBWorldMessage> dbworld_messages = dbworld_crawler.dbworld_messages;
            FileOutputStream file_out;
            try {
                file_out = new FileOutputStream("DBWorld_Data/ORIG_DATA.bin");
            } catch (FileNotFoundException fnfe) {
                System.out.println("file not found while create FileOutputStream");
                return;
            }
            try {
                ObjectOutputStream obj_out = new ObjectOutputStream(file_out);
                obj_out.writeObject(dbworld_messages);
                obj_out.close();
                file_out.close();
                System.out.println("original data serialized.");
            } catch (IOException ioe) {
                System.out.println("I/O exception encountered in writing.");
            }
        }
    }

    public void read_data_from_file() {
        FileInputStream file_in;
        try {
            file_in = new FileInputStream("DBWorld_Data/ORIG_DATA.bin");
        } catch (FileNotFoundException fnfe) {
            System.out.println("file not found while create FileInputStream");
            return;
        }
        CopyOnWriteArrayList<DBWorldMessage> dbworld_list;
        try {
            ObjectInputStream obj_in = new ObjectInputStream(file_in);
            dbworld_list = (CopyOnWriteArrayList<DBWorldMessage>) obj_in.readObject();
            obj_in.close();
            file_in.close();
        } catch (IOException ioe) {
            System.out.println("I/O exception encountered in reading.");
            return;
        } catch (ClassNotFoundException ce) {
            System.out.println("class not found.");
            return;
        }
        dbworld_info.clear();
        dbworld_info.addAll(dbworld_list);
    }
}
