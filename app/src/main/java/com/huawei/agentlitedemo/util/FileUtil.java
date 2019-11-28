package com.huawei.agentlitedemo.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    private static final String TAG = "FileUtil";

    public static final String DIR_IMAGE_CACHE = "PicCache";
    public static final String DIR_VIDEO_FOLDER = "helloworldVideo";

    private static File getInternalWorkDir(Context context) {
        String path = context.getFilesDir().getParent();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static String getAppPath(Context mcontext) {
        if (null == mcontext) {
            Log.e(TAG, "null == mcontext");
            return null;
        }

        if (null == mcontext.getFilesDir()) {
            Log.e(TAG, "null == mcontext.getFilesDir()");
            return null;
        }

        String fileDir = mcontext.getFilesDir().getAbsolutePath();
        if (null == fileDir) {
            Log.e(TAG, "getAppPath null == fileDir");
            return null;
        }

        Log.i(TAG, "get App path:" + fileDir);

        if (fileDir.lastIndexOf("/") > 0) {
            fileDir = fileDir.substring(0, fileDir.lastIndexOf("/"));
        }

        return fileDir;
    }

    public static File getExternalDCIM() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if(null != file) {
            Log.i(TAG, "getExternalDCIM : absolute file path " + file.getAbsolutePath() + ", file path " + file.getPath());
            return file;
        }
        Log.i(TAG, "getExternalDCIM : get DCIM path failed");
        return null;
    }

    public static File getExternalWorkDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //return combineDir(Environment.getExternalStorageDirectory(), DIR_ROOT);
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }

    public static File getWorkDir(Context context) {
        File file = getExternalWorkDir();
        if (file == null || !file.exists()) {
            file = getInternalWorkDir(context);
        }
        return file;
    }

    public static File combineDir(Context context, String subDirName) {
        File file = new File(getWorkDir(context) + File.separator + subDirName);
        try {
            if (!file.exists()) {
                file.mkdirs();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File combineDir(File dir, String subDirName) {
        File file = new File(dir + File.separator + subDirName);
        try {
            if (!file.exists()) {
                file.mkdirs();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    public static void copyAssets(Context context, String assetDir, String dir) {
        String[] files;
        try {
            files = context.getResources().getAssets().list(assetDir);

        } catch (IOException e1) {
            return;
        }

        File mWorkingPath = new File(dir); // if this directory does not exists,
        if (!mWorkingPath.exists()) {
            if (!mWorkingPath.mkdirs()) {
                Log.i("FileUtil:", "creat dir failed, working path " + mWorkingPath);
                return;
            }
        }
        for (int i = 0; i < files.length; i++) {
            OutputStream out = null;
            InputStream in = null;
            try {
                String fileName = files[i];

                File outFile = new File(mWorkingPath, fileName);
                if (outFile.exists()) {
                    boolean isDel = outFile.delete();
                    if (!isDel)
                        Log.e("FileUtil:", "delete file failed, file name\":" + fileName);
                }
                if (0 != assetDir.length())
                    in = context.getAssets().open(assetDir + "/" + fileName);
                else
                    in = context.getAssets().open(fileName);
                out = new FileOutputStream(outFile); // Transfer
                // bytes
                // from in
                // to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {

                    out.write(buf, 0, len);
                }

            } catch (FileNotFoundException e) {
                Log.e("FileUtil:", "File open exception:" + e.getMessage());
            } catch (IOException e) {
                Log.e("FileUtil:", "File read or write exception:" + e.getMessage());
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    Log.e("FileUtil:", "output file close exception:" + e.getMessage());
                }
                try {
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    Log.e("FileUtil:", "input file close exception:" + e.getMessage());
                }
            }
        }
    }

    public static void copyAssetDirToFiles(Context context, String dirname)
            throws IOException {
        //File dir = new File(context.getFilesDir() + "/" + dirname);
        File dir = new File(getWorkDir(context).getAbsolutePath() + "/AgentLiteDemo/" + dirname);
        
        dir.mkdir();

        AssetManager assetManager = context.getAssets();
        String[] children = assetManager.list(dirname);
        for (String child : children) {
            child = dirname + '/' + child;
            String[] grandChildren = assetManager.list(child);
            if (0 == grandChildren.length)
                copyAssetFileToFiles(context, child);
            else
                copyAssetDirToFiles(context, child);
        }
    }

    public static void copyAssetFileToFiles(Context context, String filename) throws IOException {
        String filePath = getWorkDir(context).getAbsolutePath() + "/AgentLiteDemo";
        InputStream is = context.getAssets().open(filename);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        
        //File of = new File(context.getFilesDir() + "/" + filename);

        
        Log.i(TAG, "filePath = " + filePath);
        Log.i(TAG, "filename = " + filename);
        File of = new File(filePath + "/" + filename);
        of.createNewFile();
        FileOutputStream os = new FileOutputStream(of);
        os.write(buffer);
        os.close();
    }
    
    public static boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }
}
