package com.grieex.model;

import com.grieex.helper.Constants;

public class TransactionObject {

    private Constants.TransactionTypes transactionType = Constants.TransactionTypes.INSERT;

    public Constants.TransactionTypes getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Constants.TransactionTypes transactionType) {
        this.transactionType = transactionType;
    }

}
