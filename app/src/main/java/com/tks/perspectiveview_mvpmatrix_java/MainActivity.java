package com.tks.perspectiveview_mvpmatrix_java;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

public class MainActivity extends AppCompatActivity  implements GLSurfaceView.Renderer {
    /* 頂点シェーダ*/
    private static final String VSHADER_SOURCE =
            "attribute vec4 a_Position;\n" +
            "attribute vec4 a_Color;\n" +
            "uniform mat4 u_MvpMatrix;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "  gl_Position = u_MvpMatrix * a_Position;\n" +
            "  v_Color = a_Color;\n" +
            "}\n";

    /* フラグメントシェーダ */
    private static final String FSHADER_SOURCE =
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "  gl_FragColor = v_Color;\n" +
            "}\n";

    // メンバー変数
    private int mNumVertices; // 描画する頂点数
    private int mu_MvpMatrix; // u_MvpMatrix変数の格納場所
    private float[] mViewMatrix; // ビュー行列
    private float[] mProjMatrix; // 投影行列

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GLSurfaceView glSurfaceView = findViewById(R.id.glview);
        glSurfaceView.setEGLContextClientVersion(3);            /* OpenGL ES 3.0を使用する */
        glSurfaceView.setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int program = Utils.initShaders(VSHADER_SOURCE, FSHADER_SOURCE);  // シェーダを初期化する
        mNumVertices = initVertexBuffers(program);    // 頂点座標と色を設定する(青い三角形が手前にある)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // 画面をクリアする色を設定する

        // u_MvpMatrix変数の格納場所を取得する
        mu_MvpMatrix = GLES20.glGetUniformLocation(program, "u_MvpMatrix");
        if (mu_MvpMatrix == -1) {
            throw new RuntimeException("u_MvpMatrix取得に失敗");
        }

        // ビュー行列を計算する
        mViewMatrix = new float[16];  // 投影行列
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 8.0f, 0.0f, 0.0f, -100.0f, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 表示領域を設定する
        GLES20.glViewport(0, 0, width, height);

        // 投影行列を計算する
        mProjMatrix = new float[16];  // 投影行列
        Utils.setPerspectiveM(mProjMatrix, 0, 30.0f, (float) width / height, 1.0, 100.0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        float[] modelMatrix = new float[16]; // モデル行列
        float[] vpMatrix = new float[16];
        float[] mvpMatrix = new float[16];   // モデルビュー投影行列

        // モデル行列を計算する
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.75f, 0.0f, 0.0f);
        // モデルビュー投影行列を計算する
        Matrix.multiplyMM(vpMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);
        // モデルビュー投影行列をu_MvpMatrix変数に設定する
        GLES20.glUniformMatrix4fv(mu_MvpMatrix, 1, false, mvpMatrix, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);// Canvasをクリアする
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumVertices);   // 三角形を描画する

        // もう1組の三角形用のモデル行列を用意する
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, -0.75f, 0.0f, 0.0f);
        // モデルビュー投影行列を計算する
        Matrix.multiplyMM(vpMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);
        // モデルビュー投影行列をu_MvpMatrix変数に設定する
        GLES20.glUniformMatrix4fv(mu_MvpMatrix, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumVertices);   // 三角形を描画する
    }

    private int initVertexBuffers(int program) {
        FloatBuffer verticesColors = Utils.makeFloatBuffer(new float[] {
                // 頂点座標、          色
                0.0f,  1.0f,  -4.0f,  0.4f,  1.0f,  0.4f, // 緑が奥
                -0.5f, -1.0f,  -4.0f,  0.4f,  1.0f,  0.4f,
                0.5f, -1.0f,  -4.0f,  1.0f,  0.4f,  0.4f,

                0.0f,  1.0f,  -2.0f,  1.0f,  1.0f,  0.4f, // 黄色が真ん中
                -0.5f, -1.0f,  -2.0f,  1.0f,  1.0f,  0.4f,
                0.5f, -1.0f,  -2.0f,  1.0f,  0.4f,  0.4f,

                0.0f,  1.0f,   0.0f,  0.4f,  0.4f,  1.0f,  // 青が前
                -0.5f, -1.0f,   0.0f,  0.4f,  0.4f,  1.0f,
                0.5f, -1.0f,   0.0f,  1.0f,  0.4f,  0.4f,
        });
        int n = 9;
        final int FSIZE = Float.SIZE / Byte.SIZE; // floatのバイト数

        // バッファオブジェクトを作成する
        int[] vertexColorBuffer = new int[1];
        GLES20.glGenBuffers(1, vertexColorBuffer, 0);

        // 頂点の座標を設定し、有効化する
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexColorBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, FSIZE * verticesColors.limit(), verticesColors, GLES20.GL_STATIC_DRAW);

        int a_Position = GLES20.glGetAttribLocation(program, "a_Position");
        if (a_Position == -1) {
            throw new RuntimeException("a_Positionの取得に失敗");
        }
        GLES20.glVertexAttribPointer(a_Position, 3, GLES20.GL_FLOAT, false, FSIZE * 6, 0);
        GLES20.glEnableVertexAttribArray(a_Position);

        int a_Color = GLES20.glGetAttribLocation(program, "a_Color");
        if (a_Color == -1) {
            throw new RuntimeException("a_Colorの取得に失敗");
        }
        GLES20.glVertexAttribPointer(a_Color, 3, GLES20.GL_FLOAT, false, FSIZE * 6, FSIZE * 3);
        GLES20.glEnableVertexAttribArray(a_Color);

        return n;
    }
}
