package com.pinery.audioedit.util.encode;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class AACAudioEncoder extends AudioEncoder {

	private final static String TAG = "AACAudioEncoder";

	private final static String AUDIO_MIME = "audio/mp4a-latm";
	private final static long audioBytesPerSample = 44100 * 16 / 8;

	AACAudioEncoder(String rawAudioFile) {
		super(rawAudioFile);
	}


	@Override
	public void encodeToFile(String outEncodeFile) {
		FileInputStream fisRawAudio = null;
		FileOutputStream fosAccAudio = null;
		try {
			fisRawAudio = new FileInputStream(rawAudioFile);
			fosAccAudio = new FileOutputStream(outEncodeFile);

			final MediaCodec audioEncoder = createACCAudioDecoder();
			audioEncoder.start();

			ByteBuffer[] audioInputBuffers = audioEncoder.getInputBuffers();
			ByteBuffer[] audioOutputBuffers = audioEncoder.getOutputBuffers();
			boolean sawInputEOS = false;
			boolean sawOutputEOS = false;
			long audioTimeUs = 0;
			BufferInfo outBufferInfo = new BufferInfo();

			boolean readRawAudioEOS = false;
			byte[] rawInputBytes = new byte[4096];
			int readRawAudioCount = 0;
			int rawAudioSize = 0;
			long lastAudioPresentationTimeUs = 0;

			int inputBufIndex, outputBufIndex;
			while (!sawOutputEOS) {
				if (!sawInputEOS) {
					inputBufIndex = audioEncoder.dequeueInputBuffer(10000);
					if (inputBufIndex >= 0) {
						ByteBuffer inputBuffer = audioInputBuffers[inputBufIndex];
						inputBuffer.clear();

						int bufferSize = inputBuffer.remaining();
						if (bufferSize != rawInputBytes.length) {
							rawInputBytes = new byte[bufferSize];
						}

						if (!readRawAudioEOS) {
							readRawAudioCount = fisRawAudio.read(rawInputBytes);
							if (readRawAudioCount == -1) {
								readRawAudioEOS = true;
							}
						}

						if (readRawAudioEOS) {
							audioEncoder.queueInputBuffer(inputBufIndex, 0, 0,0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
							sawInputEOS = true;
						} else {
							inputBuffer.put(rawInputBytes, 0, readRawAudioCount);
							rawAudioSize += readRawAudioCount;
							audioEncoder.queueInputBuffer(inputBufIndex, 0,readRawAudioCount, audioTimeUs, 0);
							audioTimeUs = (long) (1000000 * (rawAudioSize / 2.0) / audioBytesPerSample);
						}
					}
				}

				outputBufIndex = audioEncoder.dequeueOutputBuffer(outBufferInfo, 10000);
				if (outputBufIndex >= 0) {

					// Simply ignore codec config buffers.
					if ((outBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
						audioEncoder.releaseOutputBuffer(outputBufIndex, false);
						continue;
					}

					if (outBufferInfo.size != 0) {
						ByteBuffer outBuffer = audioOutputBuffers[outputBufIndex];
						outBuffer.position(outBufferInfo.offset);
						outBuffer.limit(outBufferInfo.offset+ outBufferInfo.size);
						if (lastAudioPresentationTimeUs < outBufferInfo.presentationTimeUs) {
							lastAudioPresentationTimeUs = outBufferInfo.presentationTimeUs;
							int outBufSize = outBufferInfo.size;
							int outPacketSize = outBufSize + 7;

							outBuffer.position(outBufferInfo.offset);
							outBuffer.limit(outBufferInfo.offset + outBufSize);

							byte[] outData = new byte[outBufSize + 7];
							addADTStoPacket(outData, outPacketSize);
							outBuffer.get(outData, 7, outBufSize);

							fosAccAudio.write(outData, 0, outData.length);
						}  
					}

					audioEncoder.releaseOutputBuffer(outputBufIndex, false);

					if ((outBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						sawOutputEOS = true;
					}
				} else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					audioOutputBuffers = audioEncoder.getOutputBuffers();
				} else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					MediaFormat audioFormat = audioEncoder.getOutputFormat();
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (fisRawAudio != null)
					fisRawAudio.close();
				if (fosAccAudio != null)
					fosAccAudio.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private MediaCodec createACCAudioDecoder() throws IOException {
		MediaCodec codec = MediaCodec.createEncoderByType(AUDIO_MIME);
		MediaFormat format = new MediaFormat();
		format.setString(MediaFormat.KEY_MIME, AUDIO_MIME);
		format.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
		format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
		format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
		format.setInteger(MediaFormat.KEY_AAC_PROFILE,
				MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		return codec;
	}

	/**
	 * Add ADTS header at the beginning of each and every AAC packet. This is
	 * needed as MediaCodec encoder generates a packet of raw AAC data.
	 * 
	 * Note the packetLen must count in the ADTS header itself.
	 **/
	private void addADTStoPacket(byte[] packet, int packetLen) {
		int profile = 2; // AAC LC 
		int freqIdx = 4; // 44.1KHz
		int chanCfg = 2; // Channel Configurations

		// fill in ADTS data
		packet[0] = (byte) 0xFF;
		packet[1] = (byte) 0xF9;
		packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
		packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
		packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
		packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
		packet[6] = (byte) 0xFC;
	}
}
