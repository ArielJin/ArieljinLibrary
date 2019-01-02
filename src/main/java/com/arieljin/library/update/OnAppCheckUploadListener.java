package com.arieljin.library.update;

import com.arieljin.library.manager.PermissionManager;

public interface OnAppCheckUploadListener {

    boolean needUpdate(boolean needUpldate);
    void updateErroe(boolean isBeforeAgree, String error);
    void checkSelfPermission(PermissionManager.OnPermissionCheckedCallback onPermissionCheckedCallback) throws Exception;
}
