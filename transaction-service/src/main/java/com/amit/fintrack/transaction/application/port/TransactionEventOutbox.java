package com.amit.fintrack.transaction.application.port;

import com.amit.fintrack.transaction.application.model.TransactionLifecycleEvent;

public interface TransactionEventOutbox {

    void save(TransactionLifecycleEvent event);
}
