package com.lexu.kia.Managers;

import android.support.annotation.Nullable;

public interface DatabaseCallback {
    void onComplete(ResponseData response);

    void onFailure(ResponseData response);

    enum ResponseStatus {
        SUCCESS, FAILURE
    }

    final class ResponseData<T> {
        private ResponseStatus mStatus = null;
        private T mData = null;
        private String mMessage = null;

        ResponseData() {
        }

        public ResponseStatus getStatus() {
            return mStatus;
        }

        void setStatus(ResponseStatus status) {
            mStatus = status;
        }

        public T getData() {
            return mData;
        }

        void setData(T data) {
            mData = data;
        }

        public String getMessage() {
            return mMessage;
        }

        void setMessage(String message) {
            mMessage = message;
        }

        final static class Builder<T> {
            private ResponseData<T> responseData = null;

            Builder() {
            }

            final Builder<T> status(ResponseStatus s) {
                this.responseData.setStatus(s);
                return this;
            }

            final Builder<T> data(@Nullable T d) {
                this.responseData.setData(d);
                return this;
            }

            final Builder<T> message(String m) {
                this.responseData.setMessage(m);
                return this;
            }

            final ResponseData build() {
                return this.responseData;
            }
        }
    }
}
