package com.android.auroramusic.util.tagUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.android.auroramusic.util.LogUtil;

public class FrameUtil {

	public static Frame CalcFrame(String fileName, int fileSize) throws Exception {
		Frame frame = new Frame();
		frame.fileSize = fileSize;
		File mFile = new File(fileName);

		RandomAccessFile fis = new RandomAccessFile(mFile, "r");
		byte[] header = new byte[10];
		fis.read(header);
		String head = new String(header, 0, 3);
		int ID3V2_frame_size = 0;
		if (head.equals("ID3")) {

			ID3V2_frame_size = (int) (header[6] & 0x7F) * 0x200000 | (int) (header[7] & 0x7F) * 0x400 | (int) (header[8] & 0x7F) * 0x80 | (int) (header[9] & 0x7F) + 10;
			if ((header[5] & 0x40) == 64) {
				fis.read(header);
			}
			int seek = 0;
			int isEnd = 0;
			do {
				isEnd = fis.read(header);
				seek = (int) (header[4] & 0xFF) * 0x1000000 | (int) (header[5] & 0xFF) * 0x10000 | (int) (header[6] & 0xFF) * 0x100 | (int) (header[7] & 0xFF);
				if ((((header[0] & 0xff) == 255) && ((header[1] & 0xe0) == 224))) {
					break;
				}
				if (new String(header, 0, 4).equals("APIC") && (seek > 14)) {
					int counts = 0;

					byte[] he = new byte[2];
					he[0] = (byte) 0xFF;
					he[1] = (byte) 0xD8;

					byte[] buf = new byte[1];
					int i;
					for (i = 0; i < seek; i++) {
						fis.read(buf);
						if (buf[0] == he[0]) {
							counts = 1;
						} else if (buf[0] == he[1]) {
							if (counts == 1) {
								counts = 2;
								break;
							} else {
								counts = 0;
							}
						} else {
							counts = 0;
						}
					}

					FileOutputStream fos = new FileOutputStream(fileName + ".jpg");

					fos.write(he);
					int skip = seek - i;
					byte[] buffer = new byte[81920];

					int count = skip / buffer.length;
					int end = skip % buffer.length;
					for (int j = 0; j < count; j++) {
						fis.read(buffer);
						fos.write(buffer);
					}
					fis.read(buffer, 0, end);
					fos.write(buffer, 0, end);
					fos.close();
				} else {
					fis.skipBytes(seek);
				}

				Thread.sleep(200);
			} while (seek > 0 && isEnd != -1);
			long position = fis.getFilePointer();
			if (isEnd != -1 && seek >= 0) {
				ID3V2_frame_size = (int) position - 10;
			}

			fis.seek(ID3V2_frame_size);

			System.out.println(ID3V2_frame_size);
		}
		fis.seek(ID3V2_frame_size);

		byte[] FrameHeader = new byte[4];
		// 找到同部位
		do {
			ID3V2_frame_size = FindSyn(fis, ID3V2_frame_size);
			System.out.println(ID3V2_frame_size);
			fis.seek(ID3V2_frame_size);
			fis.read(FrameHeader);
			CheckMP3Info(FrameHeader, frame);
			if (frame.simplingRate == 0 && frame.bitrate == 0) {
				ID3V2_frame_size++;
			} else {
				frame.CalcFrameSize();
				fis.skipBytes(frame.frameSize - 4);
				byte[] tmp = new byte[2];
				fis.read(tmp);
				if (FrameHeader[0] == tmp[0] && FrameHeader[1] == tmp[1]) {
					fis.seek(4);
					break;
				} else {
					ID3V2_frame_size++;
					fis.seek(ID3V2_frame_size);
				}
			}
		} while (ID3V2_frame_size < frame.fileSize);

		// 边信息
		byte[] vbri = new byte[frame.sideInfo];
		fis.read(vbri);
		String vbrHead = new String(vbri, 0, 4);
		if (vbrHead.toLowerCase().equals("vbri")) {
			frame.frameCount = (int) (vbri[14] & 0xFF) * 0x1000000 | (int) (vbri[15] & 0xFF) * 0x10000 | (int) (vbri[16] & 0xFF) * 0x100 | (int) (vbri[17] & 0xFF);
		}
		byte[] vbr = new byte[4];
		fis.read(vbr);
		vbrHead = new String(vbr);
		if (vbrHead.toLowerCase().equals("xing") || vbrHead.toLowerCase().equals("info")) {
			byte[] isFrameSize = new byte[4];
			byte[] frameSizeArray = new byte[4];
			fis.read(isFrameSize);
			fis.read(frameSizeArray);// 读取文件帧
			frame.frameCount = (int) (frameSizeArray[0] & 0xFF) * 0x1000000 | (int) (frameSizeArray[1] & 0xFF) * 0x10000 | (int) (frameSizeArray[2] & 0xFF) * 0x100 | (int) (frameSizeArray[3] & 0xFF);
			LogUtil.d("chenhl",frameSizeArray[0] + " " + frameSizeArray[1] + " " + frameSizeArray[2] + " " + frameSizeArray[3]);
			frame.CalcFrameSize();
		} else {
			LogUtil.d("chenhl","没有INFO");

		}

		fis.close();

		double secs;

		if (frame.frameCount != 0) {
			secs = frame.frameCount * frame.simpCount / frame.simplingRate;
		} else {
			//secs = (frame.fileSize - ID3V2_frame_size - 128) / frame.frameSize * 0.026;
			secs=((double)(frame.fileSize*8 - ID3V2_frame_size))/(frame.bitrate*1000);
		}
		frame.playTime = secs;
		LogUtil.d("chenhl",(int) (secs / 3600) + " " + (int) (secs % 3600 / 60) + " " + (int) (secs % 3600 % 60));
		return frame;
	}

	private static int FindSyn(RandomAccessFile fis, int ID3V2_frame_size) {
		byte[] spec = new byte[2];
		do {
			try {
				fis.seek(ID3V2_frame_size);
				fis.read(spec);
			} catch (IOException e) {
				e.printStackTrace();
			}
			ID3V2_frame_size++;
		} while (!(((spec[0] & 0xff) == 255) && ((spec[1] & 0xe0) == 224)));
		ID3V2_frame_size--;
		return ID3V2_frame_size;
	}

	private static void CheckMP3Info(byte[] FrameHeader, Frame frame) {
		// MP3版本
		CheckVersion(FrameHeader[1], frame);
		// 层级
		CheckLayer(FrameHeader[1], frame);
		// 是否保护
		frame.protect = FrameHeader[1] & 0x1;
		// 比特率索引
		CheckBitrate(FrameHeader[2], frame);
		// 获取采样率
		CheckSimplingRate(FrameHeader[2], frame);

		frame.paddingBits = ((FrameHeader[2] & 0x2) >>> 1);

		frame.channel = ((FrameHeader[3] & 0xc0) >>> 6) < 3 ? 1 : 0;
		CheckSimpCount(frame);
		// 采样数
		CheckSideInfo(frame);

	}

	private static void CheckSideInfo(Frame frame) {
		int i = 0;
		int j = frame.channel;
		switch (frame.version) {
		case 1:
			i = 0;
			break;
		case 2:
		case 3:
			i = 1;
			break;
		}
		frame.sideInfo = Frame.sideInfoArray[j][i];
	}

	private static void CheckSimpCount(Frame frame) {
		int i = 0;
		int j = frame.layer - 1;
		if (j < 0) {
			j = 0;
		}
		switch (frame.version) {
		case 1:
			i = 0;
			break;
		case 2:
			i = 1;
			break;
		case 3:
			i = 2;
			break;
		}
		frame.simpCount = Frame.simpcountArray[j][i];
	}

	private static void CheckSimplingRate(byte FrameHeader, Frame frame) {
		int i = 0;
		int j = ((FrameHeader & 0xc) >>> 2);

		switch (frame.version) {
		case 1:
			i = 0;
			break;
		case 2:
			i = 1;
			break;
		case 3:
			i = 2;
			break;
		}
		frame.simplingRate = Frame.simpArray[j][i];
	}

	private static void CheckBitrate(byte FrameHeader, Frame frame) {
		int j = ((FrameHeader & 0xf0) >>> 4);
		int i = 0;
		switch (frame.version) {
		case 1:
			switch (frame.layer) {
			case 1:
				i = 0;
				break;
			case 2:
				i = 1;
				break;
			case 3:
				i = 2;
				break;
			}
			break;
		case 2:
		case 3:
			switch (frame.layer) {
			case 1:
				i = 3;
				break;
			case 2:
				i = 4;
				break;
			case 3:
				i = 5;
				break;
			}
			break;
		}
		frame.bitrate = Frame.bitrateArray[j][i];
	}

	/**
	 * 检查MP3第几层
	 * 
	 * @param b
	 * @param frame
	 */
	private static void CheckLayer(byte FrameHeader, Frame frame) {
		switch (((FrameHeader & 0x6) >>> 1)) {
		case 1:
			frame.layer = 3;
			break;
		case 2:
			frame.layer = 2;
			break;
		case 3:
			frame.layer = 1;
			break;
		case 0:
			frame.layer = 0;
			break;
		}
	}

	/**
	 * 检查MP3压缩版本
	 * 
	 * @param FrameHeader
	 * @param frame
	 */
	private static void CheckVersion(byte FrameHeader, Frame frame) {
		switch ((FrameHeader & 0x18) >>> 3) {
		case 3:
			frame.version = 1;
			break;
		case 2:
			frame.version = 2;
			break;
		case 1:
			frame.version = 0;
			break;
		case 0:
			frame.version = 3;
			break;
		}
	}
}
