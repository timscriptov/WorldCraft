package com.mcal.worldcraft.multiplayer.util;

import android.util.Log;

import com.mcal.droid.rugl.util.WorldUtils;
import com.mcal.worldcraft.World;
import com.mcal.worldcraft.util.Properties;

import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WorldCopier {
    private final String worldId;

    public WorldCopier(String worldId) {
        this.worldId = worldId;
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
            return;
        }
        File directory = targetLocation.getParentFile();
        if (directory != null && !directory.exists() && !directory.mkdirs()) {
            throw new IOException("Cannot create dir " + directory.getAbsolutePath());
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            InputStream in2 = new FileInputStream(sourceLocation);
            try {
                OutputStream out2 = new FileOutputStream(targetLocation);
                try {
                    IOUtils.copy(in2, out2);
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (Throwable t2) {
                            t2.printStackTrace();
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    out = out2;
                    in = in2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable t3) {
                            t3.printStackTrace();
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Throwable t4) {
                            t4.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                in = in2;
            }
        } catch (Throwable th3) {
        }
    }

    public static void deleteAllFilesFromDirectory(File dir) {
        String[] list = dir.list();
        for (String str : list) {
            File file = new File(dir, str);
            if (file.isFile() && !file.delete()) {
                Log.d("FILE", file.getName() + "  not deleted");
            }
            if (file.isDirectory()) {
                deleteAllFilesFromDirectory(file);
            }
        }
    }

    private void copyFile(File srcDir, String srcFileName, File destDir, String destFileName) {
        File file = new File(srcDir, srcFileName);
        File mpFile = new File(destDir, destFileName);
        InputStream is = null;
        OutputStream os = null;
        try {
            if (file.exists()) {
                try {
                    InputStream is2 = new FileInputStream(file);
                    try {
                        OutputStream os2 = new FileOutputStream(mpFile);
                        try {
                            byte[] temp = new byte[is2.available()];
                            while (true) {
                                int length = is2.read(temp);
                                if (length == -1) {
                                    break;
                                }
                                os2.write(temp, 0, length);
                            }
                            if (is2 != null) {
                                try {
                                    is2.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (os2 != null) {
                                try {
                                    os2.close();
                                    os = os2;
                                    is = is2;
                                } catch (IOException e2) {
                                    os = os2;
                                    is = is2;
                                }
                            } else {
                                os = os2;
                                is = is2;
                            }
                        } catch (IOException e3) {
                            os = os2;
                            is = is2;
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Throwable th) {
                            th = th;
                            os = os2;
                            is = is2;
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e) {
                        is = is2;
                    } catch (Throwable th) {
                        is = is2;
                    }
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void copyWorld() {
        fCopyWorld();
    }

    private void fCopyWorld() {
        File dir = new File(WorldUtils.WORLD_DIR, this.worldId);
        File mpDir = new File(WorldUtils.WORLD_DIR, Properties.MULTIPLAYER_WORLD_NAME);
        if (dir.exists()) {
            try {
                if (mpDir.exists()) {
                    deleteAllFilesFromDirectory(mpDir);
                }
                mpDir.mkdir();
                copyFile(dir, World.LEVEL_DAT_FILE_NAME, mpDir, World.LEVEL_DAT_FILE_NAME);
                File dir2 = new File(dir, World.REGION_DIR_NAME);
                try {
                    File mpDir2 = new File(mpDir, World.REGION_DIR_NAME);
                    try {
                        if (!mpDir2.exists()) {
                            mpDir2.mkdir();
                        }
                        String[] fileList = dir2.list();
                        for (String fileName : fileList) {
                            copyFile(dir2, fileName, mpDir2, fileName);
                        }
                    } catch (Throwable th) {
                    }
                } catch (Throwable th2) {
                }
            } catch (Throwable th3) {
            }
        }
    }
}
