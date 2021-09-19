package com.tks.perspectiveview_mvpmatrix_java;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

public class Utils {
	private static final String TAG = "aaaaa";

	/* シェーダ初期化(頂点シェーダとフラグメントシェーダの両方) */
	public static int initShaders(String vshader, String fshader) {
		int program = createProgram(vshader, fshader);
		GLES20.glUseProgram(program);
		return program;
	}

	/* glプログラム生成 */
	public static int createProgram(String vshader, String fshader) {
		// シェーダオブジェクトを作成する
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vshader);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fshader);

		/* プログラムオブジェクト生成 */
		int program = GLES20.glCreateProgram();
		if (program == 0) {
			throw new RuntimeException("failed to create program");
		}

		/* シェーダオブジェクトを設定する */
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);

		/* プログラムオブジェクトをリンク */
		GLES20.glLinkProgram(program);

		/* リンク結果のチェック */
		int[] linked = new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
		if (linked[0] != GLES20.GL_TRUE) {
			String error = GLES20.glGetProgramInfoLog(program);
			throw new RuntimeException("failed to link program: " + error);
		}
		return program;
	}

	/* glシェーダ生成 */
	public static int loadShader(int type, String source) {
		/* シェーダ生成 */
		int shader = GLES20.glCreateShader(type);
		if (shader == 0) {
			throw new RuntimeException("unable to create shader");
		}

		/* シェーダソースを送る */
		GLES20.glShaderSource(shader, source);

		/* 送ったシェーダソースをコンパイル */
		GLES20.glCompileShader(shader);

		/* コンパイル結果判定 */
		int[] retcompile = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, retcompile, 0);
		if (retcompile[0] != GLES20.GL_TRUE) {
			String errstr = GLES20.glGetShaderInfoLog(shader);
			throw new RuntimeException("failed!! compile shader: " + errstr);
		}

		return shader;
	}

	public static void normalizeVector3(float[] v, int offset) {
		float length = (float)Math.sqrt(v[offset] * v[offset] + v[offset + 1] * v[offset + 1] + v[offset + 2] * v[offset + 2]);
		if (length == 0) return;

		v[offset]     /= length;
		v[offset + 1] /= length;
		v[offset + 2] /= length;
		return;
	}

	/* 透視投影行列を生成 */
	public static void setPerspectiveM(float[] retm, int offset, double fovy, double aspect, double zNear, double zFar) {
		Matrix.setIdentityM(retm, offset);
		double ymax = zNear * Math.tan(fovy * Math.PI / 360.0);
		double ymin = -ymax;
		double xmin = ymin * aspect;
		double xmax = ymax * aspect;
		Matrix.frustumM(retm, offset, (float)xmin, (float)xmax, (float)ymin, (float)ymax, (float)zNear, (float)zFar);
		return;
	}

	/* ダイレクトバッファ生成 */
	public static ByteBuffer makeByteBuffer(byte[] array) {
		if (array == null) throw new IllegalArgumentException();

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length);
		byteBuffer.order(ByteOrder.nativeOrder());
		byteBuffer.put(array);
		byteBuffer.position(0);
		return byteBuffer;
	}

	/* ダイレクトバッファ生成 */
	public static ShortBuffer makeShortBuffer(short[] array) {
		if (array == null) throw new IllegalArgumentException();

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2 * array.length);
		byteBuffer.order(ByteOrder.nativeOrder());
		ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
		shortBuffer.put(array);
		shortBuffer.position(0);
		return shortBuffer;
	}

	/* ダイレクトバッファ生成 */
	public static FloatBuffer makeFloatBuffer(float[] array) {
		if (array == null) throw new IllegalArgumentException();

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * array.length);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		floatBuffer.put(array);
		floatBuffer.position(0);
		return floatBuffer;
	}

	/* OpenGL側で発生したエラーを例外で通知 */
	public static void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, op + ": glError " + GLU.gluErrorString(error));
			throw new RuntimeException(op + ": glError " + GLU.gluErrorString(error));
		}
	}

	/* assetsファイル取得 */
	public static String loadFromAssetFile(Context context, String fileName) throws IOException {
		InputStream inputStream = null;
		byte[] buffer = null;
		try {
			inputStream = context.getAssets().open(fileName);
			int length = inputStream.available();
			buffer = new byte[length];
			inputStream.read(buffer);
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (inputStream != null) inputStream.close();
		}
		return new String(buffer);
	}
}
