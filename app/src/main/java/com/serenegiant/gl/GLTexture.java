package com.serenegiant.gl;
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2023 saki t_saki@serenegiant.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

/**
 * 用于OpenGL纹理操作的辅助对象类
 */
public class GLTexture implements GLConst {
	private static final boolean DEBUG = true;	// XXX 实际工作时要做false
	private static final String TAG = GLTexture.class.getSimpleName();

	private static final boolean DEFAULT_ADJUST_POWER2 = false;

	/**
	 * 用于实例生成的辅助对象方法
	 * 纹理目标为GL_TEXTURE_2D
	 * 纹理单元为GL_TEXTURE0固定，不能同时使用多个
	 * filter_param为GLES30.GL_LINEAR
	 * @param width 纹理大小
	 * @param height 纹理大小
	 */
	public static GLTexture newInstance(final int width, final int height) {
		return new GLTexture(
			GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE0, GL_NO_TEXTURE,
			width, height, DEFAULT_ADJUST_POWER2,
			GLES20.GL_LINEAR);
	}

	/**
	 * インスタンス生成のためのヘルパーメソッド
	 * テクスチャターゲットはGL_TEXTURE_2D
	 * filter_paramはGLES30.GL_LINEAR
	 * @param texUnit
	 * @param width テクスチャサイズ
	 * @param height テクスチャサイズ
	 */
	public static GLTexture newInstance(
		@TexUnit final int texUnit,
		final int width, final int height) {

		return new GLTexture(
			GLES20.GL_TEXTURE_2D, texUnit, GL_NO_TEXTURE,
			width, height, DEFAULT_ADJUST_POWER2,
			GLES20.GL_LINEAR);
	}

	/**
	 * インスタンス生成のためのヘルパーメソッド
	 * テクスチャターゲットはGL_TEXTURE_2D
	 * @param texUnit
	 * @param width テクスチャサイズ
	 * @param height テクスチャサイズ
	 * @param filter_param	テクスチャの補間方法を指定 GL_LINEARとかGL_NEAREST
	 */
	public static GLTexture newInstance(
		@TexUnit final int texUnit,
		final int width, final int height, final int filter_param) {

		return new GLTexture(
			GLES20.GL_TEXTURE_2D, texUnit, GL_NO_TEXTURE,
			width, height, DEFAULT_ADJUST_POWER2,
			filter_param);
	}

	/**
	 * インスタンス生成のためのヘルパーメソッド
	 * テクスチャターゲットはGL_TEXTURE_2D
	 * @param texUnit
	 * @param width テクスチャサイズ
	 * @param height テクスチャサイズ
	 * @param adjust_power2 テクスチャサイズを2の乗数にするかどうか
	 * @param filter_param	テクスチャの補間方法を指定 GL_LINEARとかGL_NEAREST
	 */
	public static GLTexture newInstance(
		@TexUnit final int texUnit,
		final int width, final int height,
		final boolean adjust_power2,
		@MinMagFilter final int filter_param) {
		return new GLTexture(
			GLES20.GL_TEXTURE_2D, texUnit, GL_NO_TEXTURE,
			width, height, adjust_power2,
			filter_param);
	}

	/**
	 * 现有纹理的包裹辅助对象方法
	 * @param texTarget
	 * @param texUnit
	 * @param texId
	 * @param width
	 * @param height
	 * @return
	 */
	public static GLTexture wrap(
		@TexTarget final int texTarget, @TexUnit final int texUnit, final int texId,
		final int width, final int height) {

		return new GLTexture(
			texTarget, texUnit, texId,
			width, height, false,
			GLES20.GL_LINEAR);
	}

//--------------------------------------------------------------------------------
	@TexTarget
	private final int TEX_TARGET;
	@TexUnit
	private final int TEX_UNIT;
	@MinMagFilter
	private final int FILTER_PARAM;
	private final boolean ADJUST_POWER2;
	private final boolean mWrappedTexture;
	private int mTextureId;
	@Size(min=16)
	@NonNull
	private final float[] mTexMatrix = new float[16];	// 纹理变换矩阵
	private int mTexWidth, mTexHeight;
	private int mWidth, mHeight;
	private int viewPortX, viewPortY, viewPortWidth, viewPortHeight;

	/**
	 * 构造 函数
	 * @param texTarget 不GL_TEXTURE_EXTERNAL_OES，除非包裹现有纹理
	 * @param texUnit
	 * @param texId
	 * @param width 纹理大小
	 * @param height 纹理大小
	 * @param adjust_power2 是否使纹理大小为 2 的幂
	 * @param filter_param	GL_LINEAR指定纹理的插值方式GL_NEAREST
	 */
	protected GLTexture(
		@TexTarget final int texTarget, @TexUnit final int texUnit, final int texId,
		final int width, final int height,
		final boolean adjust_power2,
		@MinMagFilter final int filter_param) {

		if (DEBUG) Log.v(TAG, String.format("GLTexture constructor (%d,%d)", width, height)
				+ ", adjust_power2=" + adjust_power2);
		TEX_TARGET = texTarget;
		TEX_UNIT = texUnit;
		mWrappedTexture = texId > GL_NO_TEXTURE;
		mTextureId = texId;
		FILTER_PARAM = filter_param;
		ADJUST_POWER2 = adjust_power2 && (texId <= GL_NO_TEXTURE);
		createTexture(width, height);
		if (DEBUG) Log.v(TAG, "GLTexture:id=" + mTextureId);
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			release();	// GLコンテキスト内じゃない可能性があるのであまり良くないけど
		} finally {
			super.finalize();
		}
	}

	/**
	 * テクスチャを破棄
	 * GLコンテキスト/EGLレンダリングコンテキスト内で呼び出すこと
	 */
	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		releaseTexture();
	}

	/**
	 * このインスタンスで管理しているテクスチャを有効にする(バインドする)
	 * ビューポートの設定も行う
	 */
	public void bind() {
//		if (DEBUG) Log.v(TAG, "makeCurrent:");
		GLES20.glActiveTexture(TEX_UNIT);	// テクスチャユニットを選択
		GLES20.glBindTexture(TEX_TARGET, mTextureId);
		setViewPort(viewPortX, viewPortY, viewPortWidth, viewPortHeight);
	}

	/**
	 * テクスチャをバインドする
	 * (ビューポートの設定はしない)
	 */
	public void bindTexture() {
		GLES20.glActiveTexture(TEX_UNIT);
		GLES20.glBindTexture(TEX_TARGET, mTextureId);
	}

	/**
	 * Viewport配置
	 * 此处设置的值将在您下次调用 makeCurrent 时恢复。
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setViewPort(final int x, final int y, final int width, final int height) {
		viewPortX = x;
		viewPortY = y;
		viewPortWidth = width;
		viewPortHeight = height;

		GLES20.glViewport(x, y, width, height);
	}

	/**
	 * 禁用此实例管理的纹理(アンバインドする)
	 */
	public void unbind() {
		if (DEBUG) Log.v(TAG, "swap:");
		GLES20.glActiveTexture(TEX_UNIT);	// 选择纹理单位
		GLES20.glBindTexture(TEX_TARGET, 0);
	}

	public boolean isValid() {
		return mTextureId > GL_NO_TEXTURE;
	}

	/**
	 * 获取纹理是否为外部纹理
	 * @return
	 */
	public boolean isOES() {
		return TEX_TARGET == GL_TEXTURE_EXTERNAL_OES;
	}

	/**
	 * 获取纹理目标(GL_TEXTURE_2D)
	 * @return
	 */
	@TexTarget
	public int getTexTarget() {
		return TEX_TARGET;
	}

	@TexUnit
	public int getTexUnit() {
		return TEX_UNIT;
	}

	/**
	 * 获取纹理名称
	 * @return
	 */
	public int getTexId() {
		return mTextureId;
	}

	public int getWidth() {
		return 0;
	}

	public int getHeight() {
		return 0;
	}

	/**
	 * #copyTexMatrix()返回值为 的浮点数组
	 */
	@Size(min=16)
	@NonNull
	private final float[] mResultMatrix = new float[16];
	/**
	 * 实现 IGLSurface
	 * 获取纹理转换矩阵的副本
	 * @return
	 */
	@Size(min=16)
	@NonNull
	public float[] copyTexMatrix() {
		System.arraycopy(mTexMatrix, 0, mResultMatrix, 0, 16);
		return mResultMatrix;
	}

	/**
	 * IGLSurfaceの実装
	 * テクスチャ座標変換行列のコピーを取得
	 * 領域チェックしていないのでoffset位置から16個以上確保しておくこと
	 * @param matrix
	 * @param offset
	 */
	public void copyTexMatrix(@NonNull @Size(min=16) final float[] matrix, final int offset) {
		System.arraycopy(mTexMatrix, 0, matrix, offset, mTexMatrix.length);
	}

	/**
	 * 获取纹理转换矩阵(内部数组按原样返回，因此在更改它时要小心。)
	 * @return
	 */
	@Size(min=16)
	@NonNull
	public float[] getTexMatrix() {
		return mTexMatrix;
	}

	/**
	 * 获取纹理宽度
	 * @return
	 */
	public int getTexWidth() {
		return mTexWidth;
	}

	/**
	 * 获取纹理高度
	 * @return
	 */
	public int getTexHeight() {
		return mTexHeight;
	}


	/**
	 * 将指定的位图加载到纹理中
 	 * @param bitmap
	 */
	public void loadBitmap(@NonNull final Bitmap bitmap) {
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		if (!mWrappedTexture && (width > mTexWidth) || (height > mTexHeight)) {
			// 自行生成纹理，当纹理大小增加时
			releaseTexture();
			createTexture(width, height);
		}
		bindTexture();
		android.opengl.GLUtils.texImage2D(TEX_TARGET, 0, bitmap, 0);
		GLES20.glBindTexture(TEX_TARGET, 0);
		// initialize texture matrix
		Matrix.setIdentityM(mTexMatrix, 0);
		mTexMatrix[0] = width / (float)mTexWidth;
		mTexMatrix[5] = height / (float)mTexHeight;
		if (DEBUG) Log.v(TAG, String.format("image(%d,%d),scale(%f,%f)",
			mWidth, mHeight, mTexMatrix[0], mTexMatrix[5]));
	}

	private void createTexture(final int width, final int height) {
		if (DEBUG) Log.v(TAG, String.format("texSize(%d,%d)", mTexWidth, mTexHeight));
		if (mTextureId <= GL_NO_TEXTURE) {
			if (ADJUST_POWER2) {
				// 当纹理大小为 2 的幂时
				int w = 1;
				for (; w < width; w <<= 1) ;
				int h = 1;
				for (; h < height; h <<= 1) ;
				if (mTexWidth != w || mTexHeight != h) {
					mTexWidth = w;
					mTexHeight = h;
				}
			} else {
				mTexWidth = width;
				mTexHeight = height;
			}
			mWidth = width;
			mHeight = height;
			mTextureId = GLUtils.initTex(TEX_TARGET, TEX_UNIT, FILTER_PARAM);
			// 为纹理保留内存空间
			GLES20.glTexImage2D(TEX_TARGET,
				0,					// mipmap级别0(不要 mipmap)
				GLES20.GL_RGBA,				// 内部格式
				mTexWidth, mTexHeight,		// 大小
				0,					// 边界宽度
				GLES20.GL_RGBA,				// 要传递的数据的格式
				GLES20.GL_UNSIGNED_BYTE,	// 数据类型
				null);				// 无像素数据
		} else {
			mWidth = mTexWidth = width;
			mHeight = mTexHeight = height;
		}
		// 初始化纹理转换矩阵
		Matrix.setIdentityM(mTexMatrix, 0);
		mTexMatrix[0] = mWidth / (float)mTexWidth;
		mTexMatrix[5] = mHeight / (float)mTexHeight;
		if (DEBUG) Log.v(TAG, String.format("image(%d,%d),scale(%f,%f)",
			mWidth, mHeight, mTexMatrix[0], mTexMatrix[5]));
		setViewPort(0, 0, mWidth, mHeight);
	}

	private void releaseTexture() {
		if (!mWrappedTexture && (mTextureId > GL_NO_TEXTURE)) {
			GLUtils.deleteTex(mTextureId);
			mTextureId = GL_NO_TEXTURE;
		}
	}

}
