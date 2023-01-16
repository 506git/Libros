package com.wjopms.bookapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;


import net.bookjam.bookapp.BookAppDelegate;
import net.bookjam.bookapp.bookview.BookViewController;
import net.bookjam.bookapp.bookview.StoryViewController;
import net.bookjam.papyrus.PapyrusActionParams;
import net.bookjam.papyrus.PapyrusBook;
import net.bookjam.papyrus.PapyrusBookViewController;
import net.bookjam.papyrus.PapyrusMark;
import net.bookjam.papyrus.basekit.BKString;
import net.bookjam.papyrus.basekit.BaseKit;

import java.io.File;
import java.util.HashMap;

public class BookApplication extends BookAppDelegate implements PapyrusBookViewController.Delegate {

    private Activity mApplication;

    public void initialize(Activity application) {
        mApplication = application;
        onCreate();

        registerObjectClasses();
    }

    // -----

    @Override
    public void onCreate() {
        super.onCreate();

        BaseKit.setMainActivity(mApplication);
    }

    // -----

    public void openBook(String filePath) {
        String identifier = BKString.getStringByDeletingPathExtension((new File(filePath)).getName());
        PapyrusBook book = new PapyrusBook(identifier);

        if (book.getType() == null) {
            book.updateWithZippedFile(filePath);
        }

        if (book.load()) {
            PapyrusBookViewController.setInitialParams(book, null, 0, false, this);
            Class controllerClass = getControllerClassForBook(book);
            Intent bookController = new Intent(this, controllerClass);
            //bookController.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            bookController.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mApplication.startActivity(bookController);
        } else {
            alertMessage("죄송합니다. 잘못된 책 파일입니다.");
        }
    }

    protected Class getControllerClassForBook(PapyrusBook book) {
        if (book.isStory()) {
            return StoryViewController.class;
        }

        return BookViewController.class;
    }

    // -----

    @Override
    public PackageManager getPackageManager() {
        return mApplication.getPackageManager();
    }

    @Override
    public String getPackageName() {
        return mApplication.getPackageName();
    }

    @Override
    public Object getSystemService(String name) {
        return mApplication.getSystemService(name);
    }

    @Override
    public File getFilesDir() {
        return mApplication.getFilesDir();
    }

    @Override
    public File getExternalFilesDir(String type) {
        return mApplication.getExternalFilesDir(type);
    }

    @Override
    public File getCacheDir() {
        return mApplication.getCacheDir();
    }

    @Override
    public AssetManager getAssets() {
        return mApplication.getAssets();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return mApplication.getSharedPreferences(name, mode);
    }

    @Override
    public Resources getResources() {
        return mApplication.getResources();
    }

    @Override
    public ContentResolver getContentResolver() {
        return mApplication.getContentResolver();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return mApplication.getApplicationInfo();
    }

    // -----

    @Override
    public void bookViewControllerDidStartReadingAtLocation(PapyrusBookViewController bookViewController, long location) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidEndReadingAtLocation(PapyrusBookViewController bookViewController, long location) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidKeepReadingForDuration(PapyrusBookViewController bookViewController, long duration) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidMoveToLocation(PapyrusBookViewController bookViewController, long location) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidSaveMark(PapyrusBookViewController bookViewController, PapyrusMark mark) {
        /* Do nothing */
    }

    @Override
    public String getPointsTypeForBookViewController(PapyrusBookViewController bookViewController) {
        return null;
    }

    @Override
    public long getTotalPointsForBookViewController(PapyrusBookViewController bookViewController) {
        return 0;
    }

    @Override
    public void bookViewControllerDidConsumePoints(PapyrusBookViewController bookViewController, long points) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidExhaustPoints(PapyrusBookViewController bookViewController) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidRequestToShowPointsInfo(PapyrusBookViewController bookViewController) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidRequestToOpenNextItem(PapyrusBookViewController bookViewController) {
        /* Do nothing */
    }

    @Override
    public boolean hasNextItemForBookViewController(PapyrusBookViewController bookViewController) {
        return false;
    }

    @Override
    public boolean shouldPromptNextItemForBookViewController(PapyrusBookViewController bookViewController) {
        return false;
    }

    @Override
    public void bookViewControllerDidRequestToOpenPrevItem(PapyrusBookViewController bookViewController) {
        /* Do nothing */
    }

    @Override
    public boolean hasPrevItemForBookViewController(PapyrusBookViewController bookViewController) {
        return false;
    }

    @Override
    public boolean shouldPromptPrevItemForBookViewController(PapyrusBookViewController bookViewController) {
        return false;
    }

    @Override
    public HashMap<String, Object> bookViewControllerGetDataDictForActionWhenStartReadingAtLocation(PapyrusBookViewController bookViewController, long location) {
        return null;
    }

    @Override
    public void bookViewControllerDidRequestToOpenItemForIdentifier(PapyrusBookViewController bookViewController, String identifier, final String episode) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidRequestToPurchaseItemForIdentifier(PapyrusBookViewController bookViewController, String identifier) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidFireAction(PapyrusBookViewController bookViewController, String action, PapyrusActionParams params) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidReachBeginOfFirstPage(PapyrusBookViewController bookViewController) {
        /* Do nothing */
    }

    @Override
    public void bookViewControllerDidReachEndOfLastPage(PapyrusBookViewController bookViewController) {
        /* Do nothing */
    }

    @Override
    public boolean bookViewControllerDidRequestToHandleBackPressed(PapyrusBookViewController bookViewController) {
        return false;
    }

    @Override
    public void didEnterForeground() {
        //super.didEnterForeground();
    }

    @Override
    public void didEnterBackground() {
        //super.didEnterBackground();
    }
}