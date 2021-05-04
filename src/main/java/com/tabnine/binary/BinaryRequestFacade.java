package com.tabnine.binary;

import com.intellij.openapi.diagnostic.Logger;
import com.tabnine.binary.exceptions.TabNineDeadException;
import io.sentry.Sentry;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.intellij.util.concurrency.AppExecutorUtil.getAppExecutorService;
import static com.tabnine.general.StaticConfig.COMPLETION_TIME_THRESHOLD;

public class BinaryRequestFacade {
    private final BinaryProcessRequesterProvider binaryProcessRequesterProvider;

    public BinaryRequestFacade(BinaryProcessRequesterProvider binaryProcessRequesterProvider) {
        this.binaryProcessRequesterProvider = binaryProcessRequesterProvider;
    }

    public <R extends BinaryResponse> R executeRequest(BinaryRequest<R> req) {
        return executeRequest(req, COMPLETION_TIME_THRESHOLD);
    }

    @Nullable
    public <R extends BinaryResponse> R executeRequest(BinaryRequest<R> req, int timeoutMillis) {
        BinaryProcessRequester binaryProcessRequester = binaryProcessRequesterProvider.get();

        try {
            R result = getAppExecutorService().submit(() -> binaryProcessRequester.request(req))
                    .get(timeoutMillis, TimeUnit.MILLISECONDS);

            if(result != null) {
                binaryProcessRequesterProvider.onSuccessfulRequest();
            }

            return result;
        } catch (TimeoutException e) {
            binaryProcessRequesterProvider.onTimeout();
            Sentry.captureException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TabNineDeadException) {
                binaryProcessRequesterProvider.onDead(e.getCause());
            }

            String message = "Tabnine's threw an unknown error during request.";
            Logger.getInstance(getClass()).warn(message, e);
            Sentry.captureException(e, message);
        } catch (CancellationException e) {
            // This is ok. Nothing needs to be done.
        } catch (Exception e) {
            String message = "Tabnine's threw an unknown error.";
            Logger.getInstance(getClass()).warn(message, e);
            Sentry.captureException(e, message);
        }

        return null;
    }
}
