/* //device/samples/SampleCode/src/com/android/samples/app/RemoteServiceInterface.java
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.android.music;

import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.downloadex.DownloadInfo;

interface IMediaPlaybackService
{
 	DownloadInfo queryDownloadSong(String title, String artist);
    void openFile(String path);
    void open(in long [] list, int position);
    void shuffleOpen(in long [] list, int position);
    int getQueuePosition();
    boolean isPlaying();
    void stop();
    void pause();
    void play();
    void playListView(int position);
    void prev();
    void next();
    long duration();
    long position();
    long seek(long pos);
    String getFilePath();
    String getTrackName();
    String getAlbumName();
    long getAlbumId();
    String getArtistName();
    long getArtistId();
    void enqueue(in long [] list, int action);
    long [] getQueue();
    void moveQueueItem(int from, int to);
    void setQueuePosition(int index);
    String getPath();
    long getAudioId();
    void setShuffleMode(int shufflemode);
    int getShuffleMode();
    int removeTracks(int first, int last);
    int removeTrack(long id);
    void setRepeatMode(int repeatmode);
    int getRepeatMode();
    int getMediaMountedCount();
    int getAudioSessionId();
    boolean isFavorite(long id);
    void addToFavorite(long id);
    void removeFromFavorite(long id);
    void toggleMyFavorite();
    void onHifiChanged(int on);

    String getLrcUri();
    boolean isOnlineSong();
    long secondaryPosition();
    String getMusicbitrate();
    void notifyLrcPath(String path);
    void setListInfo(in List<AuroraListItem> list);
    List<AuroraListItem> getListInfo();
    void online_startFile(String path);
    void online_start(in long [] list, int position);
    void online_playListView(int position);
    void updateCursor();
    void updateNotification();
    String getLryFile();
    boolean getRadioType();
}

