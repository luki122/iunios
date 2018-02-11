package com.android.auroramusic.util.tagUtils;

public class Frame {
	public int version;// MPEG版本
	public int layer;// 层级
	public int protect;// 是否受保护
	public int bitrate;// 比特率
	public int simplingRate;// 采样率
	public int paddingBits;// 填充位数
	public int channel;// 声道模式 单声道还是立体声

	public static final int[][] bitrateArray = new int[][] { { 0, 0, 0, 0, 0, 0 }, { 32, 32, 32, 32, 32, 8 }, { 64, 48, 40, 64, 48, 16 }, { 96, 56, 48, 96, 56, 24 }, { 128, 64, 56, 128, 64, 32 }, { 160, 80, 64, 160, 80, 64 }, { 192, 96, 80, 192, 96, 80 }, { 224, 112, 96, 224, 112, 56 }, { 256, 128, 112, 256, 128, 64 }, { 288, 160, 128, 288, 160, 128 }, { 320, 192, 160, 320, 192, 160 }, { 352, 224, 192, 352, 224, 112 }, { 384, 256, 224, 384, 256, 128 }, { 416, 384, 320, 416, 320, 256 }, { 448, 384, 320, 448, 384, 320 }, { 0, 0, 0, 0, 0, 0 } };

	public static int[][] sideInfoArray = new int[][] { { 17, 9 }, { 32, 17 } };

	public static int[][] simpArray = new int[][] { { 44100, 22050, 11025 }, { 48000, 24000, 12000 }, { 32000, 16000, 8000 }, { 0, 0, 0 } };
	public static int[][] simpcountArray = new int[][] { 
		{ 384, 384, 384 },
		{ 1152, 1152, 1152 }, 
		{ 1152, 576, 576 } };
	public int sideInfo;// 边信息
	public int simpCount;//
	public int frameSize;// 如果是CBR的话用这个
	public int frameCount;// 如果是VBR的话用这个(总帧数)
	public double playTime;
	public int fileSize;// 文件大小

	public int CalcFrameSize() {
		// 计算帧长度的公式
		if (frameCount != 0) {
			this.frameSize = this.fileSize / this.frameCount;
		} else {
			this.frameSize = (((this.version == 1 ? 144 : 72) * 1000 * this.bitrate) / this.simplingRate) + this.paddingBits;
		}
		return this.frameSize;
	}

	public double getDuration(int bufferSize) {

		double duration;
		if (frameCount != 0) {
			duration = bufferSize / frameSize * simpCount / simplingRate;
		} else {
			//duration = bufferSize / frameSize * 0.026;
			duration = (double)bufferSize*8 /(bitrate*1000);
		}
		return duration;
	}

}
