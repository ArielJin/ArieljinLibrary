package com.arieljin.library.update;

import com.arieljin.library.manager.PermissionManager;

public interface OnAppCheckUploadListener {

    boolean needUpdate(float serVersionCode, float currentVersionCode);
    void updateErroe(boolean isBeforeAgree, String error);
    void checkSelfPermission(PermissionManager.OnPermissionCheckedCallback onPermissionCheckedCallback) throws Exception;
}
