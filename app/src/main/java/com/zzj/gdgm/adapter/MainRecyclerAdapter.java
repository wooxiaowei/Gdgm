package com.zzj.gdgm.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.zzj.gdgm.R;
import com.zzj.gdgm.bean.CourseInfo;
import com.zzj.gdgm.support.JsoupService;
import com.zzj.gdgm.support.OkHttpUtil;
import com.zzj.gdgm.ui.CourseActivity;
import com.zzj.gdgm.ui.ScoreActivity;
import com.zzj.gdgm.view.SimpleItemHolder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by J。 on 2016/4/18.
 * MainActivity的Recycler适配器
 */
public class MainRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final String TAG = "MainRecyclerAdapter";
    private Context context;
    private LayoutInflater layoutInflater;
    private Map<String, String> linkMap;
    private Handler handler;
    private ProgressDialog progressDialog;

    public MainRecyclerAdapter(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        layoutInflater = LayoutInflater.from(context);
        progressDialog = new ProgressDialog(context);
    }

    public Map<String, String> getLinkMap() {
        return linkMap;
    }

    public void setLinkMap(Map<String, String> linkMap) {
        this.linkMap = linkMap;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SimpleItemHolder simpleItemHolder = new SimpleItemHolder(layoutInflater.inflate(R.layout.main_simple_item_layout, parent, false));
        simpleItemHolder.itemView.setOnClickListener(this);
        return simpleItemHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (position) {
            case 0:
                ((SimpleItemHolder) holder).getTextView_title().setText("课表查询");
                ((SimpleItemHolder) holder).getTextView_content().setText("课表在手，天下我有，妈妈再也不用担心我缺课啦");
                ((SimpleItemHolder) holder).getImageView_titleImage().setImageResource(R.drawable.test_1);
                holder.itemView.setTag(position);
                break;
            case 1:
                ((SimpleItemHolder) holder).getTextView_title().setText("成绩查询");
                ((SimpleItemHolder) holder).getTextView_content().setText("挂不挂科，点我就知，再也不用怕查不到成绩啦");
                ((SimpleItemHolder) holder).getImageView_titleImage().setImageResource(R.drawable.test_3);
                holder.itemView.setTag(position);
                break;
            case 2:
                ((SimpleItemHolder) holder).getTextView_title().setText("图书查询");
                ((SimpleItemHolder) holder).getTextView_content().setText("图书馆有啥书，一查便知，再也不用白跑一趟啦");
                ((SimpleItemHolder) holder).getImageView_titleImage().setImageResource(R.drawable.test_2);
                holder.itemView.setTag(position);
                break;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public void onClick(final View v) {
        int tag = (int) v.getTag();
        switch (tag) {
            case 0:
                dialogShow("正在读取数据....", false);
                for (String key : linkMap.keySet()) {
                    Log.d(TAG, "linkMap --> key = " + key + " --> value = " + linkMap.get(key));
                }
                Request request = OkHttpUtil.getRequest(OkHttpUtil.getREFERER() + linkMap.get("班级课表查询"));
                OkHttpUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Message message = Message.obtain();
                        message.obj = "获取数据失败";
                        handler.sendMessage(message);
                        Log.v(TAG, "班级课表查询  onFailure -->  = " + e.getMessage());
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Message message = Message.obtain();
                        try {
                            if (response.code() == 200) {
                                String content = new String(response.body().bytes(), "gb2312");
                                if (content != null) {
                                    Intent intent = new Intent();
                                    intent.setClass(context, CourseActivity.class);
                                    intent.putExtra("content", content);
                                    context.startActivity(intent);
                                }
                            } else {
                                message.obj = "获取数据失败";
                                handler.sendMessage(message);
                            }
                            Log.v(TAG, "班级课表查询  onResponse -->  statuscode = " + response.code());
                        } catch (IOException e) {
                            message.obj = "获取数据失败";
                            handler.sendMessage(message);
                            e.printStackTrace();
                        } finally {
                            progressDialog.dismiss();
                        }
                    }
                });
                break;
            case 1:
                dialogShow("正在拼命加载中...", false);
                Request request_score = OkHttpUtil.getRequest(OkHttpUtil.getREFERER() + linkMap.get("学习成绩查询"));
                OkHttpUtil.getOkHttpClient().newCall(request_score).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.v(TAG, "学习成绩查询  onFailure --> " + e.getMessage());
                        Message message = Message.obtain();
                        message.obj = "获取失败,请检查网络连接状况";
                        handler.sendMessage(message);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Message message = Message.obtain();
                        try {
                            if (response.code() == 200) {
                                String content = new String(response.body().bytes(), "gb2312");
                                final Map<String, Object> map = JsoupService.getScoreYear(content);
                                progressDialog.dismiss();
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showChooseYearSemesterDialog(map);
                                    }
                                });
                                Log.v(TAG, "学习成绩查询  onResponse --> content = " + content);
                            } else {
                                message.obj = "获取失败,请检查网络连接状况";
                                handler.sendMessage(message);
                            }
                            Log.v(TAG, "学习成绩查询  onResponse --> response.code = " + response.code());
                        } catch (Exception e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            message.obj = "获取失败,请检查网络连接状况";
                            handler.sendMessage(message);
                        } finally {
                            progressDialog.dismiss();
                        }
                    }
                });
                break;
            case 2:

                break;

        }
    }

    /**
     * 设置dialog状态信息并展示
     *
     * @param message    dialog内容
     * @param cancelable 是否可取消
     */
    public void dialogShow(String message, boolean cancelable) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        progressDialog.show();
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }


    /**
     * 显示自定义对话框并请求数据
     * @param map  学年学期以及请求数据的集合
     */
    private void showChooseYearSemesterDialog(final Map<String, Object> map) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.score_custom_dialog, null);
        builder.setView(view);
        builder.setTitle("请选择要查询的学年学期");
        /**
         * 学年spinner适配器
         */
        ArrayAdapter<String> arrayAdapter_year = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, (List<String>) map.get("score_year"));
        arrayAdapter_year.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner_year = ((Spinner) view.findViewById(R.id.spinner_year));
        spinner_year.setAdapter(arrayAdapter_year);
        //默认选择List集合中倒数第二个
        if (((List<String>) map.get("score_year")).size() > 1) {
            spinner_year.setSelection(((List<String>) map.get("score_year")).size() - 2);
        }
        /**
         * 学期spinner适配器
         */
        ArrayAdapter<String> arrayAdapter_semester = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, (List<String>) map.get("score_semester"));
        arrayAdapter_semester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner_semester = ((Spinner) view.findViewById(R.id.spinner_semester));
        spinner_semester.setAdapter(arrayAdapter_semester);
        //默认选择List集合中倒数第三个
        if (((List<String>) map.get("score_semester")).size() > 2) {
            spinner_semester.setSelection((((List<String>) map.get("score_semester")).size() - 3));
        }
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                RequestBody requestBody = new FormBody.Builder()
                        .add("__VIEWSTATE", (String) map.get("__VIEWSTATE"))
                        .add("__VIEWSTATEGENERATOR", (String) map.get("__VIEWSTATEGENERATOR"))
                        .add("ddlXN", spinner_year.getSelectedItem().toString())
                        .add("ddlXQ", spinner_semester.getSelectedItem().toString())
                        .add("Button1", "按学期查询")
                        .build();
                /**
                 * 对Referer中的中文进行编码
                 */
                String Referer = encodeUrl(OkHttpUtil.getREFERER() + getLinkMap().get("学习成绩查询"));
                Request request = OkHttpUtil.getRequest(Referer, Referer, requestBody);
                dialogShow("正在努力读取数据", false);
                OkHttpUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.v(TAG, "学习成绩查询  --> onFailure  --> " + e.getMessage());
                        progressDialog.dismiss();
                        dialog.dismiss();
                        Message message = Message.obtain();
                        message.obj = "获取失败,请检查网络连接状况";
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Message message = Message.obtain();
                        try {
                            if (response.code() == 200) {
                                String content = new String(response.body().bytes(), "gb2312");
                                ArrayList<CourseInfo> courseInfoArrayList = JsoupService.parseCourseScore(content);
                                Intent intent = new Intent();
                                intent.setClass(context, ScoreActivity.class);
                                intent.putExtra("score", courseInfoArrayList);
                                intent.putExtra("year", spinner_year.getSelectedItem().toString());
                                intent.putExtra("semester", spinner_semester.getSelectedItem().toString());
                                context.startActivity(intent);
                            } else {
                                message.obj = "获取失败,请检查网络连接状况";
                                handler.sendMessage(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            message.obj = "获取失败";
                            handler.sendMessage(message);
                        } finally {
                            progressDialog.dismiss();
                        }
                        Log.v(TAG, "学习成绩查询  --> onResponse  --> response.code = " + response.code());
                    }
                });
            }

        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    /**
     * 将Url中的中文进行编码
     *
     * @param url 要进行编码的Url
     * @return 编码后的url
     */
    private String encodeUrl(String url) {
        String new_url = url;
        Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(new_url);
        while (matcher.find()) {
            try {
                new_url = new_url.replaceAll(matcher.group(), URLEncoder.encode(matcher.group(), "gb2312"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return new_url;
    }
}
