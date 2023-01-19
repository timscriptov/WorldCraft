package com.solverlabs.worldcraft.framework.loader;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.framework.gl.Vertices3;
import com.solverlabs.worldcraft.framework.io.FileIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ObjLoader {
    @NonNull
    public static Vertices3 load(FileIO fileIO, String file) {
        int vi;
        InputStream in = null;
        try {
            try {
                in = fileIO.readAsset(file);
                List<String> lines = readLines(in);
                float[] vertices = new float[lines.size() * 3];
                float[] normals = new float[lines.size() * 3];
                float[] uv = new float[lines.size() * 2];
                int numVertices = 0;
                int numNormals = 0;
                int numUV = 0;
                int numFaces = 0;
                int[] facesVerts = new int[lines.size() * 3];
                int[] facesNormals = new int[lines.size() * 3];
                int[] facesUV = new int[lines.size() * 3];
                int vertexIndex = 0;
                int normalIndex = 0;
                int uvIndex = 0;
                int faceIndex = 0;
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.startsWith("v ")) {
                        String[] tokens = line.split("[ ]+");
                        vertices[vertexIndex] = Float.parseFloat(tokens[1]);
                        vertices[vertexIndex + 1] = Float.parseFloat(tokens[2]);
                        vertices[vertexIndex + 2] = Float.parseFloat(tokens[3]);
                        vertexIndex += 3;
                        numVertices++;
                    } else if (line.startsWith("vn ")) {
                        String[] tokens2 = line.split("[ ]+");
                        normals[normalIndex] = Float.parseFloat(tokens2[1]);
                        normals[normalIndex + 1] = Float.parseFloat(tokens2[2]);
                        normals[normalIndex + 2] = Float.parseFloat(tokens2[3]);
                        normalIndex += 3;
                        numNormals++;
                    } else if (line.startsWith("vt")) {
                        String[] tokens3 = line.split("[ ]+");
                        uv[uvIndex] = Float.parseFloat(tokens3[1]);
                        uv[uvIndex + 1] = Float.parseFloat(tokens3[2]);
                        uvIndex += 2;
                        numUV++;
                    } else if (line.startsWith("f ")) {
                        String[] tokens4 = line.split("[ ]+");
                        String[] parts = tokens4[1].split("/");
                        facesVerts[faceIndex] = getIndex(parts[0], numVertices);
                        if (parts.length > 2) {
                            facesNormals[faceIndex] = getIndex(parts[2], numNormals);
                        }
                        if (parts.length > 1) {
                            facesUV[faceIndex] = getIndex(parts[1], numUV);
                        }
                        int faceIndex2 = faceIndex + 1;
                        String[] parts2 = tokens4[2].split("/");
                        facesVerts[faceIndex2] = getIndex(parts2[0], numVertices);
                        if (parts2.length > 2) {
                            facesNormals[faceIndex2] = getIndex(parts2[2], numNormals);
                        }
                        if (parts2.length > 1) {
                            facesUV[faceIndex2] = getIndex(parts2[1], numUV);
                        }
                        int faceIndex3 = faceIndex2 + 1;
                        String[] parts3 = tokens4[3].split("/");
                        facesVerts[faceIndex3] = getIndex(parts3[0], numVertices);
                        if (parts3.length > 2) {
                            facesNormals[faceIndex3] = getIndex(parts3[2], numNormals);
                        }
                        if (parts3.length > 1) {
                            facesUV[faceIndex3] = getIndex(parts3[1], numUV);
                        }
                        faceIndex = faceIndex3 + 1;
                        numFaces++;
                    }
                }
                float[] verts = new float[((numUV > 0 ? 2 : 0) + (numNormals > 0 ? 3 : 0) + 3) * numFaces * 3];
                int i2 = 0;
                int vi2 = 0;
                while (i2 < numFaces * 3) {
                    int vertexIdx = facesVerts[i2] * 3;
                    int vi3 = vi2 + 1;
                    verts[vi2] = vertices[vertexIdx];
                    int vi4 = vi3 + 1;
                    verts[vi3] = vertices[vertexIdx + 1];
                    int vi5 = vi4 + 1;
                    verts[vi4] = vertices[vertexIdx + 2];
                    if (numUV > 0) {
                        int uvIdx = facesUV[i2] * 2;
                        int vi6 = vi5 + 1;
                        verts[vi5] = uv[uvIdx];
                        vi5 = vi6 + 1;
                        verts[vi6] = 1.0f - uv[uvIdx + 1];
                    }
                    int vi7 = vi5;
                    if (numNormals > 0) {
                        int normalIdx = facesNormals[i2] * 3;
                        int vi8 = vi7 + 1;
                        verts[vi7] = normals[normalIdx];
                        int vi9 = vi8 + 1;
                        verts[vi8] = normals[normalIdx + 1];
                        vi = vi9 + 1;
                        verts[vi9] = normals[normalIdx + 2];
                    } else {
                        vi = vi7;
                    }
                    i2++;
                    vi2 = vi;
                }
                Vertices3 model = new Vertices3(numFaces * 3, 0, false, numUV > 0, numNormals > 0);
                model.setVertices(verts, 0, verts.length);
                return model;
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException("couldn't load '" + file + "'", ex);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static int getIndex(String index, int size) {
        int idx = Integer.parseInt(index);
        return idx < 0 ? size + idx : idx - 1;
    }

    static List<String> readLines(InputStream in) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (true) {
            String line = reader.readLine();
            if (line != null) {
                lines.add(line);
            } else {
                return lines;
            }
        }
    }
}
