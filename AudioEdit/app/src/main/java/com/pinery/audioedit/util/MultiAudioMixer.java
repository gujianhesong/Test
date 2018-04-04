package com.pinery.audioedit.util;

import android.util.Log;

import com.pinery.audioedit.bean.ComposeInfo;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class MultiAudioMixer {
	
	private OnAudioMixListener mOnAudioMixListener;

	/**
	 * 创建默认的合成器
	 * @return
     */
	public static MultiAudioMixer createDefaultAudioMixer(){
		return createAddAudioMixer();
	}

	/**
	 * 创建叠加合成器
	 * @return
	 */
	public static MultiAudioMixer createAddAudioMixer(){
		return new AddAudioMixer();
	}

	/**
	 * 创建平均值合成器
	 * @return
     */
	public static MultiAudioMixer createAverageAudioMixer(){
		return new AverageAudioMixer();
	}

	/**
	 * 创建权值合成器
	 * @param weights
	 * @return
     */
	public static MultiAudioMixer createWeightAudioMixer(float[] weights){
		return new WeightAudioMixer(weights);
	}

	/**
	 * 设置合成监听
	 * @param l
     */
	public void setOnAudioMixListener(OnAudioMixListener l){
		this.mOnAudioMixListener = l;
	}
	

	/**
	 * 合成音频
	 *
	 * @param rawAudioFiles 合成音频的列表
     */
	public void mixAudios(String[] rawAudioFiles){
		
		final int fileSize = rawAudioFiles.length;

		FileInputStream[] audioFileStreams = new FileInputStream[fileSize];

		FileInputStream inputStream;
		byte[][] allAudioBytes = new byte[fileSize][];
		boolean[] streamDoneArray = new boolean[fileSize];
		final int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int offset;
		
		try {
			
			for (int fileIndex = 0; fileIndex < fileSize; ++fileIndex) {
				audioFileStreams[fileIndex] = new FileInputStream(rawAudioFiles[fileIndex]);
			}

			while(true){
				
				for(int streamIndex = 0 ; streamIndex < fileSize ; ++streamIndex){
					
					inputStream = audioFileStreams[streamIndex];
					if(!streamDoneArray[streamIndex] && (offset = inputStream.read(buffer)) != -1){
						allAudioBytes[streamIndex] = Arrays.copyOf(buffer,buffer.length);
					}else{
						streamDoneArray[streamIndex] = true;
						allAudioBytes[streamIndex] = new byte[bufferSize];
					}
				}

				byte[] mixBytes = mixRawAudioBytes(allAudioBytes);
				if(mixBytes != null && mOnAudioMixListener != null){
					mOnAudioMixListener.onMixing(mixBytes);
				}
				
				boolean done = true;
				for(boolean streamEnd : streamDoneArray){
					if(!streamEnd){
						done = false;
					}
				}
				
				if(done){
					if(mOnAudioMixListener != null)
						mOnAudioMixListener.onMixComplete();
					break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			if(mOnAudioMixListener != null)
				mOnAudioMixListener.onMixError(1);
		}finally{
			try {
				for(FileInputStream in : audioFileStreams){
					if(in != null)
						in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 合成音频, 可设置音频开始播放时间
	 *
	 * @param infoList 待合成的音频信息列表
     */
	public void mixAudios(List<ComposeInfo> infoList){

		if(infoList == null || infoList.size() <= 0) return;

		final int fileSize = infoList.size();

		FileInputStream[] audioFileStreams = new FileInputStream[fileSize];

		FileInputStream inputStream;
		byte[][] allAudioBytes = new byte[fileSize][];
		boolean[] streamDoneArray = new boolean[fileSize];
		final int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int offset;

		int[] audioOffset = new int[fileSize];
		for(int i=0; i<fileSize; i++){
			audioOffset[i] = (int) (infoList.get(i).offsetSeconds * 16/8 * 2 * 44100);
		}

		try {

			for (int fileIndex = 0; fileIndex < fileSize; ++fileIndex) {
				audioFileStreams[fileIndex] = new FileInputStream(infoList.get(fileIndex).pcmPath);
			}

			while(true){

				for(int streamIndex = 0 ; streamIndex < fileSize ; ++streamIndex){

					inputStream = audioFileStreams[streamIndex];

					//处理填充offset空白数据
					int curOffset = audioOffset[streamIndex];
					if(curOffset >= bufferSize){

						//填充空白数据
						allAudioBytes[streamIndex] = new byte[bufferSize];

						audioOffset[streamIndex] = curOffset - bufferSize;

						continue;

					}else if(curOffset > 0 && curOffset < bufferSize){

						//填充空白数据和读取的音频数据
						byte[] data = new byte[bufferSize];

						byte[] dataChild = new byte[bufferSize - curOffset];
						inputStream.read(dataChild);

						System.arraycopy(dataChild, 0, data, curOffset, dataChild.length);

						allAudioBytes[streamIndex] = data;

						audioOffset[streamIndex] = 0;

						continue;

					}

					//处理文件流数据
					if(!streamDoneArray[streamIndex] && (offset = inputStream.read(buffer)) != -1){
						//填充音频数据
						allAudioBytes[streamIndex] = Arrays.copyOf(buffer,buffer.length);
					}else{
						//填充空白数据
						streamDoneArray[streamIndex] = true;
						allAudioBytes[streamIndex] = new byte[bufferSize];
					}
				}

				//合成数据
				byte[] mixBytes = mixRawAudioBytes(allAudioBytes);

				if(mixBytes != null && mOnAudioMixListener != null){
					mOnAudioMixListener.onMixing(mixBytes);
				}

				boolean done = true;
				for(boolean streamEnd : streamDoneArray){
					if(!streamEnd){
						done = false;
					}
				}

				if(done){
					//全部合成完成
					if(mOnAudioMixListener != null)
						mOnAudioMixListener.onMixComplete();
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if(mOnAudioMixListener != null){
				mOnAudioMixListener.onMixError(1);
			}

		}finally{
			try {
				for(FileInputStream in : audioFileStreams){
					if(in != null)
						in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 合成音频数据
	 * @param data
	 * @return
     */
	public abstract byte[] mixRawAudioBytes(byte[][] data);

	public interface OnAudioMixListener{
		/**
		 * 合成进行
		 * @param mixBytes
		 * @throws IOException
         */
		void onMixing(byte[] mixBytes) throws IOException;

		/**
		 * 合成错误
		 * @param errorCode
         */
		void onMixError(int errorCode);
		
		/**
		 * 合成完成
		 */
		void onMixComplete();
	}
	
	/**
	 * 叠加合成器
	 * @author Darcy
	 */
	private static class AddAudioMixer extends MultiAudioMixer{

		@Override
		public byte[] mixRawAudioBytes(byte[][] bMulRoadAudioes) {
			
			if (bMulRoadAudioes == null || bMulRoadAudioes.length == 0)
				return null;

			byte[] realMixAudio = bMulRoadAudioes[0];
			
			if(bMulRoadAudioes.length == 1)
				return realMixAudio;
			
			for(int rw = 0 ; rw < bMulRoadAudioes.length ; ++rw){
				if(bMulRoadAudioes[rw].length != realMixAudio.length){
					Log.e("app", "column of the road of audio + " + rw +" is diffrent.");
					return null;
				}
			}

			//row 代表参与合成的音频数量
			//column 代表一段音频的采样点数，这里所有参与合成的音频的采样点数都是相同的
			int row = bMulRoadAudioes.length;
			int coloum = realMixAudio.length / 2;
			short[][] sMulRoadAudioes = new short[row][coloum];

			//PCM音频16位的存储是大端存储方式，即低位在前，高位在后，例如(X1Y1, X2Y2, X3Y3)数据，它代表的采样点数值就是(（Y1 * 256 + X1）, （Y2 * 256 + X2）, （Y3 * 256 + X3）)
			for (int r = 0; r < row; ++r) {
				for (int c = 0; c < coloum; ++c) {
					sMulRoadAudioes[r][c] = (short) ((bMulRoadAudioes[r][c * 2] & 0xff) | (bMulRoadAudioes[r][c * 2 + 1] & 0xff) << 8);
				}
			}

			short[] sMixAudio = new short[coloum];
			int mixVal;
			int sr = 0;
			for (int sc = 0; sc < coloum; ++sc) {
				mixVal = 0;
				sr = 0;
				//这里采取累加法
				for (; sr < row; ++sr) {
					mixVal += sMulRoadAudioes[sr][sc];
				}
				//最终值不能大于short最大值，因此可能出现溢出
				sMixAudio[sc] = (short) (mixVal);
			}

			//short值转为大端存储的双字节序列
			for (sr = 0; sr < coloum; ++sr) {
				realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
				realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
			}

			return realMixAudio;
		}
		
	}

	/**
	 * 求平均值合成器
	 * @author Darcy
	 */
	private static class AverageAudioMixer extends MultiAudioMixer{

		@Override
		public byte[] mixRawAudioBytes(byte[][] bMulRoadAudioes) {

			if (bMulRoadAudioes == null || bMulRoadAudioes.length == 0)
				return null;

			byte[] realMixAudio = bMulRoadAudioes[0];

			if(bMulRoadAudioes.length == 1)
				return realMixAudio;

			for(int rw = 0 ; rw < bMulRoadAudioes.length ; ++rw){
				if(bMulRoadAudioes[rw].length != realMixAudio.length){
					Log.e("app", "column of the road of audio + " + rw +" is diffrent.");
					return null;
				}
			}

			int row = bMulRoadAudioes.length;
			int coloum = realMixAudio.length / 2;
			short[][] sMulRoadAudioes = new short[row][coloum];

			for (int r = 0; r < row; ++r) {
				for (int c = 0; c < coloum; ++c) {
					sMulRoadAudioes[r][c] = (short) ((bMulRoadAudioes[r][c * 2] & 0xff) | (bMulRoadAudioes[r][c * 2 + 1] & 0xff) << 8);
				}
			}

			short[] sMixAudio = new short[coloum];
			int mixVal;
			int sr = 0;
			for (int sc = 0; sc < coloum; ++sc) {
				mixVal = 0;
				sr = 0;
				for (; sr < row; ++sr) {
					mixVal += sMulRoadAudioes[sr][sc];
				}
				sMixAudio[sc] = (short) (mixVal / row);
			}

			for (sr = 0; sr < coloum; ++sr) {
				realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
				realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
			}

			return realMixAudio;
		}

	}

	/**
	 * 权重求值合成器
	 * @author Darcy
	 */
	private static class WeightAudioMixer extends MultiAudioMixer{
		private float[] weights;

		public WeightAudioMixer(float[] weights){
			this.weights = weights;
		}

		@Override
		public byte[] mixRawAudioBytes(byte[][] bMulRoadAudioes) {

			if (bMulRoadAudioes == null || bMulRoadAudioes.length == 0){
				return null;
			}

			if(weights == null || weights.length != bMulRoadAudioes.length){
				return null;
			}

			byte[] realMixAudio = bMulRoadAudioes[0];

			if(bMulRoadAudioes.length == 1)
				return realMixAudio;

			for(int rw = 0 ; rw < bMulRoadAudioes.length ; ++rw){
				if(bMulRoadAudioes[rw].length != realMixAudio.length){
					Log.e("app", "column of the road of audio + " + rw +" is diffrent.");
					return null;
				}
			}

			int row = bMulRoadAudioes.length;
			int coloum = realMixAudio.length / 2;
			short[][] sMulRoadAudioes = new short[row][coloum];

			for (int r = 0; r < row; ++r) {
				for (int c = 0; c < coloum; ++c) {
					sMulRoadAudioes[r][c] = (short) ((bMulRoadAudioes[r][c * 2] & 0xff) | (bMulRoadAudioes[r][c * 2 + 1] & 0xff) << 8);
				}
			}

			short[] sMixAudio = new short[coloum];
			int mixVal;
			int sr = 0;
			for (int sc = 0; sc < coloum; ++sc) {
				mixVal = 0;
				sr = 0;
				for (; sr < row; ++sr) {
					mixVal += sMulRoadAudioes[sr][sc] * weights[sr];
				}
				sMixAudio[sc] = (short) (mixVal);
			}

			for (sr = 0; sr < coloum; ++sr) {
				realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
				realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
			}

			return realMixAudio;
		}

	}

}

