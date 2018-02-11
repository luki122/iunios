package  com.aurora.mediascanner;

import com.aurora.mediascanner.IAuroraMediaScannerListener;
interface IAuroraMediaScannerService
{
    /**
     * Requests the media scanner to scan a file.
     * @param path the path to the file to be scanned.
     * @param mimeType  an optional mimeType for the file.
     * If mimeType is null, then the mimeType will be inferred from the file extension.
     * @param listener an optional IMediaScannerListener. 
     * If specified, the caller will be notified when scanning is complete via the listener.
     */
    void auroraRequestScanFile(String path, String mimeType, in IAuroraMediaScannerListener listener);

    /**
     * Older API, left in for backward compatibility.
     * Requests the media scanner to scan a file.
     * @param path the path to the file to be scanned.
     * @param mimeType  an optional mimeType for the file.
     * If mimeType is null, then the mimeType will be inferred from the file extension.
     */
    void auroraScanFile(String path, String mimeType);
}