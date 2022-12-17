package kr.co.smartandwise.eco_epub3_module.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import kr.co.smartandwise.eco_epub3_module.Model.Book;
import kr.co.smartandwise.eco_epub3_module.Model.Bookmark;
import kr.co.smartandwise.eco_epub3_module.Model.Highlight;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper sInstance = null;

    public static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context);
        }
        return sInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, "viewer.sqlite", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS book");
        db.execSQL("DROP TABLE IF EXISTS bookmark");
        db.execSQL("DROP TABLE IF EXISTS highlight");

        initDatabase(db);
    }

    private void initDatabase(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE book (" +
                "_id INTEGER PRIMARY KEY autoincrement, " +
                "content_id TEXT, " +
                "root_path TEXT, " +
                "book_data TEXT " +
                ");");

        db.execSQL("CREATE TABLE bookmark (" +
                "_id INTEGER PRIMARY KEY autoincrement, " +
                "book_id INTEGER, " +
                "create_time INTEGER, " +
                "idref TEXT, " +
                "cfi TEXT, " +
                "content TEXT " +
                ");");

        db.execSQL("CREATE TABLE highlight (" +
                "_id INTEGER PRIMARY KEY autoincrement, " +
                "book_id INTEGER, " +
                "create_time INTEGER, " +
                "idref TEXT, " +
                "cfi TEXT, " +
                "content TEXT, " +
                "memo TEXT, " +
                "color TEXT " +
                ");");
    }

    public void insertOrUpdateBook(String contentId, String rootPath, String bookData) {
        SQLiteDatabase db = getWritableDatabase();

        if (db.rawQuery("select * from book where root_path='" + rootPath + "';", null).getCount() == 0) {
            ContentValues cvBook = new ContentValues();
            cvBook.put("content_id", contentId);
            cvBook.put("root_path", rootPath);
            cvBook.put("book_data", bookData);

            db.insert("book", null, cvBook);
        } else {
            Cursor cBook = db.query("book", new String[] {"content_id"}, "root_path=?", new String[] { rootPath }, null, null, null);
            if (cBook != null && cBook.moveToFirst()) {
                contentId = cBook.getString(0);
            }

            updateBookById(contentId, bookData);
        }

        db.close();
    }
    public void updateBookById(final String contentId, final String bookData) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cvBook = new ContentValues();
        cvBook.put("book_data", bookData);

        db.update("book", cvBook, "content_id=?", new String[]{ contentId });

        db.close();
    }
    public Book getBookById(final String contentId) {
        SQLiteDatabase db = getReadableDatabase();
        Book book = null;

        Cursor cBook = db.query("book", new String[] {"content_id, root_path, book_data"}, "content_id=?", new String[] { contentId }, null, null, null);
        if (cBook != null && cBook.moveToFirst()) {
            book = new Book();
            book.setContentId(cBook.getString(0));
            book.setRootPath(cBook.getString(1));
            book.setBookData(cBook.getString(2));
        }

        db.close();

        return book;
    }
    public Book getBookByPath(final String rootPath) {
        SQLiteDatabase db = getReadableDatabase();
        Book book = null;

        Cursor cBook = db.query("book", new String[] {"content_id, root_path, book_data"}, "root_path=?", new String[] { rootPath }, null, null, null);
        if (cBook != null && cBook.moveToFirst()) {
            book = new Book();
            book.setContentId(cBook.getString(0));
            book.setRootPath(cBook.getString(1));
            book.setBookData(cBook.getString(2));
        }

        db.close();

        return book;

    }

    public void deleteBook(final String contentId) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete("book", "content_id=?", new String[]{ contentId });
        db.close();

        /*StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for(int i=0; i<stack.length; i++) {
            Log.d("databaseHelper class", stack[i].getClassName());
            Log.d("databaseHelper line", ""+stack[i].getLineNumber());
        }
        Log.d("databaseHelper", "deleteBook id="+contentId);*/
    }

    public void clearBook() {
        SQLiteDatabase db = getWritableDatabase();

        db.delete("book", null, null);
        db.close();

        /*StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for(int i=0; i<stack.length; i++) {
            Log.d("databaseHelper class", stack[i].getClassName());
            Log.d("databaseHelper line", ""+stack[i].getLineNumber());
        }
        Log.d("databaseHelper", "clearBookCalled");*/
    }


    public Bookmark insertBookmark(final int bookId, final Bookmark bookmark) {
        SQLiteDatabase db = getWritableDatabase();

        if (db.rawQuery("select _id from bookmark where content_id = '" + String.valueOf(bookmark.getId()) + "';", null).getCount() == 0) {
            ContentValues cvBookmark = new ContentValues();
            cvBookmark.put("_id", bookmark.getId());
            cvBookmark.put("book_id", bookId);
            cvBookmark.put("create_time", System.currentTimeMillis());
            cvBookmark.put("idref", bookmark.getIdref());
            cvBookmark.put("cfi", bookmark.getCfi());
            cvBookmark.put("content", bookmark.getContent());

            db.insert("bookmark", null, cvBookmark);
        }

        Bookmark rstBookmark = getBookmark(bookId, bookmark.getIdref(), bookmark.getCfi());
        db.close();

        return rstBookmark;
    }

    public Bookmark getBookmark(final int bookId, final String idref, final String cfi) {
        SQLiteDatabase db = getReadableDatabase();

        Bookmark bookmark = new Bookmark();
        Cursor cBookmark = db.query("bookmark", new String[] {"_id, create_time, idref, cfi, content"}, "book_id=? and idref=? and cfi=?", new String[]{ String.valueOf(bookId), idref, cfi }, null, null, null);
        if (cBookmark != null && cBookmark.moveToFirst()) {
            bookmark.setId(cBookmark.getInt(0));
            bookmark.setCreateTime(cBookmark.getLong(1));
            bookmark.setIdref(cBookmark.getString(2));
            bookmark.setCfi(cBookmark.getString(3));
            bookmark.setContent(cBookmark.getString(4));
        } else {
            bookmark = null;
        }

        db.close();

        return bookmark;
    }

    public ArrayList<Bookmark> getBookmarkList(final String bookId) {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();

        Cursor cBookmark = db.query("bookmark", new String[] { "_id, create_time, idref, cfi, content" }, "book_id=?", new String[] { bookId }, null, null, "_id asc");
        if (cBookmark != null && cBookmark.moveToFirst()) {
            do {
                Bookmark bookmark = new Bookmark();
                bookmark.setId(cBookmark.getInt(0));
                bookmark.setCreateTime(cBookmark.getLong(1));
                bookmark.setIdref(cBookmark.getString(2));
                bookmark.setCfi(cBookmark.getString(3));
                bookmark.setContent(cBookmark.getString(4));

                bookmarkList.add(bookmark);
            } while (cBookmark.moveToNext());
        }

        db.close();

        return bookmarkList;
    }

    public Bookmark deleteBookmark(final int bookId, final String idref, final String cfi) {
        SQLiteDatabase db = getWritableDatabase();

        Bookmark rstBookmark = getBookmark(bookId, idref, cfi);

        if (rstBookmark != null) {
            db.delete("bookmark", "book_id=? and idref=? and cfi=?", new String[]{ String.valueOf(bookId), idref, cfi });
        }

        db.close();

        return rstBookmark;
    }

    public Highlight insertHighlight(final int bookId, final Highlight highlight) {
        SQLiteDatabase db = getWritableDatabase();

        if (db.rawQuery("select _id from highlight where book_id = '" + String.valueOf(bookId) + "' and idref = '" + highlight.getIdref() + "' and  cfi = '" + highlight.getCfi() + "';", null).getCount() == 0) {
            ContentValues cvBookmark = new ContentValues();
            cvBookmark.put("create_time", System.currentTimeMillis());
            cvBookmark.put("idref", highlight.getIdref());
            cvBookmark.put("cfi", highlight.getCfi());
            cvBookmark.put("content", highlight.getContent());
            cvBookmark.put("memo", highlight.getMemo());
            cvBookmark.put("color", highlight.getColor());

            db.insert("highlight", null, cvBookmark);
        }

        Highlight rstHighlight = getHighlight(highlight.getId());
        db.close();

        return rstHighlight;
    }

    public Highlight updateHighlight(final int bookId, final Highlight highlight) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cvBookmark = new ContentValues();
        cvBookmark.put("idref", highlight.getIdref());
        cvBookmark.put("cfi", highlight.getCfi());
        cvBookmark.put("content", highlight.getContent());
        cvBookmark.put("memo", highlight.getMemo());
        cvBookmark.put("color", highlight.getColor());

        db.update("highlight", cvBookmark, "_id=? and book_id=?", new String[] { String.valueOf(highlight.getId()), String.valueOf(bookId) });

        Highlight rstHighlight = getHighlight(highlight.getId());

        db.close();

        return rstHighlight;
    }

    public Highlight getHighlight(final int highlightId) {
        SQLiteDatabase db = getReadableDatabase();

        Highlight highlight = new Highlight();

        Cursor cHighlight = db.query("highlight", new String[] { "_id, create_time, idref, cfi, content, memo, color" }, "_id=?", new String[] { String.valueOf(highlightId) }, null, null, null);
        if (cHighlight != null && cHighlight.moveToFirst()) {
            highlight.setId(cHighlight.getInt(0));
            highlight.setCreateTime(cHighlight.getLong(1));
            highlight.setIdref(cHighlight.getString(2));
            highlight.setCfi(cHighlight.getString(3));
            highlight.setContent(cHighlight.getString(4));
            highlight.setMemo(cHighlight.getString(5));
            highlight.setColor(cHighlight.getString(6));
        } else {
            highlight = null;
        }

        db.close();

        return highlight;
    }

    public ArrayList<Highlight> getHighlightList(final int bookId) {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<Highlight> highlightList = new ArrayList<Highlight>();

        Cursor cHighlight = db.query("highlight", new String[] { "_id, create_time, idref, cfi, content, memo, color" }, "_id=?", new String[] { String.valueOf(bookId) }, null, null, "_id asc");
        if (cHighlight != null && cHighlight.moveToFirst()) {
            do {
                Highlight highlight = new Highlight();
                highlight.setId(cHighlight.getInt(0));
                highlight.setCreateTime(cHighlight.getLong(1));
                highlight.setIdref(cHighlight.getString(2));
                highlight.setCfi(cHighlight.getString(3));
                highlight.setContent(cHighlight.getString(4));
                highlight.setMemo(cHighlight.getString(5));
                highlight.setColor(cHighlight.getString(6));

                highlightList.add(highlight);
            } while (cHighlight.moveToNext());
        }

        db.close();

        return highlightList;
    }

    public Highlight deleteHighlight(final int highlightId) {
        SQLiteDatabase db = getWritableDatabase();

        Highlight rstHighlight = getHighlight(highlightId);

        if (rstHighlight != null) {
            db.delete("highlight", "highlight_id=?", new String[]{ String.valueOf(highlightId) });
        }

        db.close();

        return rstHighlight;
    }
}
