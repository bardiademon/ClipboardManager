package com.bardiademon.manager.clipboard.controller;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.manager.clipboard.ClipboardManagerApplication;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.listener.OnClipboardListener;
import com.bardiademon.manager.clipboard.manager.ClipboardManager;
import com.bardiademon.manager.clipboard.service.ClipboardService;
import com.bardiademon.manager.clipboard.view.MainFrame;

import java.io.File;
import java.util.List;

public class ClipboardController implements OnClipboardListener {

    public ClipboardController() {
        ClipboardManager.manager(this);
        System.out.println("Listening to clipboard changes...");
    }

    @Override
    public void onString(String data) {
        System.out.println("OnString Clipboard: " + data);
        ClipboardService.repository().saveClipboard(null, data, ClipboardType.STRING);
        System.gc();
    }

    @Override
    public void onImage(File image) {
        System.out.println("onImage");
        ClipboardService.repository().saveClipboard(null, image.getAbsolutePath(), ClipboardType.IMAGE);
        System.gc();
    }

    @Override
    public void onFile(List<File> files) {
        System.out.println("onFile Clipboard: " + files);
        String data = JjsonArray.ofCollection(files.stream().map(File::getAbsoluteFile).toList()).encode();
        ClipboardService.repository().saveClipboard(null, data, ClipboardType.FILE);
        System.gc();
    }

    @Override
    public void onData(ClipboardType clipboardType, Object data) {
        removeLastClipboard();
        MainFrame.update(false);
    }

    private void removeLastClipboard() {

        int clipboardSaveCount = ClipboardManagerApplication.getConfig().clipboardSaveCount();

        if (clipboardSaveCount < 0) {
            return;
        }

        int total = ClipboardService.repository().fetchTotalClipboards();

        if (total <= clipboardSaveCount) {
            return;
        }

        int removeLimit = total - clipboardSaveCount;

        ClipboardService.repository().deleteClipboard(removeLimit + 1);
    }

}
