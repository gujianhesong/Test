#include <jni.h>
#include <android/log.h>

//解码
#include "libavcodec/avcodec.h"
//封装格式
#include "libavformat/avformat.h"
//缩放
#include "libswscale/swscale.h"

#include "libavutil/imgutils.h"

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO, "zp", FORMAT, ##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR, "zp", FORMAT, ##__VA_ARGS__);

/**
 * 解码音频数据
 */
JNIEXPORT int JNICALL
Java_com_pinery_test_VideoUtil_decodeAudio(JNIEnv *env, jclass type, jstring input_jstr,
                                           jstring output_jstr) {
    const char* input_cstr = (*env) -> GetStringUTFChars(env, input_jstr, NULL);
    const char* output_cstr = (*env) -> GetStringUTFChars(env, output_jstr, NULL);

    //1. 注册所有组件
    av_register_all();

    //封装格式上下文
    AVFormatContext* pFormatCtx = avformat_alloc_context();
    //2. 打开输入视频文件，成功返回0，第三个参数为NULL，表示自动检测文件格式
    if (avformat_open_input(&pFormatCtx, input_cstr, NULL, NULL) != 0) {
        LOGE("%s", "打开输入视频文件失败");
        return -1;
    }

    //3. 获取视频文件信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("%s", "获取视频文件信息失败");
        return -1;
    }

    //查找音频流所在的位置
    //遍历所有类型的流（视频流、音频流可能还有字幕流），找到音频流的位置
    int audio_stream_index = -1;
    int i = 0;
    for(; i < pFormatCtx -> nb_streams; i++) {
        if (pFormatCtx->streams[i]->codec-> codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_stream_index = i;
        }
    }

    //编解码上下文
    AVCodecContext* pCodecCtx = pFormatCtx->streams[audio_stream_index]->codec;
    //4. 查找解码器 不能通过pCodecCtx->codec获得解码器
    AVCodec* pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
    if (pCodec == NULL) {
        LOGE("%s", "查找解码器失败");
        return -1;
    }

    //5. 打开解码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("%s", "打开解码器失败");
        return -1;
    }

    AVFrame *pFrame = av_frame_alloc();
    if (!pFrame) {
        LOGE("Could not allocate audio frame\n");
        return -1;
    }

    //frame->nb_samples     = iaCodecCtx->frame_size;
    //frame->format         = iaCodecCtx->sample_fmt;
    //frame->channel_layout = iaCodecCtx->channel_layout;

    int pktsize = 0;
    AVPacket packet;
    uint8_t *pktdata;
    int len = 0;

    FILE *fp_out = fopen(output_cstr, "wb");
    if(!fp_out){
        LOGE("Could not open file");
        return -1;
    }

    while (av_read_frame(pFormatCtx, &packet) >= 0) {
        if (packet.stream_index == audio_stream_index) {
            while (packet.size > 0) {
                int out_size;
                len = avcodec_decode_audio4(pCodecCtx, pFrame, &out_size, &packet);
                if (len < 0) {
                    pktsize = 0;
                    LOGE("Error while decoding");
                    continue;
                }
                if (out_size) {
                    int data_size = av_samples_get_buffer_size(NULL, pCodecCtx->channels,
                                                               pFrame->nb_samples,
                                                               pCodecCtx->sample_fmt, 1);
                    if(data_size > 0){
                        //fwrite(pFrame->data[0], 1, pFrame->linesize[0], fp_out);
                        fwrite(pFrame->data[0], 1, data_size, fp_out);
                        fflush(fp_out);
                    }

                }
                packet.size -= len;
                packet.data += len;
            }
        }
    }

    fclose(fp_out);
    av_frame_free(pFrame);
    avcodec_free_context(&pCodecCtx);
    avformat_free_context(pFormatCtx);

    (*env) -> ReleaseStringUTFChars(env, input_jstr, input_cstr);
    (*env) -> ReleaseStringUTFChars(env, output_jstr, output_cstr);

    return 0;
}

/**
 * 解码视频数据
 */
JNIEXPORT int JNICALL
Java_com_pinery_test_VideoUtil_decodeVideo(JNIEnv *env, jclass type, jstring input_jstr,
jstring output_jstr) {
    const char* input_cstr = (*env) -> GetStringUTFChars(env, input_jstr, NULL);
    const char* output_cstr = (*env) -> GetStringUTFChars(env, output_jstr, NULL);

    //1. 注册所有组件
    av_register_all();

    //封装格式上下文
    AVFormatContext* pFormatCtx = avformat_alloc_context();
    //2. 打开输入视频文件，成功返回0，第三个参数为NULL，表示自动检测文件格式
    if (avformat_open_input(&pFormatCtx, input_cstr, NULL, NULL) != 0) {
        LOGE("%s", "打开输入视频文件失败");
        return -1;
    }

    //3. 获取视频文件信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("%s", "获取视频文件信息失败");
        return -1;
    }

    //查找视频流所在的位置
    //遍历所有类型的流（视频流、音频流可能还有字幕流），找到视频流的位置
    int video_stream_index = -1;
    int i = 0;
    for(; i < pFormatCtx -> nb_streams; i++) {
        if (pFormatCtx->streams[i]->codec-> codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_index = i;
        }
    }

    //编解码上下文
    AVCodecContext* pCodecCtx = pFormatCtx->streams[video_stream_index]->codec;
    //4. 查找解码器 不能通过pCodecCtx->codec获得解码器
    AVCodec* pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
    if (pCodec == NULL) {
        LOGE("%s", "查找解码器失败");
        return -1;
    }

    //5. 打开解码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("%s", "打开解码器失败");
        return -1;
    }

    //编码数据
    AVPacket* pPacket = (AVPacket*)av_malloc(sizeof(AVPacket));

    //像素数据（解码数据）
    AVFrame* pFrame = av_frame_alloc();
    AVFrame* pYuvFrame = av_frame_alloc();

    FILE* fp_yuv = fopen(output_cstr, "wb");

    //只有指定了AVFrame的像素格式、画面大小才能真正分配内存
    //缓冲区分配内存
    uint8_t* out_buffer = (uint8_t*)av_malloc(avpicture_get_size(AV_PIX_FMT_YUV420P, pCodecCtx->width, pCodecCtx->height));
    //初始化缓冲区
    avpicture_fill((AVPicture*)pYuvFrame, out_buffer, AV_PIX_FMT_YUV420P, pCodecCtx->width, pCodecCtx->height);

    //srcW：源图像的宽
    //srcH：源图像的高
    //srcFormat：源图像的像素格式
    //dstW：目标图像的宽
    //dstH：目标图像的高
    //dstFormat：目标图像的像素格式
    //flags：设定图像拉伸使用的算法
    struct SwsContext* pSwsCtx = sws_getContext(
            pCodecCtx->width, pCodecCtx->height, pCodecCtx->pix_fmt,
            pCodecCtx->width, pCodecCtx->height, AV_PIX_FMT_YUV420P,
            SWS_BILINEAR, NULL, NULL, NULL);

    int got_frame, len, frameCount = 0;
    //6. 从输入文件一帧一帧读取压缩的视频数据AVPacket
    while(av_read_frame(pFormatCtx, pPacket) >= 0) {
        if (pPacket->stream_index == video_stream_index) {
            //7. 解码一帧压缩数据AVPacket ---> AVFrame，第3个参数为0时表示解码完成
            len = avcodec_decode_video2(pCodecCtx, pFrame, &got_frame, pPacket);

            if (len < 0) {
                LOGE("%s", "解码失败");
                return -1;
            }
            //AVFrame ---> YUV420P
            //srcSlice[]、dst[]        输入、输出数据
            //srcStride[]、dstStride[] 输入、输出画面一行的数据的大小 AVFrame 转换是一行一行转换的
            //srcSliceY                输入数据第一列要转码的位置 从0开始
            //srcSliceH                输入画面的高度
            sws_scale(pSwsCtx,
                      pFrame->data, pFrame->linesize, 0, pFrame->height,
                      pYuvFrame->data, pYuvFrame->linesize);

            //非0表示正在解码
            if (got_frame) {
                //图像宽高的乘积就是视频的总像素，而一个像素包含一个y，u对应1/4个y，v对应1/4个y
                int yuv_size = pCodecCtx->width * pCodecCtx->height;
                //写入y的数据
                fwrite(pYuvFrame->data[0], 1, yuv_size, fp_yuv);
                //写入u的数据
                fwrite(pYuvFrame->data[1], 1, yuv_size/4, fp_yuv);
                //写入v的数据
                fwrite(pYuvFrame->data[2], 1, yuv_size/4, fp_yuv);

                LOGI("解码第%d帧", frameCount++);
            }
            av_free_packet(pPacket);
        }
    }

    fclose(fp_yuv);
    av_frame_free(&pFrame);
    av_frame_free(&pYuvFrame);
    avcodec_free_context(&pCodecCtx);
    avformat_free_context(pFormatCtx);

    (*env) -> ReleaseStringUTFChars(env, input_jstr, input_cstr);
    (*env) -> ReleaseStringUTFChars(env, output_jstr, output_cstr);

    return 0;
}

/**
 * 编码音频数据
 */
JNIEXPORT int JNICALL
Java_com_pinery_test_VideoUtil_encodeAudio(JNIEnv *env, jclass type, jstring input_jstr,
                                      jstring output_jstr) {

    const char* input_cstr = (*env)->GetStringUTFChars(env, input_jstr, NULL);
    const char* output_cstr = (*env)->GetStringUTFChars(env, output_jstr, NULL);

    FILE *in_file = fopen(input_cstr, "rb");

    av_register_all();

    AVFormatContext *pFormatCtx = avformat_alloc_context();
    AVOutputFormat *outFormat = av_guess_format(NULL, output_cstr, NULL);
    pFormatCtx->oformat = outFormat;

    if(avio_open(pFormatCtx->pb, output_cstr, AVIO_FLAG_READ_WRITE) < 0){
        LOGE("Failed to open output file!")
        return -1;
    }

    AVStream *audio_st = avformat_new_stream(pFormatCtx, 0);
    if(audio_st == NULL){
        return -1;
    }

    AVCodecContext *pCodecCtx = audio_st->codec;
    pCodecCtx->codec_id = outFormat->audio_codec;
    pCodecCtx->codec_type = AVMEDIA_TYPE_AUDIO;
    pCodecCtx->sample_fmt = AV_SAMPLE_FMT_S16;
    pCodecCtx->sample_rate = 44100;
    pCodecCtx->channel_layout = AV_CH_LAYOUT_STEREO;
    pCodecCtx->channels = av_get_channel_layout_nb_channels(pCodecCtx->channel_layout);
    pCodecCtx->bit_rate = 64000;

    av_dump_format(pFormatCtx, 0, output_cstr, 1);

    AVCodec *pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
    if(pCodec){
        LOGE("Can not find encoder");
        return -1;
    }

    if(avcodec_open2(pCodecCtx, pCodec, NULL) < 0){
        LOGE("Failed open encoder")
    }

    AVFrame *pFrame = av_frame_alloc();
    pFrame->nb_samples = pCodecCtx->frame_size;
    pFrame->format = pCodecCtx->sample_fmt;

    int size = av_samples_get_buffer_size(NULL, pCodecCtx->channels, pCodecCtx->frame_size, pCodecCtx->sample_fmt, 1);
    uint8_t *frame_buf = av_malloc(size);
    avcodec_fill_audio_frame(pFrame, pCodecCtx->channels, pCodecCtx->frame_size, frame_buf, size, 1);

    avformat_write_header(pFormatCtx, NULL);

    AVPacket packet;
    int frame_num = 1000;
    int got_frame;
    int ret;

    av_new_packet(&packet, size);

    for(int i=0; i<frame_num; i++){
        if(fread(frame_buf, 1, size, in_file) < 0){
            LOGE("Failed to read raw data!");
            return -1;
        }else if(feof(in_file)){
            break;
        }

        pFrame->data[0] = frame_buf;
        pFrame->pts = i * 100;
        got_frame = 0;

        ret = avcodec_encode_audio2(pCodecCtx, &packet, pFrame, &got_frame);
        if(ret < 0){
            LOGE("Failed to encode!");
            return -1;
        }

        if(got_frame == 1){
            LOGI("Successed to encode frame, size %d", packet.size);
            packet.stream_index = audio_st->index;
            ret = av_write_frame(pFormatCtx, &packet);
            av_free_packet(&packet);
        }
    }

    ret = flush_encoder(pFormatCtx, 0);
    if(ret < 0){
        LOGE("Flush encoder failed");
        return -1;
    }

    av_write_trailer(pFormatCtx);

    if(audio_st){
        avcodec_close(audio_st->codec);
        av_free(pFrame);
        av_free(frame_buf);
    }

    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    fclose(in_file);

    return ret;
}

int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index){
    if(fmt_ctx->streams[stream_index]->codec->codec->capabilities && CODEC_CAP_DELAY){
        return 0;
    }

    AVPacket packet;
    int ret;
    int got_frame;

    while(1){
        packet.data = NULL;
        packet.size = 0;
        av_init_packet(&packet);
        ret = avcodec_encode_audio2(fmt_ctx->streams[stream_index]->codec, &packet, NULL, &got_frame);
        av_frame_free(NULL);
        if(ret < 0){
            break;
        }
        if(!got_frame){
            ret = 0;
            break;
        }

        LOGI("Flush encoder, Succed to encode 1 frame, size %d", packet.size);

        ret = av_write_frame(fmt_ctx, &packet);
        if(ret < 0){
            break;
        }
    }

    return ret;
}

/**
 * 编码视频数据
 */
JNIEXPORT int JNICALL
Java_com_pinery_test_VideoUtil_encodeVideo(JNIEnv *env, jclass type, jstring input_jstr,
                                           jstring output_jstr) {

    const char *input_cstr = (*env)->GetStringUTFChars(env, input_jstr, NULL);
    const char *output_cstr = (*env)->GetStringUTFChars(env, output_jstr, NULL);

    enum AVCodecID codec_id = AV_CODEC_ID_H264;
    int frame_num = 100;

    av_register_all();

    AVCodec *pCodec = avcodec_find_encoder(codec_id);
    if(!pCodec){
        LOGE("Codec not found!");
        return -1;
    }

    AVCodecContext *pCodecCtx = avcodec_alloc_context3(pCodec);
    if(!pCodecCtx){
        LOGE("Could not allocate video codec context");
        return -1;
    }

    pCodecCtx->bit_rate = 400000;
    pCodecCtx->width = 480;
    pCodecCtx->height = 272;
    pCodecCtx->time_base.den = 25;
    pCodecCtx->time_base.num = 1;
    pCodecCtx->gop_size = 10;
    pCodecCtx->max_b_frames = 1;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;

    if(avcodec_open2(pCodecCtx, pCodec, NULL) < 0){
        LOGE("Could not open codec");
        return -1;
    }

    AVFrame *pFrame = av_frame_alloc();
    if(!pFrame){
        LOGE("Could not allocate video frame");
        return -1;
    }

    pFrame->format = pCodecCtx->pix_fmt;
    pFrame->width = pCodecCtx->width;
    pFrame->height = pCodecCtx->height;

    int ret = av_image_alloc(pFrame->data, pFrame->linesize, pFrame->width, pFrame->height, pFrame->format, 1);
    if(ret < 0){
        LOGE("Could not allocate picture buffer");
        return -1;
    }

    FILE *fp_in = fopen(input_cstr, "rb");
    if(!fp_in){
        LOGE("Could not open file %s", input_cstr);
        return -1;
    }

    FILE *fp_out = fopen(output_cstr, "wb");
    if(!fp_out){
        LOGE("Could not open file %s", output_cstr);
        return -1;
    }

    AVPacket packet;
    int got_output;
    int frame_count;

    int y_size = pCodecCtx->width * pCodecCtx->height;
    for(int i=0; i<frame_num; i++){
        av_init_packet(&packet);
        packet.data = NULL;
        packet.size = 0;

        if(fread(pFrame->data[0], 1, y_size, fp_in) < 0
                || fread(pFrame->data[1], 1, y_size/4, fp_in) < 0
                || fread(pFrame->data[2], 1, y_size/4, fp_in) < 0){
            return -1;
        }else if(feof(fp_in)){
            break;
        }

        pFrame->pts = i;
        ret = avcodec_encode_video2(pCodecCtx, &packet, pFrame, &got_output);
        if(ret < 0){
            LOGE("Error encoding frame");
            return -1;
        }
        if(got_output){
            LOGI("Succeed encode frame, count %d, size %d", frame_count, packet.size);
            frame_count++;
            fwrite(packet.data, 1, packet.size, fp_out);
            av_free_packet(&packet);
        }
    }

    int i=0;
    for(got_output = 1; got_output; i++){
        ret = avcodec_encode_video2(pCodecCtx, &packet, NULL, &got_output);
        if(ret < 0){
            LOGE("Error encoding frame");
            return -1;
        }
        if(got_output){
            LOGI("Flush encoder, succeed to encode 1 frame");
            frame_count++;
            fwrite(packet.data, 1, packet.size, fp_out);
            av_free_packet(&packet);
        }
    }

    fclose(fp_out);
    avcodec_close(pCodecCtx);
    av_free(pCodecCtx);
    av_freep(&pFrame->data[0]);
    av_frame_free(&pFrame);

    return 0;
}

