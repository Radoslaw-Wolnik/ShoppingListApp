package com.example.shoppinglistapp.data.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.shoppinglistapp.MyApp;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SyncManager syncManager = MyApp.getSyncManager();
        if (syncManager != null) {
            return syncManager.syncBlocking() ? Result.success() : Result.retry();
        }
        return Result.failure();
    }
}
