package com.host.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.glassfish.jersey.spi.ScheduledExecutorServiceProvider;

import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.host.App;
import com.host.api.Console;

public class Scheduler {
    private final ScheduledExecutorServiceProvider provider = new ScheduledExecutorServiceProvider() {
        @Override
        public ScheduledExecutorService getExecutorService() {
            return Executors.newSingleThreadScheduledExecutor();
        }

        @Override
        public void dispose(ExecutorService executorService) {
            executorService.shutdown();
        }

    };

    public ScheduledExecutorService getScheduledExecutorService() {
        return provider.getExecutorService();
    }

    public void sendOutput(String containerId) {
        LogContainerCmd logContainerCmd = App.getClient().logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true);

        App.getClient().logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        // Handle each log frame (stdout or stderr)
                        String logEntry = item.toString();
                        Console console = new Console();
                        console.sendMessage(logEntry);
                    }
                });
    }

    public void stopLogMonitoring() {
        provider.dispose(provider.getExecutorService());
    }

}
