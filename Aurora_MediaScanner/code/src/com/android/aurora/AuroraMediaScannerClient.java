package com.android.aurora;

/**
 * 数据传递接口
 * @author root (modify by hujw)
 *
 */
public interface AuroraMediaScannerClient {
	
    /**
     * Called by java code to return scan media files.
     */
	
    public void auroraScanFile(String path, long lastModified, long fileSize,
            boolean isDirectory, boolean noMedia);

    /**
     * Called by native code to return metadata extracted from media files.
     */
    public void auroraHandleStringTag(String name, String value);

    /**
     * Called by native code to return mime type extracted from DRM content.
     */
    public void auroraSetMimeType(String mimeType);
}
