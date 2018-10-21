package com.test.download;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.test.download.download.DownloadApi;
import com.test.download.download.DownloadEvent;
import com.test.download.download.DownloadInfo;
import com.test.download.download.DownloadInfoUtil;
import com.test.download.download.DownloadManager;
import com.test.download.util.FormatUtil;
import com.test.download.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private EditText etUrl;
    private Button btnAddDownload;
    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private List<String> mList = new ArrayList<>();
    private HashMap<String, DownloadEvent> hashMap = new HashMap<>();
    private HashMap<String, FileViewHolder> viewMap = new HashMap<>();
    private HashMap<String, DownloadInfo> downloadInfoMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        initView();

        initData();

    }

    private void initView(){
        etUrl = (EditText) findViewById(R.id.et_url);
        btnAddDownload = (Button) findViewById(R.id.btn_add_download);
        btnAddDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etUrl.getText().toString().trim();
                addDownload(url);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void initData() {
        mList.add("http://codown.youdao.com/note/youdaonote_android_6.3.3_youdaoweb.apk");
        mList.add("http://p.gdown.baidu.com/4144d62b08165703393fd39a6bbc301ae156409ad5433fe4d13ef3ce5ed7feeaff5eee08a613cf99df28ae19de3f684c254aa1109fbac5cf49867a4a26f83a48447dbb9a6b7d4cea7472f338b4fd3158128bd5d6ad5061a441a877ac2857264e6307c236e6f1bac75fbba4a34ceeb829db54423fbd34d2bc55a7928fc96b2772825e59b62cd8607dc08f3f9396ed468725919e13bcf94349e427f3666632e5df2215c5b59c61ebb2bcbb8c5e4c18efbd4663c3b785f19e20b3e0b3916a29aa7e0f1dc4aa54b0669479c326eb87ac871c1538926fee90cd003f0598ff85629349aaaf68cff83cf4d71a54e7cb4b109053b3c317441bf8ad988be741eb0b457c50ae8fec5a41f5c133b5ed0d7a8f5b00e8b7d8b3c735d08043c39403f26a27d782b8420f96f1a4d67c");
        mList.add("http://p.gdown.baidu.com/d6575fe07172deeec1af9b5a7fda3f5dc45649d56c8e525ba41aea4e3d507fde1e8577c50b5f4b1558b9b1ce65b3445c2093102d7e6c0d3eb05a4fcc3c20304af772e767efb1d26bfad7d2a85ace4d8d4bfe2276e86064e801e5cfaf28691893c007025b54529f2d4d3c1a48dd46c91fa5c945fa4680f0dad9fc9005b8202441b8420f96f1a4d67c");
        mList.add("http://p.gdown.baidu.com/8f610fc0df38c262d391e61a044a55c2723a16b829b96b397b4b58ab83d5004952986c021807a502d58554105299ca3210704e24e154dd86944700c0ae5700fef5b88bd2648443fc7b9958e1b4070c8a9a770de4495da4ef8f1e214d4043e9956549dbf236aa3fe17289c30c3ff493e0097addd4c3c955b84e250b248818a3ad2025159dc3a2c2b5221120e2e948b463383670702674d47f9bb52ef1213ef9712093102d7e6c0d3ed25301e7d9993754784f0bd056e11994610a649aeaf7823bedefab8941c42d9fc7918e751ca001071c6552d864608a3d052692f5edf942c15be5bc3b41c04479f130c5bb4b1f60a2b31ff7a6f4c5c873");
        mList.add("http://p.gdown.baidu.com/3e2a52e238b3f427306013eed4368e7719a04e2c4b3c5981e4e53b6fa41ddfb93d55178764c1946ad6413dedd7f54e95984c95a6ecc7865d1b821f3907466a7f61b1fa301bc02e4d4a30359f7a204f607edb0835502e07cb0b6d52fd94a64be4b8b8a8d6157e7be2144e2bdb2e22f91fae1d2aaa105a8534166b10bdf3a172ccae40184e848c79d77d61322a7f3c8fff9190d0fd5485c8e343c347cffc5a1c31b2823d7d8007e5d7db765f404b8798a41662a5a89ba68d244157ba97b3222237a3d8c1a030dd6cc320d09a610c8f6003c5f2747b38e45b5bb32bde46872553549b404114cc7b9866ac47b77942780ae48a527e9556f4c50ce9d3513ec5098dc013809f8c37646b6bbc1472707ad6e3bd35ad2647b37bbc1e5ae41a6ca3a2f4e74efa8fb58ccd8fdfb72de728522a1d86d727727e9e6cad9b06ee723e20cc5de47ea17f90783841135b4a7f769d75968d");
        adapter.notifyDataSetChanged();

        for(String url : mList){
            DownloadEvent event = new DownloadEvent(DownloadEvent.State.IDEL, url);
            hashMap.put(event.getUrl(), event);
        }

        Observable.just(1).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        downloadInfoMap = DownloadInfoUtil.getAllDownloadInfo();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private void addDownload(String url) {
        if (url == null) return;
        if (!(url.startsWith("http://") || url.startsWith("https://"))) return;
        if (mList.contains(url)) return;

        mList.add(0, url);
        adapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);
        DownloadManager.excueteDownload(getBaseContext(), url);

        DownloadEvent event = new DownloadEvent(DownloadEvent.State.IDEL, url);
        hashMap.put(event.getUrl(), event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DownloadEvent event) {
        LogUtil.i("onEvent ：" + event.getState());

        hashMap.put(event.getUrl(), event);
        FileViewHolder viewHolder = viewMap.get(event.getUrl());
        if (viewHolder != null) {
            viewHolder.updateState(event);
        }
    }

    private class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_file, null);
            FileViewHolder viewHolder = new FileViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {
            String url = mList.get(position);
            LogUtil.i("onBindViewHolder ：" + url);
            holder.tvUrl.setText(url);
            DownloadEvent event = hashMap.get(url);
            holder.updateState(event);

            holder.itemView.setTag(R.id.tv_url, url);
            viewMap.put(url, holder);
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }
    }

    private class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvFilePath, tvUrl, tvState, tvDownloadSize;
        ProgressBar pbFile;
        Button btnStart, btnDelete;

        public FileViewHolder(View itemView) {
            super(itemView);

            tvFilePath = itemView.findViewById(R.id.tv_path);
            tvUrl = itemView.findViewById(R.id.tv_url);
            tvState = itemView.findViewById(R.id.tv_state);
            tvDownloadSize = itemView.findViewById(R.id.tv_download_size);
            pbFile = itemView.findViewById(R.id.progress_bar);
            btnStart = itemView.findViewById(R.id.btn_start);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            btnStart.setOnClickListener(this);
            btnDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start:

                    Object obj = itemView.getTag(R.id.tv_url);
                    if (obj != null && obj instanceof String) {
                        String url = (String) obj;
                        if (DownloadManager.isDownloading(url)) {
                            DownloadManager.cancelDownload(getBaseContext(), url);
                        } else {
                            DownloadManager.excueteDownload(getBaseContext(), url);
                        }
                    }

                    break;
                case R.id.btn_delete:

                    obj = itemView.getTag(R.id.tv_url);
                    if (obj != null && obj instanceof String) {
                        String url = (String) obj;
                        DownloadManager.cancelDownload(getBaseContext(), url);

                        DownloadEvent event = hashMap.remove(url);
                        if(event != null &&  event.getLocalPath()!= null){
                            new File(event.getLocalPath()).delete();
                        }

                        int position = mList.indexOf(url);
                        mList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }

                    break;
            }
        }

        public void updateState(DownloadEvent event) {
            if (event != null) {
                switch (event.getState()) {
                    case IDEL:

                        btnStart.setText("开始");
                        if(downloadInfoMap != null){
                            DownloadInfo downloadInfo = downloadInfoMap.get(event.getUrl());
                            if(downloadInfo != null){
                                int progress = (int) (downloadInfo.getDownloadSize() * 100.0 / downloadInfo.getTotalSize());
                                tvState.setText("");
                                tvDownloadSize.setText(FormatUtil.formetFileSize(downloadInfo.getDownloadSize()) + " / "
                                    + FormatUtil.formetFileSize(downloadInfo.getTotalSize()) + " , "
                                    + progress + "%");
                                pbFile.setProgress(progress);
                                String path = DownloadApi.DOWNLOAD_DIR + downloadInfo.getName();
                                tvFilePath.setText(path);
                            }
                        }

                        break;
                    case START:

                        tvState.setText("开始下载");
                        btnStart.setText("暂停");

                        break;
                    case PROGRESS:

                        LogUtil.i("onBindViewHolder ：" + event.getState());
                        tvState.setText("正在下载");
                        pbFile.setProgress(event.getProgress());
                        tvFilePath.setText(event.getLocalPath());

                        tvDownloadSize.setText(FormatUtil.formetFileSize(event.getDownloadedSize()) + " / "
                            + FormatUtil.formetFileSize(event.getTotalSize()) + " , "
                            + event.getProgress() + "%");

                        break;
                    case SUCCESS:

                        tvState.setText("下载完成");
                        btnStart.setText("开始");

                        break;
                    case FAIL:

                        tvState.setText("下载失败：" + event.getErrorMsg());
                        btnStart.setText("开始");

                        break;
                    case CANCEL:

                        tvState.setText("下载取消");
                        btnStart.setText("开始");

                        break;
                }
            }
        }
    }

}
