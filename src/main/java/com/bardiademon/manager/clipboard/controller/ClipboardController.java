package com.bardiademon.manager.clipboard.controller;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.manager.clipboard.ClipboardManagerApplication;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.listener.OnClipboardListener;
import com.bardiademon.manager.clipboard.manager.ClipboardManager;
import com.bardiademon.manager.clipboard.service.ClipboardService;
import com.bardiademon.manager.clipboard.view.MainFrame;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class ClipboardController implements OnClipboardListener {

    private final static Logger logger = LogManager.getLogger(ClipboardController.class);

    public ClipboardController() {
        ClipboardManager.manager(this);
        logger.trace("Listening to clipboard changes...");
    }

    @Override
    public void onString(String data) {
        logger.trace("OnString Clipboard: {}", data);
        ClipboardService.repository().saveClipboard(null, data, ClipboardType.STRING).onComplete(saveClipboardHandler -> {
            if (saveClipboardHandler.failed()) {
                logger.error("Failed to save clipboard, Data: {}", data, saveClipboardHandler.cause());
            } else {
                logger.trace("Successfully save clipboard, Data: {}", data);
                MainFrame.update(false);
            }
            System.gc();
        });
    }

    @Override
    public void onImage(File image) {
        logger.trace("onImage");
        ClipboardService.repository().saveClipboard(null, image.getAbsolutePath(), ClipboardType.IMAGE).onComplete(saveClipboardHandler -> {

            if (saveClipboardHandler.failed()) {
                logger.error("Failed to save clipboard, Image: {}", image, saveClipboardHandler.cause());
            } else {
                logger.trace("Successfully save clipboard, Image: {}", image);
                MainFrame.update(false);
            }

            System.gc();
        });
    }

    @Override
    public void onFile(List<File> files) {
        logger.trace("onFile Clipboard: {}", files);
        String data = JjsonArray.ofCollection(files.stream().map(File::getAbsolutePath).toList()).encode();
        ClipboardService.repository().saveClipboard(null, data, ClipboardType.FILE).onComplete(saveClipboardHandler -> {

            if (saveClipboardHandler.failed()) {
                logger.error("Failed to save clipboard, Files: {} , Data: {}", files, data, saveClipboardHandler.cause());
            } else {
                logger.trace("Successfully save clipboard, Files: {} , Data: {}", files, data);
                MainFrame.update(false);
            }

            System.gc();
        });
    }

    @Override
    public Future<Void> onData(ClipboardType clipboardType, Object data) {
        Promise<Void> promise = Promise.promise();

        removeLastClipboard().onSuccess(isRemoveLastClipboard -> {
            logger.trace("Successfully remove last clipboard, ClipboardType: {} , Data: {}", clipboardType, data);
            promise.complete();
        }).onFailure(failedRemoveLastClipboard -> {
            logger.error("Failed to remove last clipboard, ClipboardType: {} , Data: {}", clipboardType, data, failedRemoveLastClipboard);
            promise.fail(failedRemoveLastClipboard);
        });

        return promise.future();
    }

    private Future<Boolean> removeLastClipboard() {

        Promise<Boolean> promise = Promise.promise();

        int clipboardSaveCount = ClipboardManagerApplication.getConfig().clipboardSaveCount();

        if (clipboardSaveCount < 0) {
            promise.complete(false);
            return promise.future();
        }

        ClipboardService.repository().fetchTotalClipboards().onSuccess(total -> {

            logger.trace("Successfully fetch total clipboard, Total: {}", total);

            if (total <= clipboardSaveCount) {
                promise.complete(false);
                return;
            }

            int removeLimit = total - clipboardSaveCount;

            ClipboardService.repository().deleteClipboard(removeLimit + 1).onSuccess(successDelete -> {
                logger.trace("Successfully delete clipboard, RemoveLimit: {}", removeLimit);
                promise.complete(true);
            }).onFailure(failedDeleteClipboard -> {
                logger.error("Failed to delete clipboard, ClipboardSaveCount: {}", clipboardSaveCount, failedDeleteClipboard);
                promise.fail(failedDeleteClipboard);
            });

        }).onFailure(failedFetchTotalClipboard -> {
            logger.error("Failed to fetch total clipboard, ClipboardSaveCount: {}", clipboardSaveCount, failedFetchTotalClipboard);
            promise.fail(failedFetchTotalClipboard);
        });

        return promise.future();
    }

}
