package com.github.laim0nas100.commonslb.threads;

import java.util.concurrent.RunnableFuture;

/**
 *
 * @author laim0nas100
 */
public interface FailableRunnableFuture<T> extends RunnableFuture<T> {

    public void setException(Throwable t);
}
